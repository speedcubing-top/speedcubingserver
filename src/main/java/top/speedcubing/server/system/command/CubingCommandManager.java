package top.speedcubing.server.system.command;

import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import top.speedcubing.lib.utils.ReflectionUtils;

public class CubingCommandManager {

    public static void register(CubingCommand... commands) {
        for (CubingCommand command : commands) {
            for (String name : command.getAlias()) {
                Map<String, Command> knownCommands = (Map<String, Command>) ReflectionUtils.getField(((CraftServer) Bukkit.getServer()).getCommandMap(), "knownCommands");
                Command cmd = new Command(name) {
                    @Override
                    public boolean execute(CommandSender commandSender, String s, String[] strings) {
                        if (!command.testPermission(commandSender)) {
                            return true;
                        }
                        command.execute(commandSender, s, strings);
                        return true;
                    }

                    @Override
                    public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
                        return command.onTabComplete(sender, alias, args, location);
                    }
                };
                if (command.getPermission() != null) {
                    cmd.setPermission(command.getPermission());
                    cmd.setPermissionMessage("§cYou do not have permission to use this command.");
                }
                cmd.timings = co.aikar.timings.TimingsManager.getCommandTiming("minecraft", cmd);
                knownCommands.put(name, cmd);
            }
        }
    }
}
