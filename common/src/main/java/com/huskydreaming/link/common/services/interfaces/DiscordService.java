package com.huskydreaming.link.common.services.interfaces;

import java.util.concurrent.CompletableFuture;

public interface DiscordService {
    CompletableFuture<Boolean> addRole(long discordId);

    CompletableFuture<Boolean> removeRole(long discordId);
}
