package com.jingxiang.versionupdate.network.parse;

import java.io.Serializable;

/**
 * Created by wu on 2016/10/19.
 */
public class UpdateBean implements Serializable {
    // 服务端返回的信息
    public String version_code; //版本号
    public String version_name; //版本名
    public String intro;        //简介
    public String download_link;//下载地址
    public String apk_md5;      //文件MD5值
    public boolean force_update;//是否强制更新

    //数据库保存的信息
    public int status;          //下载的状态
    public long start;          //下载开始的位置(可能会存在多次下载,所以需要记录上一次下载的位置)
    public long end;            //下载的结束位置(可以理解成整个应用的大小)
    public long finished;       //下载完成的地址

    @Override
    public String toString() {
        return "{version_code:" + version_code + ";version_name:" + version_name
                + ";intro:" + intro + ";download_link:" + download_link
                + ";apk_md5:" + apk_md5 + ";force_update:" + force_update
                + ";status:" + status + ";start:" + start
                + ";end:" + end + ";finished:" + finished + "}";
    }
}
