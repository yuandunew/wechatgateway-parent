package com.yuandu.wechatgateway.utils;

import org.apache.commons.lang3.StringUtils;

import com.lifesense.base.cache.command.RedisString;

public class Constants {
	/**
	 * 定时器从redis中获取缓存值
	 */
	static{
		ThreadPoolRankUtils.excuteScheduledThreadPool(()->{
			Constants.setSprotsActiveKeyWordsFromRedis();
			Constants.setHealthActiveKeyWordsFromRedis();
		}, System.currentTimeMillis(), 2*60);
	}
	
	// 运动运营活动关键字
	private static String SPROTS_ACTIVE_KEY_WORDS;
	// 健康运营活动关键字
	private static String HEALTH_ACTIVE_KEY_WORDS;
	
	// 运动运营活动关键字缓存key
	private static final String SPROTS_ACTIVE_KEY = "sports:active_key_words";
	// 健康运营活动关键字缓存key
	private static final String HEALTH_ACTIVE_KEY = "health:active_key_words";
	public static final String WECHAT_RECEIVE_ASYNC_MESSAGE = "wechatgateway_wechat_receive_async_message";
	
	// 当语音消息上传到另一个公众号是，很有可能上传失败
	public static final String UPLOAD_FAIL = "[upload fail]";
	public static final String MEDIA_ID = "media_id";

	/**
	 * 从内存中获取运动获得的关键词
	 * @return
	 */
	public static String getSprotsActiveKeyWords(){
		if(SPROTS_ACTIVE_KEY_WORDS == null){
			setSprotsActiveKeyWordsFromRedis();
		}
		return SPROTS_ACTIVE_KEY_WORDS;
	}
	/**
	 * 从内存中获取健康活动的关键词
	 * @return
	 */
	public static String getHealthActiveKeyWords(){
		if(HEALTH_ACTIVE_KEY_WORDS == null){
			setHealthActiveKeyWordsFromRedis();
		}
		return HEALTH_ACTIVE_KEY_WORDS;
	}
	
//================================================================
	
	/**
	 * 从缓存中获取运动获得的关键词
	 * @return
	 */
	private static void setSprotsActiveKeyWordsFromRedis(){
		RedisString sprotsActiveKeyWords = new RedisString(SPROTS_ACTIVE_KEY);
		if(sprotsActiveKeyWords.exists()){
			String result = sprotsActiveKeyWords.get();
			SPROTS_ACTIVE_KEY_WORDS = StringUtils.isEmpty(result)?new String(""):result;
		}else{
			SPROTS_ACTIVE_KEY_WORDS = new String("");
			sprotsActiveKeyWords.set(SPROTS_ACTIVE_KEY_WORDS);
		}
	}
	
	/**
	 * 从缓存中获取健康活动的关键词
	 * @return
	 */
	private static void setHealthActiveKeyWordsFromRedis(){
		RedisString healthActiveKeyWords = new RedisString(HEALTH_ACTIVE_KEY);
		if(healthActiveKeyWords.exists()){
			String result = healthActiveKeyWords.get();
			HEALTH_ACTIVE_KEY_WORDS = StringUtils.isEmpty(result)?new String(""):result;
		}else{
			HEALTH_ACTIVE_KEY_WORDS = new String("");
			healthActiveKeyWords.set(HEALTH_ACTIVE_KEY_WORDS);
		}
	}
}
