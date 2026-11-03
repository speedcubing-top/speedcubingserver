package top.speedcubing.server.authenticator;

import org.bukkit.entity.Player;
import top.speedcubing.server.player.User;

public class AuthMessenger {

    public static void sendSetKeyMessage(Player player) {
        User.getUser(player).sendMessage("%lang_server_2fa_setup_required%");
    }

    public static void sendEnterCodeMessage(Player player) {
        User.getUser(player).sendMessage("%lang_server_2fa_enter_code%");
    }
}
