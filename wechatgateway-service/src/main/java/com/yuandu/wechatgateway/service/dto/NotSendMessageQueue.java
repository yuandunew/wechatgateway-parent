/**
 * 
 */
package com.yuandu.wechatgateway.service.dto;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/** 
 * ClassName: NotSendMessageStack
 * Function: TODO ADD FUNCTION.
 * date: 2016年1月15日 下午6:50:55
 * 未发送消息队列
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2016, lifesense.com All Rights Reserved.
 */
public class NotSendMessageQueue<T> 
{
	private Queue<T> storage = new LinkedList<T>();

    /** 将指定的元素插入队尾 */
    public void offer(T v) {
        storage.offer(v);
    }

    /** 检索，但是不移除队列的头，如果此队列为空，则返回 null */
    public T peek() {
        return storage.peek();
    }

    /** 检索，但是不移除此队列的头 */
    /** 此方法与 peek 方法的惟一不同是，如果此队列为空，它会抛出一个异常 */
    public T element() {
        return storage.element();
    }

    /** 检索并移除此队列的头，如果队列为空，则返回 null */
    public T poll() {
        return storage.poll();
    }

    /** 检索并移除此队列的头 */
    /** 此方法与 poll 方法的不同在于，如果此队列为空，它会抛出一个异常 */
    public T remove() {
        return storage.remove();
    }

    /** 队列是否为空 */
    public boolean empty() {
        return storage.isEmpty();
    }

    /** 打印队列元素 */
    public String toString() {
        return storage.toString();
    }
    
    /** 队列大小 */
    public int size()
    {
    	return storage.size();
    }
    
    /** 迭代器 */
    public Iterator<T> iterator()
    {
    	return storage.iterator();
    }
}
