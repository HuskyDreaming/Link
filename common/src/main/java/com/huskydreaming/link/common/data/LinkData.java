package com.huskydreaming.link.common.data;

import java.util.UUID;

public record LinkData(
        UUID uuid,
        long discordId,
        boolean linked,
        boolean rewardClaimed,
        long lastLinkedAt
) {}