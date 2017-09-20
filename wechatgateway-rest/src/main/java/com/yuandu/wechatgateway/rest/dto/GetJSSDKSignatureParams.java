/**
 * 
 */
package com.yuandu.wechatgateway.rest.dto;

/** 
 * ClassName: GetJSSDKSignatureParams
 * Function: TODO ADD FUNCTION.
 * date: 2016年3月21日 下午9:42:26
 * 获取JSSDK签名参数
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2016, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class GetJSSDKSignatureParams extends BaseParams 
{
	private String url;

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
