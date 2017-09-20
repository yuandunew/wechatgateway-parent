/**
 * 
 */
package com.yuandu.wechatgateway.service.dto;

import java.io.Serializable;

/** 
 * ClassName: TokenDto
 * Function: TODO ADD FUNCTION.
 * date: 2015年12月23日 下午8:47:59
 * 
 * 微信公众号访问令牌信息 
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2015, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class AccessTokenDto implements Serializable
{
	// 访问令牌
		private String accessToken;

		// 令牌有效时间，单位：秒
		private Long expiresIn;

		// 获取令牌时间
		private Long timestamp;
		
		public String getAccessToken() {
			return accessToken;
		}

		public void setAccessToken(String accessToken) {
			this.accessToken = accessToken;
		}

		public Long getExpiresIn() {
			return expiresIn;
		}

		public void setExpiresIn(Long expiresIn) {
			this.expiresIn = expiresIn;
		}

		public Long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Long timestamp) {
			this.timestamp = timestamp;
		}
}
