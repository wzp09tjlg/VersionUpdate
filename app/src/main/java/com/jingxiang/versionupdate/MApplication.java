package com.jingxiang.versionupdate;

import android.app.Application;
import android.content.Context;

import com.jingxiang.versionupdate.database.CommonDao;
import com.jingxiang.versionupdate.download.UpdateManager;
import com.jingxiang.versionupdate.util.ThreadPool;

/**
 * Created by wu on 2016/11/3.
 */
public class MApplication extends Application {
    private Context mContext;
    //应用全局的变量
    public static CommonDao mCommonDao;  //数据库
    public static UpdateManager mUpdateManager; //版本更新管理类

    /*********************************************/
    @Override
    public void onCreate() {
        super.onCreate();

        //全局的属性设置
        initGlobalVar();
    }

    private void initGlobalVar(){
        mContext = this;
        mCommonDao = new CommonDao(mContext);            //数据库的公共类
        mUpdateManager = UpdateManager.getInstance();    //版本更新工具类

        ThreadPool.init();                               //线程池的初始化
    }
}
