package com.xiaomi.shop.build.gradle.plugins.bean

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ListMultimap
import com.xiaomi.shop.build.gradle.plugins.bean.dependence.AarDependenceInfo
import com.xiaomi.shop.build.gradle.plugins.bean.dependence.DependenceInfo
import com.xiaomi.shop.build.gradle.plugins.bean.dependence.JarDependenceInfo
import com.xiaomi.shop.build.gradle.plugins.bean.res.ResourceEntry
import com.xiaomi.shop.build.gradle.plugins.bean.res.StyleableEntry
import com.xiaomi.shop.build.gradle.plugins.extension.PluginConfigExtension
import org.gradle.api.Project

class MergedPackageManifest extends PackageManifest {
    PackageManifest mHostManifest
    PackageManifest mPluginManifest
    //处理R文件后生成的资源map
    private ListMultimap<String, ResourceEntry> mMergedResourcesMap
    //处理R文件后生成的styleMap
    private List<StyleableEntry> mMergedStyleablesList

    int packageId = 0x7f //指定新的packageID,默认7f

    private Collection<DependenceInfo> mStripDependencies
    private Collection<AarDependenceInfo> mRetainedAarLibs
    private Collection<JarDependenceInfo> mRetainedJarLibs


    MergedPackageManifest(PackageManifest input, PackageManifest source, Project project) {
        super(project)
        mHostManifest = input
        mPluginManifest = source
        packageId = mProject.getExtensions().findByType(PluginConfigExtension).packageId
    }

    Collection<DependenceInfo> getStripDependencies() {
        if (null == mStripDependencies) {
            collectDependencies()
        }
        return mStripDependencies
    }

    Collection<AarDependenceInfo> getRetainedAarLibs() {
        if (null == mRetainedAarLibs) {
            collectDependencies()
        }
        return mRetainedAarLibs
    }

    Collection<JarDependenceInfo> getRetainedJarLibs() {
        if (null == mRetainedJarLibs) {
            collectDependencies()
        }
        return mRetainedJarLibs
    }

    private collectDependencies() {
        if (null == mStripDependencies) {
            mStripDependencies = [] as Set<DependenceInfo>
        }
        if (null == mRetainedAarLibs) {
            mRetainedAarLibs = [] as Set<AarDependenceInfo>
        }
        if (null == mRetainedJarLibs) {
            mRetainedJarLibs = [] as Set<JarDependenceInfo>
        }
        mPluginManifest.aarDependenciesLibs.each { pluginAar ->
            AarDependenceInfo find = mHostManifest.aarDependenciesLibs.find { hostAar ->
                hostAar.compareKey == pluginAar.compareKey
            }
            if (null == find) {
                mRetainedAarLibs.add(pluginAar)
                //更新此aar中,被plugin用到的资源,用户后续更新R.java文件
                def allResources = mPluginManifest.resourcesMap
                allResources.keySet().each { resType ->
                    allResources.get(resType).each { resEntry ->
                        if (pluginAar.resourceKeys.contains("${resType}:${resEntry.resourceName}")) {
                            pluginAar.aarResources.put(resType, resEntry)
                        }
                    }
                }

                pluginAar.aarStyleables = mPluginManifest.styleablesList.findAll { styleableEntry ->
                    pluginAar.resourceKeys.contains("styleable:${styleableEntry.name}")
                }
            } else {
                mStripDependencies.add(pluginAar)
            }

        }
        mPluginManifest.jarDependenciesLibs.each { pluginJar ->
            JarDependenceInfo find = mHostManifest.jarDependenciesLibs.find { hostJar ->
                hostJar.compareKey == pluginJar.compareKey
            }
            if (null == find) {
                mRetainedJarLibs.add(pluginJar)
            } else {
                mStripDependencies.add(pluginJar)
            }
        }
    }


    //生成依赖aar库对应的R.java文件
    def generateAarLibRJava2Dir(File aaptSourceDir) {
        getRetainedAarLibs().each { aarDep ->
            File rJava = new File([aaptSourceDir, aarDep.package.replace('.'.charAt(0), File.separatorChar), "R.java"].join(File.separator))
            println("generateAarLibRJava2Dir [${rJava}]")
            generateRJavaInner(rJava, aarDep.package, aarDep.aarResources, aarDep.aarStyleables)
        }
    }

    @Override
    Map getDependenciesMap() {
        Map result = [] as LinkedHashMap
        Map hostDepMap = mHostManifest.dependenciesMap()
        Map pluginDepMap = mPluginManifest.dependenciesMap()
        pluginDepMap.keySet().each { String plugin_key ->
            if (hostDepMap.containsKey(plugin_key)) {

            } else {
                result.put(plugin_key, pluginDepMap.get(plugin_key))
            }
        }
        return result
    }

    @Override
    ListMultimap<String, ResourceEntry> getResourcesMap() {
        def plugin = mPluginManifest.resourcesMap
        def host = mHostManifest.resourcesMap
        if (mMergedResourcesMap == null) {
            mMergedResourcesMap = ArrayListMultimap.create()
            def resourceIdList = []
            plugin.keySet().each { String resType ->
                List<ResourceEntry> entryList = plugin.get(resType)
                resourceIdList.add([resType: resType, typeId: entryList.empty ? -1
                        : parseTypeIdFromResId(entryList.first().resourceId)])
            }

            resourceIdList.sort { t1, t2 ->
                t1.typeId - t2.typeId
            }
            Set<String> typeSet = [] as Set
            resourceIdList.each { mapItem ->
                if (mapItem.typeId < 0) {
                    return
                }
                String type = mapItem.resType
                int entryId = 0
                plugin.get(type).each { pluginResEntry ->
                    def index = host.get(pluginResEntry.resourceType).indexOf(pluginResEntry)
                    if (index >= 0) {//相同的
                        //更新成host的id，保证使用hostAssetManger可以正常找到资源
                        pluginResEntry.newResourceId = host.get(pluginResEntry.resourceType).get(index).resourceId
                        //保证后续生成映射map时，能使用带更新过的resID
                        host.get(pluginResEntry.resourceType).set(index, pluginResEntry)
//                        println("plugin modify res name[${pluginResEntry.resourceName}] " +
//                                "org-id[${pluginResEntry.hexResourceId}] " +
//                                "new-id[${pluginResEntry.hexNewResourceId}]")
                    } else {
                        //重新排序
                        typeSet.add(type)
                        pluginResEntry.setNewResourceId(packageId, typeSet.size(), entryId++)
//                        println("plugin modify res name[${pluginResEntry.resourceName}] " +
//                                "org-id[${pluginResEntry.hexResourceId}] " +
//                                "new-id[${pluginResEntry.hexNewResourceId}]")
                        mMergedResourcesMap.put(pluginResEntry.resourceType, pluginResEntry)
                    }
                }
            }
        }

        return mMergedResourcesMap
    }

    @Override
    List<StyleableEntry> getStyleablesList() {
        def plugin = mPluginManifest.styleablesList
        def host = mHostManifest.styleablesList
        List<ResourceEntry> attrs = getResourcesMap().get("attr")
        if (mMergedStyleablesList == null) {
            mMergedStyleablesList = new ArrayList<>()
            plugin.each { styleable ->
                def index = host.indexOf(styleable)
                if (index >= 0) {
                    styleable.value = host.get(index).value
                    host.set(index, styleable)
                } else {
                    if (styleable.valueType == "int[]") {
                        List styleableEntries = styleable.valueAsList
                        styleableEntries.eachWithIndex { resId, i ->
                            ResourceEntry findAttr = attrs.find { it.hexResourceId == resId }
                            if (null != findAttr) {
                                styleableEntries[i] = findAttr.hexNewResourceId
                            }
                        }
                        styleable.setValue(styleableEntries)
                    }
                    mMergedStyleablesList.add(styleable)
                }
            }
        }
        return mMergedStyleablesList
    }


    @Override
    def getResourcesMapForAapt() {
        return convertResourcesForAsrsEditor(getResourcesMap())
    }

    @Override
    def getStyleablesListForAapt() {
        return convertStyleablesForArscEditor(getStyleablesList())
    }

    def getResIdMapForArsc() { //映射新的resourceID
        def idMap = [:] as Map<Integer, Integer>
        getResourcesMap()
        mPluginManifest.resourcesMap.values().each { resEntry ->
            idMap.put(resEntry.resourceId, resEntry.newResourceId)
        }
        return idMap
    }
}
