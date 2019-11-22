package com.youzan.mobile.enjoyplugin.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
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

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (e.getProject() == null || e.getProject().getBasePath() == null) {
            Utils.showNotification(e, "error", "提示", "project 有误");
        }
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                //module集合
                ArrayList<String> modules = new ArrayList<>();
                //版本集合
                HashMap<String, String> versions = new HashMap<>();
                //version文件
                File versionFile = new File(e.getProject().getBasePath() + "/version.properties");
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
                                versions.put(temp[0].trim(), temp[1].trim());
                                modules.add(temp[0].trim());
                            }
                        }
                    }
                } else {
                    Utils.showNotification(e, "error", "提示", "version.properties文件不存在，modules版本解析失败");
                }

                if (modules.size() > 0 && versions.size() > 0) {
                    //数据有效，准备构建渲染实体
                    File outputDir = new File(e.getProject().getBasePath() + "/enjoyManager");
                    //aar json
                    File output = new File(e.getProject().getBasePath() + "/enjoyManager/enjoy.json");
                    //选择情况
                    HashMap<String, Boolean> chooses = new HashMap<>();
                    //卸载情况
                    HashMap<String, Boolean> uninstallChoose = new HashMap<>();
                    if (!outputDir.exists()) {
                        outputDir.mkdir();
                    }
                    if (!output.exists()) {
                        try {
                            output.createNewFile();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        String enjoyJson = Utils.readJsonFile(output.getAbsolutePath());
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

                    JSONArray jsonArray = new JSONArray(modules.size());
                    for (String s : modules) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("name", s);
                        jsonObject.put("choose", chooses.getOrDefault(s, false));
                        jsonObject.put("version", versions.get(s));
                        jsonObject.put("uninstall", uninstallChoose.getOrDefault(s, false));
                        jsonArray.add(jsonObject);
                    }

//                    FileWriter fwriter = null;
//                    try {
//                        // true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好
//                        fwriter = new FileWriter(output);
//                        fwriter.write(jsonArray.toJSONString());
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    } finally {
//                        try {
//                            fwriter.flush();
//                            fwriter.close();
//                        } catch (IOException ex) {
//                            ex.printStackTrace();
//                        }
//                    }

                    List<Repository> data = JSON.parseArray(jsonArray.toJSONString(), Repository.class);
                    data.sort(new Comparator<Repository>() {
                        @Override
                        public int compare(Repository o1, Repository o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    List<Repository> data0 = JSON.parseArray(jsonArray.toJSONString(), Repository.class);
                    data.sort(new Comparator<Repository>() {
                        @Override
                        public int compare(Repository o1, Repository o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    if (data0 == null) {
                        Utils.showNotification(e, "error", "提示", "项目列表解析失败");
                    } else {
                        HomeDialog dialog = new HomeDialog(e, data, data0);
                        dialog.pack();
                        dialog.setVisible(true);
                    }
                }
            }
        });
    }



}
