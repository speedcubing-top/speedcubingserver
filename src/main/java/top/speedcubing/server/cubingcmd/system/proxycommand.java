package top.speedcubing.server.cubingcmd.system;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.mulitproxy.BungeeProxy;
import top.speedcubing.server.system.command.CubingCommand;

public class proxycommand extends CubingCommand {
    public proxycommand() {
        super("proxycommand");
    }

    @Override
    public void execute(CommandSender commandSender, String s, String[] args) {
        if (args.length == 0) {
            commandSender.sendMessage(Msg.SYSTEM + Msg.usage("/proxycommand <command...>"));
            return;
        }
        // Run it on the proxy of a player it names (e.g. the target of "ban <player> ..."), so that
        // proxy can kick them; a command naming no one here goes to any online player's proxy.
        Player via = null;
        for (String arg : args) {
            via = Bukkit.getPlayerExact(arg);
            if (via != null)
                break;
        }
        if (via == null)
            via = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        if (via == null) {
            commandSender.sendMessage(Msg.SYSTEM + Msg.ERROR + "No player online to reach a proxy through.");
            return;
        }
        BungeeProxy.proxyCommand(via, String.join(" ", args));
    }
}
