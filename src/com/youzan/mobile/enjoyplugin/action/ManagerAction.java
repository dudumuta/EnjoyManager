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
import com.youzan.mobile.enjoyplugin.entity.Repository;
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
        Project project = e.getProject();
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                File file = new File(e.getProject().getBasePath() + "/app/app.iml");
                ArrayList<String> resultList = new ArrayList<>();
                File versionFile = new File(e.getProject().getBasePath() + "/version.properties");
                HashMap<String, String> versions = new HashMap<>();
                if (file.exists()) {
                    ArrayList<String> list = new ArrayList<>();
                    try {
                        InputStreamReader inputReader = new InputStreamReader(new FileInputStream(file));
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
                            if (s.contains("type=\"module\"")) {
                                String[] temp = s.split("=");
                                String result="";
                                if (temp.length > 2) {
                                    result = temp[2].substring(1, temp[2].lastIndexOf("\""));
                                }
                                if (result.contains("modules-")) {
                                    resultList.add(result.substring(result.lastIndexOf("-") + 1));
                                } else {
                                    resultList.add(result);
                                }
                            }
                        }
                    }
                } else {
                    showNotification(e, "error", "提示", "modules信息解析失败");
                }

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
                            }
                        }
                    }
                } else {
                    showNotification(e, "error", "提示", "modules版本解析失败");
                }

                if (resultList.size() > 0 && versions.size() > 0) {
                    File outputDir = new File(e.getProject().getBasePath() + "/enjoyManager");
                    File output = new File(e.getProject().getBasePath() + "/enjoyManager/enjoy.json");
                    HashMap<String, Boolean> chooses = new HashMap<>();
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
                        String enjoyJson = readJsonFile(output.getAbsolutePath());
                        List<Repository> repositories = JSON.parseArray(enjoyJson, Repository.class);
                        repositories.sort(new Comparator<Repository>() {
                            @Override
                            public int compare(Repository o1, Repository o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                        for (Repository repositorie : repositories) {
                            chooses.put(repositorie.getName(), repositorie.getChoose());
                            uninstallChoose.put(repositorie.getName(), repositorie.getUninstall());
                        }
                    }

                    JSONArray jsonArray = new JSONArray(resultList.size());
                    for (String s : resultList) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("name", s);
                        jsonObject.put("choose", chooses.getOrDefault(s, false));
                        jsonObject.put("version", versions.get(s));
                        jsonObject.put("uninstall", uninstallChoose.getOrDefault(s, false));
                        jsonArray.add(jsonObject);
                    }

                    FileWriter fwriter = null;
                    try {
                        // true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好
                        fwriter = new FileWriter(output);
                        fwriter.write(jsonArray.toJSONString());
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
                    if (data == null || data0 == null) {
                        showNotification(e, "error", "提示", "项目列表解析失败");
                    } else {
                        HomeDialog dialog = new HomeDialog(e, data, data0);
                        dialog.pack();
                        dialog.setVisible(true);
                    }
                }
            }
        });
    }

    private void showNotification(@NotNull AnActionEvent e, String displayId, String title, String message) {
        NotificationGroup noti = new NotificationGroup(displayId, NotificationDisplayType.BALLOON, true);
        noti.createNotification(
                title,
                message,
                NotificationType.INFORMATION,
                null
        ).notify(e.getProject());
    }

    private String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);

            Reader reader = new InputStreamReader(new FileInputStream(jsonFile),"utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
