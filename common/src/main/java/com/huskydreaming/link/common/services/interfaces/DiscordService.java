package com.huskydreaming.link.common.services.interfaces;

import java.util.concurrent.CompletableFuture;

/**
 * Handles Discord role management for linked players.
 */
public interface DiscordService {

    /**
     * Assigns the configured link role to the Discord member identified by {@code discordId}.
     *
     * @return a future that resolves to {@code true} if the role was assigned successfully,
     *         or {@code false} if the guild, role, or member could not be resolved
     */
    CompletableFuture<Boolean> addRole(long discordId);

    /**
     * Removes the configured link role from the Discord member identified by {@code discordId}.
     *
     * @return a future that resolves to {@code true} if the role was removed successfully,
     *         or {@code false} if the guild, role, or member could not be resolved
     */
    CompletableFuture<Boolean> removeRole(long discordId);
}