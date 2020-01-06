package com.youzan.mobile.enjoyplugin.ui;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JTableButtonMouseListener extends MouseAdapter {
  private final JTable table;
		
  public JTableButtonMouseListener(JTable table) {
    this.table = table;
  }

  @Override public void mouseClicked(MouseEvent e) {
    int column = table.getColumnModel().getColumnIndexAtX(e.getX());
    int row    = e.getY()/table.getRowHeight(); 

    if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
      Object value = table.getValueAt(row, column);
      if (value instanceof JButton) {
        ((JButton)value).doClick();
      }
    }
  }
}