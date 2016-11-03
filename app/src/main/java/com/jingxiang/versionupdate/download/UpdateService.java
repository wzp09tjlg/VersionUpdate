package com.jingxiang.versionupdate.download;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.jingxiang.versionupdate.MApplication;
import com.jingxiang.versionupdate.network.parse.UpdateBean;
import com.jingxiang.versionupdate.util.LogUtil;
import com.jingxiang.versionupdate.util.ThreadPool;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by wu on 2016/10/19.
 */
public class UpdateService extends Service {
    /** Data */
    public static boolean isPause = true;//控制下载的状态,状态改变只有在进入时可以下载时改变 网络状态发生改变时改变
    private Context mContext;
    private DownTask mDownTask;
    private UpdateBean bean;

    /************************************/
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = this;
        if(isPause){ //广播中 如果出现重复的广播，就会导致开启多个updateService 可能会导致异常
            initData(mContext,intent);
            startDownload();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isPause = true;
    }

    private void initData(Context context,Intent intent){
        try{
            if(bean == null)//如果之前已经开启过服务，那么bean就应该不为空
              bean = (UpdateBean) intent.getSerializableExtra("BEAN");
        }catch (Exception e){}
        if(bean != null)
          mDownTask = new DownTask(context,bean);
    }

    private void startDownload(){
        isPause = false;
        if(mDownTask != null){
            ThreadPool.execute(mDownTask);
        } else{
            stopSelf();
        }
    }

    //请求网络
    private void doDownloadFromService(Context context, UpdateBean bean) {
        if(bean == null || TextUtils.isEmpty(bean.download_link) || bean.end <= 0){
            Intent intentStopService = new Intent(context,UpdateService.class);
            context.stopService(intentStopService);//在服务中没有更新的bean,那么不进行更新下载操作.同样也实用于网络的状态的改变
            return;//网络状态的改变 开启和关闭更新的服务,但是需要做对空的判断
        }
        HttpURLConnection conn = null;
        RandomAccessFile raf = null;
        InputStream in = null;
        int respCode = 0;
        long finished = 0;
        int requestCount = 0;//请求次数
        try {
            URL url = new URL(bean.download_link);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            // 设置下载位置
            long start = bean.start + bean.finished;
            conn.setRequestProperty("Range",
                    "bytes=" + start + "-" + bean.end);
            // 设置文件写入位置
            File file = new File(UpdateManager.DOWNLOAD_FILE_SAVE_PATH, UpdateManager.DOWNLOAD_FILE_SAVE_NAME);
            raf = new RandomAccessFile(file, "rwd");
            raf.seek(start);
            finished += bean.finished;
            respCode = conn.getResponseCode();
            // 开始下载
            if (respCode == 206 || respCode == 200) {//206 Partial Content
                // 读取数据
                in = conn.getInputStream();
                int len = -1;
                byte[] b = new byte[1024 * 4];
                while ((len = in.read(b)) != -1) {
                    // 写入文件
                    raf.write(b, 0, len);
                    finished += len;
                    LogUtil.e("abcde","finished:" + ((finished * 1.0f) / (bean.end * 1.0f) * 100) + "%");
                    bean.finished = finished;
                    if (finished == bean.end) {
                        bean.status = UpdateManager.DOWNLOAD_STATUS_FINISHED;
                    } else if (finished > 0 && finished < bean.end) {
                        bean.status = UpdateManager.DOWNLOAD_STATUS_UNFINISHED;
                    }

                    MApplication.mCommonDao.updateUpdateBean(bean);//已经在子线程中 所以这里不在开子线程进行数据的操作写数据库数据

                    if (isPause) {//因为网络切换 不在联网或者是不再是wifi情况下 就不下载，但是服务会一直存在着的
                        return;
                    }
                }
                //去关闭下载服务
                Intent intentStopDownload = new Intent(context, UpdateService.class);
                context.stopService(intentStopDownload);
            }

        } catch (Exception e) {
            if (respCode == -1 || respCode == 0) {
                if (requestCount < 3) {
                    //重新联网一次
                    requestCount++;
                    connectionClose(raf, conn);
                    doDownloadFromService(context,bean);
                } else {
                    requestCount = 0;
                    connectionClose(raf, conn);
                }
            } else if (respCode == HttpURLConnection.HTTP_INTERNAL_ERROR
                    || respCode == HttpURLConnection.HTTP_NOT_FOUND || respCode == HttpURLConnection.HTTP_BAD_GATEWAY
                    || respCode == HttpURLConnection.HTTP_UNAUTHORIZED
                    || respCode == HttpURLConnection.HTTP_GATEWAY_TIMEOUT) {
                connectionClose(raf, conn);
            }
            e.printStackTrace();
        } finally {
            connectionClose(raf, conn);
        }
    }

    /**处理连接失败*/
    public static final void connectionClose(RandomAccessFile raf, final HttpURLConnection conn) {
        try {
            if (null != raf) {
                raf.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try{
            if (null != conn) {
                conn.disconnect();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    class DownTask implements Runnable{
        private UpdateBean bean;
        private Context mContext;

        public DownTask(Context context, UpdateBean bean){
            this.mContext = context;
            this.bean = bean;
        }

        @Override
        public void run() {
            doDownloadFromService(mContext,bean);
        }
    }
}
