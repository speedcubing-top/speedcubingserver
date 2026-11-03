package top.speedcubing.server.bukkitcmd.trolls;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.EntityArrow;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutBed;
import net.minecraft.server.v1_8_R3.PacketPlayOutCollect;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutGameStateChange;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.utils.Prefix;

public class sendpacket implements CommandExecutor, Listener {
    private static final String PREFIX = Prefix.STAFF;
    public final static Set<Player> whoWasFucked = Collections.newSetFromMap(new WeakHashMap<>());

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (args.length != 2) {
            player.sendMessage(PREFIX + Msg.usage("/sendpacket <player> <packet>"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(PREFIX + Msg.PLAYER_NOT_FOUND);
            return true;
        }
        String function = args[1];
        switch (function) {
            case "sleep":
                sleep(target);
                break;
            case "wakeup":
                wakeUp(target);
                break;
            case "howtowin":
                howToWin(target);
                break;
            case "fuckpeople":
                fuckPeople(target);
                break;
            case "sit":
                sit(target);
                break;
            case "stand":
                stand(target);
                break;
            case "gameend":
                ganeEnd(target);
                break;
            case "pickup":
                pickup(target);
                break;
        }

        return true;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        whoWasFucked.remove(e.getPlayer());
    }

    private void pickup(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        if (player.getNearbyEntities(10, 10, 10).isEmpty()) return;
        PacketPlayOutCollect packet = new PacketPlayOutCollect(player.getNearbyEntities(10, 10, 10).get(0).getEntityId(), player.getEntityId());
        craftPlayer.getHandle().playerConnection.sendPacket(packet);
    }

    private void sleep(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        PacketPlayOutBed packetPlayOutBed = new PacketPlayOutBed(craftPlayer.getHandle(), new BlockPosition(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()));
        craftPlayer.getHandle().playerConnection.sendPacket(packetPlayOutBed);
        craftPlayer.getHandle().u().getTracker().a(craftPlayer.getHandle(), packetPlayOutBed);
    }

    private void wakeUp(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        PacketPlayOutAnimation packetPlayOutBed = new PacketPlayOutAnimation(craftPlayer.getHandle(), 2);
        craftPlayer.getHandle().playerConnection.sendPacket(packetPlayOutBed);
        craftPlayer.getHandle().u().getTracker().a(craftPlayer.getHandle(), packetPlayOutBed);
    }

    private void howToWin(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        PacketPlayOutBed packetPlayOutBed = new PacketPlayOutBed(craftPlayer.getHandle(), new BlockPosition(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()));
        craftPlayer.getHandle().u().getTracker().a(craftPlayer.getHandle(), packetPlayOutBed);
    }

    private void fuckPeople(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        whoWasFucked.add(player);
        ((WorldServer) craftPlayer.getHandle().world).getPlayerChunkMap().removePlayer(craftPlayer.getHandle());
        ((WorldServer) craftPlayer.getHandle().world).getPlayerChunkMap().addPlayer(craftPlayer.getHandle());
    }

    private void sit(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        Location location = player.getLocation();

        EntityArrow arrow = new EntityArrow(
                ((CraftWorld) player.getWorld()).getHandle(),
                location.getX(),
                location.getY() - 1,
                location.getZ()
        );

        PacketPlayOutSpawnEntity spawnPacket = new PacketPlayOutSpawnEntity(arrow, 60);
        PacketPlayOutAttachEntity attachPacket = new PacketPlayOutAttachEntity(0, ((CraftPlayer) player).getHandle(), arrow);

        craftPlayer.getHandle().playerConnection.sendPacket(spawnPacket);
        craftPlayer.getHandle().playerConnection.sendPacket(attachPacket);
        craftPlayer.getHandle().u().getTracker().a(craftPlayer.getHandle(), spawnPacket);
        craftPlayer.getHandle().u().getTracker().a(craftPlayer.getHandle(), attachPacket);
    }

    private void stand(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        PacketPlayOutAttachEntity detachPacket = new PacketPlayOutAttachEntity(0, ((CraftPlayer) player).getHandle(), null);
        craftPlayer.getHandle().playerConnection.sendPacket(detachPacket);
        PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(((CraftEntity) player).getHandle().passenger.getId());
        craftPlayer.getHandle().playerConnection.sendPacket(destroyPacket);
        craftPlayer.getHandle().u().getTracker().a(craftPlayer.getHandle(), detachPacket);
        craftPlayer.getHandle().u().getTracker().a(craftPlayer.getHandle(), destroyPacket);
    }

    private void ganeEnd(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        PacketPlayOutGameStateChange packet = new PacketPlayOutGameStateChange(4, 1);
        craftPlayer.getHandle().playerConnection.sendPacket(packet);
    }

    public static void initFuckPeople() {
        initSupported();
    }

    private static int reverseSectionsCount(int i, boolean flag, boolean flag1) {
        int j = 2 * 16 * 16 * 16;
        int k = 16 * 16 * 16 / 2;
        int l = flag ? 16 * 16 * 16 / 2 : 0;
        int i1 = flag1 ? 256 : 0;
        return (i - i1) / (j + k + l);
    }

    public static void randomChunkData(boolean sendBiome, boolean isInitChunk, PacketPlayOutMapChunk.ChunkMap input) {
        int sectionsCount = reverseSectionsCount(input.a.length, isInitChunk, sendBiome);

        int cur = 0;
        for (int i = 0; i < sectionsCount; i++) {
            for (int i1 = 0; i1 < 8192; i1 += 2) {
                int blockData = supported[ThreadLocalRandom.current().nextInt(supported.length)];
                input.a[cur++] = (byte) (blockData & 255);
                input.a[cur++] = (byte) (blockData >> 8 & 255);
            }
        }
    }

    private static int[] supported;

    private static void initSupported() {
        int supportedLen = 0;
        for (IBlockData iBlockData : Block.d) {
            supportedLen++;
        }
        supported = new int[supportedLen];
        int counter = 0;
        for (IBlockData iBlockData : Block.d) {
            supported[counter++] = Block.d.b(iBlockData);
        }
    }
}
