package top.speedcubing.server.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import top.speedcubing.lib.utils.SystemUtils;

/** Live server numbers for /ram, /uptime and the /tpsbar and /rambar action bar readouts. */
public final class ServerStats {
    private ServerStats() {
    }

    public static double tps() {
        return Math.min(20.0, MinecraftServer.getServer().recentTps[0]);
    }

    /** Average tick duration over the last 100 ticks. */
    public static double mspt() {
        long[] times = MinecraftServer.getServer().h;
        long sum = 0;
        for (long t : times)
            sum += t;
        return sum / (double) times.length / 1_000_000.0;
    }

    public static String tpsLine(int ping) {
        double tps = tps();
        double mspt = mspt();
        return "§fTPS " + (tps >= 19.5 ? "§a" : tps >= 15 ? "§e" : "§c") + String.format("%.2f", tps)
                + " §8| §fMSPT " + (mspt < 40 ? "§a" : mspt < 50 ? "§e" : "§c") + String.format("%.1f", mspt)
                + " §8| §fPing " + (ping < 100 ? "§a" : ping < 200 ? "§e" : "§c") + ping + "ms";
    }

    public static String ramLine() {
        MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long max = SystemUtils.getXmx();
        int percent = (int) (heap.getUsed() * 100 / max);
        return "§fRAM " + (percent < 70 ? "§a" : percent < 90 ? "§e" : "§c") + heap.getUsed() / 1048576 + "§7/§f" + max / 1048576 + " MiB §7(" + percent + "%)";
    }

    public static String uptime() {
        long seconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        long days = seconds / 86400, hours = seconds / 3600 % 24, minutes = seconds / 60 % 60;
        return (days > 0 ? days + "d " : "") + (days > 0 || hours > 0 ? hours + "h " : "") + minutes + "m " + seconds % 60 + "s";
    }
}
