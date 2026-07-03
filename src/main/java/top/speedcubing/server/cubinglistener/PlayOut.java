package top.speedcubing.server.cubinglistener;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunkBulk;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutStatistic;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import top.speedcubing.lib.bukkit.events.packet.PlayOutEvent;
import top.speedcubing.lib.eventbus.CubingEventHandler;
import top.speedcubing.lib.utils.ReflectionUtils;
import top.speedcubing.server.bukkitcmd.trolls.sendpacket;
import top.speedcubing.server.events.player.PlayerClientTickEvent;
import top.speedcubing.server.player.User;

public class PlayOut {

    @CubingEventHandler
    public void PlayOutEvent(PlayOutEvent e) {
        if (e.getPacket() instanceof PacketPlayOutStatistic packet) {
            Map<?, Integer> stats = (Map<?, Integer>) ReflectionUtils.getField(packet, "a");
            stats.replaceAll((k, v) -> 0);
        } else if (e.getPacket() instanceof PacketPlayOutMapChunk packet) {

            if (!sendpacket.whoWasFucked.contains(e.getPlayer()))
                return;

            try {
                PacketPlayOutMapChunk.ChunkMap data = (PacketPlayOutMapChunk.ChunkMap) ReflectionUtils.getField(packet, "c");
                boolean isInit = (boolean) ReflectionUtils.getField(packet, "d");
                boolean isOverworld = ((CraftPlayer) e.getPlayer()).getHandle().world.worldProvider.o();
                sendpacket.randomChunkData(isOverworld, isInit, data);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else if (e.getPacket() instanceof PacketPlayOutMapChunkBulk packet) {

            if (!sendpacket.whoWasFucked.contains(e.getPlayer()))
                return;

            try {
                PacketPlayOutMapChunk.ChunkMap[] data = (PacketPlayOutMapChunk.ChunkMap[]) ReflectionUtils.getField(packet, "c");
                boolean isOverworld = (boolean) ReflectionUtils.getField(packet, "d");
                for (PacketPlayOutMapChunk.ChunkMap datum : data) {
                    sendpacket.randomChunkData(isOverworld, true, datum);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (e.getPacket() instanceof PacketPlayOutNamedEntitySpawn packet) {
            UUID uuid = (UUID) ReflectionUtils.getField(packet, "b");
            User user = User.usersByUUID.get(uuid);
            if (user != null && user.nicked()) {
                ReflectionUtils.setField(packet, "b", user.calculateNickHashUUID());
            }
        } else if (e.getPacket() instanceof PacketPlayOutPlayerInfo packet) {
            List<PacketPlayOutPlayerInfo.PlayerInfoData> datas = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) ReflectionUtils.getField(packet, "b");

            for (int i = 0; i < datas.size(); i++) {
                PacketPlayOutPlayerInfo.PlayerInfoData data = datas.get(i);
                UUID targetID = data.a().getId();

                if (targetID.equals(e.getPlayer().getUniqueId())) { //if the target = self
                    continue;
                }

                User target = User.getUser(targetID);

                if (target == null) { //if targetID isnt a player
                    User user = User.getUser(e.getPlayer());
                    if (user == null) {
                        return;
                    }
                    if (user.calculateNickHashUUID().equals(targetID)) { //if target = self nick -> modify
                        target = user; //result name = self
                        targetID = user.uuid; //result uuid = self uuid
                    } else continue;
                } else {
                    if (!target.nicked()) {
                        continue;
                    }
                    targetID = target.calculateNickHashUUID(); //result uuid = targetID -> user -> nick uuid
                }

                Collection<Property> textures = data.a().getProperties().get("textures");
                if (textures.isEmpty()) continue;
                GameProfile profile = new GameProfile(targetID, target.bGetName());
                profile.getProperties().put("textures", textures.iterator().next());
                datas.set(i, packet.new PlayerInfoData(profile, data.b(), data.c(), data.d()));
            }
        } else if (e.getPacket() instanceof PacketPlayInFlying packet) {
            new PlayerClientTickEvent(e.getPlayer(), packet).call();
        }
    }
}
