package com.huskydreaming.link.common.services.interfaces;

import java.util.Optional;
import java.util.UUID;

/**
 * Manages short-lived one-time auth codes used during the account-linking flow.
 * Codes expire automatically after 5 minutes.
 */
public interface CodeService {

    /**
     * Returns {@code true} if the player already has a pending code that has not yet expired.
     */
    boolean hasCode(UUID uuid);

    /**
     * Returns the number of milliseconds remaining before the player's current code expires.
     * Returns {@code 0} if the player has no active code.
     */
    long getRemainingCodeTime(UUID uuid);

    /**
     * Generates and returns a new auth code for the given player, or returns their existing
     * code if one is still active.
     *
     * @throws IllegalStateException if a unique code cannot be generated after the maximum number of attempts
     */
    String generate(UUID uuid);

    /**
     * Validates {@code code} and, if valid, removes it and returns the owning player's UUID.
     * Returns {@link Optional#empty()} if the code is unknown or expired.
     */
    Optional<UUID> consume(String code);
}