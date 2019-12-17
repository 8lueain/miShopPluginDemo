package com.xiaomi.shop.build.gradle.plugins.hooker

import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.res.GenerateLibraryRFileTask
import com.xiaomi.shop.build.gradle.plugins.extension.PluginConfigExtension
import org.gradle.api.Project

class GenerateLibraryRFileHooker extends GradleTaskHooker<GenerateLibraryRFileTask> {

    String mLibraryName
    File mHookerActionDir = new File([project.getBuildDir(), 'generated', 'hooker'].join(File.separator))

    GenerateLibraryRFileHooker(Project project, ApkVariant apkVariant) {
        super(project, apkVariant)
        PluginConfigExtension pluginConfigExtension = project.getExtensions().getByName("pluginConfig")
        if (pluginConfigExtension) {
            mLibraryName = pluginConfigExtension.libraryName
        }
    }

    @Override
    String getTaskName() {
       println("GenerateLibraryRFileHooker task name [${ variantData.scope.getTaskName('generate','RFile')}]")
        return variantData.scope.getTaskName('generate','RFile')
    }

    @Override
    void beforeTaskExecute(GenerateLibraryRFileTask task) {
        println("GenerateLibraryRFileHooker beforeTaskExecute -- name[${mLibraryName}]")
        if (task.project.name != mLibraryName)
            return
    }

    @Override
    void afterTaskExecute(GenerateLibraryRFileTask task) {
        println("GenerateLibraryRFileHooker afterTaskExecute -- name[${mLibraryName}]")
        println("GenerateLibraryRFileHooker --  projectname [${project.name}] ---libname[${mLibraryName}]")
        if (task.project.name != mLibraryName)
            return
        project.copy {
            from task.textSymbolOutputFile
            into mHookerActionDir
            rename { "library_R.txt" }
        }
    }
}