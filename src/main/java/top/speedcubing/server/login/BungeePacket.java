package top.speedcubing.server.login;

import top.speedcubing.common.server.MinecraftProxy;

public class BungeePacket {
    public final MinecraftProxy proxy;
    public final String hor;
    public final String ver;
    public boolean cps;
    public final boolean vanished;

    public BungeePacket(String proxyName, String hor, String ver, boolean vanished) {
        this.proxy = MinecraftProxy.getOrCreateRedisProxy(proxyName);
        this.hor = hor;
        this.ver = ver;
        this.vanished = vanished;
    }
}
