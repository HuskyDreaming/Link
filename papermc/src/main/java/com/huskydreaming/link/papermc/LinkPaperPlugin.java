package com.huskydreaming.link.papermc;

import com.huskydreaming.link.common.LinkCommonPlugin;
import com.huskydreaming.link.common.configuration.YamlConfig;
import com.huskydreaming.link.papermc.initialization.StandaloneInitializer;
import com.huskydreaming.link.papermc.initialization.VelocityBridgeInitializer;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.LoggerFactory;

public class LinkPaperPlugin extends JavaPlugin {

    private PaperMode mode;
    private LinkCommonPlugin linkCommon;
    private VelocityBridgeInitializer velocityBridgeInit;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        var config = YamlConfig.load(getDataFolder().toPath(), "config.yml");
        mode = PaperMode.fromString(config.getString("mode", "velocity-bridge"));
        getLogger().info("Starting in " + mode.name() + " mode.");

        if (mode == PaperMode.STANDALONE) {
            initStandalone();
        } else {
            initVelocityBridge();
        }
    }

    @Override
    public void onDisable() {
        if (mode == PaperMode.VELOCITY_BRIDGE && velocityBridgeInit != null) {
            velocityBridgeInit.shutdown();
        }

        if (mode == PaperMode.STANDALONE && linkCommon != null) {
            linkCommon.shutdown();
        }
    }

    private void initVelocityBridge() {
        velocityBridgeInit = new VelocityBridgeInitializer(this, LoggerFactory.getLogger("Link"));
        velocityBridgeInit.initialize();
    }

    private void initStandalone() {
        try {
            linkCommon = new StandaloneInitializer(this).initialize();
        } catch (Exception e) {
            getLogger().severe("Failed to initialise standalone mode: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}

