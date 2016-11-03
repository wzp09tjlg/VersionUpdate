package com.jingxiang.versionupdate.util;

import android.util.Log;

/**
 * Created by wu on 2016/9/19.
 * 复用系统的日志,封装对应的方法:d e i v w
 */
public class LogUtil {
    private static String TAG = "wuzp";
    private static boolean DEBUG = true;

    public static void d(String msg){
        if(DEBUG)
            Log.d(TAG,msg);
    }

    public static void d(String tag,String msg){
        if(DEBUG)
            Log.d(tag,msg);
    }

    public static void e(String msg){
        if(DEBUG)
            Log.e(TAG,msg);
    }

    public static void e(String tag,String msg){
        if(DEBUG)
            Log.e(tag,msg);
    }

    public static void i(String msg){
        if(DEBUG)
            Log.i(TAG,msg);
    }

    public static void i(String tag,String msg){
        if(DEBUG)
            Log.i(tag,msg);
    }

    public static void v(String msg){
        if(DEBUG)
            Log.v(TAG,msg);
    }

    public static void v(String tag,String msg){
        if(DEBUG)
            Log.v(tag,msg);
    }

    public static void w(String msg){
        if(DEBUG)
            Log.w(TAG,msg);
    }

    public static void w(String tag,String msg){
        if(DEBUG)
            Log.w(tag,msg);
    }
}
