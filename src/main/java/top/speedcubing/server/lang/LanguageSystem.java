package top.speedcubing.server.lang;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

public class LanguageSystem {
    // language ids are stored per player (playersdata.lang), so the order must never change
    public static final String[] LOCALES = {"de", "en", "zh-TW", "zh-CN"};
    public static int langCount = LOCALES.length;

    public static JsonObject[] lang = new JsonObject[langCount];

    /** Loads lang/<locale>.json (server-language repo, baked into the image) from the server directory. */
    public static void init() {
        File dir = new File("lang");
        for (int id = 0; id < langCount; id++) {
            File file = new File(dir, LOCALES[id] + ".json");
            try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
                lang[id] = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (Exception e) {
                System.out.println("[LanguageSystem] could not load " + file.getAbsolutePath() + ": " + e);
            }
        }
    }
}
