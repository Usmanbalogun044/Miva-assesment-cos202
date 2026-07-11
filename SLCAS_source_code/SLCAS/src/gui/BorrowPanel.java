package gui;

import controller.BorrowController;
import controller.LibraryManager;
import model.LibraryItem;
import model.UserAccount;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Tab #2: borrow and return items, with a background Timer for overdue reminders. */
public class BorrowPanel extends JPanel {

    private final LibraryManager manager;
    private final BorrowController borrowController;
    private final MainWindow mainWindow;

    private final JTextField itemIdField = new JTextField(10);
    private final JTextField userIdField = new JTextField(10);
    private final JTextField userNameField = new JTextField(12);
    private final ItemTableModel tableModel = new ItemTableModel();
    private final JTable table = new JTable(tableModel);

    private Timer overdueTimer;

    public BorrowPanel(LibraryManager manager, BorrowController borrowController, MainWindow mainWindow) {
        this.manager = manager;
        this.borrowController = borrowController;
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildFormPanel(), BorderLayout.NORTH);

        table.getColumnModel().getColumn(6).setCellRenderer(new AvailabilityRenderer());
        table.setRowHeight(22);
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                LibraryItem item = tableModel.getItemAt(row);
                if (item != null) itemIdField.setText(item.getId());
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshTable();
        startOverdueTimer();
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Borrow / Return"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Item ID:"), gbc);
        gbc.gridx = 1; panel.add(itemIdField, gbc);

        gbc.gridx = 2; panel.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 3; panel.add(userIdField, gbc);

        gbc.gridx = 4; panel.add(new JLabel("User Name:"), gbc);
        gbc.gridx = 5; panel.add(userNameField, gbc);
        userNameField.setToolTipText("Only needed the first time this User ID is used");

        JButton borrowBtn = new JButton("Borrow");
        borrowBtn.setMnemonic('B');
        borrowBtn.addActionListener(e -> doBorrow());

        JButton returnBtn = new JButton("Return");
        returnBtn.setMnemonic('T');
        returnBtn.addActionListener(e -> doReturn());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshTable());

        gbc.gridx = 6; panel.add(borrowBtn, gbc);
        gbc.gridx = 7; panel.add(returnBtn, gbc);
        gbc.gridx = 8; panel.add(refreshBtn, gbc);

        return panel;
    }

    private void doBorrow() {
        LibraryItem item = validateAndGetItem();
        if (item == null) return;
        UserAccount user = validateAndGetUser();
        if (user == null) return;

        String result = borrowController.borrow(item, user);
        JOptionPane.showMessageDialog(this, result, "Borrow", JOptionPane.INFORMATION_MESSAGE);
        mainWindow.setStatus(result);
        refreshTable();
    }

    private void doReturn() {
        LibraryItem item = validateAndGetItem();
        if (item == null) return;
        UserAccount user = validateAndGetUser();
        if (user == null) return;

        String result = borrowController.returnItem(item, user);
        JOptionPane.showMessageDialog(this, result, "Return", JOptionPane.INFORMATION_MESSAGE);
        mainWindow.setStatus(result);
        refreshTable();
    }

    private LibraryItem validateAndGetItem() {
        String id = itemIdField.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an Item ID.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        LibraryItem item = manager.getDatabase().findById(id);
        if (item == null) {
            JOptionPane.showMessageDialog(this, "No item found with ID: " + id, "Not Found", JOptionPane.ERROR_MESSAGE);
        }
        return item;
    }

    private UserAccount validateAndGetUser() {
        String id = userIdField.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a User ID.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String name = userNameField.getText().trim();
        UserAccount existing = manager.getDatabase().findUserById(id);
        if (existing == null && name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "New user - please also enter a name.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return manager.getOrCreateUser(id, name.isEmpty() ? id : name);
    }

    public void refreshTable() {
        tableModel.setItems(manager.getDatabase().getItems());
    }

    /** Advanced GUI technique: Timer-triggered overdue reminder popups. */
    private void startOverdueTimer() {
        overdueTimer = new Timer(60000, e -> checkOverdue()); // check every 60s
        overdueTimer.setInitialDelay(5000);
        overdueTimer.start();
    }

    private void checkOverdue() {
        List<UserAccount> overdueUsers = manager.getDatabase().usersWithOverdueItems();
        if (!overdueUsers.isEmpty()) {
            StringBuilder sb = new StringBuilder("Overdue reminder:\n");
            for (UserAccount u : overdueUsers) {
                sb.append("- ").append(u.getName()).append(" has overdue item(s).\n");
            }
            mainWindow.setStatus("Overdue reminder triggered for " + overdueUsers.size() + " user(s).");
            JOptionPane.showMessageDialog(this, sb.toString(), "Overdue Reminder", JOptionPane.WARNING_MESSAGE);
        }
    }
}
