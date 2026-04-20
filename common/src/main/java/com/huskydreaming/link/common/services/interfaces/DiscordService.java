package com.huskydreaming.link.common.services.interfaces;

import com.huskydreaming.link.common.configuration.DiscordConfig;

import java.util.concurrent.CompletableFuture;

/**
 * Handles Discord role management for linked players.
 */
public interface DiscordService {

    void updateConfig(DiscordConfig config);

    CompletableFuture<Boolean> addRole(long discordId);

    CompletableFuture<Boolean> removeRole(long discordId);
}