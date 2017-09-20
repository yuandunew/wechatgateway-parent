/**
 * 
 */
package com.yuandu.wechatgateway.service.constants;

/** 
 * ClassName: WechatGratewayRedisKey
 * Function: TODO ADD FUNCTION.
 * date: 2016年1月5日 下午4:59:05
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2016, lifesense.com All Rights Reserved.
 */
public class WechatGratewayKey
{
	

	public static final String ACCESS_TOKEN = "access_token"; //ACCESS_TOKEN 键
	
	public static final String ACCESS_TOKEN_DATA = "data"; //ACCESS_TOKEN 键
		
	public static final String EXPIRES_IN = "expires_in";//失效时间 键
			
	public static final String TICKET = "ticket";//TICKET键
			
	public static final String REDIS_TICKET="_jsApiTicket";//Redis TICKET键
			
	public static final String REDIS_ACCESS_TOKEN="_accessToken";//Redis ACCESS_TOKEN 键
	
	public static final String REDIS_NOT_SEND_MESSAGE_QUEUE="NotSendMessageQueue";//Redis 未发送成功消息 键
	
	/**微信公众号基本信息，参数：根据枚举代码对应不同公众号*/
	//public static final String WECHAT_SERVICENO_INFO="wechatServiceNoInfo_%s";
	

	
	public static final String CHECKTOKEN_TIMES="checkToken:Times:%s";//标记检查接口请求，防止过频访问，10秒钟访问一次
	
	
	
	
}
