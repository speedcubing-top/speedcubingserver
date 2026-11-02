package top.speedcubing.server.bukkitcmd.trolls;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.speedcubing.lib.bukkit.inventory.BookBuilder;
import top.speedcubing.lib.minecraft.text.ComponentText;
import top.speedcubing.lib.minecraft.text.TextClickEvent;
import top.speedcubing.lib.minecraft.text.TextHoverEvent;
import top.speedcubing.server.mulitproxy.BungeeProxy;
import top.speedcubing.server.player.User;

public class bangift implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        Player target = Bukkit.getPlayerExact(args[0]);
        if (args.length != 1) {
            player.sendMessage("§cUsage: /bangift <player>");
            return true;
        }
        //troll for bangift command
        if (args[0].equals("banself")) {
            bangift.fakeBan(player);
            return true;
        }
        if (target == null) {
            player.sendMessage("§cPlayer not found.");
            return true;
        }
        openBook(player, target);
        return true;
    }

    private static void openBook(Player player, Player target) {
        ItemStack book;
        book = new BookBuilder("book", "system")
                .addPage(new ComponentText()
                        .str(User.getUser(player).getPrefixName(true) + " §0wants to give you §cBanned 365\n§0Days!\nWill you accept?\n\n")
                        .both("              §a§l§nYES§r\n\n", TextClickEvent.runCommand("/bangift banself"), TextHoverEvent.showText("Click here to accept!"))
                        .both("              §a§l§nYES\n\n", TextClickEvent.runCommand("/bangift banself"), TextHoverEvent.showText("Click here to accept!"))
                        .str("§0Issues? Contact the Speedcubing Team at \n\n§d§nhttps://speedcubing.top")
                        .toBungee())
                .build();
        BookBuilder.openBook(book, target);
    }

    public static void fakeBan(Player player) {
        BungeeProxy.proxyCommand("kick " + player.getName() + " §4You are banned from this server.\n\n§fBanned Reason: §cBanned Gift\n\n§fExpires in: §e365 days\n\n§fBan ID: §eLOLOLOLO" +
                "\n\n§bspeedcubing.top/discord §cfor more information.");
    }
}
