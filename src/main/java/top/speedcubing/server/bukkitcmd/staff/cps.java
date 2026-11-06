// GPT-5 mini
package top.speedcubing.server.bukkitcmd.staff;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import top.speedcubing.common.text.Msg;
import top.speedcubing.server.player.User;
import top.speedcubing.server.utils.Prefix;

import top.speedcubing.server.utils.CPSMonitor;

public class cps implements CommandExecutor {

    private static final String PREFIX = Prefix.CPS;

    private static void send(Audience aud, String message) {
        aud.sendMessage(LegacyComponentSerializer.legacySection().deserialize(PREFIX + message));
    }

    private final BukkitAudiences audiences;

    public cps(BukkitAudiences audiences) {
        this.audiences = audiences;
    }

    private Audience audienceFor(CommandSender sender) {
        if (sender instanceof Player) {
            return audiences.player((Player) sender);
        }
        return audiences.console();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Audience aud = audienceFor(sender);

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                send(aud, Msg.PLAYERS_ONLY);
                return true;
            }
            UUID staffId = ((Player) sender).getUniqueId();
            UUID removed = CPSMonitor.removeMonitor(staffId);
            if (removed != null) {
                send(aud, Msg.SUCCESS + "CPS monitoring stopped.");
            } else {
                send(aud, Msg.ERROR + "You are not monitoring anyone.");
            }
            return true;
        }

        if (args.length == 1) {
            if (!(sender instanceof Player staffPlayer)) {
                send(aud, Msg.PLAYERS_ONLY);
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                send(aud, Msg.PLAYER_NOT_FOUND);
                return true;
            }

            UUID staffId = staffPlayer.getUniqueId();
            UUID targetId = target.getUniqueId();

            UUID previous = CPSMonitor.putMonitor(staffId, targetId);
            if (previous != null && previous.equals(targetId)) {
                send(aud, Msg.ERROR + "You are already monitoring " + User.getUser(target).getColorName() + Msg.ERROR + ".");
            } else {
                send(aud, Msg.SUCCESS + "Started monitoring " + User.getUser(target).getColorName() + Msg.SUCCESS + "'s CPS (action bar). Run /" + label + " again to stop.");
            }
            return true;
        }

        send(aud, Msg.usage("/" + label + " [player]"));
        return true;
    }
}
