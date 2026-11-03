package top.speedcubing.server.bukkitcmd.offline;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.speedcubing.common.database.Database;
import top.speedcubing.lib.utils.SQL.SQLConnection;
import top.speedcubing.server.lang.Lang;
import top.speedcubing.server.player.User;

public class resetpassword implements CommandExecutor {
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length != 2) {
            User.getUser(commandSender).sendMessage("%lang_server_resetpassword_usage%");
            return true;
        }

        int l = strings[0].length();
        if (l > 16) {
            User.getUser(commandSender).sendMessage("%lang_server_resetpassword_too_long%");
        } else if (l < 8) {
            User.getUser(commandSender).sendMessage("%lang_server_resetpassword_too_short%");
        } else if (strings[0].equals(strings[1])) {
            try (SQLConnection connection = Database.getCubing()) {
                connection.update("playersdata")
                        .set("password=?")
                        .where("id=" + User.getUser(commandSender).id)
                        .setString(0, strings[0])
                        .execute();
            }
            ((Player) commandSender).kickPlayer(Lang.of("%lang_server_resetpassword_done%", commandSender.getName()).getString(User.getUser(commandSender).lang));
        } else {
            User.getUser(commandSender).sendMessage("%lang_server_resetpassword_mismatch%");
        }
        return true;
    }
}
