package com.example.myapplication.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtil {

    public static Bitmap base64ToImage(String base64String) {
        // 解码 Base64 字符串
        byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);

        // 将字节数组转换为 Bitmap
        Bitmap img = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        return img;
    }

    public static void saveImage(Bitmap bitmap, String filename) {
        // 获取外部存储的公共图片目录
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        // 创建文件
        File file = new File(directory, filename);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);

            // 将 Bitmap 压缩成 PNG 格式的图片并保存到文件中
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveAvatarImage(@NonNull Context context, Bitmap bitmap, String username) {
        // 获取应用的私有文件目录
        File directory = context.getFilesDir();

        // 在私有文件目录下创建一个名为 "avatar_cache" 的子目录
        File subDirectory = new File(directory, "avatar_cache");
        if (!subDirectory.exists()) {
            subDirectory.mkdirs();
        }
        String filename=username+"_avatar.png";
        // 创建文件
        File file = new File(subDirectory, filename);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);

            // 将 Bitmap 压缩成 PNG 格式的图片并保存到文件中
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Nullable
    public static String getAvatarImagePath(@NonNull Context context, String username) {
        // 获取应用的私有文件目录
        File directory = context.getFilesDir();

        // 在私有文件目录下的 "avatar_cache" 子目录中查找文件
        File file = new File(directory, "avatar_cache/" + username+"_avatar.png");

        if (file.exists()) {
            // 返回文件的路径
            return file.getAbsolutePath();
        }
        return null;
    }
    public static String bitmapToString(Bitmap bitmap) {
        // 将 Bitmap 转换为 byte[]
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // 将 byte[] 转换为 String
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

        return encoded;
    }
}