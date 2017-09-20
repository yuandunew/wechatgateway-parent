/**
 * 
 */
package com.yuandu.wechatgateway.service.dto.response;

import java.io.Serializable;

/** 
 * ClassName: HttpResponse
 * Function: TODO ADD FUNCTION.
 * date: 2015年12月25日 上午10:17:22
 * 
 * HTTP请求响应
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2015, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class HttpResponse implements Serializable 
{
	//响应代码
	private int code;
	//响应消息
	private String msg;
	
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
}
