package top.speedcubing.server.cubingcmd;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import top.speedcubing.lib.api.MojangAPI;
import top.speedcubing.lib.api.mojang.ProfileSkin;
import top.speedcubing.server.events.player.SkinEvent;
import top.speedcubing.server.player.User;
import top.speedcubing.server.system.command.CubingCommand;

public class skin extends CubingCommand {
    public skin() {
        super("skin");
    }

    @Override
    public void execute(CommandSender sender, String command, String[] args) {
        Player player = (Player) sender;
        if (!((SkinEvent) new SkinEvent(player).call()).isCancelled()) {
            User user = User.getUser(sender);
            String target = "";
            if (args.length == 0)
                target = user.realName;
            else if (args.length == 1)
                target = args[0];
            else user.sendMessage("%lang_server_skin_usage%");

            if (!target.isEmpty() && !target.equalsIgnoreCase(user.realName)) {
                ProfileSkin profileSkin;
                try {
                    profileSkin = MojangAPI.getSkinByName(target);
                    if (profileSkin == null) {
                        user.sendMessage("%lang_server_skin_not_found%");
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                user.updateSkin(profileSkin.getSkin(), target);
            } else if (target.equalsIgnoreCase(user.realName)) {
                user.updateSkin(user.defaultSkin, target);
            } else {
                user.sendMessage("%lang_server_skin_default_not_found%");
            }
        }
    }
}