package com.xiaomi.shop.build.gradle.plugins.tasks

import com.android.annotations.NonNull
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.tasks.IncrementalTask
import com.android.build.gradle.internal.tasks.factory.VariantTaskCreationAction
import com.xiaomi.shop.build.gradle.plugins.utils.Log
import org.gradle.api.Project

class AssembleFilteredPluginTask extends IncrementalTask {
    public static final String GROUP = "filter"
    public static final String TASK_NAME_PREFIX = "assembleFiltered"

    File outputDir
    File outputApkFile
    File originalApkFile
    ApplicationVariant applicationVariant

    @Override
    protected void doFullTaskAction() throws Exception {
        applicationVariant.outputs.each {
            Log.i(name, "移动package至hooker文件夹 [${it.outputFile}]")
        }
        String outputFileName = "${getProject().name}_${applicationVariant.name}_filtered.apk";
        outputApkFile = new File(outputDir, outputFileName)
        if (outputApkFile.exists()) {
            outputApkFile.delete()
        }
        getProject().copy {
            from originalApkFile
            into outputDir
            rename { outputFileName }
        }
    }

    static class AssembleFilteredPluginCreationAction extends VariantTaskCreationAction<AssembleFilteredPluginTask> {
        Project mProject
        ApplicationVariant mVariant


        AssembleFilteredPluginCreationAction(@NonNull Project project, @NonNull ApplicationVariant variant) {
            super(variant.variantData.scope)
            mVariant = variant
            mProject = project
        }

        @Override
        String getName() {
            return "${TASK_NAME_PREFIX}${mVariant.name.capitalize()}"
        }

        @NonNull
        @Override
        Class<AssembleFilteredPluginTask> getType() {
            return AssembleFilteredPluginTask.class
        }

        @Override
        void configure(@NonNull AssembleFilteredPluginTask task) {
            super.configure(task)
            task.outputDir = new File([mProject.getProjectDir(), "hooker", mVariant.name, "outputs"].join(File.separator))
            task.setGroup(GROUP)
            task.applicationVariant = mVariant
            task.originalApkFile = mVariant.outputs[0].outputFile
            task.dependsOn(mVariant.assembleProvider.name)
            task.setDescription("assemble{${mVariant.name.capitalize()}")
            task.outputs.upToDateWhen { false }
        }
    }
}
