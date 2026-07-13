package top.speedcubing.server.cubinglistener;

import java.io.DataInputStream;
import java.io.IOException;
import net.minecraft.server.v1_8_R3.PacketPlayOutGameStateChange;
import org.bukkit.Bukkit;
import top.speedcubing.common.events.ConfigReloadEvent;
import top.speedcubing.common.events.SocketReadEvent;
import top.speedcubing.lib.bukkit.PlayerUtils;
import top.speedcubing.lib.eventbus.CubingEventHandler;
import top.speedcubing.server.player.User;

public class SocketRead {
    @CubingEventHandler
    public void socketReadEvent(SocketReadEvent e) throws IOException {
        String packetID = e.getPacketID();
        DataInputStream data = e.getData();
        switch (packetID) {
            case "cfg" -> {
                new ConfigReloadEvent().call();
            }
        }
    }
}
