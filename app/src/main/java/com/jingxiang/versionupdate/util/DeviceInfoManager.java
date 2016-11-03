package com.jingxiang.versionupdate.util;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

/**
 * Created by wu on 2016/10/19.
 * 所有和设备及应用相关的方法 都会在这里定义
 */
public class DeviceInfoManager {
    /** 获取应用的相关信息 */
    // 获取应用的版本号
    public static int getAppVersionCode(Context context){
        PackageManager manager = context.getPackageManager();
        try{
            PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(),0);
            return packageInfo.versionCode;
        }catch (Exception e){}
        return 0;
    }

    // 获取应用的版本名称
    public static String getAppVersionName(Context context){
        PackageManager manager = context.getPackageManager();
        try{
            PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(),0);
            return packageInfo.versionName;
        }catch (Exception e){}
        return "1.0";
    }

    /** 获取设备的相关信息 */
    // 获取IMEI (15位) 需要获取手机的状态权限 (ps. 水货手机是不存在IMEI号,或者获取手机的IMEI失败,固定传 随机数 271234212210402)
    public static String getIMEI(Context context){
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try{
            String imei = manager.getDeviceId();
            if(imei.equals("000000000000000"))
                return "271234212210402";
            else
                return imei;
        }catch (Exception e){
            return "271234212210402";
        }
    }

    // 获取Pseudo-Unique ID, 如果不希望加入获取手机状态的权限，也可以通过获取Build类的信息 构建伪IMEI号
    public static String getPseudoIMEI(){
        StringBuilder builder = new StringBuilder();
        builder.append("35");
        builder.append(String.valueOf(Build.BOARD.length()%10));
        builder.append(String.valueOf(Build.BRAND.length()%10));
        builder.append(String.valueOf(Build.CPU_ABI.length()%10));
        builder.append(String.valueOf(Build.DEVICE.length()%10));
        builder.append(String.valueOf(Build.DISPLAY.length()%10));
        builder.append(String.valueOf(Build.HOST.length()%10));
        builder.append(String.valueOf(Build.ID.length()%10));
        builder.append(String.valueOf(Build.MANUFACTURER.length()%10));
        builder.append(String.valueOf(Build.MODEL.length()%10));
        builder.append(String.valueOf(Build.PRODUCT.length()%10));
        builder.append(String.valueOf(Build.TAGS.length()%10));
        builder.append(String.valueOf(Build.TYPE.length()%10));
        builder.append(String.valueOf(Build.USER.length()%10));
        return builder.toString();
    }

    // 获取 Android ID 这个状态值 可能会在某些情况下改变,恢复出厂设置或者被Root过
    public static String getAndroidID(){
        return Settings.Secure.ANDROID_ID;
    }

    // 获取无线的MAC地址 需要获取无线状态权限 无线MAC地址是可以被伪造的
    public static String getWlanMac(Context context){
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        try{
            return manager.getConnectionInfo().getMacAddress();
        }catch (Exception e){}
        return "";
    }

    // 获取蓝牙的地址 需要获取蓝牙的权限
    public static String getBTMAC(Context context){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.getAddress();
    }

    // 通过上述的物种方法 组成的串 MD5之后得到一个结果 来做设备的唯一值(32位16进制)
    public static String getDeviceID(Context context){
        StringBuilder builder = new StringBuilder();
        try{
            builder.append(getIMEI(context));
        }catch (Exception e){}

        try{
            builder.append(getPseudoIMEI());
        }catch (Exception e){}

        try{
            builder.append(getAndroidID());
        }catch (Exception e){}

        try{
            builder.append(getWlanMac(context));
        }catch (Exception e){}

        try{
            builder.append(getBTMAC(context));
        }catch (Exception e){}

        return MD5Helper.encode(builder.toString());
    }
}
