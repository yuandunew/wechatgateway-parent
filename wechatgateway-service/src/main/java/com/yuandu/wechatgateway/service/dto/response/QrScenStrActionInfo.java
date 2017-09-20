/**
 * 
 */
package com.yuandu.wechatgateway.service.dto.response;

import java.io.Serializable;

/**
 * ClassName: QrScenActionInfo Function: TODO ADD FUNCTION. date: 2016年2月23日
 * 下午11:13:30
 * 
 * @version
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang Copyright (c)
 *         2016, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class QrScenStrActionInfo implements Serializable {
	private SceneStr scene;

	public SceneStr getScene() {
		return scene;
	}

	public void setScene(SceneStr scene) {
		this.scene = scene;
	}

}
