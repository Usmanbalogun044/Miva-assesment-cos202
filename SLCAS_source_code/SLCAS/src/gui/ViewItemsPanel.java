package gui;

import controller.LibraryManager;
import model.LibraryItem;
import model.UserAccount;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/** Tab #1: browse the full catalogue and generate simple reports. */
public class ViewItemsPanel extends JPanel {

    private final LibraryManager manager;
    private final ItemTableModel tableModel = new ItemTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextArea reportArea = new JTextArea(8, 30);
    private final MainWindow mainWindow;

    public ViewItemsPanel(LibraryManager manager, MainWindow mainWindow) {
        this.manager = manager;
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        table.getColumnModel().getColumn(6).setCellRenderer(new AvailabilityRenderer());
        table.setRowHeight(22);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh Catalogue");
        refreshBtn.setMnemonic('R');
        refreshBtn.setToolTipText("Reload the table from the database");
        refreshBtn.addActionListener(e -> refresh());
        top.add(refreshBtn);
        add(top, BorderLayout.NORTH);

        JPanel reportPanel = new JPanel(new BorderLayout(4, 4));
        reportPanel.setBorder(BorderFactory.createTitledBorder("Reports"));
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        reportPanel.add(new JScrollPane(reportArea), BorderLayout.CENTER);

        JPanel reportButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton mostBorrowedBtn = new JButton("Most Borrowed Items");
        mostBorrowedBtn.addActionListener(e -> showMostBorrowed());
        JButton overdueBtn = new JButton("Users With Overdue Items");
        overdueBtn.addActionListener(e -> showOverdueUsers());
        JButton categoryBtn = new JButton("Category Distribution");
        categoryBtn.addActionListener(e -> showCategoryDistribution());
        JButton cacheBtn = new JButton("Frequently Accessed (Cache)");
        cacheBtn.addActionListener(e -> showFrequentCache());
        reportButtons.add(mostBorrowedBtn);
        reportButtons.add(overdueBtn);
        reportButtons.add(categoryBtn);
        reportButtons.add(cacheBtn);
        reportPanel.add(reportButtons, BorderLayout.NORTH);

        add(reportPanel, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        tableModel.setItems(manager.getDatabase().getItems());
        mainWindow.setStatus("Catalogue refreshed - " + manager.getDatabase().getItems().size() + " item(s).");
    }

    private void showMostBorrowed() {
        List<LibraryItem> top = manager.getDatabase().mostBorrowedItems(5);
        StringBuilder sb = new StringBuilder("Top borrowed items:\n");
        int rank = 1;
        for (LibraryItem item : top) {
            sb.append(rank++).append(". ").append(item.getTitle())
              .append(" (accessed ").append(item.getAccessCount()).append(" times)\n");
        }
        reportArea.setText(sb.toString());
    }

    private void showOverdueUsers() {
        List<UserAccount> overdue = manager.getDatabase().usersWithOverdueItems();
        StringBuilder sb = new StringBuilder("Users with overdue items:\n");
        if (overdue.isEmpty()) {
            sb.append("None - all borrowed items are within the loan period.\n");
        }
        for (UserAccount u : overdue) {
            double fine = u.computeOverdueFinesRecursive();
            sb.append("- ").append(u.getName()).append(" (").append(u.getUserId())
              .append(") - outstanding fine: ").append(String.format("%.2f", fine)).append("\n");
        }
        reportArea.setText(sb.toString());
    }

    private void showCategoryDistribution() {
        Map<String, Integer> dist = manager.getDatabase().categoryDistribution();
        StringBuilder sb = new StringBuilder("Category distribution:\n");
        for (Map.Entry<String, Integer> e : dist.entrySet()) {
            sb.append("- ").append(e.getKey()).append(": ").append(e.getValue()).append(" item(s)\n");
        }
        reportArea.setText(sb.toString());
    }

    private void showFrequentCache() {
        LibraryItem[] cache = manager.getFrequentItemsCache();
        StringBuilder sb = new StringBuilder("Most Frequently Accessed Items (fixed-size cache):\n");
        boolean any = false;
        for (LibraryItem item : cache) {
            if (item != null) {
                any = true;
                sb.append("- ").append(item.getTitle()).append(" (").append(item.getAccessCount()).append(" accesses)\n");
            }
        }
        if (!any) sb.append("Cache is empty - borrow or search items to populate it.\n");
        reportArea.setText(sb.toString());
    }
}
