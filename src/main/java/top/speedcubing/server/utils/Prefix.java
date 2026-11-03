package top.speedcubing.server.utils;

import top.speedcubing.common.text.Msg;

/**
 * Chat prefixes for speedcubingserver features (style from {@link Msg}).
 * Player-facing text uses the language system with the same prefix text inside the value;
 * these constants are for staff/console English output.
 */
public final class Prefix {
    public static final String TWO_FA = Msg.prefix("2FA", '6');
    public static final String NICK = Msg.prefix("Nick", 'b');
    public static final String SKIN = Msg.prefix("Skin", 'b');
    public static final String STATUS = Msg.prefix("Status", 'b');
    public static final String ACCOUNT = Msg.prefix("Account", '6');
    public static final String IMAGE = Msg.prefix("Image", 'd');
    public static final String STAFF = Msg.prefix("Staff", 'c');
    public static final String MUSIC = Msg.prefix("Music", 'd');
    public static final String CPS = Msg.prefix("CPS", 'c');
    public static final String CHAT_LOG = Msg.prefix("ChatLog", 'a');

    private Prefix() {
    }
}
