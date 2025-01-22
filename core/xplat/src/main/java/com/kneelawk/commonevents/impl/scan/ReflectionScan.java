package com.kneelawk.commonevents.impl.scan;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.EventKey;
import com.kneelawk.commonevents.api.Listen;

public record ReflectionScan(EventKey key, ResourceLocation phase, Method method) {
    private static final Cache<Class<?>, List<ReflectionScan>> instanceMethods =
        CacheBuilder.newBuilder().concurrencyLevel(8).build();
    private static final Cache<Class<?>, List<ReflectionScan>> staticMethods =
        CacheBuilder.newBuilder().concurrencyLevel(8).build();

    public static List<ReflectionScan> scanInstance(Object listeners) {
        Class<?> clazz = listeners.getClass();

        try {
            return instanceMethods.get(clazz, () -> {
                ImmutableList.Builder<ReflectionScan> builder = ImmutableList.builder();

                for (Method m : clazz.getMethods()) {
                    if (Modifier.isPublic(m.getModifiers())) {
                        Listen l = m.getAnnotation(Listen.class);
                        if (l != null) {
                            builder.add(new ReflectionScan(EventKey.fromClass(l.value(), l.qualifier()),
                                ResourceLocation.parse(l.phase()), m));
                        }
                    }
                }

                return builder.build();
            });
        } catch (ExecutionException e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }

    public static List<ReflectionScan> scanStatic(Class<?> clazz) {
        try {
            return staticMethods.get(clazz, () -> {
                ImmutableList.Builder<ReflectionScan> builder = ImmutableList.builder();

                for (Method m : clazz.getDeclaredMethods()) {
                    if (Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())) {
                        Listen l = m.getAnnotation(Listen.class);
                        if (l != null) {
                            builder.add(new ReflectionScan(EventKey.fromClass(l.value(), l.qualifier()),
                                ResourceLocation.parse(l.phase()), m));
                        }
                    }
                }

                return builder.build();
            });
        } catch (ExecutionException e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }
}
