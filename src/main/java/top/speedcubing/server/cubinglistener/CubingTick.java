package top.speedcubing.server.cubinglistener;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import top.speedcubing.common.events.ChampsChangedEvent;
import top.speedcubing.common.events.ConfigReloadEvent;
import top.speedcubing.common.events.CubingTickEvent;
import top.speedcubing.common.text.Msg;
import top.speedcubing.lib.bukkit.PlayerUtils;
import top.speedcubing.server.lang.Lang;
import top.speedcubing.lib.bukkit.pluginMessage.BungeePluginMessage;
import top.speedcubing.server.utils.Prefix;
import top.speedcubing.lib.eventbus.CubingEventHandler;
import top.speedcubing.server.player.User;
import top.speedcubing.server.utils.CPSMonitor;
import top.speedcubing.server.utils.ServerStats;

public class CubingTick {
    private static final String CPS_PREFIX = Prefix.CPS;

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

            boolean nicked = user.nickState();
            boolean lobby = Bukkit.getServerName().equals("lobby");
            UUID targetId = CPSMonitor.getTarget(user.player.getUniqueId());
            User target = targetId == null ? null : User.getUser(targetId);
            if (targetId != null && target == null) {
                CPSMonitor.removeMonitor(user.player.getUniqueId());
                user.player.sendMessage(CPS_PREFIX + Msg.ERROR + "Target went offline. Monitoring stopped.");
            }

            String actionBar = null;
            if (target != null) {
                String status = (nicked ? "§cNICKED §8| " : "") + (user.vanished ? "§cVANISHED §8| " : "");
                actionBar = status + "§6" + target.player.getName() + " §fCPS: " + cpsColor(target.leftCPS) + target.leftCPS + " §e| " + cpsColor(target.rightCPS) + target.rightCPS;
            } else if (nicked && user.vanished) {
                actionBar = Lang.of(lobby ? "%lang_server_actionbar_nicked_vanished_lobby%" : "%lang_server_actionbar_nicked_vanished%").getString(user.lang);
            } else if (user.vanished) {
                actionBar = Lang.of("%lang_server_actionbar_vanished%").getString(user.lang);
            } else if (nicked) {
                actionBar = Lang.of(lobby ? "%lang_server_actionbar_nicked_lobby%" : "%lang_server_actionbar_nicked%").getString(user.lang);
            }
            if (user.tpsBar)
                actionBar = (actionBar == null ? "" : actionBar + " §8| ") + ServerStats.tpsLine(((CraftPlayer) user.player).getHandle().ping);
            if (user.ramBar)
                actionBar = (actionBar == null ? "" : actionBar + " §8| ") + ServerStats.ramLine();
            if (actionBar != null)
                PlayerUtils.sendActionBar(user.player, actionBar);

            //limbo
            if (!Bukkit.getServerName().equals("limbo"))
                if (t - user.lastMove > 300000)
                    BungeePluginMessage.switchServer(user.player, "limbo");
        }
        CPSMonitor.removeOfflineStaff(uuid -> User.getUser(uuid) != null);

        //config reload
        if (e.getTick() % 3600 == 0) {
            new ConfigReloadEvent().call();
        }
    }

    @CubingEventHandler
    public void champsChangedEvent(ChampsChangedEvent e) {
        for (int id : e.getChanged()) {
            User user = User.getUser(id);
            if (user != null)
                user.recalculateRank();
        }
    }

    private static String cpsColor(int cps) {
        return cps <= 16 ? "§a" : cps <= 21 ? "§e" : "§c";
    }
}
