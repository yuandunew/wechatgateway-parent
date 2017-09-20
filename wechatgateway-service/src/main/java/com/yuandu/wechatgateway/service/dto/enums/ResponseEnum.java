/**
 * 
 */
package com.yuandu.wechatgateway.service.dto.enums;

/** 
 * ClassName: ResponseEnum
 * Function: TODO ADD FUNCTION.
 * date: 2016年1月13日 上午11:43:24
 * 响应枚举
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2016, lifesense.com All Rights Reserved.
 */
public enum ResponseEnum 
{
	同意接入多客服系统("1");
	
	private String responseData;
	
	public static ResponseEnum getResponseEnumByValue(String value)
	{
		switch (value)
		{
			case "1":
				return 同意接入多客服系统;
			default:
				return null;
		}
	}

	private ResponseEnum(String responseMsg) 
	{
		this.responseData = responseMsg;
	}
	
	public String toString()
	{
		return responseData;
	}
}
