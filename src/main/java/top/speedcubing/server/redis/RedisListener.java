package top.speedcubing.server.redis;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import top.speedcubing.common.redis.RedisBus;
import top.speedcubing.server.player.User;
import top.speedcubing.server.speedcubingServer;

public class RedisListener {

    public static void init() {
        RedisBus.subscribe(RedisBus.getChannelPrefix() + ":server:" + Bukkit.getServerName() + ":velocity", rawMessage -> {
            VelocityUpdateMessage message = new Gson().fromJson(rawMessage, VelocityUpdateMessage.class);
            Bukkit.getScheduler().runTask(speedcubingServer.getPlugin(speedcubingServer.class), () -> {
                User user = User.getUser(message.playerId);
                if (user != null) {
                    user.velocities = message.enabled ? new double[]{message.horizontal, message.vertical} : null;
                    System.out.println("updated velo: " + message.enabled + " " + message.horizontal + " " + message.vertical);
                }
            });
        });
    }
}

class VelocityUpdateMessage {
    int playerId;
    boolean enabled;
    double horizontal;
    double vertical;

    VelocityUpdateMessage(int playerId, boolean enabled, double horizontal, double vertical) {
        this.playerId = playerId;
        this.enabled = enabled;
        this.horizontal = horizontal;
        this.vertical = vertical;
    }
}