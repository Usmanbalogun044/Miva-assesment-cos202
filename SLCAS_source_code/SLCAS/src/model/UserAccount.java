package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a library patron. Uses composition: a UserAccount HAS-A
 * collection of currently borrowed items and a textual history log.
 */
public class UserAccount implements Serializable {

    public static final int LOAN_PERIOD_DAYS = 14;
    public static final double FINE_PER_DAY = 50.0; // currency units per overdue day

    private String userId;
    private String name;
    private final List<LibraryItem> borrowedItems = new ArrayList<>();
    private final Map<String, LocalDate> borrowDates = new HashMap<>(); // itemId -> date borrowed
    private final List<String> history = new ArrayList<>();
    private double outstandingFine;

    public UserAccount(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.outstandingFine = 0.0;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public List<LibraryItem> getBorrowedItems() { return borrowedItems; }
    public List<String> getHistory() { return history; }
    public double getOutstandingFine() { return outstandingFine; }

    public void addBorrowedItem(LibraryItem item) {
        borrowedItems.add(item);
        borrowDates.put(item.getId(), LocalDate.now());
        history.add("Borrowed \"" + item.getTitle() + "\" on " + LocalDate.now());
    }

    public void removeBorrowedItem(LibraryItem item) {
        borrowedItems.remove(item);
        borrowDates.remove(item.getId());
        history.add("Returned \"" + item.getTitle() + "\" on " + LocalDate.now());
    }

    public LocalDate getBorrowDate(LibraryItem item) {
        return borrowDates.get(item.getId());
    }

    public void addFine(double amount) {
        outstandingFine += amount;
    }

    public void clearFine() {
        outstandingFine = 0.0;
    }

    /**
     * Recursively computes the total overdue fine across all currently
     * borrowed items (recursive requirement #4 - "recursive overdue charge
     * computation"). Processes the borrowed list head-then-tail.
     */
    public double computeOverdueFinesRecursive() {
        return computeOverdueHelper(0);
    }

    private double computeOverdueHelper(int index) {
        if (index >= borrowedItems.size()) {
            return 0.0; // base case
        }
        LibraryItem item = borrowedItems.get(index);
        LocalDate borrowed = borrowDates.get(item.getId());
        double fineForThisItem = 0.0;
        if (borrowed != null) {
            long daysOut = java.time.temporal.ChronoUnit.DAYS.between(borrowed, LocalDate.now());
            long overdueDays = daysOut - LOAN_PERIOD_DAYS;
            if (overdueDays > 0) {
                fineForThisItem = overdueDays * FINE_PER_DAY;
            }
        }
        return fineForThisItem + computeOverdueHelper(index + 1); // recursive case
    }

    public boolean hasOverdueItems() {
        for (LibraryItem item : borrowedItems) {
            LocalDate borrowed = borrowDates.get(item.getId());
            if (borrowed != null) {
                long daysOut = java.time.temporal.ChronoUnit.DAYS.between(borrowed, LocalDate.now());
                if (daysOut > LOAN_PERIOD_DAYS) return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return userId + " - " + name + " (" + borrowedItems.size() + " borrowed)";
    }
}
