package com.uuc.brewerychataddon;

import java.nio.file.Path;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BreweryChatAddonMod implements ModInitializer {
    public static final String MOD_ID = "brewerychataddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static volatile MinecraftServer server;
    private static volatile boolean carbonHooked;

    private static final DrunkTracker DRUNK_TRACKER = new DrunkTracker();
    private static volatile AddonConfig config;
    private static volatile AddonLocale locale;

    @Override
    public void onInitialize() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("BreweryChatAddon");
        config = AddonConfig.load(configDir);

        AddonLocaleManager localeManager = new AddonLocaleManager(configDir);
        locale = localeManager.load(config == null ? null : config.language());

        AddonLocale l0 = locale;
        boolean enabled = config != null && config.enabled();
        String lang = config == null ? "english" : config.language();
        String loadedMsg = l0 == null
            ? "Loaded (enabled={enabled}, language={language})"
            : l0.get("log.loaded", "Loaded (enabled={enabled}, language={language})");
        LOGGER.info(format(loadedMsg, enabled, lang));

        ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, s) -> DRUNK_TRACKER.clear(handler.player.getUuid()));

        registerVanillaChatListener();

        if (FabricLoader.getInstance().isModLoaded("carbonchat")) {
            AddonLocale l = locale;
            String msg = l == null
                ? "Carbon Chat detected; waiting for carbonchat entrypoint hook"
                : l.get("log.carbon.detected", "Carbon Chat detected; waiting for carbonchat entrypoint hook");
            LOGGER.info(msg);
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("brreload")
                .requires(Perms::canReload)
                .executes(ctx -> {
                    reloadConfig();
                    AddonLocale l = locale;
                    String msg = l == null
                        ? "[BreweryChatAddon] Reloaded"
                        : l.get("command.reload.success", "[BreweryChatAddon] Reloaded");
                    ctx.getSource().sendFeedback(() -> Text.literal(msg), false);
                    return 1;
                })
            );
        });
    }

    public static MinecraftServer server() {
        return server;
    }

    public static void setCarbonHooked(boolean hooked) {
        carbonHooked = hooked;
    }

    public static boolean isCarbonHooked() {
        return carbonHooked;
    }

    public static DrunkTracker drunkTracker() {
        return DRUNK_TRACKER;
    }

    public static AddonConfig config() {
        return config;
    }

    public static void reloadConfig() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("BreweryChatAddon");
        config = AddonConfig.load(configDir);

        AddonLocaleManager localeManager = new AddonLocaleManager(configDir);
        locale = localeManager.load(config == null ? null : config.language());
    }

    private static void registerVanillaChatListener() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            if (carbonHooked) {
                return true;
            }

            AddonConfig cfg = config;
            if (cfg == null || !cfg.enabled()) {
                return true;
            }

            if (sender == null) {
                return true;
            }

            String original = message.getContent().getString();
            String replacement = ChatLogic.maybeReplace(sender, original, cfg);
            if (replacement == null) {
                return true;
            }

            MinecraftServer s = server;
            if (s == null) {
                return true;
            }

            Text out = Text.translatable("chat.type.text", sender.getDisplayName(), Text.literal(replacement));
            s.getPlayerManager().broadcast(out, false);

            AddonLocale l = locale;
            String msg = l == null
                ? "Replaced chat from {player}: '{from}' -> '{to}'"
                : l.get("log.replaced", "Replaced chat from {player}: '{from}' -> '{to}'");
            String player = sender.getName().getString();
            LOGGER.info(msg.replace("{player}", player).replace("{from}", original).replace("{to}", replacement));
            return false;
        });
    }

    private static String format(String template, boolean enabled, String language) {
        String t = template == null ? "" : template;
        return t.replace("{enabled}", String.valueOf(enabled)).replace("{language}", language == null ? "" : language);
    }
}
