package top.speedcubing.server.cubingcmd.staff;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.system.command.CubingCommand;

public class heal extends CubingCommand {
    private static final String PREFIX = Msg.STAFF;

    public heal() {
        super("heal");
    }

    @Override
    public void execute(CommandSender commandSender, String s, String[] args) {
        if (commandSender instanceof Player player) {
            if (args.length == 0) {
                player.setHealth(player.getMaxHealth());
                player.sendMessage(PREFIX + Msg.SUCCESS + "Healed!");
            } else if (args.length == 1) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null) {
                    target.setHealth(target.getMaxHealth());
                    player.sendMessage(PREFIX + Msg.SUCCESS + "Healed " + Msg.VALUE + target.getName() + Msg.SUCCESS + "!");
                } else {
                    player.sendMessage(PREFIX + Msg.PLAYER_NOT_FOUND);
                }
            } else {
                player.sendMessage(PREFIX + Msg.usage("/heal [player]"));
            }
        }
    }
}
