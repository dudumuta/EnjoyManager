package com.youzan.mobile.enjoyplugin;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.youzan.mobile.enjoyplugin.callback.ExecCallback;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class Utils {

    /**
     * 获取当前分支名
     *
     * @return
     */
    public static String getCurrentBranch(AnActionEvent event, String filePath) {
        String command = "git rev-parse --abbrev-ref HEAD";
        File file = new File(filePath);
        try {
            Process p = Runtime.getRuntime().exec(command, null, file);
            int status = p.waitFor();
            if (status != 0) {
                System.out.println("Failed to call shell's command ");
                BufferedReader errorInput = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String lineError;
                while ((lineError = errorInput.readLine()) != null) {
                    showNotification(event, "error", "提示", lineError);
                }
                return "";
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("错误了：" + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 执行命令
     *
     * @param command
     */
    public static void exec(String command, File file, ExecCallback callback) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command, null, file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder data = new StringBuilder();
            String temp;
            while ((temp = reader.readLine()) != null) {
                data.append(temp);
            }
            int exitValue = process.waitFor();
            if (exitValue != 0) {
                callback.onError();
            } else {
                callback.onSuccess(data.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getFlavor(String filePath) {
        String flavor = "";
        File file = new File(filePath);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));//构造一个BufferedReader类来读取文件
            String s;
            while ((s = br.readLine()) != null) {
                if (s.contains("SELECTED_BUILD_VARIANT")) {
                    if (s.contains("pad")) {
                        flavor = "Pad";
                    } else if (s.contains("phone")) {
                        flavor = "Phone";
                    }
                    break;
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flavor;
    }

    /**
     * 读取文件
     *
     * @param fileName
     * @return
     */
    public static String readFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            if (!jsonFile.exists()) {
                return null;
            }
            FileReader fileReader = new FileReader(jsonFile);

            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
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

    public static void showNotification(@NotNull AnActionEvent e, String displayId, String title, String message) {
        NotificationGroup noti = new NotificationGroup(displayId, NotificationDisplayType.BALLOON, true);
        noti.createNotification(
                title,
                message,
                NotificationType.INFORMATION,
                null
        ).notify(e.getProject());
    }
}
