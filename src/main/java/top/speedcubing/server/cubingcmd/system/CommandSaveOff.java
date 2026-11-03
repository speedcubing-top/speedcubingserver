package top.speedcubing.server.cubingcmd.system;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.system.command.CubingCommand;

public class CommandSaveOff extends CubingCommand {
    public CommandSaveOff() {
        super("save-off");
    }

    static Set<WorldServer> saveOffWorlds = new HashSet<>();

    @Override
    public void execute(CommandSender commandSender, String s, String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.getServer();
        for (byte b = 0; b < minecraftServer.worldServer.length; b++) {
            if (minecraftServer.worldServer[b] != null) {
                WorldServer worldServer = minecraftServer.worldServer[b];
                if (!worldServer.savingDisabled) {
                    saveOffWorlds.add(worldServer);
                    worldServer.savingDisabled = true;
                }
            }
        }
        commandSender.sendMessage(Msg.SYSTEM + Msg.SUCCESS + "OK, world saving disabled.");
    }
}
