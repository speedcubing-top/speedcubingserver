package top.speedcubing.server.cubinglistener;

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

        int onlineCount = Bukkit.getOnlinePlayers().size();
        String serverName = Bukkit.getServerName();

        RedisManager.hset("sc:servercounts", serverName, String.valueOf(onlineCount));

        try (SQLConnection connection = Database.getSystem()) {
            connection.update(
                    "servers",
                    "onlinecount=" + onlineCount,
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
