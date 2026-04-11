package com.huskydreaming.link.common.discord.events;

import java.util.UUID;

public class LinkEvent {

    private final UUID uuid;
    private final long discordId;

    public LinkEvent(UUID uuid, long discordId) {
        this.uuid = uuid;
        this.discordId = discordId;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public long getDiscordId() {
        return discordId;
    }
}