package com.yuandu.wechatgateway.response;

/**
 * 
 * 微信消息回复 <br/>
 * Created by lizhifeng on 2017年6月27日.
 *
 */
public interface WechatResponse {
	/**
	 * 回复的消息类型
	 * 
	 * @return
	 */
	public String getMsgType();

	/**
	 * 回复的消息内容
	 * 
	 * @return
	 */
	public String toContent();
}
