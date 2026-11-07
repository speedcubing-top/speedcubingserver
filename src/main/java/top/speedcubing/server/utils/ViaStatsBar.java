package top.speedcubing.server.utils;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.legacy.bossbar.BossBar;
import com.viaversion.viaversion.api.legacy.bossbar.BossColor;
import com.viaversion.viaversion.api.legacy.bossbar.BossStyle;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.UUID;
import org.bukkit.Bukkit;

/**
 * A real boss bar for 1.9+ clients connected through ViaVersion. Only referenced after
 * {@link #supports(UUID)} returned true, so servers without ViaVersion never load the Via classes.
 */
final class ViaStatsBar {
    private final UUID viewer;
    private BossBar bar;

    ViaStatsBar(UUID viewer) {
        this.viewer = viewer;
    }

    static boolean supports(UUID player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("ViaVersion"))
            return false;
        try {
            ProtocolVersion version = Via.getAPI().getPlayerProtocolVersion(player);
            return version != null && version.newerThanOrEqualTo(ProtocolVersion.v1_9);
        } catch (LinkageError e) {
            // an installed ViaVersion whose API no longer matches the one compiled against: use the wither
            return false;
        }
    }

    private static final BossColor[] LEVEL_COLORS = {BossColor.GREEN, BossColor.YELLOW, BossColor.RED};

    /** level: 0 good, 1 medium, 2 bad. Shows the bar if it is not shown yet. */
    void set(String title, float health, int level) {
        BossColor color = LEVEL_COLORS[level];
        if (bar == null) {
            bar = Via.getAPI().legacyAPI().createLegacyBossBar(title, health, color, BossStyle.SEGMENTED_20);
            bar.addPlayer(viewer);
            return;
        }
        // the setters send an update packet each, so only on change
        if (!title.equals(bar.getTitle()))
            bar.setTitle(title);
        if (health != bar.getHealth())
            bar.setHealth(health);
        if (color != bar.getColor())
            bar.setColor(color);
    }

    void remove() {
        if (bar != null) {
            bar.removePlayer(viewer);
            bar = null;
        }
    }
}
