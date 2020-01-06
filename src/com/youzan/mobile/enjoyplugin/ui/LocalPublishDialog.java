package com.youzan.mobile.enjoyplugin.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class LocalPublishDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private JTextField versionTitle;
    private JTextArea textArea1;
    private AnActionEvent event;
    private String moduleName;

    public LocalPublishDialog(AnActionEvent event, String moduleName) {
        this.event = event;
        this.moduleName = moduleName;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        textField1.setText("5.34.0-ly-SNAPSHOT");
        setTitle("发布" + moduleName + " aar 至本地仓库"); // 设置title 2017/3/18 09:50
        setSize(300, 240); // 设置窗口大小 2017/3/18 09:50
        versionTitle.setText("即将发布的Version是：");
        versionTitle.setEditable(false);
        versionTitle.setBorder(null);
        textArea1.setText("使用方式：以ModuleName=Version的格式，复制当前发布版本号至工程根目录的local.properties。例如：lib_common=5.34.0-local-SNAPSHOT");
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
        // add your code here
        dispose();
        localPublish(event.getProject().getBasePath(), this.moduleName);
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    /**
     * 自动触发clean
     */
    private void localPublish(String filePath, String moduleName) {
        String command = " ./gradlew :modules:" + moduleName + ":publishMavenPadDebugAarPublicationToMavenLocal -Pversion=\"5.34.0-ly-SNAPSHOT\"";
        File file = new File(filePath);
        try {
            Process p = Runtime.getRuntime().exec(command, null, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
