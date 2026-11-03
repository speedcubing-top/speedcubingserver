package top.speedcubing.server.bukkitcmd.offline;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.speedcubing.lib.utils.SQL.SQLRow;
import top.speedcubing.server.lang.Lang;
import top.speedcubing.server.player.User;

public class premium implements CommandExecutor {
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0) {
            User user = User.getUser(commandSender);
            SQLRow r = user.dbSelect("name,autologin");
            boolean disable = r.getBoolean(1);
            ((Player) commandSender).kickPlayer(Lang.of(disable ? "%lang_server_premium_check_disabled%" : "%lang_server_premium_check_enabled%", r.getString(0)).getString(user.lang));
            user.dbUpdate("autologin=" + (disable ? 0 : 1));
        } else User.getUser(commandSender).sendMessage("%lang_server_premium_usage%");
        return true;
    }
}