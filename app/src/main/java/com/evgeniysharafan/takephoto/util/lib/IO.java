package com.evgeniysharafan.takephoto.util.lib;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * IO utils
 */
public final class IO {

    private IO() {
    }

    public static boolean isMediaStorageMounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static long getFreeExternalSpace() {
        long freeSpace = 0;

        if (isMediaStorageMounted()) {
            freeSpace = getFreeSpace(Environment.getExternalStorageDirectory());
        } else {
            L.w("Media storage is unmounted");
        }

        return freeSpace;
    }

    public static long getFreeInternalSpace() {
        return getFreeSpace(Environment.getDataDirectory());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long getFreeSpace(File path) {
        long freeSpace = 0;

        StatFs stat = new StatFs(path.getPath());
        long blockSize = Utils.hasJellyBeanMr2() ? stat.getBlockSizeLong() : stat.getBlockSize();
        long availableBlocks = Utils.hasJellyBeanMr2() ? stat.getAvailableBlocksLong() : stat.getAvailableBlocks();
        freeSpace = availableBlocks * blockSize;

        return freeSpace;
    }

    public static int getFilesCountInDirectory(File folder) {
        int count = 0;

        if (folder.exists() && folder.isDirectory()) {
            File[] filesInFolder = folder.listFiles();

            if (filesInFolder != null) {
                count = filesInFolder.length;
            }
        }

        return count;
    }

    public static long getFolderSize(File file) {
        long totalFolderSize = 0;

        if (!file.exists() || !file.isDirectory()) {
            L.w("File does not exist or not a folder");

            return totalFolderSize;
        }

        File[] filesInFolder = file.listFiles();
        for (File f : filesInFolder) {
            if (f.isDirectory()) {
                totalFolderSize += getFolderSize(f);
            } else {
                totalFolderSize += f.length();
            }
        }

        return totalFolderSize;
    }

    public static boolean deleteFolder(String pathToFolder) {
        return deleteFolder(new File(pathToFolder));
    }

    public static boolean deleteFolder(File folder) {
        deleteFilesInFolder(folder);

        return folder.delete();
    }

    public static void deleteFilesInFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
                }
            }
        }
    }

    public static long getApkSize() {
        long apkSize = 0;

        File apk = new File(Utils.getApp().getPackageCodePath());
        apkSize = apk.length();

        return apkSize;
    }

    public static String getFormattedFileSize(long size) {
        return Formatter.formatFileSize(Utils.getApp(), size);
    }

    public static boolean removeFile(String path) {
        return removeFile(new File(path));
    }

    public static boolean removeFile(File file) {
        boolean deleted = false;

        if (file != null) {
            deleted = file.delete();
        }

        if (!deleted) {
            L.i("failed to remove " + (file != null ? file.getPath() : "null"));
        }

        return deleted;
    }

    public static void copyFile(String srcPath, String targetPath) throws IOException {
        copyFile(new File(srcPath), new File(targetPath));
    }

    public static void copyFile(File srcFile, File targetFile) throws IOException {
        FileChannel inChannel = new FileInputStream(srcFile).getChannel();
        FileChannel outChannel = new FileOutputStream(targetFile).getChannel();

        //noinspection TryFinallyCanBeTryWithResources
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            inChannel.close();
            outChannel.close();
        }
    }

    public static void writeFile(String data, File file) throws IOException {
        writeFile(data.getBytes(Charset.forName("UTF-8")), file);
    }

    public static void writeFile(byte[] data, File file) throws IOException {
        BufferedOutputStream bos = null;

        try {
            bos = new BufferedOutputStream(new FileOutputStream(file, false));
            bos.write(data);
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
                //ignore
            }
        }
    }

    public static String getAppExternalDataDirectory() {
        //noinspection StringBufferReplaceableByString
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory())
                .append(File.separator)
                .append("Android")
                .append(File.separator)
                .append("data")
                .append(File.separator)
                .append(Utils.getPackageName())
                .append(File.separator);

        return sb.toString();
    }

    public static String readFileAsString(String path) throws FileNotFoundException, IOException {
        return readFileAsString(new File(path));
    }

    public static String readFileAsString(File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader buf = null;
        try {
            buf = new BufferedReader(new InputStreamReader(new FileInputStream(f)));

            String line = "";
            while ((line = buf.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            try {
                if (buf != null) {
                    buf.close();
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return sb.toString();
    }

    public static void createNomediaFileIfNeeded(File folder) {
        File nomediaFile = new File(folder, ".nomedia");
        L.d("path to nomediaFile = " + nomediaFile.getPath());

        if (!nomediaFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                nomediaFile.createNewFile();
            } catch (IOException e) {
                L.e(e);
            }
        }
    }

}
