package com.youzan.mobile.enjoyplugin.ui;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.sun.istack.internal.NotNull;
import com.youzan.mobile.enjoyplugin.StyleUtils;
import com.youzan.mobile.enjoyplugin.Utils;
import com.youzan.mobile.enjoyplugin.callback.ExecCallback;
import com.youzan.mobile.enjoyplugin.module.EnjoyModule;
import com.youzan.mobile.enjoyplugin.module.ModuleInfo;
import com.youzan.mobile.enjoyplugin.ui.model.HomeTableModel;
import com.youzan.mobile.enjoyplugin.ui.model.PublishTableModel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeDialog extends JFrame {

    private boolean DEBUG = false;

    private AnActionEvent event;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel buttonPane;
    private JPanel bottomPane;
    private JPanel tablePane;
    private JTextField tag;
    private JTextField tips;
    private JCheckBox autoOpenBuildCheckBox;
    private JCheckBox autoCleanCheckBox;
    private JPanel publishPanel;
    private JButton buttonDiff;

    private List<ModuleInfo> ALL_DATA;

    public HomeDialog(AnActionEvent event, List<ModuleInfo> data, boolean autoClean, boolean autoOpenBuild) {

        this.event = event;
        this.ALL_DATA = data;

        setContentPane(contentPane);
//        setModal(true);
        setTitle("Enjoy Manager");
        getRootPane().setDefaultButton(buttonOK);

        //module基本信息
        JBTable table = new JBTable(new HomeTableModel(ALL_DATA));
        setColumnSize(table, 0, 100, 100, 100);
        table.getTableHeader().setReorderingAllowed(false);
        StyleUtils.setTableStyle(table, true);
        table.setPreferredScrollableViewportSize(new Dimension(800, 500));
        table.setFillsViewportHeight(true);
        JBScrollPane scrollPane = new JBScrollPane(table);
        tablePane.setLayout(new GridLayout(1, 0));
        tablePane.add(scrollPane);

        //module 本地发布
        JBTable publishT = new JBTable(new PublishTableModel(event, ALL_DATA, this, new StringBuilder()));
        publishT.getTableHeader().setReorderingAllowed(false);
        StyleUtils.setTableStyle(publishT, false);
        publishT.setPreferredScrollableViewportSize(new Dimension(160, 500));
        publishT.setFillsViewportHeight(true);
        TableCellRenderer buttonRenderer = new JTableButtonRenderer(publishT.getDefaultRenderer(JButton.class));
        publishT.getColumn("LocalPublish").setCellRenderer(buttonRenderer);
        publishT.addMouseListener(new JTableButtonMouseListener(publishT));
        JBScrollPane publishPane = new JBScrollPane(publishT);
        publishPanel.setLayout(new GridLayout(1, 0));
        publishPanel.add(publishPane);

        tag.setText("使用过程中遇到任何问题请联系Silas");
        tag.setEditable(false);
        tag.setBorder(null);
        tips.setText(" 欢迎使用EnjoyDependence");
        tips.setEditable(false);
        tips.setBorder(null);

//        autoOpenBuildCheckBox.setSelected(autoOpenBuild);
        autoOpenBuildCheckBox.setVisible(false);
        autoCleanCheckBox.setSelected(autoClean);

        buttonOK.addActionListener(e1 -> onOK());
        buttonCancel.addActionListener(e1 -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e1) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e1 -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        buttonDiff.addActionListener(e -> onDiff());
    }

    private void onDiff() {
        ProgressManager.getInstance().run(new DiffTask(event.getProject(), "diff..."));
    }

    private class DiffTask extends Task.Backgroundable {

        DiffTask(@Nullable Project project, @Nls(capitalization = Nls.Capitalization.Title) @NotNull String title) {
            super(project, title, false);
        }

        @Override
        public void run(@org.jetbrains.annotations.NotNull ProgressIndicator progressIndicator) {
            File lif = new File(myProject.getBasePath(), ".lif");
            if (!lif.exists()) {
                Utils.showNotification(".lif not exists");
                progressIndicator.setText(".lif not exists");
                progressIndicator.stop();
                return;
            }
            try {
                JSONObject jsonObject = JSON.parseObject(new FileInputStream(lif), JSONObject.class);
                String glc = jsonObject.getString("glc");
                String cmd = "git diff " + glc + " --name-only";
                progressIndicator.start();
                Utils.exec(cmd, lif.getParentFile(), new ExecCallback() {
                    @Override
                    public void onSuccess(String data) {
                        StringBuilder stringBuilder = new StringBuilder();
                        String[] split = data.split("\n");
                        for (ModuleInfo info : ALL_DATA) {
                            info.choose = true;
                        }
                        for (String path : split) {
                            if (path.isEmpty()) {
                                continue;
                            }
                            for (ModuleInfo info : ALL_DATA) {
                                if (path.contains(info.name) && info.choose) {
                                    info.choose = false;
                                    stringBuilder.append(info.name).append("\n");
                                    break;
                                }
                            }
                        }
                        Utils.showNotification("ARR模块： \n" + stringBuilder.toString());
                        progressIndicator.stop();
                        onOK();
                    }

                    @Override
                    public void onError() {
                        progressIndicator.setText("error2");
                        progressIndicator.stop();
                        Utils.showNotification("CMD执行失败");
                    }
                }, true);
            } catch (IOException ex) {
                ex.printStackTrace();
                Utils.showNotification(ex.getMessage());
                progressIndicator.stop();
            }
        }

    }

    private void onOK() {
        insetStringAfterOffset(event, ALL_DATA);
        autoOpenLocalAARBuild();
        autoClean(event.getProject().getBasePath());
    }

    private void onCancel() {
//        if (event.getProject() == null || event.getProject().getBasePath() == null) {
//            Utils.showNotification(event, "error", "提示", "project 有误");
//        }
//        File output = new File(event.getProject().getBasePath() + "/enjoyManager/enjoy.json");
//        if (output.exists()) {
//            output.delete();
//        }
        dispose();
    }

    private void insetStringAfterOffset(@NotNull AnActionEvent e, List<ModuleInfo> data) {
        if (e.getProject() == null || e.getProject().getBasePath() == null) {
            Utils.showNotification(e, "error", "提示", "project 有误");
        }

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                //构建json，准备保存文件
                String branch = Utils.getCurrentBranch(e.getProject().getBasePath());
                EnjoyModule enjoyModule = new EnjoyModule();
                enjoyModule.modules = data;
                enjoyModule.branch = branch;
                enjoyModule.autoClean = autoCleanCheckBox.isSelected();
//                enjoyModule.autoOpenBuild = autoOpenBuildCheckBox.isSelected();
                //待写入文件的列表
                List<EnjoyModule> modules = new ArrayList<>();
                String enjoyJson = Utils.readFile(e.getProject().getBasePath() + "/enjoyManager/enjoy.json");
                if (enjoyJson != null && enjoyJson.contains("branch")) {
                    //新的模型
                    modules = JSON.parseArray(enjoyJson, EnjoyModule.class);
                }
                EnjoyModule needDelete = null;
                for (EnjoyModule temp : modules) {
                    if (temp.branch.equals(branch)) {
                        needDelete = temp;
                        break;
                    }
                }
                modules.remove(needDelete);
                modules.add(0, enjoyModule);
                if (modules.size() == 6) {
                    modules.remove(5);
                }
                JSONArray modulesJson = JSONArray.parseArray(JSON.toJSONString(modules));

                //写入文件
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
                    fwriter.write(modulesJson.toJSONString());
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

                //将使用情况写入到gradle.properties，便于as提示
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
                List<String> uninstallNames = new ArrayList<>();
                for (ModuleInfo temp : data) {
                    if (temp.choose) {
                        modulesNames.add(temp.name);
                    }
                    if (temp.uninstall) {
                        uninstallNames.add(temp.name);
                    }
                }
                JSONArray chooseModules = JSONArray.parseArray(JSON.toJSONString(modulesNames));
                JSONArray uninstall = JSONArray.parseArray(JSON.toJSONString(uninstallNames));
                FileWriter propertiesW = null;
                try {
                    propertiesW = new FileWriter(properties);
                    propertiesW.write("aar依赖：" + chooseModules.toJSONString() + "\n" + "卸载module：" + uninstall.toJSONString());
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

    /**
     * 自动触发clean
     */
    private void autoClean(String filePath) {
        if (!autoCleanCheckBox.isSelected()) {
            return;
        }
        String command = "./gradlew clean";
        File file = new File(filePath);
        try {
            Process p = Runtime.getRuntime().exec(command, null, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 自动在local.properties中写入aarBuild=true
     */
    private void autoOpenLocalAARBuild() {
//        if (!autoOpenBuildCheckBox.isSelected()) {
//            return;
//        }
        FileWriter fw = null;
        try {
            //如果文件存在，则追加内容；如果文件不存在，则创建文件
            File f = new File(event.getProject().getBasePath() + "/local.properties");
            fw = new FileWriter(f, true);
            String localStr = Utils.readFile(event.getProject().getBasePath() + "/local.properties");
            PrintWriter pw = new PrintWriter(fw);
            if (localStr != null && !localStr.contains("aarBuild")) {
                if (!localStr.contains("#aar构建")) {
                    pw.println("#aar构建");
                }
                pw.println("aarBuild=true");
            }
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fw.flush();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setColumnSize(JTable table, int i, int preferedWidth, int maxWidth, int minWidth) {
        //表格的列模型
        TableColumnModel cm = table.getColumnModel();
        //得到第i个列对象
        TableColumn column = cm.getColumn(i);
        column.setPreferredWidth(preferedWidth);
        column.setMaxWidth(maxWidth);
        column.setMinWidth(minWidth);
    }
}
