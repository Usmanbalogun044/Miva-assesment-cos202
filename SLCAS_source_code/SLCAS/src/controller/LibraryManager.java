package controller;

import model.LibraryItem;
import model.LibraryDatabase;
import model.UserAccount;
import utils.IDGenerator;

import java.util.Stack;

/**
 * Central controller that mediates between the GUI and the model layer.
 * Owns the undo Stack and the fixed-size "most frequently accessed" cache
 * (a plain array, per the API requirement), on top of the LibraryDatabase.
 */
public class LibraryManager {

    private final LibraryDatabase database = new LibraryDatabase();
    private final Stack<AdminAction> undoStack = new Stack<>();

    // Fixed-size array cache of the most frequently accessed items (MFU cache)
    private static final int CACHE_SIZE = 5;
    private final LibraryItem[] frequentItemsCache = new LibraryItem[CACHE_SIZE];

    public LibraryDatabase getDatabase() { return database; }
    public Stack<AdminAction> getUndoStack() { return undoStack; }

    public LibraryItem addItem(LibraryItem item) {
        database.addItem(item);
        undoStack.push(new AdminAction(AdminAction.Type.ADD, item));
        return item;
    }

    public boolean deleteItem(LibraryItem item) {
        boolean removed = database.removeItem(item);
        if (removed) {
            undoStack.push(new AdminAction(AdminAction.Type.DELETE, item));
        }
        return removed;
    }

    /** Undoes the most recent admin action (add or delete) using the Stack. */
    public String undoLastAction() {
        if (undoStack.isEmpty()) return "Nothing to undo.";
        AdminAction last = undoStack.pop();
        if (last.getType() == AdminAction.Type.ADD) {
            database.removeItem(last.getItem());
            return "Undo: removed \"" + last.getItem().getTitle() + "\" (was added).";
        } else {
            database.addItem(last.getItem());
            return "Undo: restored \"" + last.getItem().getTitle() + "\" (was deleted).";
        }
    }

    /** Records an access (view/borrow) of an item and updates the MFU cache array. */
    public void recordAccess(LibraryItem item) {
        item.incrementAccessCount();
        updateFrequentCache(item);
    }

    private void updateFrequentCache(LibraryItem item) {
        // If already cached, nothing to insert.
        for (LibraryItem cached : frequentItemsCache) {
            if (cached == item) return;
        }
        // Find an empty slot or the slot holding the least-accessed item.
        int weakestIndex = 0;
        int weakestCount = Integer.MAX_VALUE;
        for (int i = 0; i < CACHE_SIZE; i++) {
            if (frequentItemsCache[i] == null) {
                weakestIndex = i;
                weakestCount = -1;
                break;
            }
            if (frequentItemsCache[i].getAccessCount() < weakestCount) {
                weakestCount = frequentItemsCache[i].getAccessCount();
                weakestIndex = i;
            }
        }
        if (item.getAccessCount() > weakestCount) {
            frequentItemsCache[weakestIndex] = item;
        }
    }

    public LibraryItem[] getFrequentItemsCache() { return frequentItemsCache; }

    public String generateNextItemId() { return IDGenerator.nextItemId(); }
    public String generateNextUserId() { return IDGenerator.nextUserId(); }

    public UserAccount getOrCreateUser(String userId, String name) {
        UserAccount existing = database.findUserById(userId);
        if (existing != null) return existing;
        UserAccount created = new UserAccount(userId, name);
        database.addUser(created);
        return created;
    }
}
