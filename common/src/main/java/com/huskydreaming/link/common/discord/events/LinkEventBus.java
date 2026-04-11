package com.huskydreaming.link.common.discord.events;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

public class LinkEventBus {

    private final Map<Class<?>, List<RegisteredListener>> listeners = new ConcurrentHashMap<>();
    private final Executor executor;

    public LinkEventBus(Executor executor) {
        this.executor = executor;
    }

    // 🔥 Register object with annotated methods
    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {

            if (!method.isAnnotationPresent(Subscribe.class)) continue;

            var params = method.getParameterTypes();
            if (params.length != 1) continue;

            Class<?> eventType = params[0];

            method.setAccessible(true);

            listeners
                    .computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                    .add(new RegisteredListener(listener, method));
        }
    }

    public CompletableFuture<Void> fire(Object event) {
        var handlers = listeners.getOrDefault(event.getClass(), List.of());

        var futures = handlers.stream()
                .map(handler -> CompletableFuture.runAsync(() -> {
                    try {
                        handler.method.invoke(handler.instance, event);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executor))
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures);
    }

    private record RegisteredListener(Object instance, Method method) {}
}