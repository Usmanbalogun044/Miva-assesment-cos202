package model;

public class Journal extends LibraryItem implements Borrowable {

    private String volume;

    public Journal(String id, String title, String author, int year, String category, String volume) {
        super(id, title, author, year, category);
        this.volume = volume;
    }

    public String getVolume() { return volume; }

    @Override
    public String getType() { return "Journal"; }

    @Override
    public String describe() {
        return "Journal: \"" + title + "\" volume " + volume;
    }

    @Override
    protected String extraData() { return volume; }

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
