package com.yuandu.wechatgateway.response;

import java.io.Serializable;

/**
 * 
 * 对于还没有业务处理的微信消息类型，使用此对象返回空报文即可<br/>
 * Created by lizhifeng on 2017年6月27日.
 *
 */
public class SimpleResponse implements WechatResponse, Serializable {
	private static final long serialVersionUID = 6132785108188821957L;
	/**
	 * 回复的消息内容（换行：在content中能够换行，微信客户端就支持换行显示）
	 */
	private String content;

	public SimpleResponse(String content) {
		this.content = content;
	}

	@Override
	public String getMsgType() {
		return null;
	}

	@Override
	public String toContent() {
		return content;
		
	}

	public String getContent() {
		return content;
	}
}
