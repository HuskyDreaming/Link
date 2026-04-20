package com.huskydreaming.link.velocity;

import com.google.inject.Inject;
import com.huskydreaming.link.common.LinkCommonPlugin;
import com.huskydreaming.link.velocity.initialization.VelocityInitializer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class LinkVelocityPlugin extends LinkCommonPlugin {

    private final Logger logger = LoggerFactory.getLogger("Link");
    private final ProxyServer proxy;
    private final Path dataDirectory;
    private VelocityInitializer velocityInitializer;

    @Inject
    public LinkVelocityPlugin(ProxyServer proxy, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        velocityInitializer = new VelocityInitializer(proxy, dataDirectory, logger);
        velocityInitializer.initialize(this);
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        shutdown();
    }

    public void reload() {
        if (velocityInitializer != null) {
            velocityInitializer.reload();
        }
    }
}