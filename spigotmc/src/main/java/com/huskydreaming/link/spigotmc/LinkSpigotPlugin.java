package com.huskydreaming.link.spigotmc;

import com.huskydreaming.link.common.LinkCommonPlugin;
import com.huskydreaming.link.common.configuration.YamlConfig;
import com.huskydreaming.link.spigotmc.initialization.StandaloneInitializer;
import com.huskydreaming.link.spigotmc.initialization.VelocityBridgeInitializer;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.LoggerFactory;

public class LinkSpigotPlugin extends JavaPlugin {

    private SpigotMode mode;
    private LinkCommonPlugin linkCommon;
    private VelocityBridgeInitializer velocityBridgeInit;

    @Override
    public void onEnable() {
        var config = YamlConfig.loadAndMergeDefaults(getDataFolder().toPath(), "config.yml");
        mode = SpigotMode.fromString(config.getString("mode", "velocity-bridge"));
        getLogger().info("Starting in " + mode.name() + " mode.");

        if (mode == SpigotMode.STANDALONE) {
            initStandalone();
        } else {
            initVelocityBridge();
        }
    }

    @Override
    public void onDisable() {
        if (mode == SpigotMode.VELOCITY_BRIDGE && velocityBridgeInit != null) {
            velocityBridgeInit.shutdown();
        }

        if (mode == SpigotMode.STANDALONE && linkCommon != null) {
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
