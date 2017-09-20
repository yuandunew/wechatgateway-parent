/**
 * 
 */
package com.yuandu.wechatgateway.service.dto;

import java.io.Serializable;

/** 
 * ClassName: TicketDto
 * Function: TODO ADD FUNCTION.
 * date: 2015年12月24日 下午1:59:09
 * 
 * 微信公众号jsapi_ticket信息
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2015, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class TicketDto implements Serializable 
{
	// jsapi_ticket
	private String jsapiTicket;

	// 令牌有效时间，单位：秒
	private Long expiresIn;

	// 获取令牌时间
	private Long timestamp;

	public String getJsapiTicket() {
		return jsapiTicket;
	}

	public void setJsapiTicket(String jsapiTicket) {
		this.jsapiTicket = jsapiTicket;
	}

	public Long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(Long expiresIn) {
		this.expiresIn = expiresIn;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
}
