package com.jingxiang.versionupdate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.jingxiang.versionupdate.network.parse.UpdateBean;
import com.jingxiang.versionupdate.util.LogUtil;

/**
 * Created by wu on 2016/10/9.
 * 具体实现的DaoInterface的类 依旧是根据各个表来管理(数据的操作需要放在子线程中执行,因此在每次执行数据操作时需要开启子线程操作)
 * query()方法原型各个字段的意思
 * query(tableName,selectParams,selection,selectionArgs,groupBy,having,orderBy)
 *         表名      查询的字段(Str数组) 条件    条件参数     分组     分类    排序   （having的用法类似where 可用聚合方法）
 * insert(tableName,nullColumnHack,value)
 *         表名      当插入数据为空,系统置空的列名  插入数据的键值对ContentValues
 * delete(tableName,whereClause,whereArgs)
 *         表名      删除条件     删除条件参数(Str数组)
 * update(tableName,values,whereClause,whereArgs)
 *         表名      更新数据的键值对 更新条件  更新条件的值
 * 使用数据库时需要注意几点
 * 1.数据库操作必须在子线程中执行,在主线程中执行会卡顿甚至会出现anr
 * 2.使用游标时必须在使用完成之后关闭游标,不然会很耗费资源并且会卡顿。数据库权衡频繁打开和关闭与持续开启所耗费的资源，还是建议持续的开启，在应用最后退出的时候关闭
 * 3.在使用查询时记得关闭游标,在使用更新、插入、删除的时候，为保证数据的完整性 一定得使用事务,不然万一出现数据异常就会导致数据不一致的问题。
 */
public class CommonDao implements DaoInterface {
    /** Data */
    private CommonDB commonDB;

    /**************************************/
    public CommonDao(Context context){
        LogUtil.i("CommonDao  construct");
        commonDB = new CommonDB(context);
    }

    public void closeDb(){//关闭数据库
        LogUtil.i("CommonDB closeDB");
        if(commonDB != null){
            commonDB.close();
            LogUtil.i("CommonDB closed");
        }
    }

    /** 公共的方法 */
    //查看表是否在数据库中存在
    protected boolean isTableExist(String tableName){
        String sql = "SELECT count(*) FROM sqlite_master " +
                "WHERE type='table' AND name='" + tableName + "'";
        SQLiteDatabase db = commonDB.getReadableDatabase();
        Cursor cur = null;
        try{
            cur = db.rawQuery(sql, null);
            int count = -1;
            while (cur.moveToNext()) {
                count = cur.getInt(0);
            }
            if (count <= 0) {
                return false;
            }
        }catch (Exception e){
            return false;
        }finally {
            if(!cur.isClosed())
                cur.close();
        }
        return true;
    }

    /** version_update */
    //查询
    @Override
    public UpdateBean selectUpdateBean(String versionCode) {
        if(!isTableExist(CommonDB.TABLE_VERSION_UPDATE)) return null;
        UpdateBean bean = new UpdateBean();
        SQLiteDatabase db = commonDB.getReadableDatabase();
        Cursor cursor = null;
        try{
             cursor = db.query(CommonDB.TABLE_VERSION_UPDATE,new String[]{"url,start,end,finished,status,updates"}
                     ,"versioncode=?",new String[]{versionCode},null,null,null);
            if(cursor != null){
                if(cursor.moveToNext()){
                    bean.download_link = cursor.getString(cursor.getColumnIndex("url"));
                    bean.start = cursor.getLong(cursor.getColumnIndex("start"));
                    bean.end   = cursor.getLong(cursor.getColumnIndex("end"));
                    bean.finished = cursor.getLong(cursor.getColumnIndex("finished"));
                    bean.version_code = versionCode;
                    bean.status = cursor.getInt(cursor.getColumnIndex("status"));
                    bean.intro = cursor.getString(cursor.getColumnIndex("updates"));
                }
            }
            return bean;
        }catch (Exception e){
            LogUtil.e("selectUpdateBean e:" + e.getMessage());
        }finally {
            if(cursor != null && !cursor.isClosed())
                cursor.close();
        }
        return bean;
    }

    @Override
    public UpdateBean selectUpdateBean() {
        if(!isTableExist(CommonDB.TABLE_VERSION_UPDATE)) return null;
        UpdateBean bean = new UpdateBean();
        SQLiteDatabase db = commonDB.getReadableDatabase();
        Cursor cursor = null;
        try{
            cursor = db.query(CommonDB.TABLE_VERSION_UPDATE,new String[]{"url,start,end,finished,status,updates,versioncode"}
                    ,null,null,null,null,null);
            if(cursor != null){
                if(cursor.moveToNext()){
                    bean.download_link = cursor.getString(cursor.getColumnIndex("url"));
                    bean.start = cursor.getLong(cursor.getColumnIndex("start"));
                    bean.end   = cursor.getLong(cursor.getColumnIndex("end"));
                    bean.finished = cursor.getLong(cursor.getColumnIndex("finished"));
                    bean.version_code = String.valueOf(cursor.getInt(cursor.getColumnIndex("versioncode")));
                    bean.status = cursor.getInt(cursor.getColumnIndex("status"));
                    bean.intro = cursor.getString(cursor.getColumnIndex("updates"));
                }
            }
            return bean;
        }catch (Exception e){
            LogUtil.e("selectUpdateBean e:" + e.getMessage());
        }finally {
            if(cursor != null && !cursor.isClosed())
                cursor.close();
        }
        return bean;
    }

    //插入
    @Override
    public boolean insertUpdateBean(UpdateBean bean) {
        if(bean == null) return false;
        SQLiteDatabase db = commonDB.getWritableDatabase();
        ContentValues cv = new ContentValues();
        long tempResult = 0;
        try{
            db.beginTransaction();
            cv.put("url",bean.download_link);
            cv.put("start",bean.start);
            cv.put("end",bean.end);
            cv.put("finished",bean.finished);
            cv.put("versioncode",bean.version_code);
            cv.put("status",bean.status);
            cv.put("updates",bean.intro);
            tempResult = db.insert(CommonDB.TABLE_VERSION_UPDATE,null,cv);
            db.setTransactionSuccessful();
        }catch (Exception e){
            LogUtil.e("insertUpdateBean e:" + e.getMessage());
        }
        finally {
            db.endTransaction();
        }
        return tempResult > 0;
    }

    //删除单条记录
    @Override
    public boolean deleteUpdateBean(String versionCode) {
        if(TextUtils.isEmpty(versionCode)) return false;
        SQLiteDatabase db = commonDB.getWritableDatabase();
        long tempResult = 0;
        try {
            db.beginTransaction();
            tempResult = db.delete(CommonDB.TABLE_VERSION_UPDATE
                    ,"versionCode=?",new String[]{versionCode});
            db.setTransactionSuccessful();
        }catch (Exception e){
            LogUtil.e("deleteUpdateBean e:" + e.getMessage());
        }
        finally {
            db.endTransaction();
        }
        return tempResult != 0;
    }

    //删除所有
    @Override
    public boolean deleteAllUpdateBean() {
        SQLiteDatabase db = commonDB.getWritableDatabase();
        long tempResult = 0;
        try {
            db.beginTransaction();
            tempResult = db.delete(CommonDB.TABLE_VERSION_UPDATE
                    ,null,null);
            db.setTransactionSuccessful();
        }catch (Exception e){
            LogUtil.e("deleteAllUpdateBean e:" + e.getMessage());
        }
        finally {
            db.endTransaction();
        }
        return tempResult != 0;
    }

    //更新
    @Override
    public boolean updateUpdateBean(UpdateBean bean) {
        if(bean == null) return false;
        SQLiteDatabase db = commonDB.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("start",bean.start);
        cv.put("end",bean.end);
        cv.put("finished",bean.finished);
        cv.put("status",bean.status);
        long tempResult = 0;
        try {
            db.beginTransaction();
            tempResult = db.update(CommonDB.TABLE_VERSION_UPDATE,cv,"versioncode=?",new String[]{bean.version_code});
            db.setTransactionSuccessful();
        }catch (Exception e){
            LogUtil.e("updateUpdateBean e:" + e.getMessage());
        }
        finally {
            db.endTransaction();
        }
        return tempResult > 0;
    }
}
