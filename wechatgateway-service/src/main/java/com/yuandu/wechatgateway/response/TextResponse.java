package com.yuandu.wechatgateway.response;

public class TextResponse implements WechatResponse {
	/**
	 * 回复的消息内容（换行：在content中能够换行，微信客户端就支持换行显示）
	 */
	private String content;

	public TextResponse(String content) {
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
