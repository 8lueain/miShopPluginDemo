package com.example.plugintest.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import com.example.plugintest.PluginInfo;
import dalvik.system.DexClassLoader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class PluginDataManager {
    private static final String TAG = "FilesUtils";
    private static PluginDataManager mInstance;

    private static final String plugins_asset_dir = "plugins";
    public static final String plugins_dir = "/plugins";
    public static final String unzip_dir = "/unzip";
    public static HashMap<String, PluginInfo> mPluginResources = new HashMap<>();
    private final Context mContext;

    private PluginDataManager(Context context) {
        mContext = context;
    }

    public static PluginDataManager getInstance(Context context) {
        if (null == mInstance) {
            synchronized (PluginDataManager.class) {
                if (null == mInstance) {
                    mInstance = new PluginDataManager(context);
                }
            }
        }
        return mInstance;
    }

    public String getPluginsDirFullPath(Context context) {
        return context.getApplicationContext().getFilesDir().getAbsoluteFile() + plugins_dir;
    }

    public String getPluginsUnzipDirFullPath(Context context) {
        String fullUnzipPath = getPluginsDirFullPath(context) + unzip_dir;
        File unzipDir = new File(fullUnzipPath);
        if (!unzipDir.exists()) {
            unzipDir.mkdirs();
        }
        return fullUnzipPath;
    }

    public void generateAllResource() {
        File[] files = new File(getPluginsDirFullPath(mContext)).listFiles();
        StringBuilder addApkDex = new StringBuilder();
        for (File file : files) {
            if (file.getName().contains("apk")) {
                addApkDex.append(file.getAbsolutePath()).append(File.pathSeparator);
            }
        }
        DexClassLoader pluginClassLoader = new DexClassLoader(addApkDex.toString(),
                getPluginsDirFullPath(mContext) + unzip_dir,
                null, mContext.getClassLoader());
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.contains("apk")) {
                if (null == mPluginResources.get(fileName)) {
                    PluginInfo pluginInfo = new PluginInfo();
                    pluginInfo.resources = new MixResources(mContext.getResources(),
                            createPluginResource(mContext, file.getPath()));
                    pluginInfo.classLoader = pluginClassLoader;
                    pluginInfo.theme = mContext.getTheme();
                    mPluginResources.put(fileName, pluginInfo);
                }
            }
        }
    }

    public PluginInfo getPluginInfo(String pluginName) {
        return mPluginResources.get(pluginName);
    }

    private Resources createPluginResourceByAddPath(Context context, String pluginPath) {
        try {
//            String restoredPath = getPluginsDirFullPath(context) + File.separator + pluginName;
//            Reflect reflect = Reflect.on("android.content.res.AssetManager")
//                    .create();
//            reflect.call("addAssetPath", restoredPath);
//            AssetManager assetManager = reflect.get();
            AssetManager newAssetManager = AssetManager.class.getConstructor().newInstance();
            Method mAddAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            mAddAssetPath.setAccessible(true);
            if (((Integer) mAddAssetPath.invoke(newAssetManager, pluginPath)) == 0) {
                throw new IllegalStateException("Could not create newAssetManager");
            }
            Resources res = new CustomResource(newAssetManager, context.getResources().getDisplayMetrics(),
                    context.getResources().getConfiguration());
//            AssetManager am = AssetManager.class.newInstance();
//            am.getClass().getMethod("addAssetPath", String.class).invoke(am, restoredPath);
//            Resources res = new Resources(am, context.getResources().getDisplayMetrics(), context
//                    .getResources().getConfiguration());
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "loadPlugins: ", e);
        }
        return null;
    }

    private Resources createPluginResource(Context context, String restoredPath) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageArchiveInfo(restoredPath,
                    PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES | PackageManager.GET_PROVIDERS | PackageManager.GET_RECEIVERS | PackageManager.GET_META_DATA);
            info.applicationInfo.publicSourceDir = restoredPath;
            info.applicationInfo.sourceDir = restoredPath;
            return manager.getResourcesForApplication(info.applicationInfo);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "loadPlugins: ", e);
        }
        return null;
    }

    public void copyAllPlugin(Context context) {
        try {
            //1:建立unzipPath
            File pluginDir = new File(getPluginsDirFullPath(context));

            //拷贝到本地目录
            byte[] bf = new byte[1024];
            BufferedInputStream inputStream = new BufferedInputStream(context.getAssets().open("pluginc.apk"));
            FileOutputStream fileOutputStream = new FileOutputStream(new File(pluginDir.getAbsolutePath()));
            while (inputStream.read(bf) != -1) {
                fileOutputStream.write(bf);
            }
            inputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "copyPlugin: ", e);
        }
    }
}
