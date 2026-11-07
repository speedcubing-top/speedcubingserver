package top.speedcubing.server.configuration;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import top.speedcubing.common.configuration.ServerConfig;
import top.speedcubing.common.events.ConfigReloadEvent;
import top.speedcubing.lib.eventbus.CubingEventHandler;
import top.speedcubing.server.speedcubingServer;

public class Configuration {
    public static int LeftCpsLimit = Integer.MAX_VALUE;
    public static int RightCpsLimit = Integer.MAX_VALUE;
    public static Set<String> onlineCrash = new HashSet<>();
    public static Set<Pattern> blockedLog = new HashSet<>();
    public static Set<Pattern> blockedMod = new HashSet<>();
    public static Set<Pattern> blacklistedMod = new HashSet<>();
    public static boolean removeLogs;

    public static void reload() {
        speedcubingServer.getInstance().getLogger().info("loading config");
        Configuration.LeftCpsLimit = ServerConfig.getConfig().get("leftcpslimit").getAsInt();
        Configuration.RightCpsLimit = ServerConfig.getConfig().get("rightcpslimit").getAsInt();
        Configuration.removeLogs = ServerConfig.getConfig().get("removeLogs").getAsBoolean();
        Configuration.blockedLog.clear();
        ServerConfig.getConfig().get("spigotblockedlog").getAsJsonArray().forEach(a -> Configuration.blockedLog.add(Pattern.compile(a.getAsString())));
        Configuration.blockedMod.clear();
        ServerConfig.getConfig().get("blockedmod").getAsJsonArray().forEach(a -> Configuration.blockedMod.add(Pattern.compile(a.getAsString())));
        Configuration.onlineCrash.clear();
        ServerConfig.getConfig().get("onlinecrash").getAsJsonArray().forEach(a -> Configuration.onlineCrash.add(a.getAsString()));
        Configuration.blacklistedMod.clear();
        ServerConfig.getConfig().get("blacklistedmod").getAsJsonArray().forEach(a -> Configuration.blacklistedMod.add(Pattern.compile(a.getAsString())));
    }

    @CubingEventHandler(priority = 20)
    public void reload(ConfigReloadEvent e) {
        reload();
    }
}
