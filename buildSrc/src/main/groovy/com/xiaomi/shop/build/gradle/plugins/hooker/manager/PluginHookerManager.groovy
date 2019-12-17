package com.xiaomi.shop.build.gradle.plugins.hooker.manager


import com.xiaomi.shop.build.gradle.plugins.hooker.LinkApplicationAndroidResourcesTaskHooker
import com.xiaomi.shop.build.gradle.plugins.hooker.PluginPreBuildTaskHooker
import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginHookerManager extends TaskHookerManager {

    PluginHookerManager(Project project) {
        super(project)
    }

    @Override
    void registerTaskHookers(Plugin plugin) {
        mProject.android.applicationVariants.each {
            if (it.name == "release" || it.name == "debug") {//目前不支持flavor
                registerTaskHooker(new LinkApplicationAndroidResourcesTaskHooker(mProject, it))
                registerTaskHooker(new PluginPreBuildTaskHooker(mProject, it))
            }
        }
    }
}