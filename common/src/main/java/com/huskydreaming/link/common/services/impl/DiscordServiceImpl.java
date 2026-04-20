package com.huskydreaming.link.common.services.impl;

import com.huskydreaming.link.common.configuration.DiscordConfig;
import com.huskydreaming.link.common.discord.DiscordContext;
import com.huskydreaming.link.common.discord.DiscordClient;
import com.huskydreaming.link.common.services.interfaces.DiscordService;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DiscordServiceImpl implements DiscordService {

    private volatile DiscordConfig discordConfig;
    private final Logger logger;
    private final JDA jda;

    public DiscordServiceImpl(DiscordConfig discordConfig, DiscordClient discordClient, Logger logger) {
        this.discordConfig = discordConfig;
        this.jda = discordClient.getJda();
        this.logger = logger;
    }

    @Override
    public void updateConfig(DiscordConfig config) {
        this.discordConfig = config;
    }

    @Override
    public CompletableFuture<Boolean> addRole(long discordId) {
        return withContext(discordId,
                ctx -> ctx.guild().addRoleToMember(ctx.member(), ctx.role()).queue()
        );
    }

    @Override
    public CompletableFuture<Boolean> removeRole(long discordId) {
        return withContext(discordId,
                ctx -> ctx.guild().removeRoleFromMember(ctx.member(), ctx.role()).queue()
        );
    }

    private CompletableFuture<Boolean> withContext(long discordId, Consumer<DiscordContext> action) {
        return resolveContext(discordId).thenApply(context -> {
            if (context == null) return false;

            action.accept(context);
            return true;
        });
    }

    private CompletableFuture<DiscordContext> resolveContext(long discordId) {
        var guild = jda.getGuildById(discordConfig.guildId());
        if (guild == null) {
            logger.error("Discord Guild Not Found!");
            return CompletableFuture.completedFuture(null);
        }

        var role = guild.getRoleById(discordConfig.roleId());
        if (role == null) {
            logger.error("Discord Role Not Found!");
            return CompletableFuture.completedFuture(null);
        }

        return guild.retrieveMemberById(discordId)
                .submit()
                .thenApply(member -> new DiscordContext(guild, member, role))
                .exceptionally(ex -> {
                    logger.error("Discord Member Not Found!", ex);
                    return null;
                });
    }
}