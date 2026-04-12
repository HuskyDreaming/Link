package com.huskydreaming.link.common.discord.events;

import java.util.UUID;

public class LinkEvent {

    private final UUID uuid;
    private final long discordId;
    private final boolean rewardGranted;

    public LinkEvent(UUID uuid, long discordId, boolean rewardGranted) {
        this.uuid = uuid;
        this.discordId = discordId;
        this.rewardGranted = rewardGranted;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public long getDiscordId() {
        return discordId;
    }

    public boolean isRewardGranted() {
        return rewardGranted;
    }
}