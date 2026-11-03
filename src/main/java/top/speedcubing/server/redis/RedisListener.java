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

            user.vanished = message.state();
            if (user.vanished) {
                Bukkit.getScheduler().runTask(speedcubingServer.getInstance(), () -> {
                    for (Player p : Bukkit.getOnlinePlayers())
                        if (!User.getUser(p).isStaff)
                            p.hidePlayer(user.player);
                });
            } else {
                Bukkit.getScheduler().runTask(speedcubingServer.getInstance(), () -> {
                    for (Player p : Bukkit.getOnlinePlayers())
                        p.showPlayer(user.player);
                });
            }
        });
        RedisBus.subscribe(RedisBus.getChannelPrefix() + ":server:" + Bukkit.getServerName() + ":demo", rawMessage -> {
            PacketPlayOutGameStateChange packet = new PacketPlayOutGameStateChange(5, 0);
            int id = Integer.parseInt(rawMessage);
            if (id == 0)
                User.getUsers().forEach(a -> a.sendPacket(packet));
            else
                User.getUser(id).sendPacket(packet);
        });
        RedisBus.subscribe(RedisBus.getChannelPrefix() + ":server:" + Bukkit.getServerName() + ":crash", rawMessage -> {
            int id = Integer.parseInt(rawMessage);
            if (id == 0)
                Bukkit.getOnlinePlayers().forEach(PlayerUtils::crashAll);
            else {
                User user = User.getUser(id);
                PlayerUtils.crashAll(user.player);
                user.isCrashed = true;
            }
        });
    }
}
