package top.speedcubing.server.bukkitlistener.pluginchannel;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import top.speedcubing.common.database.Database;
import top.speedcubing.lib.utils.SQL.SQLConnection;
import top.speedcubing.server.mulitproxy.BungeeProxy;
import top.speedcubing.server.player.User;
import top.speedcubing.server.configuration.Configuration;

public class FMLHSListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
        boolean store = false, punished = false;
        String name = null, a2, string;
        Boolean bypass;

        try (SQLConnection connection = Database.getCubing()) {
            bypass = connection.select("modbypass")
                    .from("playersdata")
                    .where("id=" + User.getUser(player).id)
                    .executeResult()
                    .getBoolean();
        }

        for (int i = 2; i < bytes.length; store = !store) {
            int end = i + bytes[i] + 1;
            string = new String(Arrays.copyOfRange(bytes, i + 1, end));
            a2 = name + " " + string;
            if (store && !bypass) {
                if (!punished) {
                    for (Pattern p : Configuration.blacklistedMod) {
                        if (p.matcher(a2).matches()) {
                            BungeeProxy.proxyCommand("ban " + player.getName() + " 0 Suspicious activities detected on your account.");
                            punished = true;
                            break;
                        }
                    }
                }
                if (!punished) {
                    for (Pattern p : Configuration.blockedMod) {
                        if (p.matcher(a2).matches()) {
                            player.kickPlayer("Invalid Modification Found.");
                            punished = true;
                            break;
                        }
                    }
                }
            } else {
                name = string;
            }
            i = end;
        }

        String mods = new String(bytes, StandardCharsets.UTF_8);
        int id = User.getUser(player).id;

        System.out.println(mods);
        System.out.println(Arrays.toString(bytes));

        try (SQLConnection connection = Database.getCubing()) {
            connection.prepare("UPDATE `playersdata` SET forgemod=? WHERE id=?")
                    .setString(1, mods)
                    .setInt(2, id)
                    .executeUpdate();
        }
    }
}
