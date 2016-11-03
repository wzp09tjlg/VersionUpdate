package com.jingxiang.versionupdate.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jingxiang.versionupdate.util.LogUtil;


/**
 * Created by wu on 2016/10/8.
 * 通用的数据库工具类
 * 1.SQL的关键字使用大些字母来实现,表属性使用小写字母来实现
 * 2.在创建表时,一定记得在创建表字段时注明 含义及创建的时间和版本,方便以后对数据库的维护
 * 3.查看源码得知,在每次操作数据库的时候并不一定要关闭数据库,每次多去数据库时都会进行处理。
 *   这样可以减少打开和关闭数据库的资源消耗,但是游标记得一定得关闭.不然会会耗费大量资源,
 *   也会出现错误.
 * 4.册数数据库的升级操作 版本1 只有表news 版本2 添加表video 版本3 添加表version_update
 */
public class CommonDB extends SQLiteOpenHelper {
    /** Data */
    private Context mContext;

    /** FinalData */
    private static final String  DB_NAME = "CommonDB.db";//创建数据库的名字
    public static int  VERSION = 1;

    public static final String TABLE_VERSION_UPDATE = "version_update";//应用版本更新表

    private final String CREATE_TABLE_VERSION_UPDATE = "create table if not exists " + TABLE_VERSION_UPDATE +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " url   TEXT, " +            //下载的地址
            " start INTEGER, " +         //下载开始的地址(可能存在多次下载,每次下载是基于上一次下载之后,再进行的下载)
            " end   INTEGER, " +         //下载结束的地址(总文件大小)
            " finished    INTEGER, " +   //下载已经完成的地址(上一次下载)
            " versioncode INTEGER," +    //下载的版本号 versionCode
            " status  INTEGER, " +       //状态  (未下载,下载未完成,下载已完成)
            " updates TEXT" +            //客户端更新的提示信息
              ")";

    /**************************************/
    public CommonDB(Context context){
        super(context, DB_NAME, null, VERSION);
        this.mContext = context;
        LogUtil.i("CommonDB construct");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {//第一次获取数据对象实例的时候执行onCreate方法
        LogUtil.i("CommonDB onCreate");
        db.execSQL(CREATE_TABLE_VERSION_UPDATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {//在数据库升级过程中，第一次获取数据库实例的时候执行onUpdate方法
        LogUtil.i("CommonDB onUpdate");
    }
}
