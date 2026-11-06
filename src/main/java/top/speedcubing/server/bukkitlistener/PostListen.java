package top.speedcubing.server.bukkitlistener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import top.speedcubing.lib.utils.ReflectionUtils;
import top.speedcubing.server.authenticator.AuthData;
import top.speedcubing.server.authenticator.AuthEventHandlers;
import top.speedcubing.server.bukkitcmd.nick.nick;
import top.speedcubing.server.player.User;
import top.speedcubing.server.speedcubingServer;
import top.speedcubing.server.utils.StatsBossBar;

public class PostListen implements Listener {
    // /god: cancel last so game plugins can't re-enable the damage (cancelling it also drops the knockback)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void EntityDamageEvent(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player) {
            User user = User.getUser(player);
            if (user != null && user.god)
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerMoveEvent(PlayerMoveEvent e) {
        User user = User.getUser(e.getPlayer());
        user.lastMove = System.currentTimeMillis();
        StatsBossBar statsBar = user.statsBar;
        if (statsBar != null)
            statsBar.follow();
        //Auth
        AuthEventHandlers.onPlayerMove(e);
    }

    // the client drops the bar's wither on these; the next CubingTick spawns it again
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void PlayerTeleportEvent(PlayerTeleportEvent e) {
        invalidateStatsBar(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerRespawnEvent(PlayerRespawnEvent e) {
        invalidateStatsBar(e.getPlayer());
    }

    private static void invalidateStatsBar(Player player) {
        User user = User.getUser(player);
        StatsBossBar statsBar = user == null ? null : user.statsBar;
        if (statsBar != null)
            statsBar.invalidate();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerQuitEvent(PlayerQuitEvent e) {
        e.setQuitMessage("");
        Player player = e.getPlayer();
        User user = User.getUser(player);
        if (user != null) {
            if (user.nicked())
                ReflectionUtils.setField(user.toNMS().getProfile(), "id", user.calculateNickHashUUID());
            user.dbUpdate("status='" + user.status + "'");
            AuthData.map.remove(user);
            user.removeCPSHologram();
            user.cancelRankExpiry();
            User.usersByID.remove(user.id);
        }

        //modify profile id for packets after event

        User.usersByUUID.remove(player.getUniqueId());
        nick.settingNick.remove(e.getPlayer().getUniqueId());
        nick.nickName.remove(e.getPlayer().getUniqueId());
        nick.nickRank.remove(e.getPlayer().getUniqueId());
        if (Bukkit.getOnlinePlayers().size() == 1 && speedcubingServer.restartable)
            speedcubingServer.restart();
    }
}
