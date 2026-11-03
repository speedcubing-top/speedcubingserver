package top.speedcubing.server.bukkitcmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.speedcubing.common.text.Msg;

public class getitemtype implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        Player player = (Player) commandSender;
        player.sendMessage(Msg.SYSTEM + Msg.INFO + "Item type: " + Msg.VALUE + player.getItemInHand().getType());
        return true;
    }
}
