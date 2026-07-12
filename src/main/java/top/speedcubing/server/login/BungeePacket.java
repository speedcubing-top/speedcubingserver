package top.speedcubing.server.login;

import top.speedcubing.common.server.MinecraftProxy;

public class BungeePacket {
    public final MinecraftProxy proxy;
    public boolean cps;
    public final boolean vanished;

    public BungeePacket(String proxyName, boolean vanished) {
        this.proxy = MinecraftProxy.getProxy(proxyName);
        this.vanished = vanished;
    }
}
