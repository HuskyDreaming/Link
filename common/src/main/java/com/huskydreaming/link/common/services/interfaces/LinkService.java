package com.huskydreaming.link.common.services.interfaces;

import com.huskydreaming.link.common.data.LinkResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface LinkService {

    CompletableFuture<Void> initialize();

    void updateCooldown(long cooldownMillis);

    CompletableFuture<LinkResult> checkLinkStatus(UUID uuid);

    CompletableFuture<Long> getRemainingCooldown(UUID uuid);

    CompletableFuture<LinkResult> link(UUID uuid, long discordId);

    CompletableFuture<Long> unlink(UUID uuid);
}