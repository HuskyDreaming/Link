package com.huskydreaming.link.common.discord;

import com.huskydreaming.link.common.configuration.DiscordConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.concurrent.TimeUnit;

public class DiscordClient {

    private final JDA jda;

    public DiscordClient(DiscordConfig discordConfig) {
        jda = JDABuilder.createDefault(discordConfig.token())
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .build();

        var commands = jda.updateCommands();
        commands.addCommands(
                Commands.slash("setup", "Creates an embed in the configured channel with instructions to link accounts")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .setContexts(InteractionContextType.GUILD)
        ).queue();
    }

    /**
     * Shuts down the JDA connection on a dedicated thread and joins it before returning.
     * <p>
     * Running on a separate thread keeps JDA's disconnect callbacks off the caller's thread
     * (important when called from the main Spigot thread in {@code onDisable}).
     * Joining ensures the plugin classloader is not closed before JDA's WebSocket threads
     * finish — otherwise a "zip file closed" error is thrown on shutdown.
     * </p>
     */
    public void close() {
        if (jda == null) return;

        var shutdownThread = new Thread(() -> {
            jda.shutdown();
            try {
                if (!jda.awaitShutdown(10, TimeUnit.SECONDS)) {
                    jda.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                jda.shutdownNow();
            }
        }, "link-jda-shutdown");

        shutdownThread.start();

        try {
            shutdownThread.join(11_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public JDA getJda() {
        return jda;
    }
}