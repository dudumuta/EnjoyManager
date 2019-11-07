package com.youzan.mobile.enjoyplugin.ui;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.sun.istack.internal.NotNull;
import com.youzan.mobile.enjoyplugin.StyleUtils;
import com.youzan.mobile.enjoyplugin.entity.Repository;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeDialog extends JFrame {

    private boolean DEBUG = false;

    private AnActionEvent event;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton buttonRefresh;
    private JButton buttonReset;
    private JCheckBox kotlinCheckBox;
    private JPanel buttonPane;
    private JPanel bottomPane;
    private JPanel tablePane;
    private JCheckBox androidXCheckBox;

    private List<Repository> ALL_DATA;
    private List<Repository> ALL_DATA_NO;

    public HomeDialog(AnActionEvent event, List<Repository> data, List<Repository> data0) {

        this.event = event;
        this.ALL_DATA = data;
        this.ALL_DATA_NO = data0;

        setContentPane(contentPane);
//        setModal(true);
        setTitle("Enjoy Manager");
        getRootPane().setDefaultButton(buttonOK);

        JBTable table = new JBTable(new MyTableModel(ALL_DATA));
        StyleUtils.setTableStyle(table);
        table.setPreferredScrollableViewportSize(new Dimension(800, 300));
        table.setFillsViewportHeight(true);
//        table.getTableHeader().setDefaultRenderer(new CheckHeaderCellRenderer(table));
//        table.addMouseListener(new MouseListener() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                int row = table.rowAtPoint(e.getPoint());
//                int col = table.columnAtPoint(e.getPoint());
//                System.out.println(row + " " + col);
//                if (col == 0) {
////                    table.getModel().get
//                }
//
////                if (col == 4) {
////                    Desktop desktop = Desktop.getDesktop();
////                    try {
////                        URI uri = new URI("http://www.github.com/" + ALL_DATA.get(row).getUser() + "/" + ALL_DATA.get(row).getName());
////                        desktop.browse(uri);
////                    } catch (Exception ex) {
////                        // do nothing
////                        ex.printStackTrace();
////                    }
////
////                }
//            }
//
//            @Override
//            public void mousePressed(MouseEvent e) {
//
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//
//            }
//
//            @Override
//            public void mouseEntered(MouseEvent e) {
//
//            }
//
//            @Override
//            public void mouseExited(MouseEvent e) {
//
//            }
//        });

        JBScrollPane scrollPane = new JBScrollPane(table);
        tablePane.setLayout(new GridLayout(1, 0));
        tablePane.add(scrollPane);

//        buttonRefresh.addActionListener(e1 -> onRefresh(table, ALL_DATA));

//        buttonReset.addActionListener(e1 -> onReset(table, ALL_DATA_NO));

        buttonOK.addActionListener(e1 -> onOK());

        buttonCancel.addActionListener(e1 -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e1) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e1 -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        insetStringAfterOffset(event, ALL_DATA);
    }

    private void onCancel() {
        dispose();
    }

    private void insetStringAfterOffset(@NotNull AnActionEvent e, List<Repository> data) {
        JSONArray array = JSONArray.parseArray(JSON.toJSONString(data));
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                //生成配置文件
                File output = new File(e.getProject().getBasePath() + "/enjoyManager/enjoy.json");
                if (!output.exists()) {
                    try {
                        output.createNewFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                FileWriter fwriter = null;
                try {
                    // true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好
                    fwriter = new FileWriter(output);
                    fwriter.write(array.toJSONString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        fwriter.flush();
                        fwriter.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                File properties = new File(e.getProject().getBasePath() + "/enjoyManager/gradle.properties");
                //生成properties文件触发as的sync策略
                if (!properties.exists()) {
                    try {
                        properties.createNewFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                List<String> modulesNames = new ArrayList<>();
                for (Repository temp : data) {
                    if (temp.getChoose()) {
                        modulesNames.add(temp.getName());
                    }
                }
                JSONArray modules = JSONArray.parseArray(JSON.toJSONString(modulesNames));
                FileWriter propertiesW = null;
                try {
                    propertiesW = new FileWriter(properties);
                    propertiesW.write(modules.toJSONString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        propertiesW.flush();
                        propertiesW.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

            }
        });
        dispose();
        showNotification("implementation", "Enjoy Manager", "aar dependence changed success");
    }

    private void showNotification(String displayId, String title, String message) {
        NotificationGroup noti = new NotificationGroup(displayId, NotificationDisplayType.BALLOON, true);
        noti.createNotification(
                title,
                message,
                NotificationType.INFORMATION,
                null
        ).notify(event.getProject());
    }
}
