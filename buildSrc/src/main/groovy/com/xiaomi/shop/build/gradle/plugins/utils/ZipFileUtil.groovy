package com.xiaomi.shop.build.gradle.plugins.utils


import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * operate on jar/zip file
 */
class ZipFileUtil {


    static void deleteAll(File file, final Set<String> deletes) {
        ZipFile zf = new ZipFile(file)
        File temp = new File(file.parentFile, "${file.name}_bak")
        ZipOutputStream os = new ZipOutputStream(new FileOutputStream(temp))

        def entries = zf.entries()
        while (entries.hasMoreElements()) {
            ZipEntry ze = entries.nextElement()
            if (!deletes.find { it == ze.name }) {
                writeEntry(zf, os, ze)
            }
        }

        zf.close()
        os.flush()
        os.close()

        file.delete() // delete first to avoid `renameTo' failed on Windows
        temp.renameTo(file)
    }

    private static void writeEntry(ZipFile zf, ZipOutputStream os, ZipEntry ze) throws IOException {
        ZipEntry ze2 = new ZipEntry(ze.getName());
        ze2.setMethod(ze.getMethod());
        ze2.setTime(ze.getTime());
        ze2.setComment(ze.getComment());
        ze2.setExtra(ze.getExtra());
        if (ze.getMethod() == ZipEntry.STORED) {
            ze2.setSize(ze.getSize());
            ze2.setCrc(ze.getCrc());
        }
        os.putNextEntry(ze2);
        writeBytes(zf, ze, os);
    }

    private static synchronized void writeBytes(ZipFile zf, ZipEntry ze, ZipOutputStream os) throws IOException {
        int n;
        byte[] buffer = new byte[1024]

        InputStream is = null;
        try {
            is = zf.getInputStream(ze);
            long left = ze.getSize();

            while ((left > 0) && (n = is.read(buffer, 0, buffer.length)) != -1) {
                os.write(buffer, 0, n);
                left -= n;
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}