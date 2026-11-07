package top.speedcubing.server.cubinglistener;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayInCustomPayload;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayInKeepAlive;
import net.minecraft.server.v1_8_R3.PacketPlayInTabComplete;
import net.minecraft.server.v1_8_R3.PacketPlayInUpdateSign;
import org.bukkit.Bukkit;
import top.speedcubing.lib.bukkit.events.packet.PlayInEvent;
import top.speedcubing.lib.eventbus.CubingEventHandler;
import top.speedcubing.server.bukkitcmd.nick.nick;
import top.speedcubing.server.events.player.PlayerClientTickEvent;
import top.speedcubing.server.player.User;
import top.speedcubing.server.speedcubingServer;
import top.speedcubing.server.utils.CommandParser;

import java.util.UUID;

public class PlayIn {


    @CubingEventHandler
    public void PlayInEvent(PlayInEvent e) {
        if (User.getUser(e.getPlayer()) == null) {
            return;
        }
        if (User.getUser(e.getPlayer()).isCrashed) {
            if (!(e.getPacket() instanceof PacketPlayInKeepAlive || e.getPacket() instanceof PacketPlayInCustomPayload)) {
                e.setCancelled(true);
            }
        }
        if (e.getPacket() instanceof PacketPlayInTabComplete p) {
            String s = p.a();
            CommandParser command = CommandParser.parse(s);
            User user = User.getUser(e.getPlayer());
            user.lastTabbed = command.command;
            if (!user.hasPermission("cmd." + command.command) && !user.hasPermission("cmd.*")) {
                e.setCancelled(true);
            }
        } else if (e.getPacket() instanceof PacketPlayInCustomPayload packet) {
            if (packet.a().equals("labymod3:main")) {
                PacketDataSerializer serializer = packet.b();

                String type = serializer.c(64);

                if (!type.equals("INFO")) {
                    return;
                }

                String json = serializer.c(10000);

                JsonObject parser = JsonParser.parseString(json).getAsJsonObject();
                System.out.println(parser);
            }
        } else if (e.getPacket() instanceof PacketPlayInUpdateSign packet) {
            IChatBaseComponent[] components = packet.b();
            String name = components[0].getText().trim();

            if (nick.settingNick.containsKey(e.getPlayer().getUniqueId())) {
                Bukkit.getScheduler().runTask(speedcubingServer.instance, () -> {
                    UUID uuid = e.getPlayer().getUniqueId();
                    if (name.isEmpty()) {
                        // ask again once; the hint tells them a second empty sign cancels (Esc sends one too)
                        if (nick.emptyNickSign.add(uuid)) {
                            nick.openNickBook(e.getPlayer(), nick.NickBook.NAMECUSTOM);
                            return;
                        }
                        nick.emptyNickSign.remove(uuid);
                        nick.settingNick.remove(uuid);
                        nick.nickName.remove(uuid);
                        nick.nickRank.remove(uuid);
                        return;
                    }
                    nick.emptyNickSign.remove(uuid);
                    try {
                        nick.nickName.put(uuid, name);
                        // runs nickCheck, which opens the RULE book on success
                        e.getPlayer().performCommand("nick " + name + " " + nick.nickRank.get(uuid) + " true");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        User.getUser(e.getPlayer()).sendMessage("%lang_server_command_error%", String.valueOf(ex.getMessage()));
                    }
                });
            }
        } else if (e.getPacket() instanceof PacketPlayInFlying packet) {
            new PlayerClientTickEvent(e.getPlayer(), packet).call();
        }
    }
}
