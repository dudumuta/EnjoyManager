package com.youzan.mobile.enjoyplugin.ui;

import com.youzan.mobile.enjoyplugin.ui.model.HomeTableModel;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class CheckHeaderCellRenderer implements TableCellRenderer {

    HomeTableModel tableModel;
    JTableHeader tableHeader;
    final JCheckBox selectBox;
//    final JCheckBox uninstallBox;

    public CheckHeaderCellRenderer(final JTable table) {
        this.tableModel = (HomeTableModel) table.getModel();
        this.tableHeader = table.getTableHeader();
        selectBox = new JCheckBox(tableModel.getColumnName(0));
//        uninstallBox = new JCheckBox(tableModel.getColumnName(3));
        selectBox.setSelected(false);
        tableHeader.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 0) {
                    // 获得选中列
                    int selectColumn = tableHeader.columnAtPoint(e.getPoint());
                    if (selectColumn == 0) {
                        boolean value = !selectBox.isSelected();
                        selectBox.setSelected(value);
                        tableModel.selectAllOrNull(0, value);
                        tableHeader.repaint();
                    } else if (selectColumn == 3) {
//                        boolean value = !uninstallBox.isSelected();
//                        uninstallBox.setSelected(value);
//                        tableModel.selectAllOrNull(3, value);
//                        tableHeader.repaint();
                    }
                }
            }
        });
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        String valueStr = (String) value;
        JLabel label = new JLabel(valueStr);
        label.setHorizontalAlignment(SwingConstants.CENTER); // 表头标签剧中
        selectBox.setHorizontalAlignment(SwingConstants.CENTER);// 表头标签剧中
        selectBox.setBorderPainted(true);
//        uninstallBox.setHorizontalAlignment(SwingConstants.CENTER);// 表头标签剧中
//        uninstallBox.setBorderPainted(true);
        JComponent component = label;
        if (column == 0) {
            component = selectBox;
        } else if (column == 3) {
//            component = uninstallBox;
        }
        component.setForeground(tableHeader.getForeground());
        component.setBackground(tableHeader.getBackground());
        component.setFont(tableHeader.getFont());
        component.setBorder(UIManager.getBorder("TableHeader.cellBorder"));

        return component;
    }

}