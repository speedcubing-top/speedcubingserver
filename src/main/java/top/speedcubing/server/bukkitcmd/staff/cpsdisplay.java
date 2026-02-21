package top.speedcubing.server.bukkitcmd.staff;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import top.speedcubing.lib.bukkit.entity.Hologram;
import top.speedcubing.server.player.User;

public class cpsdisplay implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length != 1) {
            commandSender.sendMessage("/cpsdisplay <player>");
            return true;
        }
        String target = strings[0];
        Player player = Bukkit.getPlayerExact(target);
        if (player == null) {
            commandSender.sendMessage("Player not found.");
            return true;
        }
        update(player);
        return true;
    }

    public static void update(Player player) {
        User user = User.getUser(player);
        if (user.cpsHologram != null) {
            user.removeCPSHologram();
            user.dbUpdate("status=" + null);
            return;
        }
        Hologram h = new Hologram("", true, true).world(player.getWorld().getName());
        h.follow(player, new Vector(0, 2.5, 0));
        h.spawn();
        user.cpsHologram = h;
        user.dbUpdate("status='cps'");
    }
}
