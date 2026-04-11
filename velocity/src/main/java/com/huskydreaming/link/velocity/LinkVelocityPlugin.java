package com.huskydreaming.link.velocity;

import com.google.inject.Inject;
import com.huskydreaming.link.common.LinkCommonPlugin;
import com.huskydreaming.link.velocity.initialization.VelocityInitializer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

@Plugin(
        id = "link",
        name = "Link",
        version = "1.0.0",
        description = "Link is a plugin that allows you to link your Minecraft account with your Discord account.",
        authors = "HuskyDreaming",
        url = "huskydreaming.com"
)
public class LinkVelocityPlugin extends LinkCommonPlugin {

    private final Logger logger = LoggerFactory.getLogger("Link");

    private final ProxyServer proxy;
    private final Path dataDirectory;
    private VelocityInitializer initializer;

    @Inject
    public LinkVelocityPlugin(ProxyServer proxy, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        initializer = new VelocityInitializer(proxy, dataDirectory, logger);
        initializer.initialize(this);
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        shutdown();
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public Logger getLogger() {
        return logger;
    }
}

