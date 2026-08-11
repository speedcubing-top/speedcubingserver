package top.speedcubing.server.mulitproxy;

import top.speedcubing.common.redis.message.ConnectServerMessage;
import top.speedcubing.common.server.MinecraftProxy;
import top.speedcubing.server.player.User;

public class BungeeProxy {
    public static void switchServer(int id, String server) {
        User user = User.getUser(id);
        if (user != null) {
            user.proxy.redisPublish("send", new ConnectServerMessage(id, "", server));
            return;
        }
        MinecraftProxy.getRandomProxy().redisPublish("send", new ConnectServerMessage(id, "", server));
    }

    public static void proxyCommand(String command) {
        MinecraftProxy.getRandomProxy().redisPublish("proxycmd", command);
    }
}
