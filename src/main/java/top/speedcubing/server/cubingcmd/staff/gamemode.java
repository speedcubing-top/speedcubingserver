package top.speedcubing.server.cubingcmd.staff;

import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.player.User;
import top.speedcubing.server.system.command.CubingCommand;

/** /gms, /gmc, /gma, /gmsp [player]: game mode shortcuts; each alias has its own cmd.<alias> permission. */
public class gamemode extends CubingCommand {
    private static final String PREFIX = Msg.STAFF;

    public gamemode() {
        super("gms", "gmc", "gma", "gmsp");
    }

    @Override
    public void execute(CommandSender sender, String command, String[] args) {
        GameMode mode = switch (command.toLowerCase()) {
            case "gmc" -> GameMode.CREATIVE;
            case "gma" -> GameMode.ADVENTURE;
            case "gmsp" -> GameMode.SPECTATOR;
            default -> GameMode.SURVIVAL;
        };
        Player target;
        if (args.length == 0 && sender instanceof Player player) {
            target = player;
        } else if (args.length == 1) {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(PREFIX + Msg.PLAYER_NOT_FOUND);
                return;
            }
        } else {
            sender.sendMessage(PREFIX + Msg.usage("/" + command.toLowerCase() + " [player]"));
            return;
        }
        target.setGameMode(mode);
        String modeName = mode.name().charAt(0) + mode.name().substring(1).toLowerCase();
        if (target == sender) {
            sender.sendMessage(PREFIX + Msg.SUCCESS + "Game mode set to " + Msg.VALUE + modeName + Msg.SUCCESS + ".");
        } else {
            sender.sendMessage(PREFIX + Msg.SUCCESS + "Set " + User.getUser(target).getColorName() + Msg.SUCCESS + "'s game mode to " + Msg.VALUE + modeName + Msg.SUCCESS + ".");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String command, String[] args, Location location) {
        return args.length == 1 ? playerNames(sender, args[0]) : Collections.emptyList();
    }
}
