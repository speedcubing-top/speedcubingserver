package top.speedcubing.server.bukkitlistener;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutBed;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import top.speedcubing.common.database.Database;
import top.speedcubing.common.io.RedisManager;
import top.speedcubing.common.rank.Rank;
import top.speedcubing.lib.bukkit.PlayerUtils;
import top.speedcubing.lib.bukkit.packetwrapper.OutScoreboardTeam;
import top.speedcubing.lib.utils.ReflectionUtils;
import top.speedcubing.lib.utils.SQL.SQLConnection;
import top.speedcubing.lib.utils.SQL.SQLRow;
import top.speedcubing.server.authenticator.AuthEventHandlers;
import top.speedcubing.server.bukkitcmd.staff.cpsdisplay;
import top.speedcubing.server.login.BungeePacket;
import top.speedcubing.server.login.LoginContext;
import top.speedcubing.server.player.User;
import top.speedcubing.server.permissions.LuckPermsHook;
import top.speedcubing.server.speedcubingServer;
import top.speedcubing.server.utils.CommandParser;
import top.speedcubing.server.configuration.Configuration;
import top.speedcubing.server.utils.RankSystem;

public class PreListen implements Listener {
    private static final long BUNGEE_PACKET_WAIT_TIMEOUT_MS = 2000L;
    private static final long BUNGEE_PACKET_WAIT_INTERVAL_MS = 25L;

    @EventHandler(priority = EventPriority.LOW)
    public void InventoryClickEvent(InventoryClickEvent e) {
        User user = User.getUser(e.getWhoClicked());
        long l = System.currentTimeMillis();
        if (l - user.lastInvClick < 100)
            e.setCancelled(true);
        else user.lastInvClick = l;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void InventoryOpenEvent(InventoryOpenEvent e) {
        InventoryType type = e.getInventory().getType();
        if (type == InventoryType.BEACON || type == InventoryType.HOPPER || type == InventoryType.ANVIL)
            e.setCancelled(true);

        //auth
        AuthEventHandlers.onInventoryOpen(e);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void PlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        CommandParser command = CommandParser.parse(e.getMessage());
        String commandName = command.command.toLowerCase();

        if (commandName.equals("terms") || commandName.equals("login") || commandName.equals("register")) {
            AuthEventHandlers.onCmdExecute(e);
            return;
        }

        //auth
        AuthEventHandlers.onCmdExecute(e);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void PlayerInteractEvent(PlayerInteractEvent e) {
        switch (e.getAction()) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                User.getUser(e.getPlayer()).leftClickTick += 1;
                break;
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                User.getUser(e.getPlayer()).rightClickTick += 1;
                break;
        }

        //auth
        AuthEventHandlers.onInteraction(e);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void PlayerChangedWorldEvent(PlayerChangedWorldEvent e) {
        User user = User.getUser(e.getPlayer());
        if (user.cpsHologram != null) {
            user.cpsHologram.changeWorld(e.getPlayer().getWorld().getName());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void PlayerLoginEvent(PlayerLoginEvent e) {
        Player player = e.getPlayer();

        try (SQLConnection connection = Database.getCubing()) {
            SQLRow row = connection.prepare("SELECT * FROM playersdata WHERE uuid=?")
                    .setString(1, player.getUniqueId().toString())
                    .executeResult().get(0);

            int id = row.getInt("id");
            String priority = row.getString("priority");
            String lpGroup = LuckPermsHook.getPrimaryGroup(player.getUniqueId());
            if (lpGroup != null && !lpGroup.isBlank() && !lpGroup.equalsIgnoreCase(priority)) {
                priority = lpGroup.toLowerCase();
                connection.update("playersdata", "priority='" + priority + "'", "id=" + id);
            }
            String realRank = Rank.getRank(priority, id);

            // maintenance
            if (!Rank.isStaff(realRank) && Bukkit.hasWhitelist() && (row.getBoolean("serverwhitelist"))) {
                e.setKickMessage("§cThis server is currently under maintenance.");
                e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                speedcubingServer.bungeePacketStorage.remove(id);
                return;
            }

            // bungee-data-not-found
            BungeePacket bungePacket = waitForBungeePacket(id);
            if (bungePacket == null) {
                speedcubingServer.getInstance().getLogger().warning(
                        "Missing bungee login packet for " + player.getName() + " (id=" + id
                                + ", uuid=" + player.getUniqueId() + ", server=" + Bukkit.getServerName() + ")");
                e.setKickMessage("§cError occurred.");
                e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                return;
            }

            speedcubingServer.bungeePacketStorage.remove(id);
            ctxMap.put(player.getUniqueId(), new LoginContext(row, realRank, bungePacket));
        }
    }

    Map<UUID, LoginContext> ctxMap = new ConcurrentHashMap<>();

    private BungeePacket waitForBungeePacket(int id) {
        long deadline = System.currentTimeMillis() + BUNGEE_PACKET_WAIT_TIMEOUT_MS;
        BungeePacket packet;
        while (System.currentTimeMillis() < deadline) {
            packet = speedcubingServer.bungeePacketStorage.get(id);
            if (packet != null) {
                RedisManager.getdel(loginHandoffKey(id));
                return packet;
            }

            packet = consumeRedisLoginHandoff(id);
            if (packet != null) {
                speedcubingServer.bungeePacketStorage.put(id, packet);
                return packet;
            }

            try {
                Thread.sleep(BUNGEE_PACKET_WAIT_INTERVAL_MS);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return speedcubingServer.bungeePacketStorage.get(id);
    }

    private BungeePacket consumeRedisLoginHandoff(int id) {
        String key = loginHandoffKey(id);
        String encoded = RedisManager.getdel(key);
        if (encoded == null || encoded.isBlank()) {
            return null;
        }

        try (DataInputStream data = new DataInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(encoded)))) {
            String packetId = data.readUTF();
            int packetPlayerId = data.readInt();
            if (!"bungee".equals(packetId) || packetPlayerId != id) {
                speedcubingServer.getInstance().getLogger().warning(
                        "Ignoring invalid Redis login handoff from " + key + " (packet=" + packetId
                                + ", id=" + packetPlayerId + ", expected=" + id + ")");
                return null;
            }
            return new BungeePacket(data.readUTF(), data.readUTF(), data.readUTF(), data.readBoolean());
        } catch (IOException | IllegalArgumentException ex) {
            speedcubingServer.getInstance().getLogger().warning(
                    "Failed to parse Redis login handoff from " + key + ": " + ex.getMessage());
            return null;
        }
    }

    private String loginHandoffKey(int id) {
        return "sc:login:" + Bukkit.getServerName() + ":" + id;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void PlayerJoinEvent(PlayerJoinEvent e) {
        e.setJoinMessage("");
        Player player = e.getPlayer();

        LoginContext ctx = ctxMap.remove(player.getUniqueId());
        if (ctx == null) {
            speedcubingServer.getInstance().getLogger().warning(
                    "Missing login context at join for " + player.getName() + " (uuid=" + player.getUniqueId()
                            + ", server=" + Bukkit.getServerName() + ")");
            player.kickPlayer("error (4)");
            return;
        }

        String displayName = player.getName();
        String displayRank = ctx.getRealRank();

        String skinValue = "";
        String skinSignature = "";

        boolean nickState = ctx.getRow().getBoolean("nicked");

        if (nickState) {
            String lpDisplayRank = LuckPermsHook.getMeta(player.getUniqueId(), "displayrank");
            String lpDisplayName = LuckPermsHook.getMeta(player.getUniqueId(), "displayname");
            displayRank = lpDisplayRank == null || lpDisplayRank.isBlank() ? ctx.getRow().getString("nickpriority") : lpDisplayRank;
            displayName = lpDisplayName == null || lpDisplayName.isBlank() ? ctx.getRow().getString("nickname") : lpDisplayName;
        } else {
            displayRank = ctx.getRealRank();
            displayName = ctx.getRow().getString("name");
        }

        if (!Bukkit.getServerName().equalsIgnoreCase("lobby")) {
            String dbSkinValue = ctx.getRow().getString("skinvalue");
            String dbSkinSignature = ctx.getRow().getString("skinsignature");
            skinValue = dbSkinValue == null ? "" : dbSkinValue;
            skinSignature = dbSkinSignature == null ? "" : dbSkinSignature;
        }

        //User
        User user = new User(player, displayRank, displayName, ctx);
        user.refreshRankFromLuckPerms();

        //modify things
        GameProfile profile = ((CraftPlayer) player).getProfile();
        ReflectionUtils.setField(profile, "name", displayName);

        if (!skinValue.isEmpty()) {
            profile.getProperties().removeAll("textures");
            profile.getProperties().put("textures", new Property("textures", skinValue, skinSignature));
        }

        //OP
        player.setOp(user.hasPermission("perm.op"));

        //send packets
        user.createTeamPacket();
        for (User u : User.getUsers()) {
            user.sendPacket(u.leavePacket, u.joinPacket);
            if (u != user) {
                u.sendPacket(user.leavePacket, user.joinPacket);
            }
        }

        if (user.status != null && user.status.equalsIgnoreCase("cps")) {
            cpsdisplay.update(player);
        }

        //nick
        if (user.nicked())
            user.sendPacket(new OutScoreboardTeam().a(Rank.getCode(user.realRank) + RankSystem.playerNameEncode(user.realName)).c(user.getPrefix(true)).d(user.getGuildTag(true)).g(Collections.singletonList(user.realName)).h(0).packet);

        //vanish
        for (User u : User.getUsers()) {
            if (u.vanished) {
                player.hidePlayer(u.player);
            }
            if (user.vanished) {
                u.bHidePlayer(player);
            }
        }

        //crash
        if (Configuration.onlineCrash.contains(player.getUniqueId().toString()) || Configuration.onlineCrash.contains(player.getAddress().getAddress().getHostAddress())) {
            speedcubingServer.scheduledPool.schedule(() -> PlayerUtils.crashAll(player), 50, TimeUnit.MILLISECONDS);
        }
        //auth
        AuthEventHandlers.onPlayerJoin(e);

        //腦癱時間
        if (Bukkit.getServerName().equalsIgnoreCase("lobby")) {
            LocalTime start = LocalTime.of(0, 0);
            LocalTime end = LocalTime.of(6, 0);
            LocalTime now = LocalTime.parse(user.getCurrentTime());
            if (now.equals(start) || now.isAfter(start) && now.isBefore(end)) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String s = DateTimeFormatter.ofPattern("HH:mm").format(now);
                        player.sendMessage("§cCurrent time is " + s + ". Please take a break.");
                        for (User u : User.getUsers()) {
                            howToWin(u.player);
                        }
                        timer.cancel();
                    }
                }, 1000);
            }
        }
    }

    private void howToWin(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        PacketPlayOutBed packetPlayOutBed = new PacketPlayOutBed(craftPlayer.getHandle(), new BlockPosition(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()));
        craftPlayer.getHandle().u().getTracker().a(craftPlayer.getHandle(), packetPlayOutBed);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void PlayerVelocityEvent(PlayerVelocityEvent e) {
        Player player = e.getPlayer();
        player.setVelocity(User.getUser(player).applyKnockback(player.getVelocity()));
    }
}
