package utils;

import java.util.concurrent.atomic.AtomicInteger;

/** Generates simple sequential, prefixed IDs for items and users. */
public class IDGenerator {

    private static final AtomicInteger itemCounter = new AtomicInteger(1000);
    private static final AtomicInteger userCounter = new AtomicInteger(1);

    public static String nextItemId() {
        return "ITM" + itemCounter.getAndIncrement();
    }

    public static String nextUserId() {
        return "USR" + userCounter.getAndIncrement();
    }

    /** Allows the loader to keep IDs unique after reading persisted data. */
    public static void reportExistingItemId(String id) {
        try {
            int n = Integer.parseInt(id.replaceAll("[^0-9]", ""));
            itemCounter.updateAndGet(cur -> Math.max(cur, n + 1));
        } catch (NumberFormatException ignored) { }
    }

    public static void reportExistingUserId(String id) {
        try {
            int n = Integer.parseInt(id.replaceAll("[^0-9]", ""));
            userCounter.updateAndGet(cur -> Math.max(cur, n + 1));
        } catch (NumberFormatException ignored) { }
    }
}
