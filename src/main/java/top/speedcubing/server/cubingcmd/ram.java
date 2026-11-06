package top.speedcubing.server.cubingcmd;

import org.bukkit.command.CommandSender;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.system.command.CubingCommand;
import top.speedcubing.server.utils.ServerStats;

public class ram extends CubingCommand {
    public ram() {
        super("ram");
    }

    @Override
    public void execute(CommandSender sender, String command, String[] args) {
        sender.sendMessage(Msg.SYSTEM + ServerStats.ramLine());
    }
}
