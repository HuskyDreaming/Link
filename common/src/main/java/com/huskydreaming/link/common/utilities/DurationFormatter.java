package com.huskydreaming.link.common.utilities;

/**
 * Utility for formatting millisecond durations into a human-readable string.
 * <p>
 * Examples:
 * <pre>
 *   DurationFormatter.format(3_661_000) → "1h 1m 1s"
 *   DurationFormatter.format(  90_000) → "1m 30s"
 *   DurationFormatter.format(  45_000) → "45s"
 * </pre>
 */
public final class DurationFormatter {

    private DurationFormatter() {}

    /**
     * Formats a duration in milliseconds into a compact {@code h m s} string.
     *
     * @param millis duration in milliseconds (non-negative)
     * @return human-readable duration, e.g. {@code "2h 5m 3s"} or {@code "30s"}
     */
    public static String format(long millis) {
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) return hours + "h " + minutes + "m " + seconds + "s";
        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }
}

