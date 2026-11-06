package top.speedcubing.server.cubingcmd;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.system.command.CubingCommand;
import top.speedcubing.server.utils.ServerStats;

public class uptime extends CubingCommand {
    public uptime() {
        super("uptime");
    }

    @Override
    public void execute(CommandSender sender, String command, String[] args) {
        sender.sendMessage(Msg.SYSTEM + Msg.INFO + Bukkit.getServerName() + " uptime: " + Msg.VALUE + ServerStats.uptime());
    }
}
