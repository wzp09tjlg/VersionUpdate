package com.jingxiang.versionupdate.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.jingxiang.versionupdate.MApplication;
import com.jingxiang.versionupdate.network.parse.UpdateBean;
import com.jingxiang.versionupdate.util.NetUtil;
import com.jingxiang.versionupdate.util.ThreadPool;

/**
 * Created by wu on 2016/10/19.
 * 监听网络切换状态的广播
 * 1.自己的网 和 wifi的网 切换
 * 2.无网 到 有网的切换
 */
public class NetStatusReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        boolean hasNet = false;
       if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
           ConnectivityManager cm= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
           if (cm!=null) {
               NetworkInfo[] networkInfos=cm.getAllNetworkInfo();
               for (int i = 0; i < networkInfos.length; i++) {
                   NetworkInfo.State state=networkInfos[i].getState();
                   if (NetworkInfo.State.CONNECTED==state) {//网络连接 包括有线(移动端可以有有线吗?) 2G 3G 4G这些网络
                       hasNet = true;
                       doOperateConnect(context);
                       return;
                   }
               }

               if(!hasNet){//网络断开的情况
                   UpdateService.isPause = true;
               }
           }
       }
    }

    private void doOperateConnect(final Context context){
        boolean isWifi = NetUtil.isNetWorkWifi(context);
        if(isWifi){
            ThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    UpdateBean mDBBean = MApplication.mCommonDao.selectUpdateBean();//提供了一个空参数的获取下载的bean
                    if(mDBBean != null && mDBBean.end != mDBBean.finished){
                        Intent intentStartDownload = new Intent(context, UpdateService.class);
                        intentStartDownload.putExtra("BEAN",mDBBean);
                        context.startService(intentStartDownload);
                    }
                }
            });
        }else{
            ThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    Intent intentStartDownload = new Intent(context, UpdateService.class);
                    context.stopService(intentStartDownload);
                }
            });
        }
    }
}
