package top.speedcubing.server.cubingcmd.staff;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.system.command.CubingCommand;

/** /top: teleports onto the highest block above you. Not named "top": that would shadow the top.* package. */
public class totop extends CubingCommand {
    private static final String PREFIX = Msg.STAFF;

    public totop() {
        super("top");
    }

    @Override
    public void execute(CommandSender sender, String command, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + Msg.PLAYERS_ONLY);
            return;
        }
        Location location = player.getLocation();
        int highest = location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ());
        if (location.getBlockY() >= highest) {
            sender.sendMessage(PREFIX + Msg.ERROR + "You are already at the top.");
            return;
        }
        player.teleport(new Location(location.getWorld(), location.getX(), highest, location.getZ(), location.getYaw(), location.getPitch()));
        sender.sendMessage(PREFIX + Msg.SUCCESS + "Teleported to the top.");
    }
}
