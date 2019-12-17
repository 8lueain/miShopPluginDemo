package com.xiaomi.shop.build.gradle.plugins.base

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.ide.dependencies.BuildMappingUtils
import com.android.builder.model.AndroidLibrary
import com.android.builder.model.Dependencies
import com.android.builder.model.JavaLibrary
import com.android.builder.model.SyncIssue
import com.google.common.collect.ImmutableMap
import com.xiaomi.shop.build.gradle.plugins.bean.PackageManifest
import com.xiaomi.shop.build.gradle.plugins.bean.dependence.AarDependenceInfo
import com.xiaomi.shop.build.gradle.plugins.bean.dependence.JarDependenceInfo
import com.xiaomi.shop.build.gradle.plugins.utils.FileUtil
import com.xiaomi.shop.build.gradle.plugins.utils.Log
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import java.util.function.Consumer

class BaseGradlePlugin implements Plugin<Project> {

    Project mProject
    AppExtension mAndroidExtension
    ApplicationVariant mAppReleaseVariant
    ApplicationVariant mAppDebugVariant
    protected DomainObjectSet<ApplicationVariant> mApplicationVariants

    public File mHookerDir
    public File aaptResourceDir
    public File aaptSourceDir
    public File outputDir
    public File mDependenciesFile

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin('com.android.application')) {
            throw new Exception("需要在application中使用")
        }
        mProject = project
        mHookerDir = new File(project.getProjectDir(), "hooker")
        mDependenciesFile = new File(mHookerDir, "dependencies.txt")
        project.afterEvaluate {
            mAndroidExtension = project.extensions.findByType(AppExtension)
            mApplicationVariants = mAndroidExtension.applicationVariants
            mAppReleaseVariant = mApplicationVariants.find {
                it.name == "release"// 暂时不支持flavor
            }
            mAppDebugVariant = mApplicationVariants.find {
                it.name == "debug"// 暂时不支持flavor
            }
            //清除hooker中间文件
            project.tasks.findByName("clean").doLast {
                if (mHookerDir.exists()) {
                    mHookerDir.deleteDir()
                }
            }
            //生成dependencies文件
            mAndroidExtension.applicationVariants.each { variant ->
                if (variant.name == "release" || variant.name == "debug") {
                    Task preBuild = project.tasks.findByName("pre${variant.name.capitalize()}Build")
                    preBuild.outputs.upToDateWhen {
                        false
                    }
                    preBuild.doFirst {
                        createHookerDir(variant)
                        onBeforePreBuildTask(variant)
                    }
                } else {
                    Log.e(project.name, "抱歉,暂不支持此[$variant.name]!")
                }
            }

        }
    }

    protected onBeforePreBuildTask(ApplicationVariant variant) {

    }


    /**
     * 备份R.txt文件
     * @param variant
     * @return
     */
    def backupOriginalRFile(ApplicationVariant variant) {
        //生成resource文件
        project.tasks["process${variant.name.capitalize()}Resources"].doLast { task ->
            mProject.copy {
                from task.textSymbolOutputFile
                into project.ext."hookerDir_${variant.name}"
                rename { "original_resource_file.txt" }
            }
        }
    }

    def createHookerDir(ApplicationVariant variant) {
        if (!mHookerDir.exists()) {
            mHookerDir.mkdir()
        }
        File dirWithVariant = new File(mHookerDir, variant.name)
        dirWithVariant.mkdir()
        println("createHookerDir [${dirWithVariant}]")
        project.ext."hookerDir_${variant.name}" = dirWithVariant
    }

    def createAaptWorkspace(ApplicationVariant variant) {
        println("method createWorkDir")
        //创建中间缓存文件
        createHookerDir(variant)
        //存放资源
        File aaptResourceDir = new File([project.ext."hookerDir_${variant.name}", "intermediates", "resource"].join(File.separator))
        if (!aaptResourceDir.parentFile.exists()) {
            aaptResourceDir.parentFile.mkdirs()
        }
        if (!aaptResourceDir.exists()) {
            aaptResourceDir.mkdir()
        }
        //存放源码
        File aaptSourceDir = new File([project.ext."hookerDir_${variant.name}", "intermediates", "source", "r"].join(File.separator))
        if (!aaptSourceDir.parentFile.exists()) {
            aaptSourceDir.parentFile.mkdirs()
        }
        if (!aaptSourceDir.exists()) {
            aaptSourceDir.mkdir()
        }
        File outputDir = new File([project.ext."hookerDir_${variant.name}", "outputs"].join(File.separator))
        if (!outputDir.parentFile.exists()) {
            outputDir.parentFile.mkdirs()
        }
        if (!outputDir.exists()) {
            outputDir.mkdir()
        }
        mProject.ext."aaptResourceDir_${variant.name}" = aaptResourceDir
        mProject.ext."aaptSourceDir_${variant.name}" = aaptSourceDir
        mProject.ext."outputDir_${variant.name}" = outputDir
    }

    def loadDependencies(ApplicationVariant variant, PackageManifest packageManifest) {
        println("loadDependencies")
        def collectAction = {
            List<String> dependenciesList = new ArrayList<String>()
            Consumer consumer = new Consumer<SyncIssue>() {
                @Override
                void accept(SyncIssue syncIssue) {
                }
            }
            ImmutableMap<String, String> buildMapping =
                    BuildMappingUtils.computeBuildMapping(mProject.getGradle())

            Dependencies dependencies = ApiVersionFactory.getInstance()
                    .getArtifactDependencyGraph(variant.variantData.scope,
                            false, buildMapping, consumer)


            dependencies.getLibraries().each { AndroidLibrary androidLibrary ->
                def androidCoordinates = androidLibrary.resolvedCoordinates
                dependenciesList.add(androidLibrary.name)
                if (null != packageManifest) {
                    packageManifest.aarDependenciesLibs.add(
                            new AarDependenceInfo(
                                    androidCoordinates.groupId,
                                    androidCoordinates.artifactId,
                                    androidCoordinates.version,
                                    androidLibrary))
                }

            }
            dependencies.getJavaLibraries().each { JavaLibrary library ->
//                println(" dependencies.getLibraries()[${library.name}]")
                dependenciesList.add(library.name)
                def jarCoordinates = library.resolvedCoordinates
                if (null != packageManifest) {
                    packageManifest.jarDependenciesLibs.add(
                            new JarDependenceInfo(
                                    jarCoordinates.groupId,
                                    jarCoordinates.artifactId,
                                    jarCoordinates.version,
                                    library))
                }

            }
            dependencies.getProjects().each { String path ->
//                println(" dependencies.getProjects[${path}]")
                dependenciesList.add(path)

            }
            Collections.sort(dependenciesList)

            return dependenciesList
        }
        if (new File(project.ext."hookerDir_${variant.name}", "dependencies").exists()) {
            collectAction()
        } else {
            FileUtil.saveFile(project.ext."hookerDir_${variant.name}", "dependencies", collectAction)
        }
    }

    Project getProject() {
        return mProject
    }

    File getHookerDir() {
        return mHookerDir
    }

    File getAaptResourceDir() {
        return aaptResourceDir
    }

}
