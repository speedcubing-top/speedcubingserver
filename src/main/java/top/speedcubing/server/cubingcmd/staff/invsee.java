package top.speedcubing.server.cubingcmd.staff;

import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.system.command.CubingCommand;

/** /invsee <player>: opens the target's live inventory; edits apply to the target. */
public class invsee extends CubingCommand {
    private static final String PREFIX = Msg.STAFF;

    public invsee() {
        super("invsee");
    }

    @Override
    public void execute(CommandSender sender, String command, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + Msg.PLAYERS_ONLY);
            return;
        }
        if (args.length != 1) {
            sender.sendMessage(PREFIX + Msg.usage("/invsee <player>"));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(PREFIX + Msg.PLAYER_NOT_FOUND);
            return;
        }
        if (target == player) {
            sender.sendMessage(PREFIX + Msg.ERROR + "You can't open your own inventory.");
            return;
        }
        player.openInventory(target.getInventory());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String command, String[] args, Location location) {
        return args.length == 1 ? playerNames(sender, args[0]) : Collections.emptyList();
    }
}
