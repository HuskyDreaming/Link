package com.huskydreaming.link.common.repositories;

import com.huskydreaming.link.common.data.LinkData;
import com.huskydreaming.link.common.database.DatabaseConnector;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class LinkRepositoryImpl implements LinkRepository {

    private final DatabaseConnector databaseConnector;

    public LinkRepositoryImpl(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    @Override
    public void createTable() {
        var query = """
            CREATE TABLE IF NOT EXISTS discord_links (
                uuid CHAR(36) PRIMARY KEY,
                discord_id BIGINT UNIQUE,
                linked BOOLEAN NOT NULL DEFAULT FALSE,
                reward_claimed BOOLEAN NOT NULL DEFAULT FALSE,
                last_linked_at BIGINT NOT NULL DEFAULT 0
            )
        """;

        try (var conn = databaseConnector.getConnection();
             var stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to create discord_links table", e);
        }
    }

    @Override
    public Optional<LinkData> get(UUID uuid) {
        var query = "SELECT * FROM discord_links WHERE uuid = ?";

        try (var conn = databaseConnector.getConnection();
             var stmt = conn.prepareStatement(query)) {

            stmt.setString(1, uuid.toString());

            try (var rs = stmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                return Optional.of(new LinkData(
                        uuid,
                        rs.getLong("discord_id"),
                        rs.getBoolean("linked"),
                        rs.getBoolean("reward_claimed"),
                        rs.getLong("last_linked_at")
                ));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to get link data for UUID: " + uuid, e);
        }
    }

    @Override
    public void insert(UUID uuid, long discordId, long time) {
        var query = """
            INSERT INTO discord_links (uuid, discord_id, linked, reward_claimed, last_linked_at)
            VALUES (?, ?, true, false, ?)
        """;

        try (var conn = databaseConnector.getConnection();
             var stmt = conn.prepareStatement(query)) {

            stmt.setString(1, uuid.toString());
            stmt.setLong(2, discordId);
            stmt.setLong(3, time);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Failed to insert link for UUID: " + uuid, e);
        }
    }

    @Override
    public void updateLink(UUID uuid, long discordId, long time) {
        var query = """
            UPDATE discord_links
            SET discord_id = ?, linked = true, last_linked_at = ?
            WHERE uuid = ?
        """;

        try (var conn = databaseConnector.getConnection();
             var stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, discordId);
            stmt.setLong(2, time);
            stmt.setString(3, uuid.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Failed to update link for UUID: " + uuid, e);
        }
    }

    @Override
    public boolean claimReward(UUID uuid) {
        var query = """
            UPDATE discord_links
            SET reward_claimed = true
            WHERE uuid = ? AND reward_claimed = false
        """;

        try (var conn = databaseConnector.getConnection();
             var stmt = conn.prepareStatement(query)) {

            stmt.setString(1, uuid.toString());
            return stmt.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RepositoryException("Failed to claim reward for UUID: " + uuid, e);
        }
    }

    @Override
    public void setLinked(UUID uuid, boolean linked) {
        var query = linked
                ? "UPDATE discord_links SET linked = ? WHERE uuid = ?"
                : "UPDATE discord_links SET linked = ?, discord_id = NULL WHERE uuid = ?";

        try (var conn = databaseConnector.getConnection();
             var stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, linked);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Failed to set linked=" + linked + " for UUID: " + uuid, e);
        }
    }
}