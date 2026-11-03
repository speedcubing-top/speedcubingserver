package top.speedcubing.server.lang;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class LanguageSystem {
    // language ids are stored per player (playersdata.lang), so the order must never change
    public static final String[] LOCALES = {"de", "en", "zh-tw", "zh-cn"};
    public static int langCount = LOCALES.length;

    public static JsonObject[] lang = new JsonObject[langCount];

    /**
     * Loads /storage/lang/<locale>/*.json (server-language repo, one file per module: system, lobby, kbffa, ...)
     * and merges each locale's files into one key table.
     */
    public static void init() {
        File root = new File("/storage/lang");
        for (int id = 0; id < langCount; id++) {
            JsonObject merged = new JsonObject();
            File[] files = new File(root, LOCALES[id]).listFiles((d, name) -> name.endsWith(".json"));
            if (files == null) {
                System.out.println("[LanguageSystem] missing directory " + new File(root, LOCALES[id]).getAbsolutePath());
                lang[id] = merged;
                continue;
            }
            Arrays.sort(files);
            for (File file : files) {
                try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
                    for (Map.Entry<String, JsonElement> entry : JsonParser.parseReader(reader).getAsJsonObject().entrySet()) {
                        if (merged.has(entry.getKey()))
                            System.out.println("[LanguageSystem] duplicate key " + entry.getKey() + " in " + file.getAbsolutePath());
                        merged.add(entry.getKey(), entry.getValue());
                    }
                } catch (Exception e) {
                    System.out.println("[LanguageSystem] could not load " + file.getAbsolutePath() + ": " + e);
                }
            }
            lang[id] = merged;
        }
    }
}
