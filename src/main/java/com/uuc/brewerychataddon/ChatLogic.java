package com.uuc.brewerychataddon;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ChatLogic {
    private ChatLogic() {
    }

    public static String maybeReplace(ServerPlayerEntity player, String original, AddonConfig cfg) {
        if (player == null || cfg == null || !cfg.enabled()) {
            return null;
        }

        if (Perms.hasBypass(player)) {
            return null;
        }

        double alcoholLevel = BreweryReflection.getAlcoholLevel(player);
        if (alcoholLevel <= 0) {
            return null;
        }

        AddonConfig.AlcoholRule rule = cfg.ruleFor(alcoholLevel);
        if (rule == null) {
            return null;
        }

        double chance = rule.normalizedChance();
        if (chance <= 0) {
            return null;
        }

        if (ThreadLocalRandom.current().nextDouble() >= chance) {
            return null;
        }

        List<String> messages = rule.messages();
        if (messages.isEmpty()) {
            return null;
        }

        return messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
    }
}
