package com.jingxiang.versionupdate.view.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.jingxiang.versionupdate.MApplication;
import com.jingxiang.versionupdate.MainActivity;
import com.jingxiang.versionupdate.R;
import com.jingxiang.versionupdate.network.parse.UpdateBean;
import com.jingxiang.versionupdate.util.CommonHelper;
import com.jingxiang.versionupdate.util.FinalUtil;
import com.jingxiang.versionupdate.util.LogUtil;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by wu on 2016/11/2.
 */
public class LoadingActivity extends Activity {
    private static final int TYPE_ENTER = 0X101;
    /** View */
    private ImageView iconLoading;
    /** Data */
    private Context mContext;
    private boolean isNeedUpdate = false;
    private String strMd5 = "";
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case TYPE_ENTER:
                    MainActivity.luanch(mContext,isNeedUpdate,strMd5);
                    finish();
                    break;
            }
        }
    };
    /**************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mHandler != null){
            mHandler.removeMessages(TYPE_ENTER);
        }
    }

    private void initViews(){
       iconLoading = (ImageView)findViewById(R.id.img_loading);

        initData();
    }

    private void initData(){
        mContext = this;
        int curVersionCode = CommonHelper.getVersionCode(mContext);
        String tempUrl= FinalUtil.HOST_UPDATE + "?app_version=" + curVersionCode + "&app_channel=Huasheng";

        //使用OkHttp 框架请求服务 获取版本更新的信息
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(tempUrl).build();
        client.newCall(request).enqueue(getCallBackListener());

        mHandler.sendEmptyMessageDelayed(TYPE_ENTER,3000);
    }

    private void checkUpdate(UpdateBean bean){
        if(bean == null) return;
        if(TextUtils.isEmpty(bean.download_link)) return;
        strMd5 = bean.apk_md5;
        MApplication.mUpdateManager.setServerBean(bean);
        if(MApplication.mUpdateManager.checkUpdate(mContext)) {
            MApplication.mUpdateManager.doUpdate(mContext,Integer.parseInt(bean.version_code));
        }
    }

    private Callback getCallBackListener(){
        Callback listener = new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.i("android","get update info failure");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String tempData = "";
                try{
                    ResponseBody body = response.body();
                    if(body != null)
                        tempData = new String(body.bytes());
                }catch (Exception e){
                    LogUtil.e("Exception: data is null:" + e.getMessage() +"  --> " + (TextUtils.isEmpty(tempData)) + "  response:" + response.toString());
                }
                if(TextUtils.isEmpty(tempData)) return;
                try{
                    JSONObject jsonObject = new JSONObject(tempData);
                    Object tempObject = jsonObject.get("data");
                    Gson tempGson = new Gson();
                    UpdateBean bean = tempGson.fromJson(tempObject.toString(), UpdateBean.class);
                    checkUpdate(bean);
                }catch (Exception e){
                    LogUtil.e("e.msg:" + e.getMessage());
                }
            }
        };
        return listener;
    }

}
