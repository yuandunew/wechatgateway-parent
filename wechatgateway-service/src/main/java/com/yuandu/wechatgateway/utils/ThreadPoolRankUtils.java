package com.yuandu.wechatgateway.utils;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
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
 * 
 * @author shiijy
 * 
 */
public class ThreadPoolRankUtils {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolRankUtils.class);
    
    // 创建定时以及周期性执行任务的线程池
 	private static ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(10);

	
    /**
     * 线程池配置
     */
    public static  ThreadPoolExecutor threadSendMessagePool = new ThreadPoolExecutor(100, 100, 60, TimeUnit.SECONDS,
			new ArrayBlockingQueue<Runnable>(2000), //
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
			logger.error(t.getMessage(), t);
	}

	/**
	 * 每隔period秒执行定时以及周期性任务的线程
	 * 
	 * @param runnable
	 * @param period
	 */
	public static void excuteScheduledThreadPool(Runnable runnable, long initialDelay, long period) {
		if (scheduledPool.isShutdown()) {
			scheduledPool = Executors.newScheduledThreadPool(1);
		}
		scheduledPool.scheduleAtFixedRate(runnable, initialDelay, period, TimeUnit.SECONDS);
	}
	
}
