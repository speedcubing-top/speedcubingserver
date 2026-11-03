package top.speedcubing.server.cubingcmd;

import org.bukkit.command.CommandSender;
import top.speedcubing.server.system.command.CubingCommand;
import top.speedcubing.common.text.Msg;

public class plugins extends CubingCommand {
    public plugins() {
        super("plugins", "pl");
    }

    @Override
    public void execute(CommandSender sender, String command, String[] args) {
        sender.sendMessage(Msg.SYSTEM + Msg.INFO + "Plugins (3): §aspeedcubing§7, §aspeedcubingLib§7, §aspeedcubingServer");
    }
}
