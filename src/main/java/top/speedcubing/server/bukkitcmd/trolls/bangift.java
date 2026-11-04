package top.speedcubing.server.bukkitcmd.trolls;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.speedcubing.common.text.Msg;
import top.speedcubing.lib.bukkit.inventory.BookBuilder;
import top.speedcubing.lib.minecraft.text.ComponentText;
import top.speedcubing.lib.minecraft.text.TextClickEvent;
import top.speedcubing.lib.minecraft.text.TextHoverEvent;
import top.speedcubing.server.lang.Lang;
import top.speedcubing.server.mulitproxy.BungeeProxy;
import top.speedcubing.server.player.User;

public class bangift implements CommandExecutor {
    private static final String PREFIX = Msg.STAFF;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (args.length != 1) {
            player.sendMessage(PREFIX + Msg.usage("/bangift <player>"));
            return true;
        }
        //troll for bangift command
        if (args[0].equals("banself")) {
            bangift.fakeBan(player);
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(PREFIX + Msg.PLAYER_NOT_FOUND);
            return true;
        }
        openBook(player, target);
        return true;
    }

    private static void openBook(Player player, Player target) {
        ItemStack book;
        int lang = User.getUser(target).lang;
        String yes = Lang.of("%lang_server_bangift_book_yes%").getString(lang);
        String hover = Lang.of("%lang_server_bangift_book_accept_hover%").getString(lang);
        book = new BookBuilder("book", "system")
                .addPage(new ComponentText()
                        .str(Lang.of("%lang_server_bangift_book_offer%", User.getUser(player).getPrefixName(true)).getString(lang))
                        .both("              " + yes + "§r\n\n", TextClickEvent.runCommand("/bangift banself"), TextHoverEvent.showText(hover))
                        .both("              " + yes + "\n\n", TextClickEvent.runCommand("/bangift banself"), TextHoverEvent.showText(hover))
                        .str(Lang.of("%lang_server_bangift_book_footer%").getString(lang))
                        .toBungee())
                .build();
        BookBuilder.openBook(book, target);
    }

    public static void fakeBan(Player player) {
        String banId = String.format("%08X", ThreadLocalRandom.current().nextInt());
        BungeeProxy.proxyCommand("kick " + player.getName() + " " + Lang.of("%lang_server_bangift_fake_ban%", banId).getString(User.getUser(player).lang));
    }
}
