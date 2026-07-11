package gui;

import controller.BorrowController;
import controller.LibraryManager;
import model.Book;
import model.Journal;
import model.Magazine;

import javax.swing.*;
import java.awt.*;

/**
 * The application's main window: a JFrame with a menu bar (mnemonics),
 * a JTabbedPane hosting the four required panels, and a status bar.
 * Uses BorderLayout at the top level.
 */
public class MainWindow extends JFrame {

    private final LibraryManager manager = new LibraryManager();
    private final BorrowController borrowController = new BorrowController(manager);

    private final JLabel statusBar = new JLabel("Ready.");

    private ViewItemsPanel viewItemsPanel;
    private BorrowPanel borrowPanel;
    private AdminPanel adminPanel;
    private SearchSortPanel searchSortPanel;

    public MainWindow() {
        super("Smart Library Circulation & Automation System (SLCAS)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        seedSampleData();

        setJMenuBar(buildMenuBar());

        JTabbedPane tabs = new JTabbedPane();
        viewItemsPanel = new ViewItemsPanel(manager, this);
        borrowPanel = new BorrowPanel(manager, borrowController, this);
        adminPanel = new AdminPanel(manager, this);
        searchSortPanel = new SearchSortPanel(manager, this);

        tabs.addTab("View Items", viewItemsPanel);
        tabs.addTab("Borrow/Return", borrowPanel);
        tabs.addTab("Admin", adminPanel);
        tabs.addTab("Search & Sort", searchSortPanel);
        tabs.setMnemonicAt(0, java.awt.event.KeyEvent.VK_1);
        tabs.setMnemonicAt(1, java.awt.event.KeyEvent.VK_2);
        tabs.setMnemonicAt(2, java.awt.event.KeyEvent.VK_3);
        tabs.setMnemonicAt(3, java.awt.event.KeyEvent.VK_4);

        add(tabs, BorderLayout.CENTER);

        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setPreferredSize(new Dimension(100, 24));
        add(statusBar, BorderLayout.SOUTH);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('X');
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "SLCAS - COS 202 Project\nSmart Library Circulation & Automation System",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    public void setStatus(String message) {
        statusBar.setText(" " + message);
    }

    public void refreshAllTabs() {
        viewItemsPanel.refresh();
        borrowPanel.refreshTable();
        adminPanel.refreshTable();
        searchSortPanel.refreshTable();
    }

    private void seedSampleData() {
        manager.addItem(new Book(manager.generateNextItemId(), "Data Structures and Algorithms in Java",
                "Robert Lafore", 2017, "Computer Science", "978-0672324536"));
        manager.addItem(new Book(manager.generateNextItemId(), "Clean Code",
                "Robert C. Martin", 2008, "Computer Science", "978-0132350884"));
        manager.addItem(new Book(manager.generateNextItemId(), "Things Fall Apart",
                "Chinua Achebe", 1958, "Literature", "978-0385474542"));
        manager.addItem(new Book(manager.generateNextItemId(), "A Brief History of Time",
                "Stephen Hawking", 1988, "Science", "978-0553380163"));
        manager.addItem(new Magazine(manager.generateNextItemId(), "National Geographic",
                "Various", 2024, "Science", "Issue 245"));
        manager.addItem(new Magazine(manager.generateNextItemId(), "TIME Magazine",
                "Various", 2024, "Current Affairs", "Issue 12"));
        manager.addItem(new Journal(manager.generateNextItemId(), "Journal of Artificial Intelligence Research",
                "IEEE", 2023, "Computer Science", "Vol. 45"));
        manager.addItem(new Journal(manager.generateNextItemId(), "Nigerian Journal of Physics",
                "NIP", 2022, "Science", "Vol. 12"));
        // Clear the undo stack so it doesn't offer to undo the demo seed data.
        manager.getUndoStack().clear();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
