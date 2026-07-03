package top.speedcubing.server.permissions;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.MetaNode;
import net.luckperms.api.query.QueryOptions;

public final class LuckPermsHook {
    private static LuckPerms luckPerms;
    private static Logger logger;

    private LuckPermsHook() {
    }

    public static void init(Logger logger) {
        LuckPermsHook.logger = logger;
        ensureAvailable();
    }

    private static boolean ensureAvailable() {
        if (luckPerms != null) {
            return true;
        }
        try {
            luckPerms = LuckPermsProvider.get();
            if (logger != null) {
                logger.info("LuckPerms API connected.");
            }
            return true;
        } catch (IllegalStateException ex) {
            return false;
        }
    }

    public static String getPrimaryGroup(UUID uuid) {
        User user = loadUser(uuid);
        if (user == null) {
            return null;
        }
        return user.getPrimaryGroup();
    }

    public static boolean setPrimaryGroup(UUID uuid, String groupName) {
        if (!ensureAvailable()) {
            return false;
        }
        Group group = luckPerms.getGroupManager().getGroup(groupName);
        if (group == null) {
            return false;
        }
        User user = loadUser(uuid);
        if (user == null) {
            return false;
        }
        user.data().clear(node -> node instanceof InheritanceNode);
        user.data().add(InheritanceNode.builder(groupName).build());
        user.setPrimaryGroup(groupName);
        luckPerms.getUserManager().saveUser(user);
        return true;
    }

    public static String getPrefix(UUID uuid) {
        if (!ensureAvailable()) {
            return null;
        }
        User user = loadUser(uuid);
        if (user == null) {
            return null;
        }
        String prefix = user.getCachedData().getMetaData(QueryOptions.defaultContextualOptions()).getPrefix();
        if (prefix == null || prefix.isBlank()) {
            return null;
        }
        return prefix;
    }

    public static String getMeta(UUID uuid, String key) {
        if (!ensureAvailable()) {
            return null;
        }
        User user = loadUser(uuid);
        if (user == null) {
            return null;
        }
        String value = user.getCachedData().getMetaData(QueryOptions.defaultContextualOptions()).getMetaValue(key);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    public static boolean setDisplayMeta(UUID uuid, String displayRank, String displayName) {
        if (!ensureAvailable()) {
            return false;
        }
        User user = loadUser(uuid);
        if (user == null) {
            return false;
        }

        for (Node node : user.data().toCollection()) {
            if (node instanceof MetaNode metaNode) {
                String key = metaNode.getMetaKey();
                if ("displayrank".equalsIgnoreCase(key) || "displayname".equalsIgnoreCase(key)) {
                    user.data().remove(metaNode);
                }
            }
        }

        user.data().add(MetaNode.builder("displayrank", displayRank).build());
        user.data().add(MetaNode.builder("displayname", displayName).build());
        luckPerms.getUserManager().saveUser(user);
        return true;
    }

    private static User loadUser(UUID uuid) {
        if (!ensureAvailable()) {
            return null;
        }
        try {
            return luckPerms.getUserManager().loadUser(uuid).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException ex) {
            return null;
        }
    }
}
