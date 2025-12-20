package com.uuc.brewerychataddon;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class CarbonEntrypoint implements Consumer {
    @Override
    public void accept(Object carbonChat) {
        if (carbonChat == null) {
            return;
        }
        try {
            Object eventHandler = carbonChat.getClass().getMethod("eventHandler").invoke(carbonChat);

            Class<?> eventClass = loadEarlyChatEventClass();
            if (eventClass == null) {
                return;
            }

            Class<?> subscriberIntf = Class.forName("net.draycia.carbon.api.event.CarbonEventSubscriber");

            Object subscriber = Proxy.newProxyInstance(
                subscriberIntf.getClassLoader(),
                new Class<?>[]{subscriberIntf},
                new EarlyChatSubscriber()
            );

            Method subscribe = findSubscribeMethod(eventHandler.getClass(), subscriberIntf);
            if (subscribe == null) {
                BreweryChatAddonMod.LOGGER.warn("Carbon hook failed: subscribe method not found");
                return;
            }

            subscribe.invoke(eventHandler, eventClass, 0, false, subscriber);
            BreweryChatAddonMod.setCarbonHooked(true);
            BreweryChatAddonMod.LOGGER.info("Carbon hook active");
        } catch (Throwable ignored) {
            BreweryChatAddonMod.LOGGER.warn("Carbon hook failed", ignored);
        }
    }

    private static Method findSubscribeMethod(Class<?> handlerClass, Class<?> subscriberIntf) {
        for (Method m : handlerClass.getMethods()) {
            if (!m.getName().equals("subscribe")) {
                continue;
            }
            Class<?>[] p = m.getParameterTypes();
            if (p.length != 4) {
                continue;
            }
            if (p[0] != Class.class || p[1] != int.class || p[2] != boolean.class) {
                continue;
            }
            if (p[3] == subscriberIntf || subscriberIntf.isAssignableFrom(p[3])) {
                return m;
            }
        }
        return null;
    }

    private static Class<?> loadEarlyChatEventClass() {
        try {
            return Class.forName("net.draycia.carbon.common.event.events.CarbonEarlyChatEvent");
        } catch (Throwable ignored) {
        }
        try {
            return Class.forName("net.draycia.carbon.api.event.events.CarbonEarlyChatEvent");
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static final class EarlyChatSubscriber implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("on") && args != null && args.length == 1) {
                handleEarlyChatEvent(args[0]);
            }
            return null;
        }

        private void handleEarlyChatEvent(Object event) {
            AddonConfig cfg = BreweryChatAddonMod.config();
            if (cfg == null) {
                BreweryChatAddonMod.reloadConfig();
                cfg = BreweryChatAddonMod.config();
            }
            if (cfg == null || !cfg.enabled()) {
                return;
            }

            try {
                Object sender = event.getClass().getMethod("sender").invoke(event);
                UUID uuid = (UUID) sender.getClass().getMethod("uuid").invoke(sender);

                MinecraftServer server = BreweryChatAddonMod.server();
                if (server == null) {
                    return;
                }

                ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
                if (player == null) {
                    return;
                }

                String original = (String) event.getClass().getMethod("message").invoke(event);
                String replacement = ChatLogic.maybeReplace(player, original, cfg);
                if (replacement == null) {
                    return;
                }

                event.getClass().getMethod("message", String.class).invoke(event, replacement);
            } catch (Throwable ignored) {
            }
        }
    }
}
