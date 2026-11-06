package top.speedcubing.server.cubingcmd.staff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.system.command.CubingCommand;

/** /tpworld <world>: teleports to the spawn point of another loaded world. */
public class tpworld extends CubingCommand {
    private static final String PREFIX = Msg.STAFF;

    public tpworld() {
        super("tpworld");
    }

    @Override
    public void execute(CommandSender sender, String command, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + Msg.PLAYERS_ONLY);
            return;
        }
        if (args.length != 1) {
            sender.sendMessage(PREFIX + Msg.usage("/tpworld <world>"));
            return;
        }
        World world = Bukkit.getWorld(args[0]);
        if (world == null) {
            sender.sendMessage(PREFIX + Msg.ERROR + "World not found.");
            return;
        }
        if (world == player.getWorld()) {
            sender.sendMessage(PREFIX + Msg.ERROR + "You are already in " + Msg.VALUE + world.getName() + Msg.ERROR + ".");
            return;
        }
        player.teleport(world.getSpawnLocation());
        sender.sendMessage(PREFIX + Msg.SUCCESS + "Teleported to " + Msg.VALUE + world.getName() + Msg.SUCCESS + ".");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String command, String[] args, Location location) {
        if (args.length != 1)
            return Collections.emptyList();
        List<String> names = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            if (world.getName().toLowerCase().startsWith(args[0].toLowerCase()))
                names.add(world.getName());
        }
        return names;
    }
}
