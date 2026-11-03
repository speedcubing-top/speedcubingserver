package top.speedcubing.server.cubingcmd.staff;

import java.util.Set;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.speedcubing.common.profiler.Profiler;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.player.User;
import top.speedcubing.server.system.command.CubingCommand;

// /serverprofiler [reset]: this server's profiler report (/profiler is the proxy's)
public class serverprofiler extends CubingCommand {
    private static final Set<String> ALLOWED = Set.of("owner", "admin", "developer");

    public serverprofiler() {
        super("serverprofiler");
    }

    @Override
    public void execute(CommandSender sender, String command, String[] args) {
        if (sender instanceof Player player) {
            User user = User.getUser(player);
            if (user == null || !ALLOWED.contains(user.realRank)) {
                if (user != null)
                    user.sendMessage("%lang_general_noperm%");
                else
                    sender.sendMessage(Msg.SYSTEM + Msg.NO_PERMISSION);
                return;
            }
        }
        boolean reset = args.length == 1 && args[0].equalsIgnoreCase("reset");
        for (String line : Profiler.report(reset).split("\n"))
            sender.sendMessage(line);
    }
}
