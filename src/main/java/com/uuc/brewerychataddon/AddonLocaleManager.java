package com.uuc.brewerychataddon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class AddonLocaleManager {
    private static final String LANG_RESOURCE_PREFIX = "brewerychataddon/languages/";

    private final Path languagesDir;

    AddonLocaleManager(Path configRoot) {
        this.languagesDir = configRoot.resolve("languages");
    }

    AddonLocale load(String language) {
        ensureDefaultsPresent(language);
        Path f = resolveLanguageFile(language);
        try (Reader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
            JsonObject o = JsonParser.parseReader(r).getAsJsonObject();
            return new AddonLocale(o);
        }
        catch (Throwable ignored) {
            return new AddonLocale(new JsonObject());
        }
    }

    private void ensureDefaultsPresent(String language) {
        try {
            Files.createDirectories(languagesDir);
        }
        catch (Throwable ignored) {
        }

        ensureLanguageFile("english");
        String lang = (language == null || language.isBlank()) ? "english" : language;
        ensureLanguageFile(lang);
    }

    private Path resolveLanguageFile(String language) {
        String lang = (language == null || language.isBlank()) ? "english" : language;
        return languagesDir.resolve(lang + ".json");
    }

    private void ensureLanguageFile(String language) {
        String lang = (language == null || language.isBlank()) ? "english" : language;
        Path outFile = resolveLanguageFile(lang);
        if (Files.exists(outFile)) {
            return;
        }
        String res = LANG_RESOURCE_PREFIX + lang + ".json";
        if (!copyResourceToFile(res, outFile)) {
            copyResourceToFile(LANG_RESOURCE_PREFIX + "english.json", outFile);
        }
    }

    private static boolean copyResourceToFile(String resourcePath, Path outFile) {
        if (resourcePath == null || outFile == null) {
            return false;
        }
        try (InputStream in = AddonLocaleManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                return false;
            }
            Files.createDirectories(outFile.getParent());
            Files.copy(in, outFile);
            return true;
        }
        catch (Throwable ignored) {
            return false;
        }
    }
}
