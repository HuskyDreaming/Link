package com.huskydreaming.link.common.repositories;

import com.huskydreaming.link.common.data.LinkData;

import java.util.Optional;
import java.util.UUID;

public interface LinkRepository {

    void createTable();

    Optional<LinkData> get(UUID uuid);

    void insert(UUID uuid, long discordId, long time);

    void updateLink(UUID uuid, long discordId, long time);

    boolean claimReward(UUID uuid);

    void setLinked(UUID uuid, boolean linked);
}