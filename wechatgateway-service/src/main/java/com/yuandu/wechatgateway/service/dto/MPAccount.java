package com.yuandu.wechatgateway.service.dto;

import java.io.Serializable;
import java.util.List;

/** 
 * ClassName: ForwardUrl
 * Function: TODO ADD FUNCTION.
 * date: 2015年12月22日 下午3:50:33
 * 
 * 转发消息URL
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2015, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class MPAccount implements Serializable
{
	private String serviceNo; // 公众号原始ID
	
	private String msgForwardUrl; // 关注、绑定、扫描等消息转发地址
	
	private String recordForwardUrl; // 测量记录消息转发地址（device_text消息）
	
	private String voiceForwardUrl; // 公众号语音转发地址（voice消息）
	
	private String customerForwardUrl; // 多客服转发地址
	
	private String whiteListForwardUrl; // 白名单转发地址
	
	private String msgToWxUserForwardUrl; // 发送消息给微信用户
	
	private List<MPAccountWhitelist> whiteList; // 白名单列表

	public String getServiceNo() {
		return serviceNo;
	}

	public void setServiceNo(String serviceNo) {
		this.serviceNo = serviceNo;
	}

	public String getMsgForwardUrl() {
		return msgForwardUrl;
	}

	public void setMsgForwardUrl(String msgForwardUrl) {
		this.msgForwardUrl = msgForwardUrl;
	}

	public String getRecordForwardUrl() {
		return recordForwardUrl;
	}

	public void setRecordForwardUrl(String recordForwardUrl) {
		this.recordForwardUrl = recordForwardUrl;
	}

	public String getVoiceForwardUrl() {
		return voiceForwardUrl;
	}

	public void setVoiceForwardUrl(String voiceForwardUrl) {
		this.voiceForwardUrl = voiceForwardUrl;
	}

	public String getCustomerForwardUrl() {
		return customerForwardUrl;
	}

	public void setCustomerForwardUrl(String customerForwardUrl) {
		this.customerForwardUrl = customerForwardUrl;
	}

	public String getWhiteListForwardUrl() {
		return whiteListForwardUrl;
	}

	public void setWhiteListForwardUrl(String whiteListForwardUrl) {
		this.whiteListForwardUrl = whiteListForwardUrl;
	}

	public String getMsgToWxUserForwardUrl() {
		return msgToWxUserForwardUrl;
	}

	public void setMsgToWxUserForwardUrl(String msgToWxUserForwardUrl) {
		this.msgToWxUserForwardUrl = msgToWxUserForwardUrl;
	}

	public List<MPAccountWhitelist> getWhiteList() {
		return whiteList;
	}

	public void setWhiteList(List<MPAccountWhitelist> whiteList) {
		this.whiteList = whiteList;
	}
}
