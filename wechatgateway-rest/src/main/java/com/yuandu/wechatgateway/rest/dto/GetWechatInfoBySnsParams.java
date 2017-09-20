/**
 * 
 */
package com.yuandu.wechatgateway.rest.dto;

/** 
 * ClassName: GetWechatOpenPrams
 * Function: TODO ADD FUNCTION.
 * date: 2016年3月21日 下午9:37:33
 * 获取openId参数
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2016, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class GetWechatInfoBySnsParams extends BaseParams 
{
	private String accessToken;
	
	private String openId;

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}
	
	
}
