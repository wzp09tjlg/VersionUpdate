package com.jingxiang.versionupdate.util;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by pin on 2015/11/4.
 */
public class CommonHelper {

    private static final String TAG = "CommonHelper";

    public static final String WEIBO_V_TYPE_BLUE = "blue";
    public static final String WEIBO_V_TYPE_YELLOW = "yellow";

    public static final String ACTION_RECEIVE_NOTIFYCENTER_TIP = "action_receive_notifycenter_tip";//通知中心广播
    public static final String ACTION_RECEIVE_NOTIFY_PUSH = "action_receive_notify_push";//推送通知和轮询广播
    public static final String INTENT_EXTRA_SHOWDOT = "intent_extra_showdot";

    private static final int MAX_GENERATE_COUNT = 99999;
    private static int sGenerateCount = 0;


    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static String getVersionName(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return "";
    }

    /*
    实现从dip到px的转换
     */
    public static int dpToPx(Context context, int dps) {
        return Math.round(context.getResources().getDisplayMetrics().density * (float) dps);
    }

    public static int pxToDp(Context context, float px) {
        return Math.round(px / context.getResources().getDisplayMetrics().density);
    }

    // 拼接URL参数
    public static final String encodeUrlParams(String url, Map<String, String> params) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        StringBuilder encodedParams = new StringBuilder();
        try {
            encodedParams.append(url);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!url.contains("?")) {
                    encodedParams.append('?');
                } else {
                    encodedParams.append('&');
                }
                encodedParams.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    // 禁掉EditText的长按功能
    public static void disableEditTextLongClick(EditText editText) {
        /*
        EditText在横屏编辑的时候会出现一个新的不同的编辑界面，这个界面里还是可以复制粘贴的。
        下面这句禁掉横屏的新编辑界面。
         */
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            editText.setLongClickable(false);
        } else {
            editText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    // 返回false，禁掉创建actionMode
                    return false;
                }

                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return false;
                }

                public void onDestroyActionMode(ActionMode mode) {
                }
            });
        }
    }

    // 处理重复按键操作方法
    public static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 800) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /**
     * 检查网络是否可用
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return null != networkInfo && networkInfo.isAvailable();
    }

    /**
     * 检查是否在wifi网络
     */
    public static boolean isNetWorkWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return null != networkInfo && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static String getContainerHtmlUrl() {
        return "file:///android_asset/container.html" + "?param=" + System.currentTimeMillis();
    }

    public static boolean isContainerHtml(String url) {
        if (!TextUtils.isEmpty(url))
            return url.contains("file:///android_asset/container.html");
        else
            return false;
    }

    public static boolean isLinkErro(String url) {
        if (!TextUtils.isEmpty(url))
            return url.contains("file:///android_asset/");
        else
            return false;
    }

    public static String fixLink(String url) {
        if (!TextUtils.isEmpty(url)) {
            return url.replaceFirst("file:///android_asset/", "");
        } else {
            return url;
        }
    }

    // map转化为字符串
    public static String paramstoString(Map<String, String> params, boolean isEncodeValue) {
        if (params != null && params.size() > 0) {
            String paramsEncoding = "UTF-8";
            StringBuilder encodedParams = new StringBuilder();
            try {
                int index = 0;
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    encodedParams.append(isEncodeValue ? URLEncoder.encode(entry.getKey(), paramsEncoding) : entry.getKey());
                    encodedParams.append('=');
                    encodedParams.append(isEncodeValue ? URLEncoder.encode(entry.getValue(), paramsEncoding) : entry.getValue());

                    index++;
                    if (index < params.size()) {
                        encodedParams.append('&');
                    }
                }
                return encodedParams.toString();
            } catch (UnsupportedEncodingException uee) {
                throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
            }
        }
        return null;
    }

    public static Map<String, String> sortMapByKey(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<String, String> sortMap = new TreeMap<String, String>(new MapKeyComparator());
        sortMap.putAll(map);
        return sortMap;
    }

    public static class MapKeyComparator implements Comparator<String> {
        @Override
        public int compare(String str1, String str2) {
            return str1.compareTo(str2);
//            return str2.compareTo(str1);
        }
    }

    // 强制打开键盘
    public static void showKeyBoard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//        boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
//        LogUtil.d("键盘状态："+ isOpen);
        //imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
//        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);

        //InputMethodManager imm = ( InputMethodManager ) v.getContext().getSystemService( Context.INPUT_METHOD_SERVICE );
        if (!imm.isActive(view)){
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    // 强制隐藏键盘
    public static void hideKeyBoard(Context context, View view) {
        if (context == null || view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY); //隐藏键盘
//        imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //强制隐藏键盘
//        boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
    }

    // 强制隐藏键盘
    public static void forceHideKeyBoard(Context context, View view) {
        if (context == null || view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //强制隐藏键盘
    }

    //获取输入法打开的状态
    public static boolean isKeyBoardKOpen(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();//isOpen若返回true，则表示输入法打开
        return isOpen;
    }

    //如果输入法在窗口上已经显示，则隐藏，反之则显示
    public static void toggleKeyBoard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static int[] getScreenInfo(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int[] info = new int[2];
        info[0] = metrics.widthPixels;
        info[1] = metrics.heightPixels;
        return info;
    }


    public static Map<String, String> clipURLParams(String text) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }

        String[] strs = text.split("&");
        HashMap<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < strs.length; ++i) {
            String[] strs2 = strs[i].split("=");
            if (strs2.length == 2) {
                map.put(strs2[0], strs2[1]);
            }
        }
        return map;
    }

    /**
     * 检测是否有emoji表情
     *
     * @param source
     * @return
     */
    public static boolean containsEmoji(String source) {
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (!isEmojiCharacter(codePoint)) { //如果不能匹配,则该字符是Emoji表情
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否是Emoji
     *
     * @param codePoint 比较的单个字符
     * @return
     */
    private static boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) ||
                (codePoint == 0xD) || ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) || ((codePoint >= 0x10000)
                && (codePoint <= 0x10FFFF));
    }

    public static String getChanel(Context context) {
        String chanel = "";
        if (context != null) {
            try {

                ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                chanel = String.valueOf(appInfo.metaData.get("UMENG_CHANNEL"));

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return chanel;
    }

    public static void appendUA(Context context, WebView webView) {
        String user_agent = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(user_agent + " huasheng/" + CommonHelper.getVersionName(context));
    }

    public static void cancelNotify(Context context, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    public static boolean isActivityTop(Context context, String actName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName componentName = activityManager.getRunningTasks(1).get(0).topActivity;
        LogUtil.d(TAG, "isActivityTop: " + componentName.getClassName());
        return componentName != null && componentName.getClassName().equals(actName);
    }

    public static String getImei(Context context) {
        String Imei = "";
        try{
            Imei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                    .getDeviceId();
        }catch (Exception e){}
        return Imei == null ? "" : Imei;
    }

    public static String GetNetworkType(Context context) {
        String strNetworkType = "";

        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                strNetworkType = "WIFI";
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                String _strSubTypeName = networkInfo.getSubtypeName();

                LogUtil.e("cocos2d-x", "Network getSubtypeName : " + _strSubTypeName);

                // TD-SCDMA   networkType is 17
                int networkType = networkInfo.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                        strNetworkType = "2G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        strNetworkType = "3G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                        strNetworkType = "4G";
                        break;
                    default:
                        // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                        if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                            strNetworkType = "3G";
                        } else {
                            strNetworkType = _strSubTypeName;
                        }

                        break;
                }

                LogUtil.e("cocos2d-x", "Network getSubtype : " + Integer.valueOf(networkType).toString());
            }
        }

        LogUtil.e("cocos2d-x", "Network Type : " + strNetworkType);

        return strNetworkType;
    }

    public static String getIpAddress(Context context) {
        String ipAddress = "";
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        String netType = GetNetworkType(context);
        if ("WIFI".equalsIgnoreCase(netType)) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddressInt = wifiInfo.getIpAddress();
            ipAddress = intToIp(ipAddressInt);
        } else {
            ipAddress = getLocalIpAddress();
        }
        return ipAddress;
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
//            Log.e("WifiPreference IpAddress", ex.toString());
        }
        return "";
    }

    private static String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    /**
     * 生成唯一字符串
     *
     * @return
     */
    public static synchronized String getUniqueString() {
        if (sGenerateCount > MAX_GENERATE_COUNT) {
            sGenerateCount = 0;
        }

        String uniqueString = Long.toString(System.currentTimeMillis()) + Integer.toString(sGenerateCount);
        sGenerateCount++;
        return uniqueString;
        //
        // String result = UUID.randomUUID().toString();
        // return result;
    }


    /**
     * r
     * json 转换成map
     */
    public static Map<String, String> JSON2Map(String json) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            JSONObject jsonMap = new JSONObject(json);
            Iterator<String> it = jsonMap.keys();
            while (it.hasNext()) {
                String key = it.next();
                String value = jsonMap.get(key).toString();
                map.put(key, value);
            }
        } catch (Exception e) {
        }
        return map;
    }

    /**
     * 判断List是否为空
     */
    public static boolean checkListEmpty(List<?> list) {
        if (list == null || list.size() == 0)
            return true;
        return false;
    }

    /**
     * 直接根据资源id 来读取图片(针对图片做了压缩处理,可以避免imageView加载bitmap出现oom)
     */
    public static Bitmap readBitmap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    /**
     * url转map 都是先进过uncode处理
     */
    public static Map<String, String> Url2Map(String url) {
        if (TextUtils.isEmpty(url)) return null;
        String SubUrl = "";
        int tempPosition = url.indexOf("?"); //"?a=1&b=2" ?的位置是0
        if (tempPosition > -1)
            SubUrl = url.substring(tempPosition + 1);
        else
            SubUrl = url;
        Map<String, String> map = null;
        if (SubUrl != null && SubUrl.indexOf("&") > -1 && SubUrl.indexOf("=") > -1) {
            map = new HashMap<String, String>();
            String[] arrTemp = SubUrl.split("&");
            for (String str : arrTemp) {
                String[] qs = str.split("=");
                String tempValue = "";
                try {
                    tempValue = URLDecoder.decode(qs[1], "UTF-8");
                } catch (Exception e) {
                }
                map.put(qs[0], tempValue);
            }
            //String encode = URLEncoder.encode(keyWord, "UTF-8");
            //String decode = URLDecoder.decode(encode, "UTF-8");
        }
        return map;
    }

    public static ArrayList getTagListFromString(String mTags) {
        ArrayList list = new ArrayList();
        if (!mTags.startsWith("[")) //检查传入的tag如果不是jsonArray这里就做处理(ps.从标签页进入会出现不是tag字符数组的情况)
            mTags = "[" + mTags;
        if (!mTags.endsWith("]"))
            mTags = mTags + "]";
        try {
            JSONArray jsonArray = new JSONArray(mTags);
            for (int i = 0; i < jsonArray.length(); i++) {
                String pickedTagName = jsonArray.getString(i);
                list.add(pickedTagName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
