package com.xiaomi.shop.build.gradle.plugins.hooker

import com.android.build.gradle.api.ApkVariant
import com.xiaomi.shop.build.gradle.plugins.ModuleGradlePlugin
import com.xiaomi.shop.build.gradle.plugins.extension.PluginConfigExtension
import com.xiaomi.shop.build.gradle.plugins.utils.ProjectDataCenter
import org.gradle.api.DefaultTask
import org.gradle.api.Project

/**
 * 负责收集project所有的依赖,主要包括三种 1:aar类型 2:jar类型 3:project类型
 * preBuildTask为占位的锚点
 */
class PluginPreBuildTaskHooker extends GradleTaskHooker<DefaultTask> {
    ModuleGradlePlugin plugin

    PluginPreBuildTaskHooker(Project project, ApkVariant apkVariant) {
        super(project, apkVariant)
        plugin = project.plugins.findPlugin(ModuleGradlePlugin.class)
    }

    @Override
    String getTaskName() {
        return scope.getTaskName('pre', 'Build')
    }


    @Override
    void beforeTaskExecute(DefaultTask task) {
        println("AppPreBuildTaskHooker beforeTaskExecute")


    }

    @Override
    void afterTaskExecute(DefaultTask task) {
        println("AppPreBuildTaskHooker afterTaskExecute")
        loadDependenciesMap()
    }

    /**
     * 初始化依赖相关的内存对象
     */
    void loadDependenciesMap() {
        PluginConfigExtension extension = project.pluginconfig
        ProjectDataCenter projectDataCenter = ProjectDataCenter.getInstance(project, apkVariant.name)
        //host
        Project hostProject = project.getRootProject().getAllprojects().find {
            it != project.getRootProject() && extension.hostPath.contains(it.name)
        }
        if (null != hostProject) {
            def hostReleaseVariant = hostProject.android.applicationVariants.find {
                it.name == apkVariant.name// 暂时不支持flavor
            }
            if (null != hostReleaseVariant) {
                projectDataCenter.hostPackageManifest.packageName = hostReleaseVariant.applicationId
                projectDataCenter.hostPackageManifest.packagePath = hostReleaseVariant.applicationId.replace('.'.charAt(0), File.separatorChar)
                plugin.loadDependencies(apkVariant, projectDataCenter.hostPackageManifest)
            }
        }
        projectDataCenter.pluginPackageManifest.packageName = apkVariant.applicationId
        projectDataCenter.pluginPackageManifest.packagePath = apkVariant.applicationId.replace('.'.charAt(0), File.separatorChar)
        projectDataCenter.pluginPackageManifest.dependenciesFile =new File(project.ext."hookerDir_${apkVariant.name}" , "dependencies")
        println("has set mDependenciesFile")
        plugin.loadDependencies(apkVariant, projectDataCenter.pluginPackageManifest)
    }

}