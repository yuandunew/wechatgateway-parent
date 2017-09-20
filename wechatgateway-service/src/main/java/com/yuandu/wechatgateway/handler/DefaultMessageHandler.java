package com.yuandu.wechatgateway.handler;

import org.springframework.stereotype.Component;

import com.yuandu.base.beans.param.RequestHeader;
import com.yuandu.base.constant.AppType;
import com.yuandu.base.dto.wechatgateway.WechatReceiveMessage;
import com.yuandu.soa.wechatgateway.context.WechatMessageContext;
import com.yuandu.soa.wechatgateway.dto.enums.WechatMessageTypeEnum;
import com.yuandu.soa.wechatgateway.dto.receive.WechatReceiveDeviceStatusEventMessage;
import com.yuandu.soa.wechatgateway.request.WechatRequest;
import com.yuandu.soa.wechatgateway.response.SimpleResponse;
import com.yuandu.soa.wechatgateway.response.WechatResponse;

/**
 * 
 * 对于只是简单将微信消息转发给其他服务处理的消息共享同一个消息处理器<br/>
 * Created by lizhifeng on 2017年6月27日.
 *
 */
@Component
public class DefaultMessageHandler extends WechatMessageHandler {

	@Override
	public boolean canHandle(WechatRequest request) {
		return true;
	}

	@Override
	public WechatResponse doHandle(WechatRequest request) {
		RequestHeader requestHeader = WechatMessageContext.translateRequestHeader(request);
		AppType appType = request.getAppType();
		String xmlMessage = request.getXmlMessage();
		WechatReceiveMessage message = request.getWechatReceiveMessage();
		WechatMessageTypeEnum wechatMessageTypeEnum = WechatMessageTypeEnum.getWechatMessageTypeEnumByValue(message.getMsgType());
		switch (wechatMessageTypeEnum) {
		case 语音消息:
			reportMsgEvent(message.getFromUserName(), appType.code());
			wechatGatewayForwardService.publishWechatVoiceMessage( message);
			break;
		case 图片消息:
			reportMsgEvent(message.getFromUserName(), appType.code());
			// 将消息转发到多客服系统
			if (appType.equals(AppType.乐心健康WECHAT)) {
				wechatGatewayForwardService.forwardWechatMessageToCustomer( message);
			}
			break;
		case 视频消息:
			reportMsgEvent(message.getFromUserName(), appType.code());
			break;
		case 设备文本消息:
            String responseXml=deviceGatewayWechatService.receiveDeviceData(requestHeader,xmlMessage);
            return new SimpleResponse(responseXml);
		case 设备状态消息:
			WechatReceiveDeviceStatusEventMessage receiveDeviceStatusEventMessage = (WechatReceiveDeviceStatusEventMessage) message;
			wechatGatewayForwardService.publishDeviceStatusEvent( receiveDeviceStatusEventMessage);
			break;
		default:
			break;
		}
		return new SimpleResponse("");
	}

	@Override
	public boolean isAsync(WechatRequest request) {
		switch (request.getWechatMessageTypeEnum()) {
		case 图片消息:
			return true;
		case 语音消息:
			return true;
		case 视频消息:
			return true;
		case 设备状态消息:
			return true;
		case 设备文本消息:
			return false;
		default:
			return false;//没有相关业务处理的默认都是同步，因为异步操作会占用线程资源
		}
	}

}
