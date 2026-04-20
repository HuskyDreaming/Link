package com.huskydreaming.link.common.configuration;

/**
 * Represents a single field inside a Discord embed.
 *
 * @param name   the field heading
 * @param value  the field body text
 * @param inline whether the field is displayed inline
 */
public record EmbedFieldConfig(String name, String value, boolean inline) {
}