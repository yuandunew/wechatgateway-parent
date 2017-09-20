/**
 * 
 */
package com.yuandu.wechatgateway.service.dto.response;

import java.io.Serializable;

import com.lifesense.soa.wechatgateway.dto.enums.ParamQrcodeActionTypeEnum;

/** 
 * ClassName: QrLimitSceneParam
 * Function: TODO ADD FUNCTION.
 * date: 2016年2月23日 下午11:34:03
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2016, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class QrLimitSceneParam implements Serializable 
{
	private String action_name=ParamQrcodeActionTypeEnum.永久二维码.toString(); //二维码类型
	private QrLimitSceneActonInfo action_info;//二维码场景信息
	
	public String getAction_name() {
		return action_name;
	}
	public void setAction_name(String action_name) {
		this.action_name = action_name;
	}
	public QrLimitSceneActonInfo getAction_info() {
		return action_info;
	}
	public void setAction_info(QrLimitSceneActonInfo action_info) {
		this.action_info = action_info;
	}
}
