package com.xiaomi.shop.build.gradle.plugins

import com.android.build.gradle.api.ApplicationVariant
import com.xiaomi.shop.build.gradle.plugins.base.BaseGradlePlugin
import com.xiaomi.shop.build.gradle.plugins.hooker.StableHostResourceHooker
import com.xiaomi.shop.build.gradle.plugins.hooker.manager.TaskHookerManager
import org.gradle.api.Plugin
import org.gradle.api.Project

class HostGradlePlugin extends BaseGradlePlugin {
    HostTaskHookerManager mTaskHookerManager


    @Override
    void apply(Project project) {
        super.apply(project)
        project.afterEvaluate {
            mTaskHookerManager = new HostTaskHookerManager(project)
            mTaskHookerManager.registerTaskHookers(this)
        }
    }

    @Override
    protected onBeforePreBuildTask(ApplicationVariant variant) {
        loadDependencies(variant, null)
        backupOriginalRFile(variant)
    }

    static class HostTaskHookerManager extends TaskHookerManager {

        HostTaskHookerManager(Project mProject) {
            super(mProject)
        }

        @Override
        void registerTaskHookers(Plugin plugin) {
            mProject.android.applicationVariants.each {
                if (it.name == "release" || it.name == "debug") {//目前不支持flavor
                    registerTaskHooker(new StableHostResourceHooker(mProject, it))
                }
            }
        }
    }
}
