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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import top.speedcubing.server.utils.CPSMonitor;

public class cps implements CommandExecutor {

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
                aud.sendMessage(Component.text("Only players can toggle CPS monitoring.").color(NamedTextColor.YELLOW));
                return true;
            }
            UUID staffId = ((Player) sender).getUniqueId();
            UUID removed = CPSMonitor.removeMonitor(staffId);
            if (removed != null) {
                aud.sendMessage(Component.text("CPS monitoring stopped.").color(NamedTextColor.GREEN));
            } else {
                aud.sendMessage(Component.text("You are not monitoring anyone.").color(NamedTextColor.YELLOW));
            }
            return true;
        }

        if (args.length == 1) {
            if (!(sender instanceof Player staffPlayer)) {
                aud.sendMessage(Component.text("Only players can monitor action bars.").color(NamedTextColor.YELLOW));
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                aud.sendMessage(Component.text("Player not found or offline.").color(NamedTextColor.RED));
                return true;
            }

            UUID staffId = staffPlayer.getUniqueId();
            UUID targetId = target.getUniqueId();

            UUID previous = CPSMonitor.putMonitor(staffId, targetId);
            if (previous != null && previous.equals(targetId)) {
                aud.sendMessage(Component.text("You are already monitoring " + target.getName() + ".").color(NamedTextColor.YELLOW));
            } else {
                aud.sendMessage(Component.text("Started monitoring " + target.getName() + "'s CPS (action bar). Run /" + label + " again to stop.").color(NamedTextColor.GREEN));
            }
            return true;
        }

        aud.sendMessage(Component.text("Usage: /" + label + " [player]").color(NamedTextColor.YELLOW));
        return true;
    }
}
