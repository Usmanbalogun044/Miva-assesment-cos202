package model;

public class Magazine extends LibraryItem implements Borrowable {

    private String issueNumber;

    public Magazine(String id, String title, String author, int year, String category, String issueNumber) {
        super(id, title, author, year, category);
        this.issueNumber = issueNumber;
    }

    public String getIssueNumber() { return issueNumber; }

    @Override
    public String getType() { return "Magazine"; }

    @Override
    public String describe() {
        return "Magazine: \"" + title + "\" issue " + issueNumber;
    }

    @Override
    protected String extraData() { return issueNumber; }

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
