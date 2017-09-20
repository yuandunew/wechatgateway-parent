package com.yuandu.wechatgateway.service.utils;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
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
    
    // 线程池维护线程的最少数量
    private static final int MIN_POOL_SIZE = 100;
    // 线程池维护线程的最大数量
    private static final int MAX_POOL_SIZE = 300;
    // 线程池维护线程所允许的空闲时间
    private static final int IDLE_TIME = 60;
    
    /**
     * 线程池配置
     */
    public static  ThreadPoolExecutor threadPool = new ThreadPoolExecutor(MIN_POOL_SIZE, MAX_POOL_SIZE, IDLE_TIME, TimeUnit.SECONDS,
			new ArrayBlockingQueue<Runnable>(10000), //
			new DefaultManagedAwareThreadFactory(),new RejectedExecutionHandler(){

				@Override
				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
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
     * 线程池配置
     */
    public static  ThreadPoolExecutor threadSendMessagePool = new ThreadPoolExecutor(MIN_POOL_SIZE, MAX_POOL_SIZE, IDLE_TIME, TimeUnit.SECONDS,
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
