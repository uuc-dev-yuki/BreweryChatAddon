package com.uuc.brewerychataddon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class AddonConfig {
    private final boolean enabled;
    private final String language;
    private final List<AlcoholRule> alcoholRules;

    private AddonConfig(boolean enabled, String language, List<AlcoholRule> alcoholRules) {
        this.enabled = enabled;
        this.language = (language == null || language.isBlank()) ? "english" : language;
        this.alcoholRules = alcoholRules;
    }

    public boolean enabled() {
        return enabled;
    }

    public String language() {
        return language;
    }

    public AlcoholRule ruleFor(double alcoholLevel) {
        if (alcoholRules == null || alcoholRules.isEmpty()) {
            return null;
        }
        for (AlcoholRule r : alcoholRules) {
            if (r.matches(alcoholLevel)) {
                return r;
            }
        }
        return null;
    }

    public static AddonConfig load(Path configDir) {
        try {
            Files.createDirectories(configDir);
        } catch (Throwable ignored) {
        }

        Path configFile = configDir.resolve("config.json");
        if (!Files.exists(configFile)) {
            try (InputStream in = AddonConfig.class.getClassLoader().getResourceAsStream("brewerychataddon_default.json")) {
                if (in != null) {
                    Files.copy(in, configFile);
                }
            } catch (Throwable ignored) {
            }
        }

        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            return parse(root);
        } catch (Throwable t) {
            try (InputStream in = AddonConfig.class.getClassLoader().getResourceAsStream("brewerychataddon_default.json")) {
                if (in != null) {
                    return parse(JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject());
                }
            } catch (Throwable ignored) {
            }
            return defaultConfig();
        }
    }

    private static AddonConfig parse(JsonObject root) {
        boolean enabled = root.has("enabled") && root.get("enabled").getAsBoolean();
        String language = root.has("language") && root.get("language").isJsonPrimitive()
            ? root.get("language").getAsString()
            : "english";

        List<AlcoholRule> rules = new ArrayList<>();

        if (root.has("alcoholRules") && root.get("alcoholRules").isJsonArray()) {
            for (JsonElement el : root.getAsJsonArray("alcoholRules")) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject o = el.getAsJsonObject();
                double min = o.has("min") ? o.get("min").getAsDouble() : 0;
                Double max = o.has("max") ? o.get("max").getAsDouble() : null;
                double chance = o.has("chance") ? o.get("chance").getAsDouble() : 0;
                List<String> messages = new ArrayList<>();
                if (o.has("messages")) {
                    JsonElement me = o.get("messages");
                    if (me.isJsonArray()) {
                        for (JsonElement e : me.getAsJsonArray()) {
                            if (e.isJsonPrimitive()) {
                                String s = e.getAsString();
                                if (s != null && !s.isBlank()) {
                                    messages.add(s);
                                }
                            }
                        }
                    }
                    else if (me.isJsonObject()) {
                        JsonObject byLang = me.getAsJsonObject();
                        messages.addAll(readMessagesForLanguage(byLang, language));
                    }
                }
                rules.add(new AlcoholRule(min, max, chance, messages));
            }
            return new AddonConfig(enabled, language, rules);
        }

        // Backward compatibility (older configs):
        // - chanceByAlcohol: ranges with chance
        // - messagesByStars: used only as a pool of messages (combined)
        List<String> fallbackMessages = new ArrayList<>();
        if (root.has("messagesByStars") && root.get("messagesByStars").isJsonObject()) {
            JsonObject msgs = root.getAsJsonObject("messagesByStars");
            for (int i = 1; i <= 5; i++) {
                String key = Integer.toString(i);
                if (!msgs.has(key)) {
                    continue;
                }
                JsonElement el = msgs.get(key);
                if (!el.isJsonArray()) {
                    continue;
                }
                for (JsonElement e : el.getAsJsonArray()) {
                    if (e.isJsonPrimitive()) {
                        String s = e.getAsString();
                        if (s != null && !s.isBlank()) {
                            fallbackMessages.add(s);
                        }
                    }
                }
            }
        }

        if (root.has("chanceByAlcohol") && root.get("chanceByAlcohol").isJsonArray()) {
            for (JsonElement el : root.getAsJsonArray("chanceByAlcohol")) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject o = el.getAsJsonObject();
                double min = o.has("min") ? o.get("min").getAsDouble() : 0;
                Double max = o.has("max") ? o.get("max").getAsDouble() : null;
                double chance = o.has("chance") ? o.get("chance").getAsDouble() : 0;
                rules.add(new AlcoholRule(min, max, chance, fallbackMessages));
            }
            return new AddonConfig(enabled, language, rules);
        }

        return new AddonConfig(enabled, language, rules);
    }

    private static List<String> readMessagesForLanguage(JsonObject byLang, String language) {
        if (byLang == null) {
            return List.of();
        }
        String lang = (language == null || language.isBlank()) ? "english" : language;
        List<String> out = readMessageArray(byLang, lang);
        if (!out.isEmpty()) {
            return out;
        }
        out = readMessageArray(byLang, "ukrainian");
        if (!out.isEmpty()) {
            return out;
        }
        out = readMessageArray(byLang, "english");
        return out;
    }

    private static List<String> readMessageArray(JsonObject byLang, String key) {
        if (byLang == null || key == null || key.isBlank() || !byLang.has(key)) {
            return List.of();
        }
        JsonElement el = byLang.get(key);
        if (el == null || !el.isJsonArray()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (JsonElement e : el.getAsJsonArray()) {
            if (!e.isJsonPrimitive()) {
                continue;
            }
            String s = e.getAsString();
            if (s != null && !s.isBlank()) {
                out.add(s);
            }
        }
        return out;
    }

    private static AddonConfig defaultConfig() {
        return new AddonConfig(true, "english", List.of());
    }

    private static double normalizeChance(double c) {
        if (c > 1.0) {
            c = c / 100.0;
        }
        if (c < 0) {
            return 0;
        }
        if (c > 1) {
            return 1;
        }
        return c;
    }

    public record AlcoholRule(double min, Double max, double chance, List<String> messages) {
        public boolean matches(double alcoholLevel) {
            return alcoholLevel >= min && (max == null || alcoholLevel < max);
        }

        public double normalizedChance() {
            return normalizeChance(chance);
        }
    }
}
