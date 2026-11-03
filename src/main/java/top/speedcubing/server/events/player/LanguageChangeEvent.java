package top.speedcubing.server.events.player;

import org.bukkit.entity.Player;

/** Called after a player's language changed; games redraw per-language things (sidebars, holograms) here. */
public class LanguageChangeEvent extends PlayerEvent {
    private final int oldLang;
    private final int newLang;

    public LanguageChangeEvent(Player player, int oldLang, int newLang) {
        super(player);
        this.oldLang = oldLang;
        this.newLang = newLang;
    }

    public int getOldLang() {
        return oldLang;
    }

    public int getNewLang() {
        return newLang;
    }
}
