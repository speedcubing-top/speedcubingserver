package top.speedcubing.server.bukkitcmd.staff;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutGameStateChange;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.player.User;

public class freeze implements CommandExecutor {
    private static final String PREFIX = Msg.STAFF;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        // Check if the command has exactly one argument
        if (strings.length != 1) {
            commandSender.sendMessage(PREFIX + Msg.usage("/freeze <player>"));
            return true;
        }

        // Get the target player
        Player targetPlayer = Bukkit.getPlayer(strings[0]);

        // Check if the target player is online
        if (targetPlayer == null) {
            commandSender.sendMessage(PREFIX + Msg.PLAYER_NOT_FOUND);
            return true;
        }

        // Get the NMS player
        EntityPlayer nmsPlayer = ((CraftPlayer) targetPlayer).getHandle();
        nmsPlayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(7, 25));
        commandSender.sendMessage(PREFIX + Msg.SUCCESS + "Frozen " + User.getUser(targetPlayer).getColorName() + Msg.SUCCESS + "!");
        return true;
    }
}
