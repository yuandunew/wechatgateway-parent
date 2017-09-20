/**
 * 
 */
package com.yuandu.wechatgateway.service.dto;


/** 
 * ClassName: TestMQ
 * Function: TODO ADD FUNCTION.
 * date: 2016年1月17日 上午9:31:12
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2016, lifesense.com All Rights Reserved.
 */
public class TestMQ {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		NotSendMessageQueue<NotSendMessage> nsmq=new NotSendMessageQueue<>();
		
		NotSendMessage sm1=new NotSendMessage();
		sm1.setReissueCount(0);
//		sm1.setServiceNo("1");
		
		NotSendMessage sm2=new NotSendMessage();
		sm2.setReissueCount(0);
//		sm2.setServiceNo("2");
		
		NotSendMessage sm3=new NotSendMessage();
		sm3.setReissueCount(0);
//		sm3.setServiceNo("3");
		
		NotSendMessage sm4=new NotSendMessage();
		sm3.setReissueCount(0);
//		sm4.setServiceNo("4");
		
		nsmq.offer(sm1);
		nsmq.offer(sm2);
		nsmq.offer(sm3);
		nsmq.offer(sm4);
		
		NotSendMessageQueue<NotSendMessage> nsmq2=new NotSendMessageQueue<>();
		for(int i=0;i<nsmq.size();i=0)
		{
			NotSendMessage sm=nsmq.remove();
			
//			System.out.println("ServiceNo:"+sm.getServiceNo());
			System.out.println("ReissueCount:"+sm.getReissueCount());
			System.out.println("*******************************");
			
//			if(sm.getServiceNo().equals("3"))
//			{
//				sm.setReissueCount(2);
//			}
			
			//最多补发5次
			if(sm.getReissueCount()<6)
			{
				nsmq2.offer(sm);
			}
		}
		
		System.out.println("nsmq.size:"+nsmq.size());
		
		for(int j=0;j<nsmq2.size();j=0)
		{
			NotSendMessage sm=nsmq2.remove();
			
//			System.out.println("ServiceNo2:"+sm.getServiceNo());
			System.out.println("ReissueCount2:"+sm.getReissueCount());
			System.out.println("*******************************");
			j++;
		}
	}
}
