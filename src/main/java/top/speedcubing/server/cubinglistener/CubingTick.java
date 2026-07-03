package top.speedcubing.server.cubinglistener;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Bukkit;
import top.speedcubing.common.database.Database;
import top.speedcubing.common.events.ConfigReloadEvent;
import top.speedcubing.common.events.CubingTickEvent;
import top.speedcubing.common.io.RedisManager;
import top.speedcubing.lib.bukkit.PlayerUtils;
import top.speedcubing.lib.bukkit.pluginMessage.BungeePluginMessage;
import top.speedcubing.lib.eventbus.CubingEventHandler;
import top.speedcubing.lib.utils.SQL.SQLConnection;
import top.speedcubing.lib.utils.bytes.ByteArrayBuffer;
import top.speedcubing.server.player.User;

public class CubingTick {

    @CubingEventHandler
    public void cubingTickEvent(CubingTickEvent e) {
        long t = System.currentTimeMillis();

        // update server status
        MemoryUsage usage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        double[] tps = MinecraftServer.getServer().recentTps;
        long[] tickDurationsNs = MinecraftServer.getServer().h;
        int onlineCount = Bukkit.getOnlinePlayers().size();
        String serverName = Bukkit.getServerName();
        double mspt = 0.0;
        int sampleCount = 0;

        for (long durationNs : tickDurationsNs) {
            if (durationNs > 0L) {
                mspt += durationNs / 1_000_000.0;
                sampleCount++;
            }
        }
        if (sampleCount > 0) {
            mspt /= sampleCount;
        }

        RedisManager.hset("sc:servercounts", serverName, String.valueOf(onlineCount));
        RedisManager.hset(
                "sc:servermetrics",
                serverName,
                onlineCount + "," +
                        (usage.getCommitted() / 1048576) + "," +
                        (usage.getUsed() / 1048576) + "," +
                        (Math.round(tps[0] * 100.0) / 100.0) + "," +
                        (Math.round(tps[1] * 100.0) / 100.0) + "," +
                        (Math.round(tps[2] * 100.0) / 100.0)
        );

        try (SQLConnection connection = Database.getSystem()) {
            connection.update(
                    "servers",
                    "onlinecount=" + onlineCount +
                            ",ram_heap=" + usage.getCommitted() / 1048576 +
                            ",ram_used=" + usage.getUsed() / 1048576 +
                            ",mspt=" + Math.round(mspt * 100.0) / 100.0 +
                            ",tps1=" + Math.round(tps[0] * 100.0) / 100.0 +
                            ",tps2=" + Math.round(tps[1] * 100.0) / 100.0 +
                            ",tps3=" + Math.round(tps[2] * 100.0) / 100.0,
                    "name='" + serverName + "'"
            );
        }
        // Publish player count via Redis so proxies get a real-time update
        // without relying solely on SQL polling.
        RedisManager.broadcast(new ByteArrayBuffer()
                .writeUTF("playercount")
                .writeUTF(serverName)
                .writeInt(onlineCount)
                .toByteArray());

        // user loop
        for (User user : User.getUsers()) {
            if (user.listened)
                user.writeToProxy(new ByteArrayBuffer().writeUTF("cps").writeInt(user.id).writeInt(user.leftCPS).writeInt(user.rightCPS).toByteArray());
                        /*
                        if (user.leftClick >= config.LeftCpsLimit || user.rightClick >= config.RightCpsLimit)
                            Bukkit.getScheduler().runTask(speedcubingServer.getInstance(), () -> user.player.kickPlayer("You are clicking too fast !"));
                        */

            if (user.nickState() && user.vanished) {
                if (Bukkit.getServerName().equals("lobby")) {
                    PlayerUtils.sendActionBar(user.player, "You are currently §cNICKED (in games only) §fand §cVANISHED");
                } else {
                    PlayerUtils.sendActionBar(user.player, "You are currently §cNICKED §fand §cVANISHED");
                }
            } else if (user.vanished) {
                PlayerUtils.sendActionBar(user.player, "You are currently §cVANISHED");
            } else if (user.nickState()) {
                if (Bukkit.getServerName().equals("lobby")) {
                    PlayerUtils.sendActionBar(user.player, "You are currently §cNICKED (in games only)");
                } else {
                    PlayerUtils.sendActionBar(user.player, "You are currently §cNICKED");
                }
            }

            //limbo
            if (!Bukkit.getServerName().equals("limbo"))
                if (t - user.lastMove > 300000)
                    BungeePluginMessage.switchServer(user.player, "limbo");
        }

        //config reload
        if (e.getTick() % 3600 == 0) {
            new ConfigReloadEvent().call();
        }
    }
}
