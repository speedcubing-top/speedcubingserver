package top.speedcubing.server.share;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import top.speedcubing.common.chatfilter.ChatFilter;
import top.speedcubing.common.chatfilter.FilterResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import top.speedcubing.common.database.Database;
import top.speedcubing.common.server.MinecraftServer;
import top.speedcubing.common.text.Msg;
import top.speedcubing.lib.discord.DiscordWebhook;
import top.speedcubing.lib.minecraft.MinecraftConsole;
import top.speedcubing.lib.minecraft.text.MinecraftTextUtils;
import top.speedcubing.server.utils.Prefix;
import top.speedcubing.lib.utils.SQL.SQLConnection;
import top.speedcubing.lib.utils.SQL.SQLResult;
import top.speedcubing.lib.utils.SQL.SQLRow;
import top.speedcubing.lib.utils.SystemUtils;
import top.speedcubing.lib.utils.TimeFormatter;
import top.speedcubing.server.lang.Lang;
import top.speedcubing.server.player.User;

public class Chat {

    public static void globalChat(Collection<? extends Player> players, Player sender, Lang format, String message, String... replace) {
        // normally stopped at the proxy already, but chat can reach a server without passing it
        FilterResult filterResult = ChatFilter.get().check(message);
        if (filterResult.isBlocked()) {
            User.getUser(sender).sendMessage("%lang_chat_filter_blocked%");
            return;
        }

        format.param(replace);

        Lang unfiltered = format.clone().param(message);
        Lang filtered = filterResult.isMasked() ? format.clone().param(filterResult.masked()) : unfiltered;

        try (SQLConnection connection = Database.getCubing()) {
            SQLResult result = connection.select("uuid")
                    .from("ignorelist")
                    .where("target='" + sender.getUniqueId() + "'")
                    .executeResult();
            User user;
            c:
            for (Player p : players) {
                user = User.getUser(p);
                for (SQLRow r : result) {
                    if (user.player.getUniqueId().toString().equals(r.getString("uuid"))) {
                        continue c;
                    }
                }
                user.sendMessage(user.chatFilt ? filtered : unfiltered);
            }
        }

        MinecraftConsole.printlnColor(Prefix.CHAT_LOG + "§8[§b" + sender.getWorld().getName() + "§8] §r" + unfiltered.getString(0));
        chatLogger(sender, MinecraftTextUtils.removeColorCode(unfiltered.getComponent(0).toText()));
    }

    private static final Executor discordWebhookPool = Executors.newSingleThreadExecutor();

    private static void chatLogger(Player sender, String message) {
        String timeFormat = TimeFormatter.unixToRealTime(SystemUtils.getCurrentSecond(), "HH:mm:ss", TimeUnit.SECONDS);
        DiscordWebhook discordWebhook = new DiscordWebhook(MinecraftServer.getServer(Bukkit.getServerName()).getWebhook());
        message = ChatColor.stripColor(message.replaceAll("`", "'").replaceAll("\n", "/n"));
        discordWebhook.setContent("```[" + timeFormat + "] " + "[" + sender.getWorld().getName() + "] " + message + "```");
        discordWebhookPool.execute(() -> {
            try {
                discordWebhook.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
