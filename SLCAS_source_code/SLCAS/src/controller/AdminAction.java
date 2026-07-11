package controller;

import model.LibraryItem;

/**
 * Represents a single reversible admin operation, pushed onto the undo
 * Stack in LibraryManager. Currently supports ADD and DELETE reversal.
 */
public class AdminAction {

    public enum Type { ADD, DELETE }

    private final Type type;
    private final LibraryItem item;

    public AdminAction(Type type, LibraryItem item) {
        this.type = type;
        this.item = item;
    }

    public Type getType() { return type; }
    public LibraryItem getItem() { return item; }

    @Override
    public String toString() {
        return type + " -> " + item.getTitle();
    }
}
