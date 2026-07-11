package gui;

import controller.LibraryManager;
import controller.SearchEngine;
import controller.SortEngine;
import model.LibraryItem;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Tab #4: search by field/algorithm, and sort by field/algorithm. */
public class SearchSortPanel extends JPanel {

    private final LibraryManager manager;
    private final MainWindow mainWindow;

    private final JTextField queryField = new JTextField(15);
    private final JComboBox<SearchEngine.Field> searchFieldCombo = new JComboBox<>(SearchEngine.Field.values());
    private final JComboBox<SearchEngine.SearchType> searchTypeCombo = new JComboBox<>(SearchEngine.SearchType.values());

    private final JComboBox<String> sortFieldCombo = new JComboBox<>(new String[]{"Title", "Author", "Year"});
    private final JComboBox<SortEngine.Algorithm> sortAlgoCombo = new JComboBox<>(SortEngine.Algorithm.values());

    private final ItemTableModel tableModel = new ItemTableModel();
    private final JTable table = new JTable(tableModel);

    private boolean lastSortedByTitle = false; // tracks whether binary search is currently valid

    public SearchSortPanel(LibraryManager manager, MainWindow mainWindow) {
        this.manager = manager;
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildControlPanel(), BorderLayout.NORTH);

        table.getColumnModel().getColumn(6).setCellRenderer(new AvailabilityRenderer());
        table.setRowHeight(22);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshTable();
    }

    private JPanel buildControlPanel() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));

        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; searchPanel.add(new JLabel("Query:"), gbc);
        gbc.gridx = 1; searchPanel.add(queryField, gbc);
        gbc.gridx = 2; searchPanel.add(new JLabel("Field:"), gbc);
        gbc.gridx = 3; searchPanel.add(searchFieldCombo, gbc);
        gbc.gridx = 4; searchPanel.add(new JLabel("Algorithm:"), gbc);
        gbc.gridx = 5; searchPanel.add(searchTypeCombo, gbc);

        JButton searchBtn = new JButton("Search");
        searchBtn.setMnemonic('S');
        searchBtn.addActionListener(e -> doSearch());
        gbc.gridx = 6; searchPanel.add(searchBtn, gbc);

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> refreshTable());
        gbc.gridx = 7; searchPanel.add(clearBtn, gbc);

        JPanel sortPanel = new JPanel(new GridBagLayout());
        sortPanel.setBorder(BorderFactory.createTitledBorder("Sort"));
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(4, 4, 4, 4);
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        gbc2.gridx = 0; gbc2.gridy = 0; sortPanel.add(new JLabel("Sort by:"), gbc2);
        gbc2.gridx = 1; sortPanel.add(sortFieldCombo, gbc2);
        gbc2.gridx = 2; sortPanel.add(new JLabel("Algorithm:"), gbc2);
        gbc2.gridx = 3; sortPanel.add(sortAlgoCombo, gbc2);

        JButton sortBtn = new JButton("Sort");
        sortBtn.setMnemonic('O');
        sortBtn.addActionListener(e -> doSort());
        gbc2.gridx = 4; sortPanel.add(sortBtn, gbc2);

        outer.add(searchPanel);
        outer.add(sortPanel);
        return outer;
    }

    private void doSearch() {
        String query = queryField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a search term first.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SearchEngine.Field field = (SearchEngine.Field) searchFieldCombo.getSelectedItem();
        SearchEngine.SearchType type = (SearchEngine.SearchType) searchTypeCombo.getSelectedItem();
        List<LibraryItem> source = manager.getDatabase().getItems();
        List<LibraryItem> results;

        if (type == SearchEngine.SearchType.BINARY) {
            if (field != SearchEngine.Field.TITLE || !lastSortedByTitle) {
                JOptionPane.showMessageDialog(this,
                        "Binary search requires an exact title match on a list already sorted by Title.\n" +
                        "Please sort by Title first (Sort panel), then search by Title.",
                        "Binary Search Requires Sorted Data", JOptionPane.WARNING_MESSAGE);
                return;
            }
            LibraryItem found = SearchEngine.binarySearchByTitle(source, query);
            results = new ArrayList<>();
            if (found != null) results.add(found);
        } else if (type == SearchEngine.SearchType.RECURSIVE) {
            results = SearchEngine.recursiveSearch(source, query, field);
        } else {
            results = SearchEngine.linearSearch(source, query, field);
        }

        for (LibraryItem item : results) manager.recordAccess(item);
        tableModel.setItems(results);
        mainWindow.setStatus("Search (" + type + " on " + field + ") found " + results.size() + " result(s).");
    }

    private void doSort() {
        String sortBy = (String) sortFieldCombo.getSelectedItem();
        SortEngine.Algorithm algo = (SortEngine.Algorithm) sortAlgoCombo.getSelectedItem();
        List<LibraryItem> items = manager.getDatabase().getItems();

        Comparator<LibraryItem> cmp;
        switch (sortBy) {
            case "Author": cmp = SortEngine.byAuthor(); lastSortedByTitle = false; break;
            case "Year": cmp = SortEngine.byYear(); lastSortedByTitle = false; break;
            default: cmp = SortEngine.byTitle(); lastSortedByTitle = true; break;
        }
        SortEngine.sort(items, cmp, algo);
        tableModel.setItems(items);
        mainWindow.setStatus("Sorted " + items.size() + " item(s) by " + sortBy + " using " + algo + " sort.");
    }

    public void refreshTable() {
        tableModel.setItems(manager.getDatabase().getItems());
        lastSortedByTitle = false;
    }
}
