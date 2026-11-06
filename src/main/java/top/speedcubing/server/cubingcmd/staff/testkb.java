package top.speedcubing.server.cubingcmd.staff;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.player.User;
import top.speedcubing.server.system.command.CubingCommand;

public class testkb extends CubingCommand {
    private static final String PREFIX = Msg.STAFF;

    public testkb() {
        super("testkb");
    }
    @Override
    public void execute(CommandSender commandSender, String s, String[] args) {
        switch (args.length) {
            case 1 -> {
                Player player = Bukkit.getPlayer(args[0]);
                if (player == null)
                    commandSender.sendMessage(PREFIX + Msg.PLAYER_NOT_FOUND);
                else
                    test(0.1, 0.1, 0.1, player, commandSender);
                break;
            }
            case 4 -> {
                Player player = Bukkit.getPlayer(args[0]);
                if (player == null)
                    commandSender.sendMessage(PREFIX + Msg.PLAYER_NOT_FOUND);
                else
                    try {
                        test(Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), player, commandSender);
                    } catch (Exception e) {
                        commandSender.sendMessage(PREFIX + Msg.ERROR + "Invalid number.");
                    }
            }
            default -> {
                commandSender.sendMessage(PREFIX + Msg.usage("/testkb <player> [<x> <y> <z>]"));
            }
        }
    }

    private void test(double x, double y, double z, Player player, CommandSender commandSender) {
        player.setVelocity(new Vector(x, y, z));
        commandSender.sendMessage(PREFIX + Msg.SUCCESS + "Tested " + User.getUser(player).getColorName() + Msg.SUCCESS + " for anti kb! " + Msg.INFO + "(" + x + "," + y + "," + z + ")");
    }
}
