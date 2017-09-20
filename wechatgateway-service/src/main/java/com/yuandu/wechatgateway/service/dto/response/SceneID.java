/**
 * 
 */
package com.yuandu.wechatgateway.service.dto.response;

import java.io.Serializable;

/** 
 * ClassName: SceneID
 * Function: TODO ADD FUNCTION.
 * date: 2016年2月23日 下午11:15:45
 * 场景值ID（用于临时二维码）
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2016, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class SceneID implements Serializable 
{
	private long scene_id;

	public long getScene_id() {
		return scene_id;
	}

	public void setScene_id(long scene_id) {
		this.scene_id = scene_id;
	}
}
