package com.huskydreaming.link.folia;

import com.huskydreaming.link.common.LinkCommonPlugin;
import com.huskydreaming.link.common.configuration.YamlConfig;
import com.huskydreaming.link.folia.initialization.StandaloneInitializer;
import com.huskydreaming.link.folia.initialization.VelocityBridgeInitializer;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.LoggerFactory;

public class LinkFoliaPlugin extends JavaPlugin {

    private FoliaMode mode;
    private LinkCommonPlugin linkCommon;
    private StandaloneInitializer standaloneInit;
    private VelocityBridgeInitializer velocityBridgeInit;

    @Override
    public void onEnable() {
        var config = YamlConfig.loadAndMergeDefaults(getDataFolder().toPath(), "config.yml");
        mode = FoliaMode.fromString(config.getString("mode", "velocity-bridge"));
        getLogger().info("Starting in " + mode.name() + " mode.");

        if (mode == FoliaMode.STANDALONE) {
            initStandalone();
        } else {
            initVelocityBridge();
        }
    }

    @Override
    public void onDisable() {
        if (mode == FoliaMode.VELOCITY_BRIDGE && velocityBridgeInit != null) {
            velocityBridgeInit.shutdown();
        }

        if (mode == FoliaMode.STANDALONE && linkCommon != null) {
            linkCommon.shutdown();
        }
    }

    public void reload() {
        if (mode == FoliaMode.STANDALONE && standaloneInit != null) {
            standaloneInit.reload();
        }
    }

    private void initVelocityBridge() {
        velocityBridgeInit = new VelocityBridgeInitializer(this, LoggerFactory.getLogger("Link"));
        velocityBridgeInit.initialize();
    }

    private void initStandalone() {
        try {
            standaloneInit = new StandaloneInitializer(this);
            linkCommon = standaloneInit.initialize();
        } catch (Exception e) {
            getLogger().severe("Failed to initialise standalone mode: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}