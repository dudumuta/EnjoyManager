package com.youzan.mobile.enjoyplugin.ui;

import com.youzan.mobile.enjoyplugin.module.ModuleInfo;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class MyTableModel extends AbstractTableModel {

    private List<ModuleInfo> allData;

    public MyTableModel(List<ModuleInfo> ALL_DATA) {
        this.allData = ALL_DATA;
    }

    private String[] columnNames = {"AarBuild", "ModuleName", "Version", "Owner"};

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
        String moduleName = allData.get(rowIndex).name;
        return columnIndex == 0 || columnIndex == 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object o;
        switch (columnIndex) {
            case 0: {
                o = allData.get(rowIndex).choose;
                break;
            }
            case 1: {
                o = allData.get(rowIndex).name;
                break;
            }
            case 2: {
                o = allData.get(rowIndex).version;
                break;
            }
            case 3: {
                o = allData.get(rowIndex).owner;
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
                allData.get(rowIndex).choose = (boolean) aValue;
                break;
            case 2:
                allData.get(rowIndex).version = (String) aValue;
                break;
            case 3:
                allData.get(rowIndex).owner = (String) aValue;
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