package top.speedcubing.server.redis;

import com.google.gson.Gson;
import net.minecraft.server.v1_8_R3.PacketPlayOutGameStateChange;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.speedcubing.common.rank.PermissionSet;
import top.speedcubing.common.rank.RankUpdate;
import top.speedcubing.common.redis.RedisBus;
import top.speedcubing.common.redis.message.RankUpdateMessage;
import top.speedcubing.common.redis.message.VanishUpdateMessage;
import top.speedcubing.common.redis.message.VelocityUpdateMessage;
import top.speedcubing.lib.bukkit.PlayerUtils;
import top.speedcubing.server.player.User;
import top.speedcubing.server.speedcubingServer;

public class RedisListener {

    public static void init() {
        RedisBus.subscribe(RedisBus.getChannelPrefix() + ":server:" + Bukkit.getServerName() + ":" + RankUpdate.CHANNEL, rawMessage -> {
            RankUpdateMessage message = new Gson().fromJson(rawMessage, RankUpdateMessage.class);
            if (message.reloadGroups())
                PermissionSet.reload();
            if (message.playerId() == 0) {
                User.getUsers().forEach(User::recalculateRank);
            } else {
                User user = User.getUser(message.playerId());
                if (user != null)
                    user.recalculateRank();
            }
        });
        RedisBus.subscribe(RedisBus.getChannelPrefix() + ":server:" + Bukkit.getServerName() + ":velocity", rawMessage -> {
            VelocityUpdateMessage message = new Gson().fromJson(rawMessage, VelocityUpdateMessage.class);
            User user = User.getUser(message.playerId());
            if (user == null) {
                return;
            }

            user.velocities = message.enabled() ? new double[]{message.horizontal(), message.vertical()} : null;
        });
        RedisBus.subscribe(RedisBus.getChannelPrefix() + ":server:" + Bukkit.getServerName() + ":vanish", rawMessage -> {
            VanishUpdateMessage message = new Gson().fromJson(rawMessage, VanishUpdateMessage.class);
            User user = User.getUser(message.playerId());
            if (user == null) {
                return;
            }

            boolean vanished = message.state();
            Bukkit.getScheduler().runTask(speedcubingServer.getInstance(), () -> {
                user.vanished = vanished;
                for (User viewer : User.getUsers()) {
                    // staff, including the player themself, get the [V] tag added or removed;
                    // the team must be removed before it is re-created
                    if (viewer.isStaff)
                        viewer.sendPacket(user.leavePacket, user.joinPacketFor(viewer));
                    if (viewer == user)
                        continue;
                    if (user.canBeSeenBy(viewer))
                        viewer.player.showPlayer(user.player);
                    else
                        viewer.player.hidePlayer(user.player);
                }
            });
        });
        RedisBus.subscribe(RedisBus.getChannelPrefix() + ":server:" + Bukkit.getServerName() + ":demo", rawMessage -> {
            PacketPlayOutGameStateChange packet = new PacketPlayOutGameStateChange(5, 0);
            int id = Integer.parseInt(rawMessage);
            if (id == 0)
                User.getUsers().forEach(a -> a.sendPacket(packet));
            else {
                // target may have left or be on another server; throwing here would drop the subscriber
                User user = User.getUser(id);
                if (user != null)
                    user.sendPacket(packet);
            }
        });
        RedisBus.subscribe(RedisBus.getChannelPrefix() + ":server:" + Bukkit.getServerName() + ":crash", rawMessage -> {
            int id = Integer.parseInt(rawMessage);
            if (id == 0)
                Bukkit.getOnlinePlayers().forEach(PlayerUtils::crashAll);
            else {
                User user = User.getUser(id);
                if (user == null)
                    return;
                PlayerUtils.crashAll(user.player);
                user.isCrashed = true;
            }
        });
    }
}
