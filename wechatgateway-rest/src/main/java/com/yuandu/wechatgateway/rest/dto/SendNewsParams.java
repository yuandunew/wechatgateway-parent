/**
 * 
 */
package com.yuandu.wechatgateway.rest.dto;

import com.lifesense.base.dto.wechatgateway.WechatCustomServiceNewsMessage;

/** 
 * ClassName: SendNewsParams
 * Function: TODO ADD FUNCTION.
 * date: 2016年3月21日 下午9:34:44
 * 发送图文消息参数
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2016, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class SendNewsParams extends BaseParams
{
	//图文消息
	private WechatCustomServiceNewsMessage newsMessage;

	public WechatCustomServiceNewsMessage getNewsMessage() {
		return newsMessage;
	}

	public void setNewsMessage(WechatCustomServiceNewsMessage newsMessage) {
		this.newsMessage = newsMessage;
	}
}
