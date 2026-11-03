package top.speedcubing.server.lang;

import com.google.gson.JsonObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import top.speedcubing.lib.minecraft.text.ComponentText;
import top.speedcubing.server.player.User;

public class Lang {
    private static final Pattern pattern = Pattern.compile("%lang_([A-Za-z_\\d]+)%");
    private final ComponentText unformatted;
    private String[] param;

    public static Lang of(String unformatted, String... param) {
        return of(new ComponentText().str(unformatted), param);
    }

    public static Lang of(ComponentText unformatted, String... param) {
        return new Lang(unformatted, param);
    }

    private Lang(ComponentText unformatted, String... param) {
        this.unformatted = unformatted;
        this.param = param;
    }

    public Lang param(String... param) {
        String[] merged = new String[this.param.length + param.length];
        System.arraycopy(this.param, 0, merged, 0, this.param.length);
        System.arraycopy(param, 0, merged, this.param.length, param.length);
        this.param = merged;
        return this;
    }

    public TextComponent get(int lang) {
        return getComponent(lang).toBungee();
    }

    public TextComponent get(CommandSender source) {
        return get(source instanceof ConsoleCommandSender ? 1 : User.getUser(source).lang);
    }

    public TextComponent get(User user) {
        return get(user.lang);
    }

    public String getString(int lang) {
        return getComponent(lang).toText();
    }

    public ComponentText getComponent(int lang) {
        Matcher matcher = pattern.matcher(unformatted.serialize());
        String result = matcher.replaceAll(r -> Matcher.quoteReplacement(lookup(lang, r.group(1))));
        for (String string : param) {
            result = result.replaceFirst("%p%", Matcher.quoteReplacement(string));
        }
        return ComponentText.unSerialize(result);
    }

    // missing keys (e.g. a language file without the newest keys) fall back to English, then the raw key
    private static String lookup(int lang, String key) {
        JsonObject[] files = LanguageSystem.lang;
        if (files[lang] != null && files[lang].has(key))
            return files[lang].get(key).getAsString();
        if (files[1] != null && files[1].has(key))
            return files[1].get(key).getAsString();
        return "%lang_" + key + "%";
    }

    public Lang clone() {
        return Lang.of(unformatted, param);
    }
}