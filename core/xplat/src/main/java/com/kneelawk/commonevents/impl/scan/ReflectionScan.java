package com.kneelawk.commonevents.impl.scan;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.EventKey;
import com.kneelawk.commonevents.api.Listen;

public record ReflectionScan(EventKey key, ResourceLocation phase, Method method) {
    public static List<ReflectionScan> scanInstance(Object listeners) {
        List<ReflectionScan> results = new ArrayList<>();

        Class<?> clazz = listeners.getClass();
        for (Method m : clazz.getMethods()) {
            if (Modifier.isPublic(m.getModifiers())) {
                Listen l = m.getAnnotation(Listen.class);
                if (l != null) {
                    results.add(
                        new ReflectionScan(EventKey.fromClass(l.value(), l.qualifier()),
                            ResourceLocation.parse(l.phase()),
                            m));
                }
            }
        }

        return results;
    }

    public static List<ReflectionScan> scanStatic(Class<?> clazz) {
        List<ReflectionScan> results = new ArrayList<>();

        for (Method m : clazz.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())) {
                Listen l = m.getAnnotation(Listen.class);
                if (l != null) {
                    results.add(
                        new ReflectionScan(EventKey.fromClass(l.value(), l.qualifier()),
                            ResourceLocation.parse(l.phase()),
                            m));
                }
            }
        }

        return results;
    }
}
