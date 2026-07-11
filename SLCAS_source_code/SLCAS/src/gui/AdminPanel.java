package gui;

import controller.LibraryManager;
import model.*;
import utils.FileHandler;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/** Tab #3: add/delete items, undo the last action, import/export, save/load. */
public class AdminPanel extends JPanel {

    private static final String DEFAULT_SAVE_PATH = "data/library_data.txt";

    private final LibraryManager manager;
    private final MainWindow mainWindow;

    private final JTextField titleField = new JTextField(15);
    private final JTextField authorField = new JTextField(15);
    private final JTextField yearField = new JTextField(6);
    private final JTextField categoryField = new JTextField(10);
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Book", "Magazine", "Journal"});

    // Dynamic extra-field area switched via CardLayout depending on chosen type
    private final CardLayout extraCardLayout = new CardLayout();
    private final JPanel extraFieldsPanel = new JPanel(extraCardLayout);
    private final JTextField isbnField = new JTextField(12);
    private final JTextField issueField = new JTextField(12);
    private final JTextField volumeField = new JTextField(12);

    private final ItemTableModel tableModel = new ItemTableModel();
    private final JTable table = new JTable(tableModel);

    public AdminPanel(LibraryManager manager, MainWindow mainWindow) {
        this.manager = manager;
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildFormPanel(), BorderLayout.NORTH);

        table.getColumnModel().getColumn(6).setCellRenderer(new AvailabilityRenderer());
        table.setRowHeight(22);
        add(new JScrollPane(table), BorderLayout.CENTER);

        add(buildActionPanel(), BorderLayout.SOUTH);

        refreshTable();
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add New Item"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; panel.add(typeCombo, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 3; panel.add(titleField, gbc);

        gbc.gridx = 4; panel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 5; panel.add(authorField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1; panel.add(yearField, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 3; panel.add(categoryField, gbc);

        // Dynamic extra field, swapped at runtime via CardLayout (advanced GUI technique #2)
        JPanel bookCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bookCard.add(new JLabel("ISBN: "));
        bookCard.add(isbnField);
        JPanel magazineCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        magazineCard.add(new JLabel("Issue #: "));
        magazineCard.add(issueField);
        JPanel journalCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        journalCard.add(new JLabel("Volume: "));
        journalCard.add(volumeField);

        extraFieldsPanel.add(bookCard, "Book");
        extraFieldsPanel.add(magazineCard, "Magazine");
        extraFieldsPanel.add(journalCard, "Journal");

        gbc.gridx = 4; panel.add(new JLabel("Details:"), gbc);
        gbc.gridx = 5; panel.add(extraFieldsPanel, gbc);

        typeCombo.addActionListener(e -> extraCardLayout.show(extraFieldsPanel, (String) typeCombo.getSelectedItem()));

        JButton addBtn = new JButton("Add Item");
        addBtn.setMnemonic('A');
        addBtn.addActionListener(e -> doAdd());
        gbc.gridx = 6; gbc.gridy = 0; panel.add(addBtn, gbc);

        return panel;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.setMnemonic('D');
        deleteBtn.addActionListener(e -> doDelete());

        JButton undoBtn = new JButton("Undo Last Action");
        undoBtn.setMnemonic('U');
        undoBtn.addActionListener(e -> doUndo());

        JButton exportBtn = new JButton("Export CSV...");
        exportBtn.addActionListener(e -> doExport());

        JButton importBtn = new JButton("Import CSV...");
        importBtn.addActionListener(e -> doImport());

        JButton saveBtn = new JButton("Save Database");
        saveBtn.addActionListener(e -> doSave());

        JButton loadBtn = new JButton("Load Database");
        loadBtn.addActionListener(e -> doLoad());

        panel.add(deleteBtn);
        panel.add(undoBtn);
        panel.add(exportBtn);
        panel.add(importBtn);
        panel.add(saveBtn);
        panel.add(loadBtn);
        return panel;
    }

    private void doAdd() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String category = categoryField.getText().trim();
        String yearText = yearField.getText().trim();
        String type = (String) typeCombo.getSelectedItem();

        if (title.isEmpty() || author.isEmpty() || category.isEmpty() || yearText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Input Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int year;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Year must be a whole number.", "Input Validation", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String id = manager.generateNextItemId();
        LibraryItem item;
        switch (type) {
            case "Magazine":
                item = new Magazine(id, title, author, year, category, issueField.getText().trim());
                break;
            case "Journal":
                item = new Journal(id, title, author, year, category, volumeField.getText().trim());
                break;
            default:
                item = new Book(id, title, author, year, category, isbnField.getText().trim());
        }
        manager.addItem(item);
        refreshTable();
        clearForm();
        mainWindow.setStatus("Added new " + type + ": \"" + title + "\" (" + id + ")");
        mainWindow.refreshAllTabs();
    }

    private void doDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an item to delete first.", "Nothing Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LibraryItem item = tableModel.getItemAt(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + item.getTitle() + "\"? This can be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            manager.deleteItem(item);
            refreshTable();
            mainWindow.setStatus("Deleted \"" + item.getTitle() + "\". Use Undo to restore.");
            mainWindow.refreshAllTabs();
        }
    }

    private void doUndo() {
        String result = manager.undoLastAction();
        refreshTable();
        mainWindow.setStatus(result);
        mainWindow.refreshAllTabs();
        JOptionPane.showMessageDialog(this, result, "Undo", JOptionPane.INFORMATION_MESSAGE);
    }

    /** File chooser dialog for CSV export (advanced GUI technique #3). */
    private void doExport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("library_export.csv"));
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                FileHandler.exportCsv(manager.getDatabase(), chooser.getSelectedFile().getAbsolutePath());
                mainWindow.setStatus("Exported catalogue to " + chooser.getSelectedFile().getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doImport() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                List<LibraryItem> imported = FileHandler.importCsv(chooser.getSelectedFile().getAbsolutePath());
                for (LibraryItem item : imported) manager.addItem(item);
                refreshTable();
                mainWindow.setStatus("Imported " + imported.size() + " item(s) from " + chooser.getSelectedFile().getName());
                mainWindow.refreshAllTabs();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doSave() {
        try {
            new File("data").mkdirs();
            FileHandler.saveDatabase(manager.getDatabase(), DEFAULT_SAVE_PATH);
            mainWindow.setStatus("Database saved to " + DEFAULT_SAVE_PATH);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doLoad() {
        try {
            FileHandler.loadDatabase(manager.getDatabase(), DEFAULT_SAVE_PATH);
            refreshTable();
            mainWindow.setStatus("Database loaded from " + DEFAULT_SAVE_PATH);
            mainWindow.refreshAllTabs();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        titleField.setText("");
        authorField.setText("");
        yearField.setText("");
        categoryField.setText("");
        isbnField.setText("");
        issueField.setText("");
        volumeField.setText("");
    }

    public void refreshTable() {
        tableModel.setItems(manager.getDatabase().getItems());
    }
}
