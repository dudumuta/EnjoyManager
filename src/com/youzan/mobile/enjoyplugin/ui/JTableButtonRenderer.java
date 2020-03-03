package com.youzan.mobile.enjoyplugin.ui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

class JTableButtonRenderer implements TableCellRenderer {
    private TableCellRenderer defaultRenderer;

    public JTableButtonRenderer(TableCellRenderer renderer) {
        defaultRenderer = renderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        if (row > 0 && column == 4) {
            JButton button = (JButton) value;
            if (isSelected) {
                button.setForeground(Color.red);
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(UIManager.getColor("Button.background"));
            }
            return button;
        } else {
            return (Component) value;
        }
    }
}