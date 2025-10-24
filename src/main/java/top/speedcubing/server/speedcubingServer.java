package top.speedcubing.server;

import java.io.DataInputStream;
import java.io.File;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.RestartCommand;
import org.spigotmc.SpigotConfig;
import top.speedcubing.common.CommonLib;
import top.speedcubing.common.database.Database;
import top.speedcubing.common.io.SocketReader;
import top.speedcubing.common.server.MinecraftProxy;
import top.speedcubing.lib.api.mojang.ProfileSkin;
import top.speedcubing.lib.eventbus.CubingEventManager;
import top.speedcubing.lib.utils.SQL.SQLConnection;
import top.speedcubing.lib.utils.SQL.SQLRow;
import top.speedcubing.lib.utils.SystemUtils;
import top.speedcubing.lib.utils.internet.HostAndPort;
import top.speedcubing.server.authenticator.AuthenticatorCommand;
import top.speedcubing.server.bukkitcmd.discord;
import top.speedcubing.server.bukkitcmd.getitemtype;
import top.speedcubing.server.bukkitcmd.hub;
import top.speedcubing.server.bukkitcmd.image;
import top.speedcubing.server.bukkitcmd.nick.nick;
import top.speedcubing.server.bukkitcmd.nick.unnick;
import top.speedcubing.server.bukkitcmd.ranks;
import top.speedcubing.server.bukkitcmd.staff.cpsdisplay;
import top.speedcubing.server.bukkitcmd.staff.freeze;
import top.speedcubing.server.bukkitcmd.staff.history;
import top.speedcubing.server.cubingcmd.staff.nicklogs;
import top.speedcubing.server.cubingcmd.staff.testkb;
import top.speedcubing.server.bukkitcmd.staff.cps;
import top.speedcubing.server.bukkitcmd.status;
import top.speedcubing.server.bukkitcmd.trolls.bangift;
import top.speedcubing.server.bukkitcmd.trolls.deepfry;
import top.speedcubing.server.bukkitcmd.trolls.kaboom;
import top.speedcubing.server.bukkitcmd.trolls.music;
import top.speedcubing.server.bukkitcmd.trolls.sendpacket;
import top.speedcubing.server.bukkitlistener.PostListen;
import top.speedcubing.server.bukkitlistener.PreListen;
import top.speedcubing.server.bukkitlistener.SingleListen;
import top.speedcubing.server.bukkitlistener.pluginchannel.FMLHSListener;
import top.speedcubing.server.configuration.Configuration;
import top.speedcubing.server.cubinglistener.CubingTick;
import top.speedcubing.server.cubinglistener.PlayIn;
import top.speedcubing.server.cubinglistener.PlayOut;
import top.speedcubing.server.cubinglistener.SocketRead;
import top.speedcubing.server.lang.LanguageSystem;
import top.speedcubing.server.login.BungeePacket;
import top.speedcubing.server.player.User;
import top.speedcubing.server.system.command.CubingCommandLoader;
import top.speedcubing.server.utils.CPSMonitor;
import top.speedcubing.server.utils.LogListener;
import top.speedcubing.server.utils.WordDictionary;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class speedcubingServer extends JavaPlugin {
    public static final Pattern nameRegex = Pattern.compile("^\\w{3,16}$");
    public static final Pattern legacyNameRegex = Pattern.compile("^\\w{1,16}$");
    public static Map<Integer, BungeePacket> bungeePacketStorage = new HashMap<>();

    public static boolean canRestart = true; //can Timer/Quit restart server?
    public static boolean restartable = false; //is it time to restart ?
    public static speedcubingServer instance;

    public static ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(10);

    public static boolean loaded = false;

    private BukkitAudiences audiences; // <- added field

    // CPS monitoring moved to CPSMonitor helper class

    @Override
    public void onEnable() {
        instance = this;

        // initialize Adventure audiences
        audiences = BukkitAudiences.create(this);

        // start the CPSMonitor with the plugin and audiences
        CPSMonitor.start(this, audiences);

        if (!SpigotConfig.bungee) {
            speedcubingServer.getInstance().getLogger().warning("bungeecord shouldn't be false, shutting down server.");
            Bukkit.getServer().shutdown();
        }

        CubingEventManager.registerListeners(
                new CubingTick(),
                new PlayIn(),
                new PlayOut(),
                new SocketRead(),
                new Configuration());

        registerCommands();
        registerListeners(new PreListen(), new PostListen(), new SingleListen(), new history(), new sendpacket(), new music(), new nicklogs());

        CommonLib.init(getLogger());

        Configuration.reload();

        new SocketReader(new HostAndPort("0.0.0.0", Bukkit.getPort() + 1000));

        LanguageSystem.init();

        sendpacket.initFuckPeople();

        Bukkit.getMessenger().registerIncomingPluginChannel(this, "FML|HS", new FMLHSListener());

        CubingCommandLoader.loadCommands("top.speedcubing.server.cubingcmd", speedcubingServer.class);

        new LogListener().reloadFilter();

        try (SQLConnection connection = Database.getSystem()) {
            connection.update("servers",
                    "launchtime=" + SystemUtils.getCurrentSecond() +
                            ",ram_max=" + SystemUtils.getXmx() / 1048576,
                    "name='" + Bukkit.getServerName() + "'"
            );
        }

        if (!Configuration.removeLogs) {
            for (File f : new File("logs").listFiles()) {
                if (!f.getName().equals("latest.log"))
                    f.delete();
            }
            for (File f : new File("./").listFiles()) {
                if (f.getName().startsWith("hs_err_pid"))
                    f.delete();
            }
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (User u : User.getUsers()) {
                if (u.leftClickQueue.size() == 20) {
                    u.leftCPS -= u.leftClickQueue.poll();
                }
                u.leftCPS += u.leftClickTick;
                u.leftClickQueue.add(u.leftClickTick);
                u.leftClickTick = 0;

                if (u.rightClickQueue.size() == 20) {
                    u.rightCPS -= u.rightClickQueue.poll();
                }
                u.rightCPS += u.rightClickTick;
                u.rightClickQueue.add(u.rightClickTick);
                u.rightClickTick = 0;

                if (u.cpsHologram != null) {
                    int leftClick = u.leftCPS;
                    int rightClick = u.rightCPS;
                    String colorCodeLeft = leftClick <= 10 ? "§a" : leftClick <= 20 ? "§2" : leftClick <= 30 ? "§e" : leftClick <= 40 ? "§6" : leftClick <= 50 ? "§c" : "§4";
                    String colorCodeRight = rightClick <= 10 ? "§a" : rightClick <= 20 ? "§2" : rightClick <= 30 ? "§e" : rightClick <= 40 ? "§6" : rightClick <= 50 ? "§c" : "§4";
                    u.cpsHologram.setName("§8[CPS§r " + colorCodeLeft + leftClick + " §8|§r " + colorCodeRight + rightClick + "§8]");
                }
            }
        }, 0, 1);
        loaded = true;
    }

    @Override
    public void onDisable() {
        // stop CPS monitoring
        CPSMonitor.stop();

        // close audiences if created
        if (audiences != null) {
            audiences.close();
            audiences = null;
        }

        try (SQLConnection connection = Database.getSystem()) {
            connection.update(
                    "servers",
                    "onlinecount=-1,ram_max=-1,ram_heap=-1,ram_used=-1,tps1=-1,tps2=-1,tps3=-1",
                    "name='" + Bukkit.getServerName() + "'"
            );
        }

        CommonLib.shutdown();
        WordDictionary.dict.close();
    }

    public static Plugin getInstance() {
        return instance;
    }

    // Null-safe command registration helper to avoid NPE if plugin.yml lacks a command entry
    private void setExecutorSafe(String name, org.bukkit.command.CommandExecutor executor) {
        org.bukkit.command.PluginCommand cmd = getCommand(name);
        if (cmd == null) {
            getLogger().warning("Command '" + name + "' not defined in plugin.yml, skipping registration.");
            return;
        }
        cmd.setExecutor(executor);
    }

    private void registerCommands() {
        setExecutorSafe("nick", new nick());
        setExecutorSafe("unnick", new unnick());
        setExecutorSafe("discord", new discord());
        setExecutorSafe("hub", new hub());
        setExecutorSafe("kaboom", new kaboom());
        setExecutorSafe("deepfry", new deepfry());
        setExecutorSafe("freeze", new freeze());
        setExecutorSafe("music", new music());
        setExecutorSafe("2fa", new AuthenticatorCommand());
        setExecutorSafe("image", new image());
        setExecutorSafe("history", new history());
        setExecutorSafe("getitemtype", new getitemtype());
        setExecutorSafe("sendpacket", new sendpacket());
        setExecutorSafe("cpsdisplay", new cpsdisplay());
        setExecutorSafe("bangift", new bangift());
        setExecutorSafe("ranks", new ranks());
        setExecutorSafe("status", new status());
        setExecutorSafe("cps", new cps(audiences));
    }

    public static void registerListeners(Listener... listeners) {
        for (Listener l : listeners) {
            Bukkit.getPluginManager().registerEvents(l, instance);
        }
    }

    public static CompletableFuture<DataInputStream> writeToInternal(byte[] b) {
        return MinecraftProxy.getProxy("internal").write(b);
    }

    public static void restart() {
        if (canRestart)
            RestartCommand.restart();
    }

    public static ProfileSkin generateRandomSkinFromDB() {
        try (SQLConnection connection = Database.getCubing()) {
            int size = connection.select("COUNT(*)")
                    .from("playersdata")
                    .where("profile_textures_value != ''")
                    .executeResult().getInt();
            int index = new SecureRandom().nextInt(size);
            SQLRow r = connection.select("name,uuid,profile_textures_value,profile_textures_signature")
                    .from("playersdata")
                    .where("profile_textures_value != ''")
                    .limit(index, 1)
                    .executeResult().get(0);
            return new ProfileSkin(r.getString(0), r.getString(1), r.getString(2), r.getString(3));
        }
    }
}
