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
import top.speedcubing.lib.utils.SQL.SQLConnection;
import top.speedcubing.lib.utils.StringUtils;
import top.speedcubing.server.mulitproxy.BungeeProxy;
import top.speedcubing.server.player.User;

public class AuthenticatorCommand implements CommandExecutor {
    private final Map<UUID, Integer> verifedCount = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (Bukkit.getServerName().equalsIgnoreCase("limbo")) {
            sender.sendMessage("§c2FA is disabled here.");
            Player player = (Player) sender;
            player.performCommand("l");
            return true;
        }
        if (args.length == 0) {
            usage(sender);
            return true;
        } else if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cThis command can only be used by players.");
                return true;
            }

            Player player = (Player) sender;

            AuthData auth = AuthData.map.get(User.getUser(player));

            String code = args[0];
            if (StringUtils.isInt(code)) {
                if (code.length() == 6) {
                    if (auth.hasSessions()) {
                            player.sendMessage("§aYou have successfully authenticated");
                        return true;
                    }
                    if (auth.hasKey()) {
                        String key = auth.getKey();
                        if (AuthUtils.authorize(key, Integer.parseInt(code))) {
                            auth.setSession(true);
                            player.sendMessage("§aYou have successfully authenticated");
                        } else {
                            player.sendMessage("§cThe key you entered is not valid. Please try again.");
                            if (verifedCount.containsKey(player.getUniqueId())) {
                                Integer score = verifedCount.get(player.getUniqueId());
                                if (score != 10) {
                                    verifedCount.put(player.getUniqueId(), score + 1);
                                } else {
                                    verifedCount.remove(player.getUniqueId());
                                    BungeeProxy.proxyCommand("ban " + player.getName() + " 0 Suspicious activities detected on your account , contact support for assistance. -hideid");
                                }
                            } else {
                                verifedCount.put(player.getUniqueId(), 1);
                            }
                        }
                        } else {
                        player.sendMessage("§cYou don't have a key. Please use /2fa setup <code>.");
                    }
                } else {
                    player.sendMessage("§cInvalid key entered");
                }
            } else if (args[0].equals("reset")) {
                if (auth.hasSessions()) {
                    auth.setSession(false);
                    player.sendMessage("§aSuccessfully reset your trusted sessions");
                } else {
                    player.sendMessage("§cYou don't have a trusted session");
                }
            }
        } else if (args.length == 2) {

            if (args[0].equalsIgnoreCase("setup")) {
                Player player = (Player) sender;
                AuthData auth = AuthData.map.get(User.getUser(player));

                if (auth.isAuthEnable()) {
                    if (!auth.hasKey()) {
                        String code = args[1];
                        if (code.length() == 6 && StringUtils.isInt(code)) {
                            if (auth.noKey != null) {
                                String key = auth.noKey;
                                if (AuthUtils.authorize(key, Integer.parseInt(code))) {
                                    auth.setKey(key);
                                    player.sendMessage("§a2FA successfully set up.");
                                    removeMap(player);
                                    auth.noKey = null;
                                } else {
                                    player.sendMessage("§cInvalid key entered.");
                                }
                            } else {
                                player.sendMessage("§cAn error occurred. Please contact staff.");
                            }
                        } else {
                            player.sendMessage("§cInvalid key entered");
                        }
                    } else {
                        player.sendMessage("§c2FA is already set up. Use `/2fa <code>` to authenticate yourself.");
                    }
                } else {
                    player.sendMessage("§c2FA is disabled.");
                }
            } else if (args[0].equalsIgnoreCase("reset")) {
                if (sender instanceof Player) {
                    sender.sendMessage("§cThis command can only be used from the console.");
                    return true;
                }

                String targetName = args[1];
                try (SQLConnection connection = Database.getCubing()) {
                    String realTargetName = connection.select("name").from("playersdata").where("name='" + targetName + "'").executeResult().getString();
                    if (realTargetName == null) {
                        sender.sendMessage("§cThe player you entered does not exist.");
                        return true;
                    }
                    int id = connection.select("id").from("playersdata").where("name='" + targetName + "'").executeResult().getInt();
                    AuthData.map.get(User.getUser(id)).setSession(false);
                    sender.sendMessage("§aSuccessfully reset " + realTargetName + " trusted sessions");
                }
            }
        }
        return true;
    }

    private void removeMap(Player player) {
        player.getInventory().remove(Material.MAP);
    }

    private void usage(CommandSender sender) {
        sender.sendMessage("§6[2FA Commands]\n" +
                "/2fa <code> - login\n" +
                "/2fa setup <code> - setup your 2FA\n" +
                "/2fa reset - Reset your trusted sessions");
    }
}
