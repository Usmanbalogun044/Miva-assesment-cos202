package gui;

import model.LibraryItem;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Table model backing the item tables used across several panels. */
public class ItemTableModel extends AbstractTableModel {

    private final String[] columns = {"ID", "Title", "Author", "Year", "Category", "Type", "Status"};
    private List<LibraryItem> items = new ArrayList<>();

    public void setItems(List<LibraryItem> newItems) {
        this.items = new ArrayList<>(newItems);
        fireTableDataChanged();
    }

    public LibraryItem getItemAt(int row) {
        if (row < 0 || row >= items.size()) return null;
        return items.get(row);
    }

    @Override public int getRowCount() { return items.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int col) { return columns[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        LibraryItem item = items.get(row);
        switch (col) {
            case 0: return item.getId();
            case 1: return item.getTitle();
            case 2: return item.getAuthor();
            case 3: return item.getYear();
            case 4: return item.getCategory();
            case 5: return item.getType();
            case 6: return item.isAvailable() ? "Available" : "Borrowed";
            default: return "";
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) { return false; }
}
