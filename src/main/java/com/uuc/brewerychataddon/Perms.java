package com.uuc.brewerychataddon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public final class Perms {
    private static final String RELOAD_PERMISSION = "brewerychataddon.reload";
    private static final String BYPASS_PERMISSION = "brewerychataddon.bypass";

    private static volatile Class<?> permissionsClass;
    private static final ConcurrentHashMap<Class<?>, Method> checkMethods = new ConcurrentHashMap<>();

    private Perms() {
    }

    public static boolean canReload(ServerCommandSource source) {
        return check(source, RELOAD_PERMISSION, 2);
    }

    public static boolean hasBypass(ServerPlayerEntity player) {
        return check(player, BYPASS_PERMISSION, 2);
    }

    private static boolean check(Object subject, String permission, int fallbackOpLevel) {
        if (subject == null || permission == null) {
            return false;
        }

        Method m = findCheckMethod(subject.getClass());
        if (m != null) {
            try {
                Object out = m.invoke(null, subject, permission, fallbackOpLevel);
                if (out instanceof Boolean b) {
                    return b;
                }
            } catch (IllegalAccessException | InvocationTargetException ignored) {
            } catch (Throwable ignored) {
            }
        }

        if (subject instanceof ServerCommandSource source) {
            return source.hasPermissionLevel(fallbackOpLevel);
        }

        return false;
    }

    private static Method findCheckMethod(Class<?> subjectClass) {
        Method cached = checkMethods.get(subjectClass);
        if (cached != null) {
            return cached;
        }
        if (!checkMethods.containsKey(subjectClass)) {
            Method resolved = resolveCheck(subjectClass);
            if (resolved != null) {
                checkMethods.put(subjectClass, resolved);
                return resolved;
            }
            checkMethods.put(subjectClass, null);
        }
        return null;
    }

    private static Method resolveCheck(Class<?> subjectClass) {
        try {
            Class<?> perms = permissionsClass;
            if (perms == null) {
                perms = Class.forName("me.lucko.fabric.api.permissions.v0.Permissions");
                permissionsClass = perms;
            }
            for (Method m : perms.getMethods()) {
                if (!m.getName().equals("check")) {
                    continue;
                }
                Class<?>[] p = m.getParameterTypes();
                if (p.length != 3) {
                    continue;
                }
                if (p[1] != String.class || p[2] != int.class) {
                    continue;
                }
                if (p[0].isAssignableFrom(subjectClass)) {
                    return m;
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
