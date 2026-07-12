package top.speedcubing.server.redis;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.speedcubing.common.redis.RedisBus;
import top.speedcubing.common.redis.message.VanishUpdateMessage;
import top.speedcubing.common.redis.message.VelocityUpdateMessage;
import top.speedcubing.server.player.User;
import top.speedcubing.server.speedcubingServer;

public class RedisListener {

    public static void init() {
        RedisBus.subscribe(RedisBus.getChannelPrefix() + ":server:" + Bukkit.getServerName() + ":velocity", rawMessage -> {
            VelocityUpdateMessage message = new Gson().fromJson(rawMessage, VelocityUpdateMessage.class);
            Bukkit.getScheduler().runTask(speedcubingServer.getPlugin(speedcubingServer.class), () -> {
                User user = User.getUser(message.playerId);
                if (user == null) {
                    return;
                }

                user.velocities = message.enabled ? new double[]{message.horizontal, message.vertical} : null;
            });
        });
        RedisBus.subscribe(RedisBus.getChannelPrefix() + ":server:" + Bukkit.getServerName() + ":vanish", rawMessage -> {
            VanishUpdateMessage message = new Gson().fromJson(rawMessage, VanishUpdateMessage.class);
            Bukkit.getScheduler().runTask(speedcubingServer.getPlugin(speedcubingServer.class), () -> {
                User user = User.getUser(message.playerId);
                if (user == null) {
                    return;
                }

                user.vanished = message.state;
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
        });
    }
}
