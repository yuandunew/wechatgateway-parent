/**
 * 
 */
package com.yuandu.wechatgateway.service.dto.response;

/** 
 * ClassName: CustomerResponse
 * Function: TODO ADD FUNCTION.
 * date: 2015年12月25日 上午10:15:30
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2015, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class CustomerResponse extends HttpResponse
{
	//响应内容
	private String data;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
