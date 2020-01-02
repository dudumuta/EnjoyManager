package com.youzan.mobile.enjoyplugin.ui;

import com.youzan.mobile.enjoyplugin.StyleUtils;
import com.youzan.mobile.enjoyplugin.module.Repository;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class MyTableModel extends AbstractTableModel {

    private List<Repository> allData;

    public MyTableModel(List<Repository> ALL_DATA) {
        this.allData = ALL_DATA;
    }

    private String[] columnNames = {"AAR依赖", "ModuleLib", "Version"};

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
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        String moduleName = allData.get(rowIndex).getName();
        return columnIndex == 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object o;
        switch (columnIndex) {
            case 0: {
                o = allData.get(rowIndex).getChoose();
                break;
            }
            case 1: {
                o = allData.get(rowIndex).getName();
                break;
            }
            case 2: {
                o = allData.get(rowIndex).getVersion();
                break;
            }
            default: {
                o = "";
                break;
            }
        }
        return o;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                allData.get(rowIndex).setChoose((boolean) aValue);
                break;
            case 2:
                allData.get(rowIndex).setVersion((String) aValue);
                break;
            default:
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public void selectAllOrNull(int columnIndex, boolean value) {
        for (int index = 0; index < getRowCount(); index++) {
            this.setValueAt(value, index, columnIndex);
        }
    }
}