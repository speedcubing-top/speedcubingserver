package top.speedcubing.server.cubinglistener;

import java.io.DataInputStream;
import java.io.IOException;
import net.minecraft.server.v1_8_R3.PacketPlayOutGameStateChange;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spigotmc.RestartCommand;
import top.speedcubing.common.events.ConfigReloadEvent;
import top.speedcubing.common.events.SocketReadEvent;
import top.speedcubing.lib.bukkit.PlayerUtils;
import top.speedcubing.lib.eventbus.CubingEventHandler;
import top.speedcubing.server.login.BungeePacket;
import top.speedcubing.server.player.User;
import top.speedcubing.server.speedcubingServer;

public class SocketRead {
    @CubingEventHandler
    public void socketReadEvent(SocketReadEvent e) throws IOException {
        String packetID = e.getPacketID();
        DataInputStream data = e.getData();
        switch (packetID) {
            case "cpsrequest" -> {
                int id = data.readInt();
                User user = User.getUser(id);
                if (user != null)
                    user.listened = data.readBoolean();
                else speedcubingServer.bungeePacketStorage.get(id).cps = true;
            }
            case "cfg" -> {
                new ConfigReloadEvent().call();
            }
            case "bungee" -> {
                int i = e.getData().readInt();
                speedcubingServer.bungeePacketStorage.put(i, new BungeePacket(e.getData().readUTF(), e.getData().readBoolean()));
            }
            case "demo" -> {
                PacketPlayOutGameStateChange packet = new PacketPlayOutGameStateChange(5, 0);
                int id = data.readInt();
                if (id == 0)
                    User.getUsers().forEach(a -> a.sendPacket(packet));
                else
                    User.getUser(id).sendPacket(packet);
            }
            case "crash" -> {
                int id = data.readInt();
                if (id == 0) {
                    Bukkit.getOnlinePlayers().forEach(PlayerUtils::crashAll);
                    User.getUsers().forEach(a -> {
                        a.isCrashed = true;
                    });
                } else {
                    PlayerUtils.crashAll(User.getUser(id).player);
                    User.getUser(id).isCrashed = true;
                }
            }
            case "vanish" -> {
                User user = User.getUser(data.readInt());
                boolean vanish = data.readBoolean();
                user.vanished = vanish;
                if (vanish)
                    Bukkit.getScheduler().runTask(speedcubingServer.getInstance(), () -> {
                        for (Player p : Bukkit.getOnlinePlayers())
                            if (!User.getUser(p).isStaff)
                                p.hidePlayer(user.player);
                    });
                else
                    Bukkit.getScheduler().runTask(speedcubingServer.getInstance(), () -> {
                        for (Player p : Bukkit.getOnlinePlayers())
                            p.showPlayer(user.player);
                    });
            }
        }
    }
}
