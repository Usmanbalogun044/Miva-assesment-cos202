package model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the entire library catalogue and its users.
 * Composition: LibraryDatabase HAS-A list of items, a list of users, and a
 * reservation queue per item.
 */
public class LibraryDatabase {

    private final ArrayList<LibraryItem> items = new ArrayList<>();
    private final ArrayList<UserAccount> users = new ArrayList<>();

    // Reservation/waitlist queue keyed by item id - FIFO queue requirement
    private final Map<String, Queue<UserAccount>> reservationQueues = new HashMap<>();

    public ArrayList<LibraryItem> getItems() { return items; }
    public ArrayList<UserAccount> getUsers() { return users; }

    public void addItem(LibraryItem item) { items.add(item); }
    public boolean removeItem(LibraryItem item) { return items.remove(item); }
    public void addUser(UserAccount user) { users.add(user); }

    public LibraryItem findById(String id) {
        for (LibraryItem item : items) {
            if (item.getId().equals(id)) return item;
        }
        return null;
    }

    public UserAccount findUserById(String id) {
        for (UserAccount u : users) {
            if (u.getUserId().equals(id)) return u;
        }
        return null;
    }

    public Queue<UserAccount> getReservationQueue(String itemId) {
        return reservationQueues.computeIfAbsent(itemId, k -> new LinkedList<>());
    }

    public void enqueueReservation(String itemId, UserAccount user) {
        getReservationQueue(itemId).offer(user);
    }

    public UserAccount pollNextReservation(String itemId) {
        Queue<UserAccount> q = reservationQueues.get(itemId);
        return (q == null) ? null : q.poll();
    }

    /**
     * Polymorphic processing: applies to any LibraryItem regardless of its
     * concrete subclass. Demonstrates polymorphism requirement.
     */
    public static String processItem(LibraryItem item) {
        return item.describe() + " | " + (item.isAvailable() ? "On shelf" : "Checked out");
    }

    /**
     * Recursively computes the number of items belonging to a category
     * (recursive requirement #2 - "recursively compute total resource count
     * by category"). Walks the list index by index.
     */
    public int countByCategoryRecursive(String category) {
        return countByCategoryHelper(category, 0);
    }

    private int countByCategoryHelper(String category, int index) {
        if (index >= items.size()) {
            return 0; // base case
        }
        int match = items.get(index).getCategory().equalsIgnoreCase(category) ? 1 : 0;
        return match + countByCategoryHelper(category, index + 1); // recursive case
    }

    /** Returns a map of category -> count, used for the category distribution report. */
    public Map<String, Integer> categoryDistribution() {
        Map<String, Integer> dist = new HashMap<>();
        for (LibraryItem item : items) {
            dist.merge(item.getCategory(), 1, Integer::sum);
        }
        return dist;
    }

    public List<LibraryItem> mostBorrowedItems(int topN) {
        List<LibraryItem> copy = new ArrayList<>(items);
        copy.sort((a, b) -> b.getAccessCount() - a.getAccessCount());
        return copy.subList(0, Math.min(topN, copy.size()));
    }

    public List<UserAccount> usersWithOverdueItems() {
        List<UserAccount> result = new ArrayList<>();
        for (UserAccount u : users) {
            if (u.hasOverdueItems()) result.add(u);
        }
        return result;
    }
}
