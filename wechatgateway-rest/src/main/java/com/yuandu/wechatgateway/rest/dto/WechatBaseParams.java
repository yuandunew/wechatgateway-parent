package com.yuandu.wechatgateway.rest.dto;

import java.io.Serializable;

public class WechatBaseParams extends BaseParams implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7863236045412323767L;
	private String openId;
	private String userId;

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	
}
