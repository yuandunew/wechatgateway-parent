package com.yuandu.wechatgateway.service.dto;

import java.io.Serializable;

/** 
 * ClassName: MPAccountWhitelist
 * Function: TODO ADD FUNCTION.
 * date: 2015年12月22日 下午3:53:44
 * 
 *  微信用户白名单
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2015, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class MPAccountWhitelist implements Serializable
{
	private String openId; // 白名单openid

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}
}
