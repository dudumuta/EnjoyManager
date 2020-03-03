package com.youzan.mobile.enjoyplugin;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.youzan.mobile.enjoyplugin.module.EnjoyModule;
import com.youzan.mobile.enjoyplugin.module.ModuleInfo;

import java.io.*;
import java.util.*;

/**
 * 文件管理
 */
public class FileManager {

    private static volatile FileManager instance;

    private final String versionFilePath = "/version.properties";
    private final String localFilePath = "/local.properties";
    private final String enjoyFilePath = "/enjoyManager/enjoy.json";

    //module owner
    private HashMap<String, String> moduleOwner = new HashMap<>();

    //module info 集合
    private List<ModuleInfo> moduleInfos = new ArrayList<>();

    //分支缓存
    private EnjoyLRU<String, EnjoyModule> enjoyModuleCache;

    private FileManager() {

    }

    private FileManager(AnActionEvent event) {
        Objects.requireNonNull(event.getProject());
        clearInfo();
        File versionFile = new File(event.getProject().getBasePath() + versionFilePath);
        File localFile = new File(event.getProject().getBasePath() + localFilePath);
        enjoyModuleCache = EnjoyLRU.getInstance();
        fillModuleOwner();
        fillModuleInfo(event, versionFile);
        updateModuleInfoFromLocal(localFile);
        if (moduleInfos.size() > 0) {
            File enjoyFile = new File(event.getProject().getBasePath() + enjoyFilePath);
            fillModuleOtherInfo(event, enjoyFile);
        }
    }

    public static FileManager getInstance(AnActionEvent e) {
//        if (instance == null) {
//            synchronized (FileManager.class) {
//                if (instance == null) {
//                    instance = new FileManager(e);
//                }
//            }
//        }
        return new FileManager(e);
    }

    private HashMap<String, String> loadOwnerMap() {
        return moduleOwner;
    }

    public List<ModuleInfo> loadModuleInfoList() {
        for (ModuleInfo moduleInfo : moduleInfos) {
            moduleInfo.owner = moduleOwner.get(moduleInfo.name);
        }

        moduleInfos.sort(new Comparator<ModuleInfo>() {
            @Override
            public int compare(ModuleInfo o1, ModuleInfo o2) {
                return o1.name.compareTo(o2.name);
            }
        });

        return moduleInfos;
    }

    public EnjoyLRU<String, EnjoyModule> loadEnjoyModule() {
        return enjoyModuleCache;
    }

    private void clearInfo() {
        moduleInfos.clear();
        enjoyModuleCache = null;
    }

    /**
     * 填充module owner
     */
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

    /**
     * 填充module Info
     * 是首次数据来源
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
                        ModuleInfo moduleInfo = new ModuleInfo();
                        moduleInfo.name = temp[0].trim();
                        moduleInfo.version = temp[1].trim();
                        moduleInfo.choose = false;
                        moduleInfos.add(moduleInfo);
                    }
                }
            }
        } else {
            Utils.showNotification(e, "error", "提示", "version.properties文件不存在，modules版本解析失败");
        }
    }

    /**
     * 从local.properties来加载数据
     */
    private void updateModuleInfoFromLocal(File localFile) {
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
                if (!list.contains(str)) {
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
                    for (ModuleInfo info : moduleInfos) {
                        if (info.name.equals(temp[0].trim())) {
                            info.version = temp[1].trim();
                            break;
                        }
                    }
                }
            }
        }
    }

    private void fillModuleOtherInfo(AnActionEvent event, File enjoyFile) {
        Objects.requireNonNull(event.getProject());
        if (!enjoyFile.getParentFile().exists()) {
            enjoyFile.getParentFile().mkdir();
        }

        if (!enjoyFile.exists()) {
            try {
                enjoyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        String enjoyJson = Utils.readFile(enjoyFile.getAbsolutePath());
        if (enjoyJson == null) {
            return;
        }
        if (!enjoyJson.contains("branch")) {
            //不包含branch字段，则直接删除，新建
            enjoyFile.delete();
            try {
                enjoyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        List<EnjoyModule> enjoyModules = JSON.parseArray(enjoyJson, EnjoyModule.class);
        for (EnjoyModule enjoy : enjoyModules) {
            enjoyModuleCache.put(enjoy.branch, enjoy);
        }
        List<ModuleInfo> temp = null;
        for (EnjoyModule module : enjoyModules) {
            if (Utils.getCurrentBranch(event, event.getProject().getBasePath()).equals(module.branch)) {
                //如果分支匹配，则提取数据
                temp = module.modules;
                temp.sort(new Comparator<ModuleInfo>() {
                    @Override
                    public int compare(ModuleInfo o1, ModuleInfo o2) {
                        return o1.name.compareTo(o2.name);
                    }
                });
//                autoClean = module.autoClean;
//                autoOpenBuild = module.autoOpenBuild;
                break;
            }
        }

        if (temp != null) {
            for (ModuleInfo moduleInfo : temp) {
                for (ModuleInfo info : moduleInfos) {
                    if (info.name.equals(moduleInfo.name)) {
                        info.choose = moduleInfo.choose;
                        info.uninstall = moduleInfo.uninstall;
                    }
                }
            }
        }
    }
}
