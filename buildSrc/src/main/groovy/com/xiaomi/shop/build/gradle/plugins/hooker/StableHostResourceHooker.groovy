package com.xiaomi.shop.build.gradle.plugins.hooker

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask
import com.xiaomi.shop.build.gradle.plugins.utils.Log
import com.xiaomi.shop.build.gradle.plugins.utils.ResourceFormatUtils
import org.gradle.api.Project

class StableHostResourceHooker extends GradleTaskHooker<LinkApplicationAndroidResourcesTask> {
    final static String sFileInputName = 'stable_id_input.txt'
    final static String sFileOutputName = 'stable_id_output.txt'
    File mStableOutputFile
    File mStableInputFile
    AppExtension mAndroidExtension

    @Override
    String getTaskName() {
        return scope.getTaskName('process', 'Resources')
    }

    StableHostResourceHooker(Project project, ApkVariant apkVariant) {
        super(project, apkVariant)
//        mStableOutputFile = new File([ShopBasePlugin.mHookerDir, sFileOutputName].join(File.separator))
        mAndroidExtension = project.getExtensions().findByType(AppExtension.class)
//        configStableParam()
    }


    void configStableParamFromTask(LinkApplicationAndroidResourcesTask task) {
//        if (mStableOutputFile.exists()) {
//            Log.i "ProcessResourcesHooker", "${mStableOutputFile} not exist , generate it."
//            mStableOutputFile.delete()
//        }
//        mStableOutputFile.createNewFile()
        mStableInputFile = new File([project.ext."hookerDir_${apkVariant.name}", sFileInputName].join(File.separator))
        if (mStableInputFile.exists()) {
            task.aaptOptions.additionalParameters("--stable-ids", "${mStableInputFile}")
        }
//        task.aaptOptions.additionalParameters("--emit-ids", "${mStableOutputFile}")
    }


    @Override
    void beforeTaskExecute(LinkApplicationAndroidResourcesTask linkAndroidResForBundleTask) {
        Log.i("StableHostResourceHooker", "beforeTaskExecute")
        configStableParamFromTask(linkAndroidResForBundleTask)
    }

    @Override
    void afterTaskExecute(LinkApplicationAndroidResourcesTask linkAndroidResForBundleTask) {
        Log.i("StableHostResourceHooker", "afterTaskExecute")
//        if (mStableOutputFile.exists()) {
//            mStableOutputFile.delete()
//        }
        if (mStableInputFile.exists()) {
            mStableInputFile.delete()
        }
        ResourceFormatUtils.convertRFile2Stable(apkVariant.applicationId,
                linkAndroidResForBundleTask.textSymbolOutputFile, mStableInputFile)
//        project.copy {
//            from mStableOutputFile
//            into mStableOutputFile.getParentFile()
//            rename { sFileInputName }
//        }
//        mStableOutputFile.delete()
    }
}
