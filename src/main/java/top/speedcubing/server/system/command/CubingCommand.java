package top.speedcubing.server.system.command;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

public abstract class CubingCommand {

    private final Set<String> alias;

    public final Set<String> getAlias() {
        return alias;
    }

    public CubingCommand(String... command) {
        this.alias = Sets.newHashSet(command);
    }

    public abstract void execute(CommandSender sender, String command, String[] args);

    public List<String> onTabComplete(CommandSender sender, String command, String[] args, Location location) {
        return Collections.emptyList();
    }

    /** Names of online players the sender can see (vanished staff stay hidden) that start with {@code prefix}. */
    protected static List<String> playerNames(CommandSender sender, String prefix) {
        String lower = prefix.toLowerCase();
        List<String> names = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if ((!(sender instanceof Player viewer) || viewer.canSee(p)) && p.getName().toLowerCase().startsWith(lower))
                names.add(p.getName());
        }
        return names;
    }

    public boolean shouldLoad() {
        return true;
    }

    final void load() {
        CubingCommandManager.register(this);
    }
}
