package com.xiaomi.shop.build.gradle.plugins.utils

import com.xiaomi.shop.build.gradle.plugins.bean.MergedPackageManifest
import com.xiaomi.shop.build.gradle.plugins.bean.PackageManifest
import com.xiaomi.shop.build.gradle.plugins.extension.PluginConfigExtension
import org.gradle.api.Project

/**
 * 汇总处理host和工程中各个plugin和lib的数据中心，通信中心
 *
 * */
class ProjectDataCenter {
    public static final String TYPE_DEBUG = "debug"
    public static final String TYPE_RELEASE = "release"
    static ProjectDataCenter sInstanceRelease
    static ProjectDataCenter sInstanceDebug

    private Project mProject
    private PluginConfigExtension mPluginConfigExtension

    private PackageManifest hostPackageManifest
    private PackageManifest pluginPackageManifest
    private MergedPackageManifest mergedPluginPackageManifest

    //refresh 是否刷新上次运行数据， 在重新运行plugin时，设为true
    private boolean hostNeedRefresh
    private boolean pluginNeedRefresh
    private boolean mergedPluginNeedRefresh

    static void init(Project project) {
        getInstance(project, TYPE_DEBUG).needRefresh = true
        getInstance(project, TYPE_RELEASE).needRefresh = true
    }

    static ProjectDataCenter getInstance(Project project) {
        return getInstance(project, TYPE_RELEASE)
    }

    static ProjectDataCenter getInstance(Project project, String variantName) {
        if (variantName == TYPE_DEBUG) {
            if (null == sInstanceDebug) {
                synchronized (ProjectDataCenter.class) {
                    if (null == sInstanceDebug) {
                        sInstanceDebug = new ProjectDataCenter(project)
                    }
                }
            }
            return sInstanceDebug
        }
        if (null == sInstanceRelease) {
            synchronized (ProjectDataCenter.class) {
                if (null == sInstanceRelease) {
                    sInstanceRelease = new ProjectDataCenter(project)
                }
            }
        }
        return sInstanceRelease
    }

    ProjectDataCenter(Project project) {
        mProject = project
        mPluginConfigExtension = mProject.pluginconfig
    }

    Project getProject() {
        return mProject
    }

    void setProject(Project project) {
        this.mProject = mProject
    }

    boolean getNeedRefresh() {
        return needRefresh
    }

    void setNeedRefresh(boolean needRefresh) {
        println("need data center refresh !!")
        this.hostNeedRefresh = needRefresh
        this.pluginNeedRefresh = needRefresh
        this.mergedPluginNeedRefresh = needRefresh
    }

    PackageManifest getHostPackageManifest() {
        if (null == hostPackageManifest || hostNeedRefresh) {
            println("create new hostPackageManifest ")
            hostNeedRefresh = false
            hostPackageManifest = new PackageManifest(mProject)
        }
        return hostPackageManifest
    }

    PackageManifest getPluginPackageManifest() {
        if (null == pluginPackageManifest || pluginNeedRefresh) {
            println("recreate getPluginPackageManifest null ?[${null == pluginPackageManifest}] isNeedFresh[${pluginNeedRefresh}]")
            pluginNeedRefresh = false
            pluginPackageManifest = new PackageManifest(mProject)
        }
        return pluginPackageManifest
    }

    MergedPackageManifest getMergedPluginPackageManifest() {
        if (!PackageManifestUtils.hasAaptProcessed(hostPackageManifest)) {
            throw new IllegalArgumentException("宿主资源初始化失败")
        }
        if (!PackageManifestUtils.hasAaptProcessed(pluginPackageManifest)) {
            throw new IllegalArgumentException("插件资源初始化失败")
        }
        if (null == mergedPluginPackageManifest || mergedPluginNeedRefresh) {
            mergedPluginNeedRefresh = false
            mergedPluginPackageManifest = new MergedPackageManifest(hostPackageManifest, pluginPackageManifest, mProject)
        }
        return mergedPluginPackageManifest
    }


    PluginConfigExtension getPluginConfigExtension() {
        return mPluginConfigExtension
    }

}
