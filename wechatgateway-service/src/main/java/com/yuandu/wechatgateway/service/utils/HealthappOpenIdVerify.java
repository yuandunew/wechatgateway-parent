/**
 * 
 */
package com.yuandu.wechatgateway.service.utils;

import org.apache.commons.lang.StringUtils;

/** 
 * ClassName: HealthappOpenIdVerify
 * Function: TODO ADD FUNCTION.
 * date: 2015年12月24日 下午6:50:24
 * 
 * 乐心健康-针对OPENID的校验
 * 若有此前缀的OPENDID，都不调用微信服务器接口
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2015, lifesense.com All Rights Reserved.
 */
public class HealthappOpenIdVerify 
{
	//乐心健康APP生成账号时加上此前缀区分
	public static final String HEALTHAPP_OPENID_PREFIX = "healthapp_";
		
	/**
	 * 判断是否乐心健康生成的OPENID
	 * @param openId
	 * @return
	 */
	public static boolean isHealthApp(String openId)
	{
		if(StringUtils.isEmpty(openId))
			return false;
			
		return StringUtils.startsWith(openId, HEALTHAPP_OPENID_PREFIX);
	}
}
