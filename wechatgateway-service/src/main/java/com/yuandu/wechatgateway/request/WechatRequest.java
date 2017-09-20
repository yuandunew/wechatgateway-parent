package com.yuandu.wechatgateway.request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lifesense.base.constant.AppType;
import com.lifesense.base.dto.wechatgateway.WechatReceiveMessage;
import com.lifesense.soa.wechatgateway.dto.enums.WechatMessageTypeEnum;
import com.lifesense.soa.wechatgateway.dto.receive.WechatReceiveAllMessage;
import com.lifesense.soa.wechatgateway.handler.WechatMessageHandler;

/**
 * 
 * 微信请求对象<br/>
 * Created by lizhifeng on 2017年6月27日.
 *
 */
public class WechatRequest {

	private HttpServletRequest httpServletRequest;//异步消息不要使用此对象
	private HttpServletResponse httpServletResponse;//异步消息不要使用此对象
	private WechatReceiveMessage wechatReceiveMessage;
	private WechatReceiveAllMessage allMessage;
	private WechatMessageHandler wechatMessageHandler;
	private WechatMessageTypeEnum wechatMessageTypeEnum;
	public WechatMessageHandler getWechatMessageHandler() {
		return wechatMessageHandler;
	}

	public void setWechatMessageHandler(WechatMessageHandler wechatMessageHandler) {
		this.wechatMessageHandler = wechatMessageHandler;
	}

	private String xmlMessage;// 微信请求的xml报文
	private String requestId;
	private AppType appType;

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public HttpServletResponse getHttpServletResponse() {
		return httpServletResponse;
	}

	public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
		this.httpServletResponse = httpServletResponse;
	}

	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}

	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}

	public String getXmlMessage() {
		return xmlMessage;
	}

	public void setXmlMessage(String xmlMessage) {
		this.xmlMessage = xmlMessage;
	}

	public AppType getAppType() {
		return appType;
	}

	public void setAppType(AppType appType) {
		this.appType = appType;
	}

	public WechatReceiveMessage getWechatReceiveMessage() {
		return wechatReceiveMessage;
	}

	public void setWechatReceiveMessage(WechatReceiveMessage wechatReceiveMessage) {
		this.wechatReceiveMessage = wechatReceiveMessage;
	}

	public WechatReceiveAllMessage getAllMessage() {
		return allMessage;
	}

	public void setAllMessage(WechatReceiveAllMessage allMessage) {
		this.allMessage = allMessage;
	}

	public WechatMessageTypeEnum getWechatMessageTypeEnum() {
		return wechatMessageTypeEnum;
	}

	public void setWechatMessageTypeEnum(WechatMessageTypeEnum wechatMessageTypeEnum) {
		this.wechatMessageTypeEnum = wechatMessageTypeEnum;
	}

}
