package com.xiaomi.shop.build.gradle.plugins.utils

import com.xiaomi.shop.build.gradle.plugins.bean.PackageManifest

class PackageManifestUtils {

    //是否已经经过aapt处理
    static boolean hasAaptProcessed(PackageManifest manifest) {
        return null != manifest && null != manifest.originalResourceTxtFile
    }
}
