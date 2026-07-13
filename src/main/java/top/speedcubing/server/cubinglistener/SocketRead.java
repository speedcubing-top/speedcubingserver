package top.speedcubing.server.cubinglistener;

import top.speedcubing.common.events.ConfigReloadEvent;
import top.speedcubing.common.events.SocketReadEvent;
import top.speedcubing.lib.eventbus.CubingEventHandler;

import java.io.DataInputStream;
import java.io.IOException;

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
