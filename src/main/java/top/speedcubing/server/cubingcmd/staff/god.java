package top.speedcubing.server.cubingcmd.staff;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.player.User;
import top.speedcubing.server.system.command.CubingCommand;

/** /god: toggles invulnerability for this session; PostListen cancels the damage. */
public class god extends CubingCommand {
    private static final String PREFIX = Msg.STAFF;

    public god() {
        super("god");
    }

    @Override
    public void execute(CommandSender sender, String command, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + Msg.PLAYERS_ONLY);
            return;
        }
        User user = User.getUser(player);
        user.god = !user.god;
        sender.sendMessage(PREFIX + Msg.INFO + "God mode " + (user.god ? Msg.SUCCESS + "enabled" : Msg.ERROR + "disabled") + Msg.INFO + ".");
    }
}
