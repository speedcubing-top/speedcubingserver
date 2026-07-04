package top.speedcubing.server.system.command;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class CubingCommand {

    private final Set<String> alias;
    private final String permission;

    public final Set<String> getAlias() {
        return alias;
    }

    public final String getPermission() {
        return permission;
    }

    public CubingCommand(String... command) {
        this.alias = Sets.newHashSet(command);
        this.permission = resolvePermission(command);
    }

    public abstract void execute(CommandSender sender, String command, String[] args);

    public List<String> onTabComplete(CommandSender sender, String command, String[] args, Location location) {
        return Collections.emptyList();
    }

    public boolean shouldLoad() {
        return true;
    }

    final void load() {
        CubingCommandManager.register(this);
    }

    private String resolvePermission(String[] command) {
        Package commandPackage = getClass().getPackage();
        if (commandPackage == null || !commandPackage.getName().contains(".staff")) {
            return null;
        }
        if (command.length == 0) {
            return null;
        }
        return "speedcubing.command.server." + command[0].toLowerCase();
    }

    protected final boolean testPermission(CommandSender sender) {
        if (permission == null || !(sender instanceof Player)) {
            return true;
        }
        if (sender.hasPermission(permission)) {
            return true;
        }
        sender.sendMessage("§cYou do not have permission to use this command.");
        return false;
    }
}
