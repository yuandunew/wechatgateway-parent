/**
 * 
 */
package com.yuandu.wechatgateway.rest.dto;

import java.io.Serializable;

/** 
 * ClassName: BaseParams
 * Function: TODO ADD FUNCTION.
 * date: 2016年3月21日 下午9:45:59
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2016, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class BaseParams implements Serializable 
{
	private int appType;

	public int getAppType() {
		return appType;
	}

	public void setAppType(int appType) {
		this.appType = appType;
	}
}
