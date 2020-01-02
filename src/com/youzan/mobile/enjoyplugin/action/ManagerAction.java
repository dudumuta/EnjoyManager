package com.youzan.mobile.enjoyplugin.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.youzan.mobile.enjoyplugin.Utils;
import com.youzan.mobile.enjoyplugin.module.EnjoyModule;
import com.youzan.mobile.enjoyplugin.module.Repository;
import com.youzan.mobile.enjoyplugin.ui.HomeDialog;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ManagerAction extends AnAction {

    private final String versionFilePath = "/version.properties";
    private final String enjoyDirPath = "/enjoyManager";
    private final String enjoyFilePath = "/enjoyManager/enjoy.json";

    //module name list
    private ArrayList<String> modules = new ArrayList<>();
    //module version map
    private HashMap<String, String> versionMap = new HashMap<>();
    //module choose info
    HashMap<String, Boolean> chooses = new HashMap<>();
    //module uninstall info
    HashMap<String, Boolean> uninstallChoose = new HashMap<>();

    private boolean autoClean;

    private boolean autoOpenBuild;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (e.getProject() == null || e.getProject().getBasePath() == null) {
            Utils.showNotification(e, "error", "提示", "project 有误");
        }
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                //version文件
                File versionFile = new File(e.getProject().getBasePath() + versionFilePath);
                fillModuleInfo(e, versionFile);

                if (modules.size() > 0 && versionMap.size() > 0) {
                    File enjoyFile = new File(e.getProject().getBasePath() + enjoyFilePath);
                    fillModuleOtherInfo(e, enjoyFile);

                    JSONArray jsonArray = new JSONArray(modules.size());
                    for (String s : modules) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("name", s);
                        jsonObject.put("choose", chooses.getOrDefault(s, false));
                        jsonObject.put("version", versionMap.get(s));
                        jsonObject.put("uninstall", uninstallChoose.getOrDefault(s, false));
                        jsonArray.add(jsonObject);
                    }

                    List<Repository> data = JSON.parseArray(jsonArray.toJSONString(), Repository.class);
                    data.sort(new Comparator<Repository>() {
                        @Override
                        public int compare(Repository o1, Repository o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    if (data.size() == 0) {
                        Utils.showNotification(e, "error", "提示", "项目列表解析失败");
                    } else {
                        HomeDialog dialog = new HomeDialog(e, data, autoClean, autoOpenBuild);
                        dialog.pack();
                        dialog.setVisible(true);
                    }
                }
            }
        });
    }

    /**
     * 填充module info
     */
    private void fillModuleInfo(AnActionEvent e, File versionFile) {
        if (versionFile.exists()) {
            ArrayList<String> list = new ArrayList<>();
            try {
                InputStreamReader inputReader = new InputStreamReader(new FileInputStream(versionFile));
                BufferedReader bf = new BufferedReader(inputReader);
                // 按行读取字符串
                String str;
                while ((str = bf.readLine()) != null) {
                    list.add(str);
                }
                bf.close();
                inputReader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (list.size() > 0) {
                for (String s : list) {
                    String[] temp = s.split("=");
                    if (temp.length >= 2) {
                        versionMap.put(temp[0].trim(), temp[1].trim());
                        modules.add(temp[0].trim());
                    }
                }
            }
        } else {
            Utils.showNotification(e, "error", "提示", "version.properties文件不存在，modules版本解析失败");
        }
    }

    /**
     * 填充选择情况
     *
     * @param e
     * @param enjoyFile
     */
    private void fillModuleOtherInfo(AnActionEvent e, File enjoyFile) {
        if (!enjoyFile.getParentFile().exists()) {
            enjoyFile.getParentFile().mkdir();
        }
        if (!enjoyFile.exists()) {
            try {
                enjoyFile.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            String enjoyJson = Utils.readFile(enjoyFile.getAbsolutePath());
            if (enjoyJson != null && !enjoyJson.contains("branch")) {
                //没有branch字段的认为是老版本，采用老逻辑
                List<Repository> repositories = JSON.parseArray(enjoyJson, Repository.class);
                if (repositories != null) {
                    repositories.sort(new Comparator<Repository>() {
                        @Override
                        public int compare(Repository o1, Repository o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    for (Repository repository : repositories) {
                        chooses.put(repository.getName(), repository.getChoose());
                        uninstallChoose.put(repository.getName(), repository.getUninstall());
                    }
                }
            } else if (enjoyJson != null) {
                //如果有branch字段认为是新版本，采用新逻辑
                List<EnjoyModule> enjoyModules = JSON.parseArray(enjoyJson, EnjoyModule.class);
                List<Repository> temp = null;
                for (EnjoyModule module : enjoyModules) {
                    if (Utils.getCurrentBranch(e.getProject().getBasePath()).equals(module.branch)) {
                        //如果分支匹配，则提取数据
                        temp = module.modules;
                        temp.sort(new Comparator<Repository>() {
                            @Override
                            public int compare(Repository o1, Repository o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                        autoClean = module.autoClean;
                        autoOpenBuild = module.autoOpenBuild;
                    }
                }
                if (temp != null) {
                    for (Repository repository : temp) {
                        chooses.put(repository.getName(), repository.getChoose());
                        uninstallChoose.put(repository.getName(), repository.getUninstall());
                    }
                }
            }
        }
    }
}
