package controller;

import model.Borrowable;
import model.LibraryItem;
import model.LibraryDatabase;
import model.UserAccount;

/**
 * Handles the borrow/return workflow, including the reservation waitlist
 * (a Queue) that activates whenever a requested item is already checked out.
 */
public class BorrowController {

    private final LibraryManager manager;

    public BorrowController(LibraryManager manager) {
        this.manager = manager;
    }

    /**
     * Attempts to borrow an item for a user. If the item is unavailable the
     * user is placed on the reservation queue and false is returned.
     */
    public String borrow(LibraryItem item, UserAccount user) {
        if (!(item instanceof Borrowable)) {
            return "This item type cannot be borrowed.";
        }
        Borrowable borrowable = (Borrowable) item;
        if (borrowable.isAvailable()) {
            borrowable.borrowItem(user);
            manager.recordAccess(item);
            return "\"" + item.getTitle() + "\" successfully borrowed by " + user.getName() + ".";
        } else {
            LibraryDatabase db = manager.getDatabase();
            db.enqueueReservation(item.getId(), user);
            return "\"" + item.getTitle() + "\" is currently unavailable. "
                    + user.getName() + " added to the reservation queue.";
        }
    }

    /**
     * Returns an item. If someone is waiting on the reservation queue, the
     * item is automatically handed to the next person in line (FIFO).
     */
    public String returnItem(LibraryItem item, UserAccount user) {
        if (!(item instanceof Borrowable)) {
            return "This item type cannot be returned.";
        }
        Borrowable borrowable = (Borrowable) item;
        boolean ok = borrowable.returnItem();
        if (!ok) {
            return "\"" + item.getTitle() + "\" was not marked as borrowed.";
        }
        user.removeBorrowedItem(item);

        LibraryDatabase db = manager.getDatabase();
        UserAccount nextInLine = db.pollNextReservation(item.getId());
        if (nextInLine != null) {
            borrowable.borrowItem(nextInLine);
            manager.recordAccess(item);
            return "\"" + item.getTitle() + "\" returned by " + user.getName()
                    + " and automatically handed to " + nextInLine.getName() + " from the waitlist.";
        }
        return "\"" + item.getTitle() + "\" successfully returned by " + user.getName() + ".";
    }
}
