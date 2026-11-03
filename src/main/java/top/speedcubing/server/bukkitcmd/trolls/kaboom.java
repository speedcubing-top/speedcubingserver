package top.speedcubing.server.bukkitcmd.trolls;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;
import top.speedcubing.common.text.Msg;
import top.speedcubing.lib.minecraft.MinecraftConsole;
import top.speedcubing.server.lang.Lang;
import top.speedcubing.server.player.User;
import top.speedcubing.server.utils.Prefix;

public class kaboom implements CommandExecutor {
    private static final String PREFIX = Prefix.STAFF;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0) {
            String senderName = commandSender.getName();
            displaySubtitleToAllPlayers("§l§cKABOOM!", senderName, commandSender);
            applyVelocityToAllPlayers(new Vector(0, 4, 0), commandSender);
            issueLightningStrikesToAllPlayers(commandSender);
            commandSender.sendMessage(PREFIX + "§l§cKABOOM!");
            MinecraftConsole.printlnColor(PREFIX + Msg.INFO + senderName + " caused a nuclear reaction!");
        } else {
            commandSender.sendMessage(PREFIX + Msg.usage("/kaboom"));
            commandSender.sendMessage(PREFIX + "§4WARNING! §cTHIS COMMAND IS NOT FOR PRODUCTION AND MIGHT CAUSE FALSE BANS ON KNOCKBACKFFA");
        }
        return true;
    }

    private void applyVelocityToAllPlayers(Vector velocity, CommandSender commandSender) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.setVelocity(velocity);
            commandSender.sendMessage(PREFIX + Msg.SUCCESS + "Launched " + Msg.VALUE + User.getUser(player).realName + Msg.SUCCESS + "!");
        });
    }

    private void issueLightningStrikesToAllPlayers(CommandSender commandSender) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.getWorld().strikeLightning(player.getLocation());
            player.setHealth(20);
        });
    }

    private void displaySubtitleToAllPlayers(String subtitle, String senderName, CommandSender commandSender) {
        String by = User.getUser(commandSender).getPrefixName(true);
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendTitle(subtitle, Lang.of("%lang_server_kaboom_by%", by).getString(User.getUser(player).lang));
        });
    }

    private String velocityToString(Vector velocity) {
        return "(" + velocity.getX() + "," + velocity.getY() + "," + velocity.getZ() + ")";
    }
}
