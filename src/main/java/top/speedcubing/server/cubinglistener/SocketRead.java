package top.speedcubing.server.cubinglistener;

import top.speedcubing.common.events.ConfigReloadEvent;
import top.speedcubing.common.events.SocketReadEvent;
import org.bukkit.Bukkit;
import top.speedcubing.lib.eventbus.CubingEventHandler;
import top.speedcubing.server.lang.LanguageSystem;
import top.speedcubing.server.player.User;
import top.speedcubing.server.speedcubingServer;

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
            case "lang" -> {
                // sent by the proxy's /language after it saved the new language
                int id = data.readInt();
                int lang = data.readInt();
                Bukkit.getScheduler().runTask(speedcubingServer.getInstance(), () -> {
                    User user = User.getUser(id);
                    if (user != null && lang >= 0 && lang < LanguageSystem.langCount)
                        user.applyLanguage(lang);
                });
            }
        }
    }
}
