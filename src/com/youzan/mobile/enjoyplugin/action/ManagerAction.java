package com.youzan.mobile.enjoyplugin.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.youzan.mobile.enjoyplugin.Utils;
import com.youzan.mobile.enjoyplugin.module.EnjoyModule;
import com.youzan.mobile.enjoyplugin.module.ModuleInfo;
import com.youzan.mobile.enjoyplugin.ui.HomeDialog;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ManagerAction extends AnAction {

    private final String versionFilePath = "/version.properties";
    private final String localFilePath = "/local.properties";
    private final String enjoyFilePath = "/enjoyManager/enjoy.json";

    //module name list
    private ArrayList<String> modules = new ArrayList<>();
    //module version map
    private HashMap<String, String> versionMap = new HashMap<>();
    //module choose info
    HashMap<String, Boolean> chooses = new HashMap<>();
    //module uninstall info
    HashMap<String, Boolean> uninstallChoose = new HashMap<>();
    //module owner
    HashMap<String, String> moduleOwner = new HashMap<>();

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
                clearInfo();
                fillModuleOwner();
                //version文件
                File versionFile = new File(e.getProject().getBasePath() + versionFilePath);
                fillModuleInfo(e, versionFile);
                File localFile = new File(e.getProject().getBasePath() + localFilePath);
                fillModuleInfoFromLocal(localFile);

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
                        jsonObject.put("owner", moduleOwner.get(s));
                        jsonArray.add(jsonObject);
                    }

                    List<ModuleInfo> data = JSON.parseArray(jsonArray.toJSONString(), ModuleInfo.class);
                    data.sort(new Comparator<ModuleInfo>() {
                        @Override
                        public int compare(ModuleInfo o1, ModuleInfo o2) {
                            return o1.name.compareTo(o2.name);
                        }
                    });
                    if (data.size() == 0) {
                        Utils.showNotification(e, "error", "提示", "项目列表解析失败");
                    } else {
                        HomeDialog dialog = new HomeDialog(e, data, autoClean, autoOpenBuild);
                        dialog.pack();
                        dialog.setLocationRelativeTo(null);
                        dialog.setVisible(true);
                    }
                }
            }
        });
    }

    /**
     * 从local.properties来加载数据
     */
    private void fillModuleInfoFromLocal(File localFile) {
        if (!localFile.exists()) {
            return;
        }

        ArrayList<String> list = new ArrayList<>();
        try {
            InputStreamReader inputReader = new InputStreamReader(new FileInputStream(localFile));
            BufferedReader bf = new BufferedReader(inputReader);
            // 按行读取字符串
            String str;
            while ((str = bf.readLine()) != null) {
                if (isLegalInfo(str)) {
                    list.add(str);
                }
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
                    versionMap.replace(temp[0].trim(), temp[1].trim());
                }
            }
        }
    }

    /**
     * 判断传入字段是否合法
     *
     * @param line
     */
    private boolean isLegalInfo(String line) {
        if (line.isEmpty()) {
            return false;
        }

        for (String name : modules) {
            if (line.contains(name) && line.contains("=")) {
                return true;
            }
        }
        return false;
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
                List<ModuleInfo> moduleInfos = JSON.parseArray(enjoyJson, ModuleInfo.class);
                if (moduleInfos != null) {
                    moduleInfos.sort(new Comparator<ModuleInfo>() {
                        @Override
                        public int compare(ModuleInfo o1, ModuleInfo o2) {
                            return o1.name.compareTo(o2.name);
                        }
                    });
                    for (ModuleInfo moduleInfo : moduleInfos) {
                        chooses.put(moduleInfo.name, moduleInfo.choose);
                        uninstallChoose.put(moduleInfo.name, moduleInfo.uninstall);
                    }
                }
            } else if (enjoyJson != null) {
                //如果有branch字段认为是新版本，采用新逻辑
                List<EnjoyModule> enjoyModules = JSON.parseArray(enjoyJson, EnjoyModule.class);
                List<ModuleInfo> temp = null;
                for (EnjoyModule module : enjoyModules) {
                    if (Utils.getCurrentBranch(e.getProject().getBasePath()).equals(module.branch)) {
                        //如果分支匹配，则提取数据
                        temp = module.modules;
                        temp.sort(new Comparator<ModuleInfo>() {
                            @Override
                            public int compare(ModuleInfo o1, ModuleInfo o2) {
                                return o1.name.compareTo(o2.name);
                            }
                        });
                        autoClean = module.autoClean;
                        autoOpenBuild = module.autoOpenBuild;
                    }
                }
                if (temp != null) {
                    for (ModuleInfo moduleInfo : temp) {
                        chooses.put(moduleInfo.name, moduleInfo.choose);
                        uninstallChoose.put(moduleInfo.name, moduleInfo.uninstall);
                    }
                }
            }
        }
    }

    private void fillModuleOwner() {
        moduleOwner.put("module_data", "伯安");
        moduleOwner.put("module_sale", "浅浅");
        moduleOwner.put("module_paysdk", "明义");
        moduleOwner.put("module_trade", "昌平");
        moduleOwner.put("module_device", "龙舟");
        moduleOwner.put("module_settings", "伯安/腊肠");
        moduleOwner.put("module_verify", "浅浅/光线");
        moduleOwner.put("module_subscreen", "明天");
        moduleOwner.put("module_message", "昌平");
        moduleOwner.put("module_marketing", "明天");
        moduleOwner.put("module_asset", "刘洋");
        moduleOwner.put("lib_common", "coffee涛");
        moduleOwner.put("module_goods", "Alex");
        moduleOwner.put("module_stock", "永康");
        moduleOwner.put("module_member", "光线");
        moduleOwner.put("module_prepay", "光线");
        moduleOwner.put("module_account", "腊肠");
        moduleOwner.put("module_shop", "腊肠");
        moduleOwner.put("module_home", "腊肠");
        moduleOwner.put("module_shoppingguide", "佳娟");
        moduleOwner.put("module_mediator", "光线");
        moduleOwner.put("module_face", "川普");
        moduleOwner.put("module_scanner", "光线");
        moduleOwner.put("module_staff", "腊肠/伯安");
        moduleOwner.put("module_expenses", "鲁班");
    }

    private void clearInfo() {
        modules.clear();
        versionMap.clear();
        chooses.clear();
        uninstallChoose.clear();
        moduleOwner.clear();
    }
}
