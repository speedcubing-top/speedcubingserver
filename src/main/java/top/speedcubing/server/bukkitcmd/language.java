package top.speedcubing.server.bukkitcmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.speedcubing.server.lang.LanguageSystem;
import top.speedcubing.server.player.User;

/** /language <id>: used by the proxy's "your game language differs" prompt (and the lobby language menu logic). */
public class language implements CommandExecutor {

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player))
            return true;
        User user = User.getUser(commandSender);
        int lang;
        try {
            lang = strings.length == 1 ? Integer.parseInt(strings[0]) : -1;
        } catch (NumberFormatException e) {
            lang = -1;
        }
        if (lang < 0 || lang >= LanguageSystem.langCount) {
            user.sendMessage("%lang_server_language_usage%");
            return true;
        }
        user.setLanguage(lang);
        return true;
    }
}
