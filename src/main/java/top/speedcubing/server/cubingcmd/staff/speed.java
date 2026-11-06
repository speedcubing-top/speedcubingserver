package top.speedcubing.server.cubingcmd.staff;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.system.command.CubingCommand;

/** /speed <fly|walk> <1-10>: speed in tenths of the client maximum; vanilla defaults are fly 1 and walk 2. */
public class speed extends CubingCommand {
    private static final String PREFIX = Msg.STAFF;

    public speed() {
        super("speed");
    }

    @Override
    public void execute(CommandSender sender, String command, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + Msg.PLAYERS_ONLY);
            return;
        }
        boolean fly = args.length == 2 && args[0].equalsIgnoreCase("fly");
        if (args.length != 2 || !(fly || args[0].equalsIgnoreCase("walk"))) {
            sender.sendMessage(PREFIX + Msg.usage("/speed <fly|walk> <1-10>"));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            amount = 0;
        }
        if (amount < 1 || amount > 10) {
            sender.sendMessage(PREFIX + Msg.ERROR + "Speed must be between " + Msg.VALUE + "1" + Msg.ERROR + " and " + Msg.VALUE + "10" + Msg.ERROR + ".");
            return;
        }
        if (fly)
            player.setFlySpeed(amount * 0.1F);
        else
            player.setWalkSpeed(amount * 0.1F);
        sender.sendMessage(PREFIX + Msg.SUCCESS + (fly ? "Fly" : "Walk") + " speed set to " + Msg.VALUE + amount + Msg.SUCCESS + ".");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String command, String[] args, Location location) {
        return args.length == 1 ? Arrays.asList("fly", "walk") : Collections.emptyList();
    }
}
