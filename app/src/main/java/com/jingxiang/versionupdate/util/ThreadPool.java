package com.jingxiang.versionupdate.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by wu on 2016/10/5.
 * 线程池的管理,常见的四个线程池 分别是NewFixedThreadPool NewCacheThreadPool
 * NewScheduleThreadPool NewInstanceExcutor
 * 但是在自己构建线程池的时候，并非使用的是上边四种线程池.就是使用的最基本的ThreadPoolExcutor来构建的
 */
public class ThreadPool {
    private static ThreadPoolExecutor executorService;

    public static void init(){
        //使用机器最多可使用的内核数,最多创建8个线程,超时时间是60秒(即一个线程超过60秒的时候就被回收)
        executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                8,60l, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
        executorService.allowCoreThreadTimeOut(true);
    }

    //执行任务
    public static void execute(Runnable runnable){
        LogUtil.i("线程池中线程数目：" + executorService.getPoolSize() + "，队列中等待执行的任务数目：" +
                executorService.getQueue().size() + "，已执行完别的任务数目：" + executorService.getCompletedTaskCount());
        executorService.execute(runnable);
    }

    //结束任务
    public static void shutdown(){
        executorService.shutdown();
    }

}
