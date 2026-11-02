package top.speedcubing.server.cubingcmd.system;

import org.bukkit.command.CommandSender;
import top.speedcubing.server.mulitproxy.BungeeProxy;
import top.speedcubing.server.system.command.CubingCommand;

public class proxycommand extends CubingCommand {
    public proxycommand() {
        super("proxycommand");
    }

    @Override
    public void execute(CommandSender commandSender, String s, String[] args) {
        if (args.length == 0)
            commandSender.sendMessage("/proxycommand <command...>");
        else {
            StringBuilder commandStrings = new StringBuilder();
            for (String string : args) {
                commandStrings.append(" ").append(string);
            }
            BungeeProxy.proxyCommand(commandStrings.substring(1));
        }
    }
}
