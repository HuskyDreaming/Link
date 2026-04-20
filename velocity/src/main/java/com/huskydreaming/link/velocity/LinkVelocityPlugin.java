package com.huskydreaming.link.velocity;

import com.google.inject.Inject;
import com.huskydreaming.link.common.LinkCommonPlugin;
import com.huskydreaming.link.common.dependency.DependencyManager;
import com.huskydreaming.link.common.dependency.DependencyManifest;
import com.huskydreaming.link.velocity.dependency.VelocityClassPathAppender;
import com.huskydreaming.link.velocity.initialization.VelocityInitializer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;

public class LinkVelocityPlugin {

    private final Logger logger = LoggerFactory.getLogger("Link");
    private final ProxyServer proxy;
    private final Path dataDirectory;
    private final LinkCommonPlugin common = new LinkCommonPlugin();
    private VelocityInitializer velocityInitializer;

    @Inject
    public LinkVelocityPlugin(ProxyServer proxy, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        // Download and load external libraries before anything else
        loadDependencies();

        velocityInitializer = new VelocityInitializer(proxy, dataDirectory, logger);
        velocityInitializer.initialize(common);
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        common.shutdown();
    }

    public void reload() {
        if (velocityInitializer != null) {
            velocityInitializer.reload();
        }
    }

    private void loadDependencies() {
        var appender = new VelocityClassPathAppender(proxy, this);
        var manager = new DependencyManager(dataDirectory, appender, logger);
        var manifest = DependencyManifest.load();

        // Velocity provides Adventure and SLF4J — no need to download them
        var deps = new ArrayList<>(manifest.core());
        deps.addAll(manifest.allDrivers());
        deps.addAll(manifest.jda());
        manager.loadAll(deps);
    }
}