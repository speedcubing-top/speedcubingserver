package top.speedcubing.server.bukkitcmd.nick;

import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import top.speedcubing.common.database.Database;
import top.speedcubing.common.player.PlayerCache;
import top.speedcubing.common.rank.Rank;
import top.speedcubing.lib.api.MojangAPI;
import top.speedcubing.lib.api.mojang.ProfileSkin;
import top.speedcubing.lib.api.mojang.Skin;
import top.speedcubing.lib.bukkit.inventory.BookBuilder;
import top.speedcubing.lib.bukkit.inventory.SignBuilder;
import top.speedcubing.lib.math.scMath;
import top.speedcubing.lib.minecraft.text.ComponentText;
import top.speedcubing.lib.minecraft.text.TextClickEvent;
import top.speedcubing.lib.minecraft.text.TextHoverEvent;
import top.speedcubing.lib.utils.SQL.SQLConnection;
import top.speedcubing.lib.utils.SQL.SQLRow;
import top.speedcubing.lib.utils.SystemUtils;
import top.speedcubing.lib.utils.bytes.ByteArrayBuffer;
import top.speedcubing.server.events.player.NickEvent;
import top.speedcubing.server.player.User;
import top.speedcubing.server.speedcubingServer;
import top.speedcubing.server.utils.WordDictionary;
import top.speedcubing.server.lang.Lang;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class nick implements CommandExecutor, Listener {
    public static final Map<UUID, Boolean> settingNick = new HashMap<>();
    public static final Map<UUID, String> nickName = new HashMap<>();
    public static final Map<UUID, String> nickRank = new HashMap<>();
    private static final Skin STEVESKIN = new Skin("ewogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJpZCIgOiAiZWZhMTFjN2U1YThlNGIwM2JjMDQ0MWRmNzk1YjE0YjIiLAogICAgICAidHlwZSIgOiAiU0tJTiIsCiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTYzNDM4OTUzYTc4MmRhNzY5NDgwYjBhNDkxMjVhOTJlMjU5MjA3NzAwY2I4ZTNlMWFhYzM4ZTQ3MWUyMDMwOCIsCiAgICAgICJwcm9maWxlSWQiIDogImZkNjBmMzZmNTg2MTRmMTJiM2NkNDdjMmQ4NTUyOTlhIiwKICAgICAgInRleHR1cmVJZCIgOiAiOTYzNDM4OTUzYTc4MmRhNzY5NDgwYjBhNDkxMjVhOTJlMjU5MjA3NzAwY2I4ZTNlMWFhYzM4ZTQ3MWUyMDMwOCIKICAgIH0KICB9LAogICJza2luIiA6IHsKICAgICJpZCIgOiAiZWZhMTFjN2U1YThlNGIwM2JjMDQ0MWRmNzk1YjE0YjIiLAogICAgInR5cGUiIDogIlNLSU4iLAogICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85NjM0Mzg5NTNhNzgyZGE3Njk0ODBiMGE0OTEyNWE5MmUyNTkyMDc3MDBjYjhlM2UxYWFjMzhlNDcxZTIwMzA4IiwKICAgICJwcm9maWxlSWQiIDogImZkNjBmMzZmNTg2MTRmMTJiM2NkNDdjMmQ4NTUyOTlhIiwKICAgICJ0ZXh0dXJlSWQiIDogIjk2MzQzODk1M2E3ODJkYTc2OTQ4MGIwYTQ5MTI1YTkyZTI1OTIwNzcwMGNiOGUzZTFhYWMzOGU0NzFlMjAzMDgiCiAgfSwKICAiY2FwZSIgOiBudWxsCn0=", "T6Czh1iATQTwG/ppZyY9N7cNASVHfGkiicrFykYAve4C7vG36ql0EPf6gMfMIS2eL0FdGLznnWiEC2dUxwNCJwiEyzTo/chlxZMk4TSzkBdBU3KTUZdNZrS/YhTzhi7C4eUVaEtXMRlCVtLQUa8Nb18SFYz243C9tlDsONNk42+xHPN1vRCRGIxfJbcU/mk4/XZzS4zHwPCkB6N4dKX2F6LA+a2P+CUMBluXKF56UiT1j7DjWs8B+6ES0kkmZUGkRaxTtcyN2Rqpx/2wCroohxkyVRAdlkcnwbEHOEKGoYMKdjUWpSm8QrsLkUiyLL3IK/hgd5ET2nI/aE1AloAwr1fotmvf9KF1JIfZljoefYZIaYZ1PpvduwIkAaeeIC4FFcdcBIheHitYyXOBAr/t5E+pTzCJOttDfYggFSyGxOj5yxgXTT4gSwTKp5zkQqiCKdAQQPmgFqxhWkZ2UaE9zq+E5jSOD0OJj3FmBscdZWKoOm+mWZkXbw9z2ZvuqXAKHsi6uVJyGeUzt2hJL8eqOyAmfYsJgfxhGZen5oOlxZra8OxIYlp8TcTwzEIDievgp0dfsGPObGVgtA8D39QiwLXs6e/o0qnzl3+wQJDa/ZqDMISULkBNhPx/TvhYW5MJw3hZIj2gsbf73n+jId1GOUfTVMaFlVf7pvPNqW0PieY=");
    private static final Skin ALEXSKIN = new Skin("ewogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJpZCIgOiAiMWRjODQ3ZGViZTg2NDBhOGEzODExODkwZTk0ZTdmNmIiLAogICAgICAidHlwZSIgOiAiU0tJTiIsCiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmI5YWIzNDgzZjgxMDZlY2M5ZTc2YmQ0N2M3MTMxMmIwZjE2YTU4Nzg0ZDYwNjg2NGYzYjNlOWNiMWZkN2I2YyIsCiAgICAgICJwcm9maWxlSWQiIDogIjc3MjdkMzU2NjlmOTQxNTE4MDIzZDYyYzY4MTc1OTE4IiwKICAgICAgInRleHR1cmVJZCIgOiAiZmI5YWIzNDgzZjgxMDZlY2M5ZTc2YmQ0N2M3MTMxMmIwZjE2YTU4Nzg0ZDYwNjg2NGYzYjNlOWNiMWZkN2I2YyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfSwKICAic2tpbiIgOiB7CiAgICAiaWQiIDogIjFkYzg0N2RlYmU4NjQwYThhMzgxMTg5MGU5NGU3ZjZiIiwKICAgICJ0eXBlIiA6ICJTS0lOIiwKICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmI5YWIzNDgzZjgxMDZlY2M5ZTc2YmQ0N2M3MTMxMmIwZjE2YTU4Nzg0ZDYwNjg2NGYzYjNlOWNiMWZkN2I2YyIsCiAgICAicHJvZmlsZUlkIiA6ICI3NzI3ZDM1NjY5Zjk0MTUxODAyM2Q2MmM2ODE3NTkxOCIsCiAgICAidGV4dHVyZUlkIiA6ICJmYjlhYjM0ODNmODEwNmVjYzllNzZiZDQ3YzcxMzEyYjBmMTZhNTg3ODRkNjA2ODY0ZjNiM2U5Y2IxZmQ3YjZjIiwKICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgIH0KICB9LAogICJjYXBlIiA6IG51bGwKfQ==", "Bl/hfaMcGIDwYEl1fqSiPxj2zTGrTMJomqEODvB97VbJ8cs7kfLZIC1bRaCzlFHo5BL0bChL8aQRs/DJGkxmfOC5PfQXubxA4/PHgnNq6cqPZvUcC4hjWdTSKAbZKzHDGiH8aQtuEHVpHeb9T+cutsS0i2zEagWeYVquhFFtctSZEh5+JWxQOba+eh7xtwmzlaXfUDYguzHSOSV4q+hGzSU6osxO/ddiy4PhmFX1MZo237Wp1jE5Fjq+HN4J/cpm/gbtGQBfCuTE7NP3B+PKCXAMicQbQRZy+jaJ+ysK8DJP/EulxyERiSLO9h8eYF5kP5BT5Czhm9FoAwqQlpTXkJSllcdAFqiEZaRNYgJqdmRea4AeyCLPz83XApTvnHyodss1lQpJiEJuyntpUy1/xYNv+EdrNvwCnUPS/3/+jA/VKjAiR9ebKTVZL8A5GHR4mKp7uaaL1DouQa2VOJmQHKo3++v6HGsz1Xk6J7n/8qVUp3oS79WqLxlZoZPBIuQ90xt8Yqhxv6e9FXD4egHsabVj5TO/bZE6pEUaVTrKv49ciE0RqjZHxR5P13hFsnMJTXnT5rzAVCkJOvjaPfZ70WiLJL3X4OOt1TrGK0CoBKQt7yLbU5Eap6P+SLusHrZx+oU4Xspimb79splBxOsbhvb+olbRrJhmxIcrhVIqHDY=");

    public enum NickBook {
        EULA,
        RANK,
        SKIN,
        NAMECHOOSE,
        NAMECUSTOM,
        NAMERANDOM,
        RULE
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!((NickEvent) new NickEvent((Player) commandSender).call()).isCancelled()) {
            Player player = (Player) commandSender;
            if (strings.length == 1) {
                if (strings[0].equalsIgnoreCase("nickeula") && settingNick.get(player.getUniqueId())) {
                    return true;
                } else if (strings[0].equalsIgnoreCase("nickrank") && settingNick.get(player.getUniqueId())) {
                    openNickBook(player, NickBook.RANK);
                    return true;
                } else if (strings[0].contains("nickskin") && settingNick.get(player.getUniqueId())) {
                    switch (strings[0]) {
                        case "nickskindefault":
                            nickRank.put(player.getUniqueId(), "default");
                            break;
                        case "nickskinvip":
                            nickRank.put(player.getUniqueId(), "vip");
                            break;
                        case "nickskinvipplus":
                            nickRank.put(player.getUniqueId(), "vipplus");
                            break;
                        case "nickskinpremium":
                            nickRank.put(player.getUniqueId(), "premium");
                            break;
                        case "nickskinpremiumplus":
                            nickRank.put(player.getUniqueId(), "premiumplus");
                            break;
                        case "nickskinyt":
                            nickRank.put(player.getUniqueId(), "yt");
                            break;
                        case "nickskinytplus":
                            nickRank.put(player.getUniqueId(), "ytplus");
                            break;
                    }
                    openNickBook(player, NickBook.SKIN);
                    return true;
                } else if (strings[0].contains("nicknamechoose") && settingNick.get(player.getUniqueId())) {
                    switch (strings[0]) {
                        case "nicknamechoosemyskin":
                            player.performCommand("skin " + User.getUser(player).realName);
                            break;
                        case "nicknamechoosesaskin":
                            boolean random = new Random().nextBoolean();
                            User.getUser(player).uploadSkin(random ? STEVESKIN : ALEXSKIN);
                            if (random) {
                                User.getUser(player).sendMessage("%lang_server_nick_skin_steve%");
                            } else {
                                User.getUser(player).sendMessage("%lang_server_nick_skin_alex%");
                            }
                            break;
                        case "nicknamechooserandomskin":
                            ProfileSkin profileSkin = speedcubingServer.generateRandomSkinFromDB();
                            User.getUser(player).uploadSkin(profileSkin.getSkin());
                            User.getUser(player).sendMessage("%lang_server_nick_skin_random%", profileSkin.getName());
                            break;
                    }
                    openNickBook(player, NickBook.NAMECHOOSE);
                    return true;
                } else if (strings[0].equalsIgnoreCase("nicknamecustom") && settingNick.get(player.getUniqueId())) {
                    openNickBook(player, NickBook.NAMECUSTOM);
                    return true;
                } else if (strings[0].equalsIgnoreCase("nicknamerandom") && settingNick.get(player.getUniqueId())) {
                    openNickBook(player, NickBook.NAMERANDOM);
                    return true;
                } else if (strings[0].equalsIgnoreCase("nickrule") && settingNick.get(player.getUniqueId())) {
                    openNickBook(player, NickBook.RULE);
                    return true;
                } else if (strings[0].equalsIgnoreCase("reuse")) {
                    SQLRow result = User.getUser(commandSender).dbSelect("nickname,nickpriority");
                    if (result.getString(0).isEmpty()) {
                        User.getUser(commandSender).sendMessage("%lang_server_nick_no_previous%");
                    } else if (result.getString(0).equals(commandSender.getName())) {
                        User.getUser(commandSender).sendMessage("%lang_nick_already%");
                    } else {
                        nick.nickPlayer(result.getString(0), result.getString(1), true, (Player) commandSender, false);
                        User.getUser(commandSender).sendMessage("%lang_server_nick_reused%");
                    }
                    return true;
                }
                String name = strings[0];
                User user = User.getUser(commandSender);
                if (name.equals(commandSender.getName()))
                    user.sendMessage("%lang_nick_same%");
                else if (name.equals(user.realName))
                    user.sendMessage("%lang_nick_default%");
                else
                    nickCheck(user, name, user.player, user.dbSelect("nickpriority").getString(0), false);
            } else if (strings.length == 2) {
                User user = User.getUser(commandSender);
                if (user.hasPermission("perm.nick.nickrank%")) {
                    String name = strings[0];
                    if (Rank.rankByName.containsKey(strings[1].toLowerCase())) {
                        nickCheck(user, name, user.player, strings[1].toLowerCase(), false);
                    } else
                        user.sendMessage("%lang_rank_unknown%");
                } else user.sendMessage("%lang_server_nick_usage%");
            } else if (strings.length == 3) {
                User user = User.getUser(commandSender);
                if (user.hasPermission("perm.nick.nickrank") || nickRank.get(player.getUniqueId()).equals("default")
                        || nickRank.get(player.getUniqueId()).equals("vip") || nickRank.get(player.getUniqueId()).equals("vipplus")
                        || nickRank.get(player.getUniqueId()).equals("premium") || nickRank.get(player.getUniqueId()).equals("premiumplus")) {
                    String name = strings[0];
                    if (Rank.rankByName.containsKey(strings[1].toLowerCase())) {
                        nickCheck(user, name, user.player, strings[1].toLowerCase(), Boolean.parseBoolean(strings[2]));
                        if (strings[2].equalsIgnoreCase("true")) {
                            openNickBook(player, NickBook.RULE);
                        }
                    } else
                        user.sendMessage("%lang_rank_unknown%");
                } else user.sendMessage("%lang_server_nick_usage%");
            } else if (strings.length == 0) {
                settingNick.put(player.getUniqueId(), true);
                openNickBook(player, NickBook.EULA);
            } else User.getUser(commandSender).sendMessage("%lang_server_nick_usage%");
        }
        return true;
    }


    private void nickCheck(User user, String name, Player player, String rank, boolean openBook) {
        if (!user.hasPermission("perm.nick.customname") && !settingNick.containsKey(user.bGetUniqueId())) {
            openNickBook(player, NickBook.EULA);
            settingNick.put(user.bGetUniqueId(), true);
            return;
        }

        boolean allow;

        try (SQLConnection connection = Database.getCubing()) {
            allow = (user.hasPermission("perm.nick.legacyregex") ?
                    speedcubingServer.legacyNameRegex :
                    speedcubingServer.nameRegex)
                    .matcher(name).matches() &&
                    !connection.exist("playersdata",
                            "name='" + name + "' OR id!='" + user.id + "' AND nickname='" + name + "'");
        }

        if (allow) {
            if (!user.hasPermission("perm.nick.anyname")) {
                try {
                    if (MojangAPI.getByName(name) != null)
                        allow = false;
                } catch (IOException ignored) {
                }
            }
        }
        if (allow)
            nickPlayer(name, rank, true, player, openBook);
        else {
            settingNick.remove(player.getUniqueId());
            nickName.remove(player.getUniqueId());
            nickRank.remove(player.getUniqueId());
            user.sendMessage("%lang_nick_unaval%");
        }
    }

    static void nickPlayer(String displayName, String displayRank, boolean nick, Player player, boolean openBook) {
        User user = User.getUser(player);


        //packet
        //user.createTeamPacket(nick, displayName);

        //send packets
//        for (User u : User.getUsers()) {
//            if (u.player.canSee(player)) {
//                u.bHidePlayer(player);
//                u.bShowPlayer(player);
//            }
//            u.sendPacket(user.joinPacket);
//        }

//        user.sendPacket(
//                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer),
//                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
        try (SQLConnection connection = Database.getCubing()) {
            user.dbUpdate("nicked=" + (nick ? 1 : 0) + (nick ? ",nickname='" + displayName + "',nickpriority='" + displayRank + "'" : ""));
            connection.update("onlineplayer", "displayname='" + displayName + "',displayrank='" + displayRank + "'", "id=" + user.id);
            PlayerCache.invalidate(user.uuid);
            user.writeToProxy(new ByteArrayBuffer().writeUTF("nick").writeInt(user.id).writeUTF(displayRank).writeUTF(displayName).toByteArray());
        }
        user.setNickState(nick);

        if (nick) {
            try (SQLConnection connection = Database.getSystem()) {
                connection.insert("nicknames", "uuid,name,nickname,nicktime")
                        .values("'" + user.uuid + "','" + user.realName + "','" + displayName + "'," + SystemUtils.getCurrentSecond())
                        .execute();
            }
        }

        if (openBook) {
            openNickBook(player, NickBook.RULE);
        }

        settingNick.remove(player.getUniqueId());
    }

    private static String t(int lang, String key, String... param) {
        return Lang.of("%lang_" + key + "%", param).getString(lang);
    }

    private static ComponentText rankOption(ComponentText text, int lang, String display, String rank) {
        return text.both("§0➤ " + display + "\n", TextClickEvent.runCommand("/nick nickskin" + rank), TextHoverEvent.showText(t(lang, "server_nick_book_rank_hover", display)));
    }

    public static void openNickBook(Player player, NickBook type) {
        int lang = User.getUser(player).lang;
        ItemStack book;
        switch (type) {
            case EULA:
                book = new BookBuilder("eula", "system")
                        .addPage(new ComponentText().str(t(lang, "server_nick_book_eula"))
                                .both("\n\n➤ §n" + t(lang, "server_nick_book_eula_accept"), TextClickEvent.runCommand("/nick nickrank"), TextHoverEvent.showText(t(lang, "server_nick_book_eula_accept_hover")))
                                .toBungee())
                        .build();
                BookBuilder.openBook(book, player);
                break;
            case RANK:
                ComponentText rankPage = new ComponentText().str(t(lang, "server_nick_book_rank") + "\n\n");
                rankOption(rankPage, lang, "§8DEFAULT", "default");
                rankOption(rankPage, lang, "§dVIP", "vip");
                rankOption(rankPage, lang, "§dVIP+", "vipplus");
                rankOption(rankPage, lang, "§6PREMIUM", "premium");
                rankOption(rankPage, lang, "§6PREMIUM+", "premiumplus");
                if (User.getUser(player).hasPermission("perm.nick.nickrank")) {
                    rankOption(rankPage, lang, "§5YT", "yt");
                    rankOption(rankPage, lang, "§4YT+", "ytplus");
                }
                book = new BookBuilder("rank", "system")
                        .addPage(rankPage.toBungee())
                        .build();
                BookBuilder.openBook(book, player);
                break;
            case SKIN:
                book = new BookBuilder("skin", "system")
                        .addPage(new ComponentText().str(t(lang, "server_nick_book_skin") + "\n\n")
                                .both("➤ " + t(lang, "server_nick_book_skin_normal") + "\n", TextClickEvent.runCommand("/nick nicknamechoosemyskin"), TextHoverEvent.showText(t(lang, "server_nick_book_skin_normal_hover")))
                                .both("➤ " + t(lang, "server_nick_book_skin_steve_alex") + "\n", TextClickEvent.runCommand("/nick nicknamechoosesaskin"), TextHoverEvent.showText(t(lang, "server_nick_book_skin_steve_alex_hover")))
                                .both("➤ " + t(lang, "server_nick_book_skin_random") + "\n", TextClickEvent.runCommand("/nick nicknamechooserandomskin"), TextHoverEvent.showText(t(lang, "server_nick_book_skin_random_hover")))
                                .toBungee())
                        .build();
                BookBuilder.openBook(book, player);
                break;
            case NAMECHOOSE:
                try (SQLConnection connection = Database.getCubing()) {
                    String data = connection.select("nickname")
                            .from("playersdata")
                            .where("id=" + User.getUser(player).id)
                            .executeResult().getString();
                    nickName.put(player.getUniqueId(), data);

                    ComponentText namePage = new ComponentText().str(t(lang, "server_nick_book_name") + "\n\n");
                    if (User.getUser(player).hasPermission("perm.nick.customname"))
                        namePage.both("➤ " + t(lang, "server_nick_book_name_custom") + "\n", TextClickEvent.runCommand("/nick nicknamecustom"), TextHoverEvent.showText(t(lang, "server_nick_book_name_custom_hover")));
                    namePage.both("➤ " + t(lang, "server_nick_book_name_random") + "\n", TextClickEvent.runCommand("/nick nicknamerandom"), TextHoverEvent.showText(t(lang, "server_nick_book_name_random_hover")))
                            .both("➤ " + t(lang, "server_nick_book_name_reuse", data) + "\n\n", TextClickEvent.runCommand("/nick " + data + " " + nickRank.get(player.getUniqueId()) + " true"), TextHoverEvent.showText(t(lang, "server_nick_book_name_reuse_hover", data)))
                            .str(t(lang, "server_nick_book_unnick_hint"));
                    book = new BookBuilder("name", "system")
                            .addPage(namePage.toBungee())
                            .build();
                    BookBuilder.openBook(book, player);
                }

                break;
            case NAMECUSTOM:
                String[] lines = {"", t(lang, "server_nick_sign_enter_name")};
                SignBuilder.openSign(player, -61, 1, 41, lines);
                break;
            case NAMERANDOM:
                User.getUser(player).sendMessage("%lang_server_nick_generating%");
                speedcubingServer.scheduledPool.execute(() -> {
                    String name = generateRandomString();
                    nickName.put(player.getUniqueId(), name);
                    ComponentText randomPage = new ComponentText().str(t(lang, "server_nick_book_random", name) + "\n\n")
                            .both("   §a§n" + t(lang, "server_nick_book_random_use") + "§r\n", TextClickEvent.runCommand("/nick " + name + " " + nickRank.get(player.getUniqueId()) + " true"),
                                    TextHoverEvent.showText(t(lang, "server_nick_book_random_use_hover")))
                            .both("   §c§n" + t(lang, "server_nick_book_random_retry") + "§r\n", TextClickEvent.runCommand("/nick nicknamerandom"), TextHoverEvent.showText(t(lang, "server_nick_book_random_retry_hover")));
                    if (User.getUser(player).hasPermission("perm.nick.customname"))
                        randomPage.both("\n§0§n" + t(lang, "server_nick_book_random_custom"), TextClickEvent.runCommand("/nick nicknamecustom"), TextHoverEvent.showText(t(lang, "server_nick_book_random_custom_hover")));
                    ItemStack b = new BookBuilder("random", "system")
                            .addPage(randomPage.toBungee())
                            .build();
                    BookBuilder.openBook(b, player);
                });
                break;
            case RULE:
                book = new BookBuilder("rule", "system")
                        .addPage(new ComponentText().str(t(lang, "server_nick_book_done", Rank.rankByName.get(nickRank.get(player.getUniqueId())).getFormat().getPrefix() + nickName.get(player.getUniqueId())) +
                                        "\n\n" + t(lang, "server_nick_book_unnick_hint"))
                                .toBungee())
                        .build();
                BookBuilder.openBook(book, player);
                break;
        }
    }

    public static String generateRandomString() {
        String name = "";
        Random random = new Random();
        int r = random.nextInt(5);
        name = switch (r) {
            case 0 -> getRandomAdjective() + getRandomWord() + getRandomWord();
            case 1 -> getRandomAdjective() + getRandomWord() + getRandomNumber();
            case 2 -> getRandomWord() + getRandomAdjective();
            case 3 -> getRandomAdjective() + getRandomVerb();
            case 4 -> getRandomWord() + getRandomAdjective() + getRandomNumber();
            default -> name;
        };
        if (!speedcubingServer.nameRegex.matcher(name).matches()) {
            return generateRandomString();
        }
        return name;
    }

    public static String upper(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String getRandomAdjective() {
        return upper(getRandomWord(POS.ADJECTIVE));
    }

    public static String getRandomVerb() {
        return upper(getRandomWord(POS.VERB));
    }

    public static String getRandomAdverb() {
        return upper(getRandomWord(POS.ADVERB));
    }

    public static String getRandomWord() {
        return upper(getRandomWord(POS.NOUN));
    }

    public static int getRandomNumber() {
        return scMath.randomInt(1990, 2010);
    }

    public static String getRandomWord(POS pos) {
        List<String> words = new ArrayList<>();
        for (Iterator<IIndexWord> it = WordDictionary.dict.getIndexWordIterator(pos); it.hasNext(); ) {
            IIndexWord indexWord = it.next();
            String lemma = indexWord.getLemma();
            if (!lemma.contains(" ") && !lemma.contains("-")) {
                words.add(lemma);
            }
        }
        if (words.isEmpty()) {
            speedcubingServer.getInstance().getLogger().warning("No words found in WordNet for POS: " + pos);
            return "unknown";
        }
        Random random = new Random();
        return words.get(random.nextInt(words.size()));
    }

}