package top.speedcubing.server.bukkitcmd.staff;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import top.speedcubing.common.text.Msg;
import top.speedcubing.lib.bukkit.entity.Hologram;
import top.speedcubing.server.player.User;
import top.speedcubing.server.utils.Prefix;

public class cpsdisplay implements CommandExecutor {
    private static final String PREFIX = Prefix.CPS;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length != 1) {
            commandSender.sendMessage(PREFIX + Msg.usage("/cpsdisplay <player>"));
            return true;
        }
        String target = strings[0];
        Player player = Bukkit.getPlayerExact(target);
        if (player == null) {
            commandSender.sendMessage(PREFIX + Msg.PLAYER_NOT_FOUND);
            return true;
        }
        update(player);
        return true;
    }

    public static void update(Player player) {
        User user = User.getUser(player);
        if (user.cpsHologram != null) {
            user.removeCPSHologram();
            // keep the in-memory status in sync, the quit handler writes it back to the DB
            user.status = "null";
            user.dbUpdate("status=" + null);
            return;
        }
        Hologram h = new Hologram("", true, true).world(player.getWorld().getName());
        h.follow(player, new Vector(0, 2.5, 0));
        h.spawn();
        user.cpsHologram = h;
        user.status = "cps";
        user.dbUpdate("status='cps'");
    }
}
