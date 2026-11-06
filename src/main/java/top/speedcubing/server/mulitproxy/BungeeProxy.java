package top.speedcubing.server.mulitproxy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.speedcubing.common.database.Database;
import top.speedcubing.common.redis.message.ConnectServerMessage;
import top.speedcubing.common.server.MinecraftProxy;
import top.speedcubing.lib.utils.SQL.SQLConnection;
import top.speedcubing.lib.utils.SQL.SQLResult;
import top.speedcubing.server.player.User;

/**
 * Messages for the proxy a player is connected to. Every message targets that player's own
 * proxy: with several proxies registered, any other proxy cannot kick or move the player and
 * may not even be listening on this Redis.
 */
public class BungeeProxy {
    public static void switchServer(int id, String server) {
        User user = User.getUser(id);
        MinecraftProxy proxy = user != null ? user.proxy : proxyOf(id);
        if (proxy == null) {
            Bukkit.getLogger().warning("Cannot send player " + id + " to " + server + ": not online on any proxy");
            return;
        }
        proxy.redisPublish("send", new ConnectServerMessage(id, "", server));
    }

    /** Runs a console command on the proxy {@code target} is connected to, e.g. to ban or kick them. */
    public static void proxyCommand(Player target, String command) {
        User user = User.getUser(target);
        if (user == null) {
            Bukkit.getLogger().warning("Dropped proxy command for " + target.getName() + ", who is not online here: " + command);
            return;
        }
        user.proxy.redisPublish("proxycmd", command);
    }

    private static MinecraftProxy proxyOf(int id) {
        try (SQLConnection connection = Database.getCubing()) {
            SQLResult result = connection.prepare("SELECT proxy FROM onlineplayer WHERE id=?").setInt(1, id).executeResult();
            return result.isEmpty() ? null : MinecraftProxy.getProxy(result.get(0).getString(0));
        }
    }
}
