package com.huskydreaming.link.common.configuration;

import java.util.List;

/**
 * Immutable representation of the reward commands for a single Velocity backend server,
 * read from a {@code link.servers.<name>} block in {@code config.yml}.
 *
 * @param linkCommands   Console commands dispatched when a player links.
 * @param unlinkCommands Console commands dispatched when a player unlinks.
 */
public record ServerConfig(List<String> linkCommands, List<String> unlinkCommands) {
}