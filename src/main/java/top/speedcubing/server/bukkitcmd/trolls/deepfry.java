package top.speedcubing.server.bukkitcmd.trolls;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutGameStateChange;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.player.User;

public class deepfry implements CommandExecutor {
    private static final String PREFIX = Msg.STAFF;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        // Check if the command has exactly one argument
        if (strings.length != 1) {
            commandSender.sendMessage(PREFIX + Msg.usage("/deepfry <player>"));
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
        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE / 2, 9));
        nmsPlayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(2, 0));
        nmsPlayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(8, 1000f));
        nmsPlayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(7, 4));
        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 9));
        targetPlayer.removePotionEffect(PotionEffectType.NIGHT_VISION);
        commandSender.sendMessage(PREFIX + Msg.SUCCESS + "Deep fried " + User.getUser(targetPlayer).getColorName() + Msg.SUCCESS + "!");
        //nmsPlayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(7, 0));


        return true;
    }
}