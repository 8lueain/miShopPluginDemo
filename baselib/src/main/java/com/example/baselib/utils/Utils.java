package com.example.baselib.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {
    public static final String UTILS_STRIGN = "util_string";

    public static String getUtilsString() {
        return "from utils string";
    }

    public static String getUtilsString2() {
        return "from utils string2";
    }

    public static boolean deleteDirectory(String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            System.out.println("删除目录失败：" + dir + "不存在！");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (files[i].isDirectory()) {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            System.out.println("删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            System.out.println("删除目录" + dir + "成功！");
            return true;
        } else {
            return false;
        }
    }

    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }

    static public boolean copyAllAssertToCacheFolder(Context c)
            throws IOException {
        String[] files = c.getAssets().list("plugins");
        String filefolder = c.getFilesDir().toString();
        File devicefile = new File(filefolder + "/plugins/");
        if (devicefile.exists()) {
            deleteDirectory(devicefile.getPath());
        }
        devicefile.mkdirs();
        for (int i = 0; i < files.length; i++) {
            File devfile = new File(filefolder + "/plugins/" + files[i]);
            if (!devfile.exists()) {
                copyFileTo(c, "plugins/" + files[i], filefolder
                        + "/plugins/" + files[i]);
            }
        }
        String[] filestr = devicefile.list();
        for (int i = 0; i < filestr.length; i++) {
            Log.i("file", filestr[i]);
        }
        return true;
    }

    public static String[] getPluginFilePath(Context context) throws IOException {
        return context.getAssets().list("plugins");
    }


    public static String getPluginFilePathAll(Context context) {
        StringBuilder builder = new StringBuilder();
        String[] pluginFilePath = new File(context.getFilesDir().toString() + "/plugins/").list();
        for (int i = 0; i < pluginFilePath.length; i++) {
            String s = pluginFilePath[i];
            builder.append("/plugins/").append(s);
            if (i != pluginFilePath.length - 1) {
                builder.append(File.separator);
            }
        }
        return builder.toString();
    }

    static public boolean copyFileTo(Context c, String orifile,
                                     String desfile) throws IOException {
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(desfile);
        myInput = c.getAssets().open(orifile);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
        return true;
    }
}

