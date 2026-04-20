package com.huskydreaming.link.velocity.dependency;

import com.huskydreaming.link.common.dependency.ClassPathAppender;
import com.velocitypowered.api.proxy.ProxyServer;

import java.nio.file.Path;

/**
 * Adds JARs to the Velocity plugin's classpath using the Velocity API.
 */
public class VelocityClassPathAppender implements ClassPathAppender {

    private final ProxyServer proxyServer;
    private final Object pluginInstance;

    public VelocityClassPathAppender(ProxyServer proxyServer, Object pluginInstance) {
        this.proxyServer = proxyServer;
        this.pluginInstance = pluginInstance;
    }

    @Override
    public void addJar(Path jar) {
        proxyServer.getPluginManager().addToClasspath(pluginInstance, jar);
    }
}

