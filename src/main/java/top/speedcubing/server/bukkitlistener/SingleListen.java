package top.speedcubing.server.bukkitlistener;

import net.minecraft.server.v1_8_R3.WorldData;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import top.speedcubing.lib.utils.ReflectionUtils;

public class SingleListen implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void CreatureSpawnEvent(CreatureSpawnEvent e) {
        String serverName = Bukkit.getServerName();
        if (serverName.equals("Bedwars") || serverName.equalsIgnoreCase("knockbackffa") || serverName.equalsIgnoreCase("fastbuilder_test"))
            return;
        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER && e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            e.setCancelled(true);
    }

    @EventHandler
    public void FoodLevelChangeEvent(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent e) {
        e.setDeathMessage("");
    }

    @EventHandler
    public void PlayerKickEvent(org.bukkit.event.player.PlayerKickEvent e) {
        if (e.getReason().equals("disconnect.spam"))
            e.setCancelled(true);
        else if (e.getReason().equals("Timed out")) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("You were disconnected: Timed out.");
        }
    }


    @EventHandler
    public void WeatherChangeEvent(WeatherChangeEvent e) {
        if (e.getWorld().hasStorm()) {
            WorldData worldData = ((CraftWorld) e.getWorld()).getHandle().worldData;
            worldData.setWeatherDuration(0);
            ReflectionUtils.setField(worldData, "q", false);
        } else e.setCancelled(true);
    }

}
