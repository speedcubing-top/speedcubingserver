package top.speedcubing.server.cubinglistener;

import org.bukkit.Bukkit;
import top.speedcubing.common.events.ConfigReloadEvent;
import top.speedcubing.common.events.CubingTickEvent;
import top.speedcubing.lib.bukkit.PlayerUtils;
import top.speedcubing.lib.bukkit.pluginMessage.BungeePluginMessage;
import top.speedcubing.lib.eventbus.CubingEventHandler;
import top.speedcubing.server.player.User;

public class CubingTick {

    @CubingEventHandler
    public void cubingTickEvent(CubingTickEvent e) {
        long t = System.currentTimeMillis();

        // user loop
        for (User user : User.getUsers()) {
            /*
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
