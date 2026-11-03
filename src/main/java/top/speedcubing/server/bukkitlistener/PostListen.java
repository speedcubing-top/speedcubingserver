package top.speedcubing.server.bukkitlistener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import top.speedcubing.lib.utils.ReflectionUtils;
import top.speedcubing.server.authenticator.AuthData;
import top.speedcubing.server.authenticator.AuthEventHandlers;
import top.speedcubing.server.bukkitcmd.nick.nick;
import top.speedcubing.server.player.User;
import top.speedcubing.server.speedcubingServer;

public class PostListen implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerMoveEvent(PlayerMoveEvent e) {
        User.getUser(e.getPlayer()).lastMove = System.currentTimeMillis();
        //Auth
        AuthEventHandlers.onPlayerMove(e);
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
