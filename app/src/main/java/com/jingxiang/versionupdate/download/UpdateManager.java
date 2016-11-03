package com.jingxiang.versionupdate.download;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;

import com.jingxiang.versionupdate.MApplication;
import com.jingxiang.versionupdate.network.parse.UpdateBean;
import com.jingxiang.versionupdate.util.DeviceInfoManager;
import com.jingxiang.versionupdate.util.NetUtil;
import com.jingxiang.versionupdate.util.ThreadPool;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by wu on 2016/10/19.
 * 1.检查应用版本更新的工具类
 *  保存的路径 及 文件 是 .../Huasheng/Huasheng.apk
 */
public class UpdateManager {
    //下载状态
    public static final int DOWNLOAD_STATUS_UNSTART    = 1;//未开始
    public static final int DOWNLOAD_STATUS_UNFINISHED = 2;//未完成
    public static final int DOWNLOAD_STATUS_FINISHED   = 3;//已完成
    //下载保存文件路径
    public static final String DOWNLOAD_FILE_SAVE_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "Huasheng";
    //下载保存文件名
    public static final String DOWNLOAD_FILE_SAVE_NAME = "Huasheng.apk";

    /** Data */
    private static UpdateManager manager;
    private static UpdateBean mServerBean;//从服务获取的bean
    private UpdateBean mDBBean;    //从数据库中获取的bean
    private int mServerVersionCode = 0;
    private int mAppVersionCode = 0;

   /***************************/
    private UpdateManager(){}

    public static UpdateManager getInstance(){
        if(manager == null){
            manager = new UpdateManager();
        }
        return manager;
    }

    //检查是否需要更新
    public boolean checkUpdate(Context context){
        if(mServerBean != null){
            mServerVersionCode = Integer.parseInt(mServerBean.version_code);
            mAppVersionCode = DeviceInfoManager.getAppVersionCode(context);
            if(mServerVersionCode > mAppVersionCode&& mServerVersionCode > 0 && mAppVersionCode > 0){//需要下载
                mDBBean = MApplication.mCommonDao.selectUpdateBean(String.valueOf(mServerVersionCode));
                if(mDBBean != null && !TextUtils.isEmpty(mDBBean.version_code)){ //重新下载
                    int tempDBCode = Integer.parseInt(mDBBean.version_code);
                    if( tempDBCode != mServerVersionCode){//之前下载的版本与当前服务器上最新版本不一致,删除之前下载的版本
                         doDeleteLocalFile();
                         doDeleteDBRecord();
                         mDBBean = null;
                        return true;
                     }else{
                         if(mDBBean.end == mDBBean.finished)
                             return false;//表示之前已经下载完了，不需要再次下载
                          else
                             return true;
                     }
                }
                return true;
            }
            return false;//当前版本与服务器版本一致时 是不开下载服务的
        }else
          return false;
    }

    //进行更新
    public void doUpdate(final Context context,final int versionCode){
        if(mDBBean == null || TextUtils.isEmpty(mDBBean.version_code)){//初次下载处理,必须得保存下载的数据长度,不然会出现刚进入是自己网,之后切换成无线网进行下载异常情况
            ThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    long tempFileLength = doGetDownloadLength(mServerBean.download_link);
                    mDBBean = new UpdateBean();
                    mDBBean.end = tempFileLength;
                    mDBBean.download_link = mServerBean.download_link;
                    mDBBean.version_code = mServerBean.version_code;
                    mDBBean.intro = mServerBean.intro;
                    boolean isInserted = doSaveDBRecord(mDBBean);
                    if(isInserted && NetUtil.isNetworkConnected(context) && NetUtil.isNetWorkWifi(context)){//有网 有无线网时进行下载
                        Intent intentStartDownload = new Intent(context,UpdateService.class);
                        intentStartDownload.putExtra("BEAN",mDBBean);
                        context.startService(intentStartDownload);
                    }
                }
            });
        }else if(mDBBean != null && !TextUtils.isEmpty(mDBBean.download_link) && NetUtil.isNetworkConnected(context) && NetUtil.isNetWorkWifi(context)){//表示之前已经下载过,并且没有下载完
            if(mDBBean.end > mDBBean.finished){
                ThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        Intent intentStartDownload = new Intent(context,UpdateService.class);
                        intentStartDownload.putExtra("BEAN",mDBBean);
                        context.startService(intentStartDownload);
                    }
                });
            }
        }
    }

    //删除本地文件
    private void doDeleteLocalFile(){
        File tempFile = new File(DOWNLOAD_FILE_SAVE_PATH + File.separator + DOWNLOAD_FILE_SAVE_NAME);
        if(tempFile.exists()){
            try {
                tempFile.delete();
            }catch (Exception e){}
        }
    }

    //删除本地的文件
    private void doDeleteDBRecord(){
        MApplication.mCommonDao.deleteAllUpdateBean();
    }

    //获取下载文件的长度
    private long doGetDownloadLength(String urlPath){
        if(TextUtils.isEmpty(urlPath)) return 0;
        long tempFileLength = 0;
        HttpURLConnection urlcon = null;
        try{
            URL url = new URL(urlPath);
            urlcon = (HttpURLConnection) url.openConnection();
            tempFileLength = urlcon.getContentLength();
        }catch (Exception e){}
        finally {
            if(urlcon != null){
                urlcon.disconnect();
            }
        }
        return tempFileLength;
    }

    //下载的第一次保存要下载的文件(必须要做这个动作,因为存在自己流量进入,在使用过程中切换成无线wifi)
    private boolean doSaveDBRecord(UpdateBean bean){
        boolean isInsert =  MApplication.mCommonDao.insertUpdateBean(bean);
        return isInsert;
    }

    public void setServerBean(UpdateBean mServerBean) {
        this.mServerBean = mServerBean;
    }
}
