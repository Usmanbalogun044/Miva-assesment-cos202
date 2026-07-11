package model;

import java.io.Serializable;

/**
 * Abstract base class representing any item that can live in the library
 * catalogue. Concrete subclasses (Book, Magazine, Journal) provide the
 * type-specific behaviour while this class holds common state.
 */
public abstract class LibraryItem implements Serializable {

    protected String id;
    protected String title;
    protected String author;
    protected int year;
    protected String category;
    protected boolean available;
    protected int accessCount; // how many times this item has been looked up/borrowed

    protected LibraryItem(String id, String title, String author, int year, String category) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.category = category;
        this.available = true;
        this.accessCount = 0;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }
    public String getCategory() { return category; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public int getAccessCount() { return accessCount; }
    public void incrementAccessCount() { accessCount++; }

    /** Every subclass must declare what kind of item it represents. */
    public abstract String getType();

    /** Polymorphic hook: describes the item in a type-specific way. */
    public abstract String describe();

    @Override
    public String toString() {
        return String.format("[%s] %s by %s (%d) - %s - %s",
                id, title, author, year, category, available ? "Available" : "Borrowed");
    }

    /** Serialize this item to a single pipe-delimited line for file persistence. */
    public String toDataLine() {
        return String.join("|",
                getType(), id, escape(title), escape(author), String.valueOf(year),
                escape(category), String.valueOf(available), String.valueOf(accessCount),
                extraData());
    }

    /** Subclasses provide their extra fields here (e.g. isbn, issueNumber). */
    protected abstract String extraData();

    protected static String escape(String s) {
        return s == null ? "" : s.replace("|", "/");
    }
}
