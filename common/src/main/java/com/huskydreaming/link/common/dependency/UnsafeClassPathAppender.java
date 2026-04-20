package com.huskydreaming.link.common.dependency;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

/**
 * Adds JARs to a {@link URLClassLoader} by obtaining a trusted {@link MethodHandles.Lookup}
 * via {@code sun.misc.Unsafe} (accessed reflectively to avoid deprecation warnings).
 * <p>
 * This is the standard approach used by LuckPerms, Essentials, and other plugins on Java 16+
 * where the module system prevents direct access to {@link URLClassLoader#addURL}.
 */
public class UnsafeClassPathAppender implements ClassPathAppender {

    private final URLClassLoader classLoader;
    private final MethodHandle addUrlHandle;

    public UnsafeClassPathAppender(ClassLoader classLoader) {
        if (!(classLoader instanceof URLClassLoader urlCl)) {
            throw new IllegalArgumentException("Plugin classloader is not a URLClassLoader: " + classLoader.getClass().getName());
        }
        this.classLoader = urlCl;

        try {
            // Access Unsafe reflectively to avoid compile-time deprecation warnings
            var unsafeClass = Class.forName("sun.misc.Unsafe");
            var theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            var unsafe = theUnsafe.get(null);

            // Use Unsafe to read the trusted IMPL_LOOKUP from MethodHandles.Lookup
            var implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            var staticFieldOffset = unsafeClass.getMethod("staticFieldOffset", Field.class);
            var getObject = unsafeClass.getMethod("getObject", Object.class, long.class);

            var offset = (long) staticFieldOffset.invoke(unsafe, implLookupField);
            MethodHandles.Lookup trustedLookup = (MethodHandles.Lookup) getObject.invoke(unsafe, MethodHandles.Lookup.class, offset);

            addUrlHandle = trustedLookup.findVirtual(URLClassLoader.class, "addURL", MethodType.methodType(void.class, URL.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialise classpath appender", e);
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
