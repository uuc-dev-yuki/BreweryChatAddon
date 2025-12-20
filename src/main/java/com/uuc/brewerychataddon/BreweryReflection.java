package com.uuc.brewerychataddon;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public final class BreweryReflection {
    private static volatile Method drinkUtilsGetQuality;
    private static volatile Method alcoholManagerOf;
    private static volatile Field alcoholManagerAlcoholLevel;
    private static volatile Field alcoholManagerQuality;

    private BreweryReflection() {
    }

    public static double getDrinkQuality(ItemStack stack) {
        try {
            Method m = drinkUtilsGetQuality;
            if (m == null) {
                Class<?> c = Class.forName("eu.pb4.brewery.drink.DrinkUtils");
                m = c.getMethod("getQuality", ItemStack.class);
                drinkUtilsGetQuality = m;
            }
            Object out = m.invoke(null, stack);
            if (out instanceof Double d) {
                return d;
            }
            if (out instanceof Number n) {
                return n.doubleValue();
            }
        } catch (Throwable ignored) {
        }
        return -1;
    }

    public static double getAlcoholLevel(LivingEntity entity) {
        try {
            Object manager = alcoholManager(entity);
            if (manager == null) {
                return 0;
            }
            Field f = alcoholManagerAlcoholLevel;
            if (f == null) {
                f = manager.getClass().getField("alcoholLevel");
                alcoholManagerAlcoholLevel = f;
            }
            Object out = f.get(manager);
            if (out instanceof Double d) {
                return d;
            }
            if (out instanceof Number n) {
                return n.doubleValue();
            }
        } catch (Throwable ignored) {
        }
        return 0;
    }

    public static double getAlcoholQuality(LivingEntity entity) {
        try {
            Object manager = alcoholManager(entity);
            if (manager == null) {
                return -1;
            }
            Field f = alcoholManagerQuality;
            if (f == null) {
                f = manager.getClass().getField("quality");
                alcoholManagerQuality = f;
            }
            Object out = f.get(manager);
            if (out instanceof Double d) {
                return d;
            }
            if (out instanceof Number n) {
                return n.doubleValue();
            }
        } catch (Throwable ignored) {
        }
        return -1;
    }

    private static Object alcoholManager(LivingEntity entity) {
        try {
            Method m = alcoholManagerOf;
            if (m == null) {
                Class<?> c = Class.forName("eu.pb4.brewery.drink.AlcoholManager");
                m = c.getMethod("of", LivingEntity.class);
                alcoholManagerOf = m;
            }
            return m.invoke(null, entity);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
