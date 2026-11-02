package top.speedcubing.server.utils;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

// staff UUID -> monitored target UUID; written by /cps on the main thread, rendered by CubingTick on Cubing-Tick-Thread
public class CPSMonitor {

    private static final ConcurrentHashMap<UUID, UUID> monitoring = new ConcurrentHashMap<>();

    public static UUID putMonitor(UUID staff, UUID target) {
        return monitoring.put(staff, target);
    }

    public static UUID removeMonitor(UUID staff) {
        return monitoring.remove(staff);
    }

    public static UUID getTarget(UUID staff) {
        return monitoring.get(staff);
    }

    public static void removeOfflineStaff(Predicate<UUID> isOnline) {
        monitoring.keySet().removeIf(isOnline.negate());
    }
}
