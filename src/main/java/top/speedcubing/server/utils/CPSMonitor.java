// GPT-5 mini
package top.speedcubing.server.utils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import top.speedcubing.server.player.User;
import org.bukkit.plugin.java.JavaPlugin;

public class CPSMonitor {

    private static final ConcurrentHashMap<UUID, UUID> monitoring = new ConcurrentHashMap<>();
    private static int taskId = -1;
    private static BukkitAudiences audiences;
    private static JavaPlugin plugin;

    public static void start(JavaPlugin pl, BukkitAudiences aud) {
        if (taskId != -1) return; // already started
        plugin = pl;
        audiences = aud;

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            try {
                for (Map.Entry<UUID, UUID> e : monitoring.entrySet()) {
                    UUID staffId = e.getKey();
                    UUID targetId = e.getValue();

                    Player staff = Bukkit.getPlayer(staffId);
                    if (staff == null) {
                        monitoring.remove(staffId);
                        continue;
                    }

                    Player target = Bukkit.getPlayer(targetId);
                    if (target == null) {
                        // target went offline: inform staff and stop monitoring
                        monitoring.remove(staffId);
                        if (audiences != null)
                            audiences.player(staff).sendMessage(Component.text("Target went offline. Monitoring stopped.").color(NamedTextColor.YELLOW));
                        continue;
                    }
                    User u = User.getUser(target.getUniqueId());
                    Component comp;
                    if (u != null) {
                        int left = u.leftCPS;
                        int right = u.rightCPS;
                        comp = Component.text(target.getName()).color(NamedTextColor.GOLD)
                                .append(Component.text(" CPS: ").color(NamedTextColor.WHITE))
                                .append(Component.text(String.valueOf(left)).color(left <= 16 ? NamedTextColor.GREEN : (left <= 21 ? NamedTextColor.YELLOW : NamedTextColor.RED)))
                                .append(Component.text(" | ").color(NamedTextColor.YELLOW))
                                .append(Component.text(String.valueOf(right)).color(right <= 16 ? NamedTextColor.GREEN : (right <= 21 ? NamedTextColor.YELLOW : NamedTextColor.RED)));

                    } else if (target.hasMetadata("cps")) {
                        List<org.bukkit.metadata.MetadataValue> meta = target.getMetadata("cps");
                        double cpsVal = meta.isEmpty() ? 0.0 : meta.get(0).asDouble();
                        comp = Component.text(target.getName()).color(NamedTextColor.GOLD)
                                .append(Component.text(" CPS: ").color(NamedTextColor.WHITE))
                                .append(Component.text(String.format("%.2f", cpsVal)).color(NamedTextColor.GREEN));
                    } else {
                        comp = Component.text("No CPS data for " + target.getName()).color(NamedTextColor.YELLOW);
                    }

                    if (audiences != null) {
                        try {
                            audiences.player(staff).sendActionBar(comp);
                        } catch (NoSuchMethodError ex) {
                            audiences.player(staff).sendMessage(comp);
                        }
                    }
                }
            } catch (Throwable t) {
                if (plugin != null) plugin.getLogger().warning("Error in CPS monitoring task: " + t.getMessage());
            }
        }, 0L, 1L);
    }

    public static void stop() {
        if (taskId != -1 && plugin != null) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        monitoring.clear();
        audiences = null;
        plugin = null;
    }

    public static UUID putMonitor(UUID staff, UUID target) {
        return monitoring.put(staff, target);
    }

    public static UUID removeMonitor(UUID staff) {
        return monitoring.remove(staff);
    }
}
