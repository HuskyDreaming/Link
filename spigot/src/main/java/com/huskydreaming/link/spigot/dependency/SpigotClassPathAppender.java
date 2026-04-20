package com.huskydreaming.link.spigot.dependency;

import com.huskydreaming.link.common.dependency.ClassPathAppender;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

/**
 * Adds JARs to the Spigot plugin's classloader using {@link MethodHandles}
 * to access {@link URLClassLoader#addURL}.
 * <p>
 * This is the standard approach used by LuckPerms, Essentials, and many other
 * plugins on Java 16+.
 */
public class SpigotClassPathAppender implements ClassPathAppender {

    private final URLClassLoader classLoader;
    private final MethodHandle addUrlHandle;

    public SpigotClassPathAppender(ClassLoader classLoader) {
        if (!(classLoader instanceof URLClassLoader urlCl)) {
            throw new IllegalArgumentException("Plugin classloader is not a URLClassLoader: " + classLoader.getClass().getName());
        }
        this.classLoader = urlCl;

        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(URLClassLoader.class, MethodHandles.lookup());
            addUrlHandle = lookup.findVirtual(URLClassLoader.class, "addURL", MethodType.methodType(void.class, URL.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialise classpath appender via MethodHandles", e);
        }
    }

    @Override
    public void addJar(Path jar) {
        try {
            addUrlHandle.invoke(classLoader, jar.toUri().toURL());
        } catch (Throwable e) {
            throw new RuntimeException("Failed to add JAR to classpath: " + jar, e);
        }
    }
}

