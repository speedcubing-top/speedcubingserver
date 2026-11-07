package top.speedcubing.server.utils;

import net.minecraft.server.v1_8_R3.EntityWither;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * 1.8 has no boss bar packet: the client shows the bar of a boss it is rendering,
 * so each viewer gets their own invisible wither kept in front of their view
 * (approach from TigerHix/BossbarLib). The wither stays at full health, since at
 * half health or less the client draws its armour aura even while invisible, so
 * the bar is always full and the text carries the information; TPS and RAM share it.
 * <p>
 * 1.9+ clients on ViaVersion get real boss bars instead, one per readout like Purpur's
 * /tpsbar and /rambar: filled by tick time or heap use and coloured green/yellow/red.
 * The wither, translated by Via and teleported every move, does not hold up there.
 */
public final class StatsBossBar {
    private static final double DISTANCE = 20;

    private final Player viewer;
    // one wither per viewer: the spawn packet keeps a reference to the entity's DataWatcher
    private final EntityWither wither;
    // non-null for 1.9+ clients through ViaVersion; then the wither is never sent
    private final ViaStatsBar viaTps, viaRam;
    // update runs on the CubingTick timer thread, the rest on the main thread
    private boolean spawned, removed;

    public StatsBossBar(Player viewer) {
        this.viewer = viewer;
        this.wither = new EntityWither(((CraftWorld) viewer.getWorld()).getHandle());
        this.wither.setInvisible(true);
        boolean via = ViaStatsBar.supports(viewer.getUniqueId());
        this.viaTps = via ? new ViaStatsBar(viewer.getUniqueId()) : null;
        this.viaRam = via ? new ViaStatsBar(viewer.getUniqueId()) : null;
    }

    /** Shows the readouts that are switched on; ping is the viewer's. */
    public synchronized void update(boolean tps, boolean ram, int ping) {
        if (removed)
            return;
        if (viaTps != null) {
            if (tps)
                viaTps.set(ServerStats.tpsLine(ping), ServerStats.msptProgress(), ServerStats.msptLevel());
            else
                viaTps.remove();
            if (ram)
                viaRam.set(ServerStats.ramLine(), ServerStats.ramProgress(), ServerStats.ramLevel());
            else
                viaRam.remove();
            return;
        }
        String text = tps && ram ? ServerStats.tpsLine(ping) + " §8| " + ServerStats.ramLine() : tps ? ServerStats.tpsLine(ping) : ServerStats.ramLine();
        wither.setCustomName(text);
        if (spawned) {
            connection().sendPacket(new PacketPlayOutEntityMetadata(wither.getId(), wither.getDataWatcher(), true));
        } else {
            moveInFront();
            connection().sendPacket(new PacketPlayOutSpawnEntityLiving(wither));
            spawned = true;
        }
    }

    /** Keep the wither in view; call when the viewer moves or turns. */
    public synchronized void follow() {
        if (!spawned || viaTps != null)
            return;
        moveInFront();
        connection().sendPacket(new PacketPlayOutEntityTeleport(wither));
    }

    /** The client dropped the entity (teleport, world change, respawn); spawn it again on the next update. */
    public synchronized void invalidate() {
        spawned = false;
    }

    public synchronized void remove() {
        if (viaTps != null) {
            viaTps.remove();
            viaRam.remove();
        }
        if (spawned)
            connection().sendPacket(new PacketPlayOutEntityDestroy(wither.getId()));
        spawned = false;
        removed = true;
    }

    private void moveInFront() {
        Location eye = viewer.getEyeLocation();
        Location at = eye.add(eye.getDirection().multiply(DISTANCE));
        wither.setLocation(at.getX(), at.getY(), at.getZ(), 0, 0);
    }

    private PlayerConnection connection() {
        return ((CraftPlayer) viewer).getHandle().playerConnection;
    }
}
