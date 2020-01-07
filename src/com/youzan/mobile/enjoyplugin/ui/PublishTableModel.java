package com.youzan.mobile.enjoyplugin.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.youzan.mobile.enjoyplugin.module.ModuleInfo;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class PublishTableModel extends AbstractTableModel {

    private List<ModuleInfo> allData;
    private AnActionEvent event;

    public PublishTableModel(AnActionEvent event, List<ModuleInfo> ALL_DATA) {
        this.event = event;
        this.allData = ALL_DATA;
    }

    private String[] columnNames = {"LocalPublish"};

    @Override
    public int getRowCount() {
        return allData == null ? 0 : allData.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object o;
        switch (columnIndex) {
            case 0: {
                o = new JButton("publish");
                final JButton button = new JButton(allData.get(rowIndex).name);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        new LocalPublishDialog(event, allData.get(rowIndex).name, allData).setVisible(true);
                    }
                });
                o = button;
                break;
            }
            default: {
                o = "";
                break;
            }
        }
        return o;
    }
}
