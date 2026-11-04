package top.speedcubing.server.authenticator;

import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;
import top.speedcubing.server.player.User;
import top.speedcubing.server.speedcubingServer;
import top.speedcubing.server.utils.Prefix;
import top.speedcubing.server.utils.CommandParser;
import top.speedcubing.common.text.Msg;

public class AuthEventHandlers implements Listener {
    private static final String qrCodeURL = "https://quickchart.io/chart?chs=128x128&cht=qr&chl=otpauth://totp/%label?secret=%key";

    public static void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        User user = User.getUser(p);

        AuthData auth = new AuthData(user);

        if (auth.isAuthBypass())
            return;

        if (!auth.isAuthEnable()) {
            if (user.isStaff) {
                auth.setAuthEnable();
                p.kickPlayer(Prefix.TWO_FA + Msg.SUCCESS + "2FA has been enabled for your staff account.\n" + Msg.INFO + "Please rejoin the server!");
            }
            return;
        }

        if (Bukkit.getServerName().equalsIgnoreCase("limbo"))
            return;

        AuthData.map.put(user, auth);

        if (!auth.hasKey()) {
            if (auth.noKey == null) {
                auth.noKey = AuthUtils.generateSecret();
                AuthMessenger.sendSetKeyMessage(p);
            }
            String url = qrCodeURL.replace("%label", "speedcubing.top:%20" + user.bGetName())
                    .replace("%key", auth.noKey);
            new BukkitRunnable() {
                @Override
                public void run() {
                    // only the QR download runs async; the map and inventory are Bukkit API and go back to the main thread
                    ImageRenderer renderer;
                    try {
                        renderer = new ImageRenderer(url);
                    } catch (IOException ee) {
                        ee.printStackTrace();
                        Bukkit.getScheduler().runTask(speedcubingServer.instance, () -> {
                            if (p.isOnline())
                                user.sendMessage("%lang_server_2fa_qr_error%");
                        });
                        return;
                    }
                    Bukkit.getScheduler().runTask(speedcubingServer.instance, () -> {
                        if (!p.isOnline())
                            return;
                        MapView view = Bukkit.createMap(p.getWorld());
                        view.getRenderers().forEach(view::removeRenderer);
                        view.addRenderer(renderer);
                        ItemStack mapItem = new ItemStack(Material.MAP, 1, view.getId());
                        ItemMeta mapMeta = mapItem.getItemMeta();
                        mapMeta.setDisplayName(ChatColor.GOLD + "QR Code");
                        mapItem.setItemMeta(mapMeta);
                        p.getInventory().addItem(mapItem);
                        user.sendMessage("%lang_server_2fa_qr_url%", url);
                    });
                }
            }.runTaskAsynchronously(speedcubingServer.instance);
        }
        String oldIp = auth.getIp();

        String ip = p.getAddress().getAddress().getHostAddress();
        if (oldIp != null && !oldIp.isEmpty()) {
            if (!ip.equals(oldIp)) {
                auth.setSession(false);
                auth.setIp(ip);
            }
        } else {
            auth.setIp(ip);
        }
    }

    public static void onInteraction(PlayerInteractEvent e) {
        User user = User.getUser(e.getPlayer());
        AuthData auth = AuthData.map.get(user);

        if (auth == null)
            return;

        if (auth.allowAction())
            return;

        e.setCancelled(true);

        if (auth.hasKey()) {
            AuthMessenger.sendEnterCodeMessage(e.getPlayer());
        } else {
            AuthMessenger.sendSetKeyMessage(e.getPlayer());
        }
    }

    //this event is blocked paper command, proxy command blocker in speedcubingProxy

    public static void onCmdExecute(PlayerCommandPreprocessEvent e) {
        AuthData auth = AuthData.map.get(User.getUser(e.getPlayer()));

        if (auth == null)
            return;

        if (auth.allowAction())
            return;

        // only /2fa itself (also as plugin:2fa); contains("2fa") let "/op Me 2fa" through
        String command = CommandParser.parse(e.getMessage()).command;
        if (!command.substring(command.indexOf(':') + 1).equalsIgnoreCase("2fa")) {
            e.setCancelled(true);
            if (auth.hasKey()) {
                AuthMessenger.sendEnterCodeMessage(e.getPlayer());
            } else {
                AuthMessenger.sendSetKeyMessage(e.getPlayer());
            }
        }
    }


    public static void onInventoryOpen(InventoryOpenEvent e) {
        AuthData auth = AuthData.map.get(User.getUser(e.getPlayer()));

        if (auth == null)
            return;

        if (auth.allowAction())
            return;

        InventoryType inventoryType = e.getInventory().getType();
        if (inventoryType == InventoryType.CHEST) {
            e.setCancelled(true);
            if (auth.hasKey()) {
                AuthMessenger.sendEnterCodeMessage((Player) e.getPlayer());
            } else {
                AuthMessenger.sendSetKeyMessage((Player) e.getPlayer());
            }
        }
    }

    public static void onPlayerMove(PlayerMoveEvent e) {
        AuthData auth = AuthData.map.get(User.getUser(e.getPlayer()));

        if (auth == null)
            return;

        if (auth.allowAction()) {
            return;
        }

        Location oldLoc = e.getFrom();
        Location newLoc = e.getTo();
        if (newLoc.getX() != oldLoc.getX() || newLoc.getZ() != oldLoc.getZ()) {
            e.getPlayer().teleport(oldLoc);

            if (auth.hasKey()) {
                AuthMessenger.sendEnterCodeMessage(e.getPlayer());
            } else {
                AuthMessenger.sendSetKeyMessage(e.getPlayer());
            }
        }
    }
}
