package com.youzan.mobile.enjoyplugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.youzan.mobile.enjoyplugin.FileManager;
import com.youzan.mobile.enjoyplugin.Utils;
import com.youzan.mobile.enjoyplugin.ui.HomeDialog;
import org.jetbrains.annotations.NotNull;

public class ManagerAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        if (anActionEvent.getProject() == null || anActionEvent.getProject().getBasePath() == null) {
            Utils.showNotification(anActionEvent, "error", "提示", "project 有误");
        }
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                FileManager fileManager = FileManager.getInstance(anActionEvent);
                if (fileManager.loadModuleInfoList().size() > 0) {
                    HomeDialog dialog = new HomeDialog(anActionEvent, fileManager.loadModuleInfoList(), false, false);
                    dialog.pack();
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                }
            }
        });
    }
}
