package com.uuc.brewerychataddon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

final class AddonLocale {
    private final JsonObject messages;

    AddonLocale(JsonObject messages) {
        this.messages = messages == null ? new JsonObject() : messages;
    }

    String get(String key, String def) {
        if (key == null || key.isBlank()) {
            return def;
        }
        try {
            JsonElement el = messages.get(key);
            if (el == null || !el.isJsonPrimitive()) {
                return def;
            }
            String s = el.getAsString();
            return s == null ? def : s;
        }
        catch (Throwable ignored) {
            return def;
        }
    }
}
