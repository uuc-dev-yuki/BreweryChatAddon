package com.uuc.brewerychataddon;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DrunkTracker {
    private final ConcurrentHashMap<UUID, Double> lastDrinkQuality = new ConcurrentHashMap<>();

    public void setLastDrinkQuality(UUID playerId, double quality) {
        if (playerId == null) {
            return;
        }
        lastDrinkQuality.put(playerId, quality);
    }

    public Double getLastDrinkQuality(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        return lastDrinkQuality.get(playerId);
    }

    public void clear(UUID playerId) {
        if (playerId == null) {
            return;
        }
        lastDrinkQuality.remove(playerId);
    }
}
