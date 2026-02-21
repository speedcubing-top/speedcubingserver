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
                                player.sendMessage("§aSet your skin to Steve.");
                            } else {
                                player.sendMessage("§aSet your skin to Alex.");
                            }
                            break;
                        case "nicknamechooserandomskin":
                            ProfileSkin profileSkin = speedcubingServer.generateRandomSkinFromDB();
                            User.getUser(player).uploadSkin(profileSkin.getSkin());
                            player.sendMessage("§aSet your skin to " + profileSkin.getName() + ".");
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
                        commandSender.sendMessage("You haven't used a nick before. Please use /nick <nickname>");
                    } else if (result.getString(0).equals(commandSender.getName())) {
                        User.getUser(commandSender).sendMessage("%lang_nick_already%");
                    } else {
                        nick.nickPlayer(result.getString(0), result.getString(1), true, (Player) commandSender, false);
                        commandSender.sendMessage("§aYou have reused your nickname!");
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
                } else commandSender.sendMessage("/nick <nickname>\n/nick (use the previous nick)");
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
                } else commandSender.sendMessage("/nick <nickname>\n/nick (use the previous nick)");
            } else if (strings.length == 0) {
                settingNick.put(player.getUniqueId(), true);
                openNickBook(player, NickBook.EULA);
            } else commandSender.sendMessage("/nick <nickname>\n/nick (use the previous nick)");
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
            user.writeToProxy(new ByteArrayBuffer().writeUTF("nick").writeInt(user.id).writeUTF(displayRank).writeUTF(displayName).toByteArray());
        }

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

    public static void openNickBook(Player player, NickBook type) {
        ItemStack book;
        switch (type) {
            case EULA:
                book = new BookBuilder("eula", "system")
                        .addPage(new ComponentText().str("Nicknames allow you to\nplay with a different\n\n All rules still apply.\nYou can still be\nreported and all name\nhistory is stored.")
                                .both("\n\n➤ §nI understand, set up my nickname", TextClickEvent.runCommand("/nick nickrank"), TextHoverEvent.showText("Click here to proceed."))
                                .toBungee())
                        .build();
                BookBuilder.openBook(book, player);
                break;
            case RANK:
                if (User.getUser(player).hasPermission("perm.nick.nickrank")) {
                    book = new BookBuilder("rank", "system")
                            .addPage(new ComponentText().str("Let's get you set up\nwith your nickname!\nFirst, you'll need to\n choose which §lRANK\n§r§0you would like to be\n" +
                                            "shown as when nicked.\n\n")
                                    .both("§0➤ §8DEFAULT\n", TextClickEvent.runCommand("/nick nickskindefault"), TextHoverEvent.showText("Click her to be shown as §8DEFAULT"))
                                    .both("§0➤ §dVIP\n", TextClickEvent.runCommand("/nick nickskinvip"), TextHoverEvent.showText("Click her to be shown as §dVIP"))
                                    .both("§0➤ §dVIP+\n", TextClickEvent.runCommand("/nick nickskinvipplus"), TextHoverEvent.showText("Click her to be shown as §dVIP+"))
                                    .both("§0➤ §6PREMIUM\n", TextClickEvent.runCommand("/nick nickskinpremium"), TextHoverEvent.showText("Click her to be shown as §6PREMIUM"))
                                    .both("§0➤ §6PREMIUM+\n", TextClickEvent.runCommand("/nick nickskinpremiumplus"), TextHoverEvent.showText("Click her to be shown as §6PREMIUM+"))
                                    .both("§0➤ §5YT\n", TextClickEvent.runCommand("/nick nickskinyt"), TextHoverEvent.showText("Click her to be shown as §5YT"))
                                    .both("§0➤ §4YT+\n", TextClickEvent.runCommand("/nick nickskinytplus"), TextHoverEvent.showText("Click her to be shown as §4YT+"))
                                    .toBungee())
                            .build();
                } else {
                    book = new BookBuilder("rank", "system")
                            .addPage(new ComponentText().str("Let's get you set up\nwith your nickname!\nFirst, you'll need to\n choose which §lRANK\n§r§0you would like to be\n" +
                                            "shown as when nicked.\n\n")
                                    .both("§0➤ §8DEFAULT\n", TextClickEvent.runCommand("/nick nickskindefault"), TextHoverEvent.showText("Click her to be shown as §8DEFAULT"))
                                    .both("§0➤ §dVIP\n", TextClickEvent.runCommand("/nick nickskinvip"), TextHoverEvent.showText("Click her to be shown as §dVIP"))
                                    .both("§0➤ §dVIP+\n", TextClickEvent.runCommand("/nick nickskinvipplus"), TextHoverEvent.showText("Click her to be shown as §dVIP+"))
                                    .both("§0➤ §6PREMIUM\n", TextClickEvent.runCommand("/nick nickskinpremium"), TextHoverEvent.showText("Click her to be shown as §6PREMIUM"))
                                    .toBungee())
                            .build();
                }
                BookBuilder.openBook(book, player);
                break;
            case SKIN:
                book = new BookBuilder("skin", "system")
                        .addPage(new ComponentText().str("Awesome! Now, which §lSKIN §r§0would you like to\nhave while nicked?\n\n")
                                .both("➤ My normal skin\n", TextClickEvent.runCommand("/nick nicknamechoosemyskin"), TextHoverEvent.showText("Click here to use your normal skin."))
                                .both("➤ Steve/Alex skin\n", TextClickEvent.runCommand("/nick nicknamechoosesaskin"), TextHoverEvent.showText("Click here to use Steve/Alex skin."))
                                .both("➤ Random skin\n", TextClickEvent.runCommand("/nick nicknamechooserandomskin"), TextHoverEvent.showText("Click here to use random preset skin."))
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

                    if (User.getUser(player).hasPermission("perm.nick.customname")) {
                        book = new BookBuilder("name", "system")
                                .addPage(new ComponentText().str("Alright, now you'll need\nto choose the §lNAME to use!§r§0\n\n")
                                        .both("➤ Enter a name\n", TextClickEvent.runCommand("/nick nicknamecustom"), TextHoverEvent.showText("Click here to enter a custom name."))
                                        .both("➤ Use a random name\n", TextClickEvent.runCommand("/nick nicknamerandom"), TextHoverEvent.showText("Click here to use randomly generated name."))
                                        .both("➤ Reuse '" + data + "'\n\n", TextClickEvent.runCommand("/nick " + data + " " + nickRank.get(player.getUniqueId()) + " true"), TextHoverEvent.showText("Click here to reuse '" + data + "'"))
                                        .str("To go back to being\nyour usual self, type:\n§l/unnick")
                                        .toBungee())
                                .build();
                    } else {
                        book = new BookBuilder("name", "system")
                                .addPage(new ComponentText().str("Alright, now you'll need\nto choose the §lNAME to use!§r§0\n\n")
                                        .both("➤ Use a random name\n", TextClickEvent.runCommand("/nick nicknamerandom"), TextHoverEvent.showText("Click here to use randomly generated name."))
                                        .both("➤ Reuse '" + data + "'\n\n", TextClickEvent.runCommand("/nick " + data + " " + nickRank.get(player.getUniqueId()) + " true"), TextHoverEvent.showText("Click here to reuse '" + data + "'"))
                                        .str("To go back to being\nyour usual self, type:\n§l/unnick")
                                        .toBungee())
                                .build();
                    }
                    BookBuilder.openBook(book, player);
                }

                break;
            case NAMECUSTOM:
                String[] lines = {"", "Enter a name"};
                SignBuilder.openSign(player, -61, 1, 41, lines);
                break;
            case NAMERANDOM:
                player.sendMessage("§eGenerating a unique random name. Please wait...");
                speedcubingServer.scheduledPool.execute(() -> {
                    String name = generateRandomString();
                    nickName.put(player.getUniqueId(), name);
                    if (User.getUser(player).hasPermission("perm.nick.customname")) {
                        ItemStack b = new BookBuilder("random", "system")
                                .addPage(new ComponentText().str("We've generated a\nrandom username for\nyou:\n§l" + name + "\n\n")
                                        .both("   §a§nUSE NAME§r\n", TextClickEvent.runCommand("/nick " + name + " " + nickRank.get(player.getUniqueId()) + " true"),
                                                TextHoverEvent.showText("Click here to use this name."))
                                        .both("   §c§nTRY AGAIN§r\n", TextClickEvent.runCommand("/nick nicknamerandom"), TextHoverEvent.showText("Click here to generate another name."))
                                        .both("\n§0§nOr click here to use custom name", TextClickEvent.runCommand("/nick nicknamecustom"), TextHoverEvent.showText("Click here to use custom name."))
                                        .toBungee())
                                .build();
                        BookBuilder.openBook(b, player);
                    } else {
                        ItemStack b = new BookBuilder("random", "system")
                                .addPage(new ComponentText().str("We've generated a\nrandom username for\nyou:\n§l" + name + "\n\n")
                                        .both("   §a§nUSE NAME§r\n", TextClickEvent.runCommand("/nick " + name + " " + nickRank.get(player.getUniqueId()) + " true"),
                                                TextHoverEvent.showText("Click here to use this name."))
                                        .both("   §c§nTRY AGAIN§r\n", TextClickEvent.runCommand("/nick nicknamerandom"), TextHoverEvent.showText("Click here to generate another name."))
                                        .toBungee())
                                .build();
                        BookBuilder.openBook(b, player);
                    }
                });
                break;
            case RULE:
                book = new BookBuilder("rule", "system")
                        .addPage(new ComponentText().str("You have finished\nsetting up your\nnickname!\n\nYour nickname is:\n" + Rank.rankByName.get(nickRank.get(player.getUniqueId())).getFormat().getPrefix() + nickName.get(player.getUniqueId()) + "§0." +
                                        "\n\n§0To go back to being\nyour usual self, type:\n§l/unnick")
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