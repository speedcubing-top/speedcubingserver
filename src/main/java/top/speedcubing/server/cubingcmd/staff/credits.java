package top.speedcubing.server.cubingcmd.staff;

import net.minecraft.server.v1_8_R3.PacketPlayOutGameStateChange;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.player.User;
import top.speedcubing.server.system.command.CubingCommand;

public class credits extends CubingCommand {
    private static final String PREFIX = Msg.STAFF;

    public credits() {
        super("credits");
    }

    @Override
    public void execute(CommandSender sender, String command, String[] args) {
        Player target;
        if (args.length == 0 && sender instanceof Player player) {
            target = player;
        } else if (args.length == 1) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(PREFIX + Msg.PLAYER_NOT_FOUND);
                return;
            }
        } else {
            sender.sendMessage(PREFIX + Msg.usage("/credits [player]"));
            return;
        }
        // Reason 4 opens the end-game credits. viewingCredits stays false, so the
        // respawn request the client sends when they close is ignored and the player stays put.
        ((CraftPlayer) target).getHandle().playerConnection.sendPacket(new PacketPlayOutGameStateChange(4, 0));
        sender.sendMessage(PREFIX + Msg.SUCCESS + "Showing the credits to " + User.getUser(target).getColorName() + Msg.SUCCESS + "!");
    }
}
