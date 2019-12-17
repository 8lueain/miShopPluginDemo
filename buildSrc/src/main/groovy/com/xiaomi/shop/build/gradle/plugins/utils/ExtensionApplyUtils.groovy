package com.xiaomi.shop.build.gradle.plugins.utils

import com.xiaomi.shop.build.gradle.plugins.extension.PluginConfigExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.artifacts.ModuleVersionSelector
import org.gradle.api.artifacts.ResolutionStrategy

class ExtensionApplyUtils {

    //检查是否强制使用host的依赖文件版本
    static void applyUseHostResourceConfig(Project project) {
        HashSet<String> replacedSet = [] as HashSet
        project.configurations.all { Configuration configuration ->
            configuration.resolutionStrategy { ResolutionStrategy resolutionStrategy ->
                resolutionStrategy.eachDependency { DependencyResolveDetails pluginDr ->

                    ModuleVersionSelector pluginDependency = pluginDr.requested
                    PluginConfigExtension pluginConfigExtension = ProjectDataCenter.getInstance(project).pluginConfigExtension

                    def hostDependency = ProjectDataCenter.getInstance(project).hostPackageManifest
                            .dependenciesMap.get("${pluginDependency.group}:${pluginDependency.name}")

                    if (hostDependency != null) {
                        if ("${pluginDependency.version}" != "${hostDependency['version']}") {
                            String key = "${project.name}:${pluginDependency}"
                            if (!replacedSet.contains(key)) {
                                replacedSet.add(key)
                                if (pluginConfigExtension.useHostDependencies) {
                                    Log.i 'Dependencies', "注意，依赖版本出现不一致，已将 ${project.name} 的 [${pluginDependency}] 更换为 host 的版本: [${hostDependency['version']}]!"
                                } else {
                                    Log.i 'Dependencies', "注意，依赖版本出现不一致， ${project.name} 的 [${pluginDependency}] 更换为 host 的版本: [${hostDependency['version']}]!"
                                }
                            }

                            if (pluginConfigExtension.useHostDependencies) {
                                pluginDr.useVersion(hostDependency['version'])
                            }
                        }
                    }
                }
            }
        }
    }
}
