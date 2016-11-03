package com.jingxiang.versionupdate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.jingxiang.versionupdate.download.UpdateManager;
import com.jingxiang.versionupdate.network.parse.UpdateBean;
import com.jingxiang.versionupdate.util.DeviceInfoManager;
import com.jingxiang.versionupdate.util.MD5Helper;
import com.jingxiang.versionupdate.util.ThreadPool;
import com.jingxiang.versionupdate.view.widget.CommonDialog;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    /** View */

    /** Data */
    private Context mContext;
    private boolean isNeedUpdate = false;
    private String strMd5 = "";
    private UpdateBean bean;
    private CommonDialog commonDialog;    //更新弹框
    /***********************************************/
    public static void luanch(Context context,boolean isNeedUpdate,String strMd5){
        Bundle bundle = new Bundle();
        bundle.putBoolean("NEEDUPDATE",isNeedUpdate);
        bundle.putString("MD5",strMd5);
        Intent intentMain = new Intent(context,MainActivity.class);
        intentMain.putExtras(bundle);
        context.startActivity(intentMain);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getExtra(getIntent());
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void getExtra(Intent intent){
        Bundle bundle = intent.getExtras();
        isNeedUpdate = bundle.getBoolean("NEEDUPDATE",false);
        strMd5 = bundle.getString("MD5");
    }

    private void initViews(){
        mContext = this;
        checkNeedUpdate();
    }

    //应用的版本更新(最新处理)
    private void checkNeedUpdate(){
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                bean = MApplication.mCommonDao.selectUpdateBean();
                if(bean == null || TextUtils.isEmpty(bean.version_code)) return;//如果数据库只能那个无记录, 或者记录的版本号为空 不更新处理
                int dbVerCode = 0;
                if(bean != null && TextUtils.isEmpty(bean.version_code))
                    dbVerCode = Integer.parseInt(bean.version_code);
                int curVerCode =  DeviceInfoManager.getAppVersionCode(mContext);
                if(bean != null && dbVerCode == curVerCode && dbVerCode > 0){
                    deleteUpdateDBandFile();
                }
                if(bean != null && bean.end == bean.finished && bean.end > 0 && bean.status == 3){ //在下一版中添加这个状态 1未下载 2下载未完成 3下载完成
                    doUpdateOperate();
                }
            }
        });
    }

    private void deleteUpdateDBandFile(){
        try{
            MApplication.mCommonDao.deleteUpdateBean(bean.version_code);
            File tempFile = new File(UpdateManager.DOWNLOAD_FILE_SAVE_PATH + File.separator
                    + UpdateManager.DOWNLOAD_FILE_SAVE_NAME);
            if(tempFile != null && tempFile.exists()){
                tempFile.delete();//如果下载的文件已经和安装的一个版本 就直接删除数据文件和下载的文件
            }
            return;
        }catch (Exception e){}
    }

    private void doUpdateOperate(){
        File file = new File(UpdateManager.DOWNLOAD_FILE_SAVE_PATH + File.separator + UpdateManager.DOWNLOAD_FILE_SAVE_NAME);
        String localFileMd5 = "";
        if(!file.exists()){
            try{
                File tempFile = new File(UpdateManager.DOWNLOAD_FILE_SAVE_PATH + File.separator);
                if(!tempFile.exists()){
                    tempFile.mkdirs();
                }
            }catch (Exception e){}
        }else{
            localFileMd5 = MD5Helper.getMD5(file).toUpperCase();//转成大写
        }
        strMd5 = strMd5.toUpperCase();                 //转成大写
        boolean checkFileMd5 = false;
        if(!TextUtils.isEmpty(strMd5) && !TextUtils.isEmpty(localFileMd5) && localFileMd5.equals(strMd5))
            checkFileMd5 = true;
        if(checkFileMd5 && file != null && file.exists()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    commonDialog = new CommonDialog(mContext);
                    commonDialog.setCanceledOnTouchOutside(false);
                    commonDialog.setShowTitle(false);
                    commonDialog.setShowClose(false);
                    View view = getLayoutInflater().inflate(R.layout.update_dialog, null);
                    ((TextView) view.findViewById(R.id.umeng_update_content)).setText(bean.intro);

                    commonDialog.setContainer(view);
                    view.findViewById(R.id.umeng_update_id_ok).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            commonDialog.dismiss();
                            //设置你的操作事项
                            //安装应用程序
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            File file = new File(UpdateManager.DOWNLOAD_FILE_SAVE_PATH + File.separator + UpdateManager.DOWNLOAD_FILE_SAVE_NAME);

                            intent.setDataAndType(Uri.fromFile(file),
                                    "application/vnd.android.package-archive");
                            startActivity(intent);
                        }
                    });

                    view.findViewById(R.id.umeng_update_id_cancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            commonDialog.dismiss();
                        }
                    });

                    view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            commonDialog.dismiss();
                            //设置你的操作事项
                            //安装应用程序
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            File file = new File(UpdateManager.DOWNLOAD_FILE_SAVE_PATH + File.separator
                                    + UpdateManager.DOWNLOAD_FILE_SAVE_NAME);
                            intent.setDataAndType(Uri.fromFile(file),
                                    "application/vnd.android.package-archive");
                            startActivity(intent);
                        }
                    });
                    commonDialog.showDialog();
                }
            });
        }else{//如果下载的文件MD5值不等,肯定安装不成功.所以这是删除本地数据库记录和本地文件
            ThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    deleteUpdateDBandFile();
                }
            });
        }
    }
}
