package top.speedcubing.server.cubingcmd.staff;

import org.bukkit.command.CommandSender;
import top.speedcubing.common.events.ConfigReloadEvent;
import top.speedcubing.server.system.command.CubingCommand;

public class serverconfig extends CubingCommand {
    public serverconfig() {
        super("serverconfig");
    }

    @Override
    public void execute(CommandSender commandSender, String s, String[] args) {
        new ConfigReloadEvent().call();
    }
}
