/**
 * 
 */
package com.yuandu.wechatgateway.service.dto.response;

import java.io.Serializable;


/** 
 * ClassName: QrSceneParam
 * Function: TODO ADD FUNCTION.
 * date: 2016年2月23日 下午11:26:39
 * 创建临时带参二维码参数
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2016, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class QrSceneParam implements Serializable 
{
	private String action_name; //二维码类型
	private QrScenActionInfo action_info;//二维码场景信息
	private Long expire_seconds;//失效时间
	
	public String getAction_name() {
		return action_name;
	}
	public void setAction_name(String action_name) {
		this.action_name = action_name;
	}
	public QrScenActionInfo getAction_info() {
		return action_info;
	}
	public void setAction_info(QrScenActionInfo action_info) {
		this.action_info = action_info;
	}
	public Long getExpire_seconds() {
		return expire_seconds;
	}
	public void setExpire_seconds(Long expire_seconds) {
		this.expire_seconds = expire_seconds;
	}
}
