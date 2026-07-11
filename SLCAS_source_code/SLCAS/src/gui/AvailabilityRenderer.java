package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom renderer (advanced GUI technique #1) that colours the
 * "Available/Borrowed" status cell green or red instead of showing plain text.
 */
public class AvailabilityRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                     boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String status = String.valueOf(value);
        if (!isSelected) {
            if ("Available".equalsIgnoreCase(status)) {
                c.setBackground(new Color(214, 245, 214));
                c.setForeground(new Color(20, 110, 20));
            } else {
                c.setBackground(new Color(250, 220, 220));
                c.setForeground(new Color(150, 20, 20));
            }
        }
        setHorizontalAlignment(CENTER);
        setFont(getFont().deriveFont(Font.BOLD));
        return c;
    }
}
