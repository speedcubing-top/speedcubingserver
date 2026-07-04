package top.speedcubing.server.bukkitcmd.nick;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.speedcubing.common.rank.Rank;
import top.speedcubing.server.events.player.NickEvent;
import top.speedcubing.server.player.User;

public class unnick implements CommandExecutor {
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!Bukkit.getServerName().equalsIgnoreCase("lobby")) {
            commandSender.sendMessage("§cYou can only use /unnick in the lobby.");
            return true;
        }
        Player player = (Player) commandSender;

        if (((NickEvent) new NickEvent(player).call()).isCancelled()) {
            return true;
        }

        User user = User.getUser(player);
        if (!user.nickState()) {
            User.getUser(commandSender).sendMessage("%lang_nick_not%");
            return true;
        }

        if (strings.length != 0) {
            commandSender.sendMessage("/unnick");
            return true;
        }

        nick.nickPlayer(user.realName, Rank.getRank(user.realRank, User.getUser(commandSender).id), false, player, false);
        user.updateSkin(user.defaultSkin, user.realName);
        commandSender.sendMessage("§aYou have unnicked successfully.");
        return true;
    }
}