package top.speedcubing.server.cubingcmd;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.player.User;
import top.speedcubing.server.system.command.CubingCommand;

public class rambar extends CubingCommand {
    public rambar() {
        super("rambar");
    }

    @Override
    public void execute(CommandSender sender, String command, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Msg.PLAYERS_ONLY);
            return;
        }
        User user = User.getUser(player);
        user.ramBar = !user.ramBar;
        sender.sendMessage(Msg.SYSTEM + Msg.INFO + "RAM bar " + (user.ramBar ? Msg.SUCCESS + "enabled" : Msg.ERROR + "disabled") + Msg.INFO + ".");
    }
}
