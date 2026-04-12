package com.huskydreaming.link.common.services.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.huskydreaming.link.common.services.interfaces.CodeService;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CodeServiceImpl implements CodeService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 20;

    private static final long EXPIRY_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private final Cache<String, UUID> codeToPlayer = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    private final Cache<UUID, String> playerToCode = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    private final SecureRandom random = new SecureRandom();

    @Override
    public boolean hasCode(UUID uuid) {
        return playerToCode.getIfPresent(uuid) != null;
    }

    @Override
    public long getRemainingCodeTime(UUID uuid) {
        var policyOpt = playerToCode.policy().expireAfterWrite();
        if (policyOpt.isEmpty()) return 0L;
        var age = policyOpt.get().ageOf(uuid, TimeUnit.MILLISECONDS);
        return age.isPresent() ? Math.max(0L, EXPIRY_MILLIS - age.getAsLong()) : 0L;
    }

    @Override
    public String generate(UUID uuid) {
        for (var attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            var code = generateCode();

            var existing = playerToCode.asMap().putIfAbsent(uuid, code);
            if (existing != null) {
                return existing;
            }

            var displaced = codeToPlayer.asMap().putIfAbsent(code, uuid);
            if (displaced == null) {
                return code;
            }

            playerToCode.asMap().remove(uuid, code);
        }

        throw new IllegalStateException(
                "Could not generate a unique code after " + MAX_ATTEMPTS + " attempts — cache may be full"
        );
    }

    @Override
    public Optional<UUID> consume(String code) {
        var normalized = code.trim().toUpperCase();
        var uuid = codeToPlayer.asMap().remove(normalized);
        if (uuid == null) {
            return Optional.empty();
        }

        playerToCode.invalidate(uuid);

        return Optional.of(uuid);
    }

    private String generateCode() {
        var chars = new char[CODE_LENGTH];
        for (var i = 0; i < CODE_LENGTH; i++) {
            chars[i] = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        }
        return new String(chars);
    }
}