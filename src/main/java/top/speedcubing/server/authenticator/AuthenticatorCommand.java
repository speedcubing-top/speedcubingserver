package top.speedcubing.server.authenticator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.speedcubing.common.database.Database;
import top.speedcubing.common.player.PlayerCache;
import top.speedcubing.lib.utils.SQL.SQLConnection;
import top.speedcubing.lib.utils.SQL.SQLRow;
import top.speedcubing.lib.utils.StringUtils;
import top.speedcubing.server.mulitproxy.BungeeProxy;
import top.speedcubing.server.player.User;
import top.speedcubing.server.utils.Prefix;
import top.speedcubing.common.text.Msg;

public class AuthenticatorCommand implements CommandExecutor {
    private final Map<UUID, Integer> verifiedCount = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (Bukkit.getServerName().equalsIgnoreCase("limbo")) {
            User.getUser(sender).sendMessage("%lang_server_2fa_disabled_here%");
            Player player = (Player) sender;
            player.performCommand("l");
            return true;
        }
        if (args.length == 0) {
            usage(sender);
            return true;
        } else if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Prefix.TWO_FA + Msg.PLAYERS_ONLY);
                return true;
            }

            Player player = (Player) sender;

            AuthData auth = AuthData.map.get(User.getUser(player));
            if (auth == null) {
                // no AuthData: 2FA bypassed or not enabled for this account
                User.getUser(player).sendMessage("%lang_server_2fa_disabled%");
                return true;
            }

            String code = args[0];
            if (StringUtils.isInt(code)) {
                if (code.length() == 6) {
                    if (auth.hasSessions()) {
                            User.getUser(player).sendMessage("%lang_server_2fa_authenticated%");
                        return true;
                    }
                    if (auth.hasKey()) {
                        String key = auth.getKey();
                        if (AuthUtils.authorize(key, Integer.parseInt(code))) {
                            auth.setSession(true);
                            User.getUser(player).sendMessage("%lang_server_2fa_authenticated%");
                        } else {
                            User.getUser(player).sendMessage("%lang_server_2fa_wrong_code%");
                            if (verifiedCount.containsKey(player.getUniqueId())) {
                                Integer score = verifiedCount.get(player.getUniqueId());
                                if (score != 10) {
                                    verifiedCount.put(player.getUniqueId(), score + 1);
                                } else {
                                    verifiedCount.remove(player.getUniqueId());
                                    BungeeProxy.proxyCommand("ban " + player.getName() + " 0 Suspicious activities detected on your account , contact support for assistance. -hideid");
                                }
                            } else {
                                verifiedCount.put(player.getUniqueId(), 1);
                            }
                        }
                        } else {
                        User.getUser(player).sendMessage("%lang_server_2fa_no_key%");
                    }
                } else {
                    User.getUser(player).sendMessage("%lang_server_2fa_invalid_code%");
                }
            } else if (args[0].equals("reset")) {
                if (auth.hasSessions()) {
                    auth.setSession(false);
                    User.getUser(player).sendMessage("%lang_server_2fa_sessions_reset%");
                } else {
                    User.getUser(player).sendMessage("%lang_server_2fa_no_session%");
                }
            }
        } else if (args.length == 2) {

            if (args[0].equalsIgnoreCase("setup")) {
                Player player = (Player) sender;
                AuthData auth = AuthData.map.get(User.getUser(player));
                if (auth == null) {
                    User.getUser(player).sendMessage("%lang_server_2fa_disabled%");
                    return true;
                }

                if (auth.isAuthEnable()) {
                    if (!auth.hasKey()) {
                        String code = args[1];
                        if (code.length() == 6 && StringUtils.isInt(code)) {
                            if (auth.noKey != null) {
                                String key = auth.noKey;
                                if (AuthUtils.authorize(key, Integer.parseInt(code))) {
                                    auth.setKey(key);
                                    User.getUser(player).sendMessage("%lang_server_2fa_setup_done%");
                                    removeMap(player);
                                    auth.noKey = null;
                                } else {
                                    User.getUser(player).sendMessage("%lang_server_2fa_invalid_code%");
                                }
                            } else {
                                User.getUser(player).sendMessage("%lang_server_2fa_error%");
                            }
                        } else {
                            User.getUser(player).sendMessage("%lang_server_2fa_invalid_code%");
                        }
                    } else {
                        User.getUser(player).sendMessage("%lang_server_2fa_already_setup%");
                    }
                } else {
                    User.getUser(player).sendMessage("%lang_server_2fa_disabled%");
                }
            } else if (args[0].equalsIgnoreCase("reset")) {
                if (sender instanceof Player) {
                    User.getUser(sender).sendMessage("%lang_server_2fa_console_only%");
                    return true;
                }

                String targetName = args[1];
                try (SQLConnection connection = Database.getCubing()) {
                    String realTargetName = connection.select("name").from("playersdata").where("name='" + targetName + "'").executeResult().getString();
                    if (realTargetName == null) {
                        sender.sendMessage(Prefix.TWO_FA + Msg.PLAYER_NOT_FOUND);
                        return true;
                    }
                    SQLRow target = connection.select("id,uuid").from("playersdata").where("name='" + targetName + "'").executeResult().get(0);
                    User targetUser = User.getUser(target.getInt(0));
                    AuthData targetAuth = targetUser == null ? null : AuthData.map.get(targetUser);
                    if (targetAuth != null) {
                        targetAuth.setSession(false);
                    } else {
                        // offline, on another server or without 2FA data here: clear the stored sessions directly
                        connection.update("playersdata", "auth_sessions=0", "id=" + target.getInt(0));
                        PlayerCache.invalidateForUpdate(UUID.fromString(target.getString(1)), "auth_sessions=0");
                    }
                    sender.sendMessage(Prefix.TWO_FA + Msg.SUCCESS + "Reset trusted sessions of " + Msg.VALUE + realTargetName + Msg.SUCCESS + ".");
                }
            }
        }
        return true;
    }

    private void removeMap(Player player) {
        player.getInventory().remove(Material.MAP);
    }

    private void usage(CommandSender sender) {
        if (sender instanceof Player)
            User.getUser(sender).sendMessage("%lang_server_2fa_usage%");
        else
            sender.sendMessage(Prefix.TWO_FA + Msg.usage("/2fa reset <player>"));
    }
}
