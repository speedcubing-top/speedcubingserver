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

    // colours and thresholds follow Purpur's /tpsbar and /rambar
    private static String good(boolean good, boolean medium) {
        return good ? "§a" : medium ? "§e" : "§c";
    }

    public static String tpsLine(int ping) {
        double tps = tps();
        double mspt = mspt();
        return "§7TPS§e: " + good(tps >= 19, tps >= 15) + String.format("%.2f", tps)
                + " §7MSPT§e: " + good(mspt < 40, mspt < 50) + String.format("%.2f", mspt)
                + " §7Ping§e: " + good(ping < 100, ping < 200) + ping + "§7ms";
    }

    /** Fill of the TPS bar: tick time against the 50 ms budget. */
    public static float msptProgress() {
        return (float) Math.max(0, Math.min(1, mspt() / 50));
    }

    /** 0 good, 1 medium, 2 bad; the TPS bar's colour, by tick time. */
    public static int msptLevel() {
        double mspt = mspt();
        return mspt < 40 ? 0 : mspt < 50 ? 1 : 2;
    }

    public static String ramLine() {
        MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long max = SystemUtils.getXmx();
        float used = ramProgress();
        String colour = good(used < 0.60F, used < 0.85F);
        return "§7Ram§e: " + colour + bytes(heap.getUsed()) + "§7/" + colour + bytes(max) + " §7(" + (int) (used * 100) + "%)";
    }

    /** Fill of the RAM bar: used heap against -Xmx. */
    public static float ramProgress() {
        long max = SystemUtils.getXmx();
        return Math.max(0, Math.min(1, ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / (float) max));
    }

    /** 0 good, 1 medium, 2 bad; the RAM bar's colour. */
    public static int ramLevel() {
        float used = ramProgress();
        return used < 0.5F ? 0 : used < 0.75F ? 1 : 2;
    }

    private static String bytes(long v) {
        if (v < 1024)
            return v + "B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f%s", (double) v / (1L << (z * 10)), "BKMGTPE".charAt(z));
    }

    public static String uptime() {
        long seconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        long days = seconds / 86400, hours = seconds / 3600 % 24, minutes = seconds / 60 % 60;
        return (days > 0 ? days + "d " : "") + (days > 0 || hours > 0 ? hours + "h " : "") + minutes + "m " + seconds % 60 + "s";
    }
}
