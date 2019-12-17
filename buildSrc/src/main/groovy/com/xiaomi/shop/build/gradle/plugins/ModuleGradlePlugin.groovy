package com.xiaomi.shop.build.gradle.plugins

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.tasks.factory.TaskFactory
import com.android.build.gradle.internal.variant.VariantFactory
import com.xiaomi.shop.build.gradle.plugins.base.ApiVersionFactory
import com.xiaomi.shop.build.gradle.plugins.base.BaseGradlePlugin
import com.xiaomi.shop.build.gradle.plugins.extension.PluginConfigExtension
import com.xiaomi.shop.build.gradle.plugins.hooker.manager.PluginHookerManager
import com.xiaomi.shop.build.gradle.plugins.tasks.AssembleFilteredPluginTask
import com.xiaomi.shop.build.gradle.plugins.utils.ExtensionApplyUtils
import com.xiaomi.shop.build.gradle.plugins.utils.Log
import com.xiaomi.shop.build.gradle.plugins.utils.ProjectDataCenter
import com.xiaomi.shop.build.gradle.plugins.utils.Reflect
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 *
 */
class ModuleGradlePlugin extends BaseGradlePlugin {

    AppPlugin mAppPlugin
    AppExtension mAndroidExtension
    PluginConfigExtension mPluginExtension
    boolean isTaskOfFilter //记录启动参数,区分config状态

    @Override
    void apply(Project project) {
        super.apply(project)
        Log.i("ModuleGradlePlugin", "apply")
        mAppPlugin = project.plugins.findPlugin(AppPlugin)
        mAndroidExtension = project.getExtensions().findByType(AppExtension)
        injectBaseExtension(project)
        hookVariantFactory()
        project.afterEvaluate {
            mApplicationVariants.each { variant ->
                injectCustomBuildTask(project, variant)
            }
        }
    }

    /**
     插入自定义extension
     */
    def injectBaseExtension(Project project) {
        project.getExtensions().add("pluginconfig", PluginConfigExtension)
        mPluginExtension = project.extensions.findByType(PluginConfigExtension)
    }

    /*生成自定义的编译task,目的主要有两个
   * 1:不干扰已有assembletask输出
   * 2:隔离其他module运行时config过程的调用
   *
   * */

    def injectCustomBuildTask(Project project, ApplicationVariant variant) {
        TaskFactory factory = ApiVersionFactory.getInstance().getTaskFactory(project)
        factory.register(new AssembleFilteredPluginTask.AssembleFilteredPluginCreationAction(project, variant))
    }

    //生命周期「解析variant之前」,可以修改一些build.gradle原定的配置
    def hookVariantFactory() {
        Reflect reflect = Reflect.on(mAppPlugin.variantManager)
        VariantFactory variantFactory = Proxy.newProxyInstance(this.class.classLoader, [VariantFactory.class] as Class[],
                new InvocationHandler() {
                    Object delegate = reflect.get('variantFactory')

                    @Override
                    Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ('preVariantWork' == method.name) {
                            def result = project.gradle.startParameter.taskNames.find() {
                                it.contains(AssembleFilteredPluginTask.TASK_NAME_PREFIX)
                            }
                            isTaskOfFilter = (result != null)
                            if (isTaskOfFilter) { //只应答自己的task
                                initialConfig(project)
                            }
                        }
                        return method.invoke(delegate, args)
                    }
                })
        reflect.set('variantFactory', variantFactory)
    }

    @Override
    /**
     * 在preBuildTask前调用
     * @return
     */
    protected onBeforePreBuildTask(ApplicationVariant variant) {
        //创建工作目录
        createAaptWorkspace(variant)
    }

    void initialConfig(Project project) {
        //初始化dataCenter
        ProjectDataCenter.init(project)
        //检查host依赖
        checkHostDependencies()
        //同步依赖版本
        ExtensionApplyUtils.applyUseHostResourceConfig(mProject)
        //反序列化依赖
        project.afterEvaluate {
            PluginHookerManager manager = new PluginHookerManager(project)
            manager.registerTaskHookers(this)
        }
    }


    void checkHostDependencies() {
        String targetHost = mPluginExtension.hostPath
        if (!targetHost) {
            def err = new StringBuilder("\n必须需要指定host路径 targetHost = ../xxxProject/app \n")
            throw new InvalidUserDataException(err.toString())
        }
        File hostLocalDir = new File(targetHost)
        if (!hostLocalDir.exists()) {
            def err = "此路径不存在: ${hostLocalDir.canonicalPath}"
            throw new InvalidUserDataException(err)
        }
        //Todo 读取release版本的依赖, 得想下区别
        File releaseHostR = new File(hostLocalDir, "hooker/release/original_resource_file.txt")
        File debugHostR = new File(hostLocalDir, "hooker/debug/original_resource_file.txt")
        File hostDependencies = new File(hostLocalDir, "hooker/release/dependencies.txt")
        if (!releaseHostR.exists() || !debugHostR.exists() || !hostDependencies.exists()) {
            def err = new StringBuilder("没有找到 \n" +
                    "[${releaseHostR.canonicalPath}] \n" +
                    "[${debugHostR.canonicalPath}] \n" +
                    "${hostDependencies.canonicalPath}\n," +
                    " 需要先buildHost\n")
            throw new InvalidUserDataException(err.toString())
        }
        ProjectDataCenter.getInstance(project, ProjectDataCenter.TYPE_DEBUG).hostPackageManifest
                .dependenciesFile = hostDependencies
        ProjectDataCenter.getInstance(project, ProjectDataCenter.TYPE_RELEASE).hostPackageManifest
                .dependenciesFile = hostDependencies

        ProjectDataCenter.getInstance(project, ProjectDataCenter.TYPE_DEBUG).hostPackageManifest
                .originalResourceTxtFile = debugHostR
        ProjectDataCenter.getInstance(project, ProjectDataCenter.TYPE_RELEASE).hostPackageManifest
                .originalResourceTxtFile = releaseHostR
    }
}