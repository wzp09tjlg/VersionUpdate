package com.jingxiang.versionupdate.database;


import com.jingxiang.versionupdate.network.parse.UpdateBean;

/**
 * Created by wu on 2016/10/9.
 * dao中执行的动作都统一在这里定义,便于统一管理。这里是根据表名来划分逻辑处理
 */
public interface DaoInterface {
    /** version_update */
    //查询
    UpdateBean selectUpdateBean(String versionCode);
    UpdateBean selectUpdateBean();
    //插入
    boolean insertUpdateBean(UpdateBean bean);
    //删除
    boolean deleteUpdateBean(String versionCode);
    boolean deleteAllUpdateBean();
    //更新
    boolean updateUpdateBean(UpdateBean bean);
}
