package com.wuyz.hotpatchdemo;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Created by Administrator on 2017/5/5.
 */

public class App extends Application {
    private static final String APK_NAME = "hotpatchdemo.apk";

    @Override
    public void onCreate() {
        super.onCreate();
        copyApk();

        File file = new File(getDir("dex", Context.MODE_PRIVATE), APK_NAME);
        if (file.exists() && file.isFile() && file.canRead()) {
            PathClassLoader classLoader = (PathClassLoader) getClassLoader();
            DexClassLoader classLoader2 = new DexClassLoader(file.getAbsolutePath(),
                    getDir("cache", MODE_PRIVATE).getAbsolutePath(),
                    getDir("lib", MODE_PRIVATE).getAbsolutePath(), classLoader);

            try {
                Field pathListField = Class.forName("dalvik.system.BaseDexClassLoader").getDeclaredField("pathList");
                pathListField.setAccessible(true);
                Object pathList = pathListField.get(classLoader);
                Object pathList2 = pathListField.get(classLoader2);

                Field dexElementsField = Class.forName("dalvik.system.DexPathList").getDeclaredField("dexElements");
                dexElementsField.setAccessible(true);

                Object dexElements = dexElementsField.get(pathList);
                Object dexElements2 = dexElementsField.get(pathList2);

                Object dexElements3 = combineArray(dexElements2, dexElements);
                dexElementsField.set(pathList, dexElements3);

                Class cls = classLoader.loadClass("com.wuyz.hotpatchdemo.Person");
//            Class cls = Class.forName("com.wuyz.test5.Person");
                Log.d("hotpatchdemo", "cls getClassLoader = " + cls.getClassLoader());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Object combineArray(Object value, Object value2) {
        Class type = value.getClass().getComponentType();
        int i = Array.getLength(value);
        int j = Array.getLength(value2) + i;
        Object value3 = Array.newInstance(type, j);
        for (int k = 0; k < j; k++) {
            if (k < i) {
                Array.set(value3, k, Array.get(value, k));
            } else {
                Array.set(value3, k, Array.get(value2, k - i));
            }
        }
        return value3;
    }

    private void copyApk() {
        final File destFile = new File(getDir("dex", Context.MODE_PRIVATE), APK_NAME);
        if (!destFile.exists()) {
            BufferedInputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = new BufferedInputStream(getAssets().open(APK_NAME));
                outputStream = new BufferedOutputStream(new FileOutputStream(destFile));
                byte[] buf = new byte[10240];
                int len;
                while ((len = inputStream.read(buf, 0, buf.length)) > 0) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.close();
                inputStream.close();
                Log.d("hotpatchdemo", "copyApk");
            } catch (IOException e) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        }
    }
}
