package com.youzan.mobile.enjoyplugin.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.youzan.mobile.enjoyplugin.Utils;
import com.youzan.mobile.enjoyplugin.callback.ExecCallback;
import com.youzan.mobile.enjoyplugin.module.ModuleInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class LocalPublishDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private JTextField versionTitle;
    private JTextArea textArea1;
    private AnActionEvent event;
    private String moduleName;
    private List<ModuleInfo> allData;
    private HomeDialog homeDialog;
    private StringBuilder copyContent;

    public LocalPublishDialog(AnActionEvent event, String moduleName, List<ModuleInfo> ALL_DATA, HomeDialog homeDialog, StringBuilder stringBuilder) {
        this.event = event;
        this.moduleName = moduleName;
        this.allData = ALL_DATA;
        this.homeDialog = homeDialog;
        this.copyContent = stringBuilder;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setSize(300, 240); // 设置窗口大小 2017/3/18 09:50
        setTitle("发布" + moduleName + " aar 至本地仓库"); // 设置title 2017/3/18 09:50

        textField1.setText(buildVersion());
        versionTitle.setText("即将发布的Version是：");
        versionTitle.setEditable(false);
        versionTitle.setBorder(null);
        textArea1.setText("使用方式：发布完成后，打开工程root目录下的local.properties,直接Command+V即可");
        textArea1.setLineWrap(true);
        textArea1.setEditable(false);
        textArea1.setBorder(null);
        setLocationRelativeTo(null);

        buttonOK.setText("确定");
        buttonCancel.setText("取消");

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        dispose();
//        homeDialog.dispose();
        File file = new File(this.event.getProject().getBasePath());
        Utils.exec("./gradlew :modules:" + this.moduleName + ":publishMaven" + Utils.getFlavor(this.event.getProject().getBasePath() + File.separator + "app/app.iml") + "DebugAarPublicationToMavenLocal -Pversion=" + textField1.getText() + " -PlocalPublish=true", file, new ExecCallback() {
            @Override
            public void onSuccess(String data) {
                copy();
                Utils.showNotification(event, "success", "本地发布成功", "版本号信息已复制到粘贴板，请手动粘贴至root目录下的local.properties中");
            }

            @Override
            public void onError() {
                Utils.showNotification(event, "error", "本地发布失败", "请检查编译环节是否出错");
            }
        });
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private String buildVersion() {
        String currentBranch = Utils.getCurrentBranch(this.event.getProject().getBasePath());
        String currentVersion = "";
        for (ModuleInfo temp : this.allData) {
            if (temp.name.equals(this.moduleName)) {
                currentVersion = temp.version;
                if (currentVersion.contains("-SNAPSHOT")) {
                    String[] versionTemp = currentVersion.split("-");
                    currentVersion = versionTemp[0];
                }
                break;
            }
        }
        return currentVersion + "-" + currentBranch.hashCode() + "-local-SNAPSHOT";
    }

    private String formatVersionInfo(String version) {
        return this.moduleName + "=" + version;
    }

    /**
     * 复制到剪贴板
     */
    private void copy() {
        String temp = formatVersionInfo(textField1.getText());
        copyContent.append(temp).append("\n");
        StringSelection content = new StringSelection(copyContent.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(content, content);
    }
}
