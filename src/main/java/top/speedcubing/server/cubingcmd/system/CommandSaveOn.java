package top.speedcubing.server.cubingcmd.system;

import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.command.CommandSender;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.system.command.CubingCommand;

public class CommandSaveOn extends CubingCommand {
    public CommandSaveOn() {
        super("save-on");
    }

    @Override
    public void execute(CommandSender commandSender, String s, String[] args) {
        for (WorldServer worldServer : CommandSaveOff.saveOffWorlds) {
            worldServer.savingDisabled = false;
        }
        CommandSaveOff.saveOffWorlds.clear();
        commandSender.sendMessage(Msg.SYSTEM + Msg.SUCCESS + "OK, world saving enabled.");
    }
}