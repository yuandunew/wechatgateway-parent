/**
 * 
 */
package com.yuandu.wechatgateway.service.dto;

import java.io.Serializable;

import com.lifesense.base.constant.AppType;

/** 
 * ClassName: NotSendMessage
 * Function: TODO ADD FUNCTION.
 * date: 2016年1月15日 下午2:54:46
 * 未发送成功的消息
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2016, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class NotSendMessage implements Serializable 
{
	private String sendUrl;       //发送URL
//	private String serviceNo;     //微信公众号
	private String jsonMessage;   //消息内容json串
	
	private int reissueCount;   //已补发次数
	
	private AppType appType;  //应用类型
	
	
	public String getSendUrl() {
		return sendUrl;
	}
	public void setSendUrl(String sendUrl) {
		this.sendUrl = sendUrl;
	}
//	public String getServiceNo() {
//		return serviceNo;
//	}
//	public void setServiceNo(String serviceNo) {
//		this.serviceNo = serviceNo;
//	}
	public String getJsonMessage() {
		return jsonMessage;
	}
	public void setJsonMessage(String jsonMessage) {
		this.jsonMessage = jsonMessage;
	}
	public int getReissueCount() {
		return reissueCount;
	}
	public void setReissueCount(int reissueCount) {
		this.reissueCount = reissueCount;
	}
	public AppType getAppType() {
		return appType;
	}
	public void setAppType(AppType appType) {
		this.appType = appType;
	}
}
