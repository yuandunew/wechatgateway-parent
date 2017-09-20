/**
 * 
 */
package com.yuandu.wechatgateway.service.dto.response;

import java.io.Serializable;

/** 
 * ClassName: SceneStr
 * Function: TODO ADD FUNCTION.
 * date: 2016年2月23日 下午11:17:46
 * 场景值ID 字符串（用于永久二维码）
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2016, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class SceneStr implements Serializable 
{
	private String scene_str;

	public String getScene_str() {
		return scene_str;
	}

	public void setScene_str(String scene_str) {
		this.scene_str = scene_str;
	}
}
