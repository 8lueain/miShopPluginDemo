package com.xiaomi.shop.build.gradle.plugins.utils

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ListMultimap
import com.google.common.collect.Lists
import com.google.common.io.Files
import com.xiaomi.shop.build.gradle.plugins.bean.res.ResourceEntry
import com.xiaomi.shop.build.gradle.plugins.bean.res.StyleableEntry

class ResourceFormatUtils {

    /**
     * 将R文件转为aapt固定资源时"--emit-id"文件格式的
     * @param rSymbolFile R.txt文件
     */
    static void convertRFile2Stable(String applicationId, File rSymbolFile, File stableIDFile) {
        if (!rSymbolFile.exists()) {
            return
        }
        ListMultimap<String, ResourceEntry> resourcesContainer = ArrayListMultimap.create()
        List<StyleableEntry> styleablesContainer = Lists.newArrayList()
        rSymbolFile.eachLine { line ->
            if (!line.empty) {
                def tokenizer = new StringTokenizer(line)
                def valueType = tokenizer.nextToken()
                def resType = tokenizer.nextToken()
                def resName = tokenizer.nextToken()
                def resId = tokenizer.nextToken('\r\n').trim()

                if (resType == 'styleable') {
                    StyleableEntry styleableEntry = new StyleableEntry(resName, resId, valueType);
                    styleableEntry.setPackageId(applicationId)
                    styleablesContainer.add(styleableEntry)
                } else {
                    ResourceEntry resourceEntry = new ResourceEntry(resType, resName, Integer.decode(resId))
                    resourceEntry.setPackageId(applicationId)
                    resourcesContainer.put(resType, resourceEntry)
                }
            }
        }
//        if (!styleablesContainer.empty) {
//            for (int index = 0; index < styleablesContainer.size() - 1; index++) {
//                StyleableEntry item = styleablesContainer.get(index)
//                if (item.valueType == 'int') {
//                    StyleableEntry arrayItem = styleablesContainer.get(index - item.valueInt() - 1)
//                    String realValue = arrayItem.convertValue2List().get(item.valueInt())
//                    resourcesContainer.put("attr",
//                            new ResourceEntry("attr", item.getStyleableItemName(), Integer.decode(realValue)))
//                }
//            }
//        }


        resourcesContainer.keySet().each { String key ->
//            println("resourcesContainer type ----- ${key}\n")
            FileUtil.appendFile(stableIDFile.getParentFile(),
                    Files.getNameWithoutExtension(stableIDFile.name),
                    true,
                    resourcesContainer.get(key))
//            resourcesContainer.get(key).each { ResourceEntry value ->
//                println(value)
//            }
        }
    }
    /**
     * 将R文件转为实体类
     * @param rSymbolFile R.txt文件
     */
    static ListMultimap convertR2ResourceMap(File RSymbolFile) {
        if (!RSymbolFile.exists()) {
            return
        }
        ListMultimap<String, ResourceEntry> resourcesMap = ArrayListMultimap.create()
        RSymbolFile.eachLine { line ->
            if (!line.empty) {
                def tokenizer = new StringTokenizer(line)
                def valueType = tokenizer.nextToken()     // value type (int or int[])
                def resType = tokenizer.nextToken()      // resource type (attr/string/color etc.)
                def resName = tokenizer.nextToken()
                def resId = tokenizer.nextToken('\r\n').trim()
                resourcesMap.put(resType, new ResourceEntry(resType, resName, Integer.decode(resId)))
            }
        }
        return resourcesMap
    }
}
