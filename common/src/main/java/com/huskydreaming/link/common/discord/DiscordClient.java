package com.huskydreaming.link.common.discord;

import com.huskydreaming.link.common.configuration.DiscordConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

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

    public void close() {
        if (jda != null) {
            jda.shutdown();
        }
    }

    public JDA getJda() {
        return jda;
    }
}