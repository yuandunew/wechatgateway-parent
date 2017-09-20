/**
 * 
 */
package com.yuandu.wechatgateway.service.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.DefaultManagedAwareThreadFactory;

/** 
 * ClassName: ThreadPoolUtils
 * Function: TODO ADD FUNCTION.
 * date: 2015年12月25日 下午5:03:30
 * 
 * 线程池工具类
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2015, lifesense.com All Rights Reserved.
 */
public class ThreadPoolUtils 
{
	 private static final Logger logger = LoggerFactory.getLogger(ThreadPoolUtils.class);
	//创建可缓存的线程池
	private static ExecutorService cachedPool = Executors.newCachedThreadPool();
		
	//创建定时以及周期性执行任务的线程池（1个线程）
	private static ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(1);

	/**
	 * 执行异步方法
	 * @param runnable
	 */
	public static void excuteCachedThreadPool(Runnable runnable) 
	{
		if(cachedPool.isShutdown())
		{
			cachedPool = Executors.newCachedThreadPool();
		}
		cachedPool.execute(runnable);
	}
		
	/**
	 * 每隔period秒执行定时以及周期性任务的线程
	 * @param runnable
	 * @param period
	 */
	public static void excuteScheduledThreadPool(Runnable runnable,long initialDelay, long period) 
	{
		if(scheduledPool.isShutdown())
		{
			scheduledPool = Executors.newScheduledThreadPool(1);
		}
		scheduledPool.scheduleAtFixedRate(runnable, initialDelay, period, TimeUnit.SECONDS);
	}
	
    /**
     * 线程池配置
     */
    public static  ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 100, 60, TimeUnit.SECONDS,
			new ArrayBlockingQueue<Runnable>(10000), //
			new DefaultManagedAwareThreadFactory(),new RejectedExecutionHandler(){

				@Override
				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
					// TODO Auto-generated method stub
			        if (!executor.isShutdown()) {
			            r.run();
			        }
				}}) {
		protected void afterExecute(Runnable r, Throwable t) {
			super.afterExecute(r, t);
			printException(r, t);
		}
	};
	
	/**
	 * 线程池内异常处理
	 * @param r
	 * @param t
	 */
	private static void printException(Runnable r, Throwable t) {
		if (t == null && r instanceof Future<?>) {
			try {
				Future<?> future = (Future<?>) r;
				if (future.isDone())
					future.get();
			} catch (CancellationException ce) {
				t = ce;
			} catch (ExecutionException ee) {
				t = ee.getCause();
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt(); // ignore/reset
			}
		}
		if (t != null)
			logger.error("ThreadPoolRankUtils_"+t.getMessage(), t);
	}

}
