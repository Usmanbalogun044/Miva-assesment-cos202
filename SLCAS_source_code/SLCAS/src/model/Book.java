package model;

public class Book extends LibraryItem implements Borrowable {

    private String isbn;

    public Book(String id, String title, String author, int year, String category, String isbn) {
        super(id, title, author, year, category);
        this.isbn = isbn;
    }

    public String getIsbn() { return isbn; }

    @Override
    public String getType() { return "Book"; }

    @Override
    public String describe() {
        return "Book: \"" + title + "\" (ISBN " + isbn + ") by " + author;
    }

    @Override
    protected String extraData() { return isbn; }

    @Override
    public boolean borrowItem(UserAccount user) {
        if (!available) return false;
        available = false;
        user.addBorrowedItem(this);
        incrementAccessCount();
        return true;
    }

    @Override
    public boolean returnItem() {
        if (available) return false;
        available = true;
        return true;
    }
}
