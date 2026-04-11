package com.huskydreaming.link.common.data;

public enum LinkResult {
    OK,                 // Used for Velocity pre-checks
    SUCCESS_REWARD,     // Linked, Discord layer should give a reward
    SUCCESS_NO_REWARD,  // Linked (re-linked), Discord layer should NOT give a reward
    ALREADY_LINKED,
    COOLDOWN
}