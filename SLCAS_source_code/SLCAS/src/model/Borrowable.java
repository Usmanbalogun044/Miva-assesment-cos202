package model;

/**
 * Interface implemented by any LibraryItem that can be borrowed and returned.
 */
public interface Borrowable {
    boolean borrowItem(UserAccount user);
    boolean returnItem();
    boolean isAvailable();
}
