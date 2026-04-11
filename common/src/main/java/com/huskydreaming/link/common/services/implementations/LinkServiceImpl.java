package com.huskydreaming.link.common.services.implementations;

import com.huskydreaming.link.common.data.LinkResult;
import com.huskydreaming.link.common.repositories.LinkRepository;
import com.huskydreaming.link.common.services.interfaces.LinkService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class LinkServiceImpl implements LinkService {

    private final LinkRepository repository;
    private final ExecutorService executor;

    private static final long COOLDOWN = TimeUnit.HOURS.toMillis(1);

    public LinkServiceImpl(LinkRepository repository, ExecutorService executor) {
        this.repository = repository;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(repository::createTable, executor);
    }

    @Override
    public CompletableFuture<LinkResult> checkLinkStatus(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            var dataOpt = repository.get(uuid);
            if (dataOpt.isEmpty()) {
                return LinkResult.OK;
            }

            var data = dataOpt.get();
            if (data.linked()) {
                return LinkResult.ALREADY_LINKED;
            }

            var now = System.currentTimeMillis();
            if (now - data.lastLinkedAt() < COOLDOWN) {
                return LinkResult.COOLDOWN;
            }

            return LinkResult.OK;
        }, executor);
    }

    @Override
    public CompletableFuture<Long> getRemainingCooldown(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            var dataOpt = repository.get(uuid);
            if (dataOpt.isEmpty()) return 0L;

            var data = dataOpt.get();
            long now = System.currentTimeMillis();
            long remaining = COOLDOWN - (now - data.lastLinkedAt());
            return Math.max(0, remaining);
        }, executor);
    }

    @Override
    public CompletableFuture<LinkResult> link(UUID uuid, long discordId) {
        return CompletableFuture.supplyAsync(() -> {
            var now = System.currentTimeMillis();
            var existing = repository.get(uuid);

            if (existing.isEmpty()) {
                repository.insert(uuid, discordId, now);
                repository.claimReward(uuid);
                return LinkResult.SUCCESS_REWARD;
            }

            var data = existing.get();
            if (data.linked()) {
                return LinkResult.ALREADY_LINKED;
            }

            if (now - data.lastLinkedAt() < COOLDOWN) {
                return LinkResult.COOLDOWN;
            }

            repository.updateLink(uuid, discordId, now);

            var reward = repository.claimReward(uuid);
            return reward ? LinkResult.SUCCESS_REWARD : LinkResult.SUCCESS_NO_REWARD;
        }, executor);
    }

    @Override
    public CompletableFuture<Long> unlink(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            var dataOpt = repository.get(uuid);
            if (dataOpt.isEmpty()) {
                return 0L;
            }

            repository.setLinked(uuid, false);
            var data = dataOpt.get();
            return data.discordId();
        }, executor);
    }
}