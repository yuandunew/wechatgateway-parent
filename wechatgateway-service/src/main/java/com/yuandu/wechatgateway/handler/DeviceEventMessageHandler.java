package com.yuandu.wechatgateway.handler;

import org.springframework.stereotype.Component;

import com.lifesense.base.beans.param.RequestHeader;
import com.lifesense.soa.wechatgateway.context.WechatMessageContext;
import com.lifesense.soa.wechatgateway.dto.enums.WechatDeviceStatusEnum;
import com.lifesense.soa.wechatgateway.dto.enums.WechatEventTypeEnum;
import com.lifesense.soa.wechatgateway.dto.enums.WechatMessageTypeEnum;
import com.lifesense.soa.wechatgateway.dto.receive.WechatReceiveDeviceEventMessage;
import com.lifesense.soa.wechatgateway.dto.send.WechatDeviceStatusMessage;
import com.lifesense.soa.wechatgateway.request.WechatRequest;
import com.lifesense.soa.wechatgateway.response.SimpleResponse;
import com.lifesense.soa.wechatgateway.response.WechatResponse;

/**
 * 
 * 设备事件处理器<br/>
 * Created by lizhifeng on 2017年6月27日.
 *
 */
@Component
public class DeviceEventMessageHandler extends WechatMessageHandler {

	@Override
	public boolean canHandle(WechatRequest request) {
		return true;
	}

	@Override
	public WechatResponse doHandle(WechatRequest request) {
		RequestHeader requestHeader = WechatMessageContext.translateRequestHeader(request);
		// requestHeader.setAppType(appType);
		// 调用设备服务中的微信设备事件服务
		WechatReceiveDeviceEventMessage wechatReceiveDeviceEventMessage = (WechatReceiveDeviceEventMessage) request.getWechatReceiveMessage();

		WechatEventTypeEnum wechatEventTypeEnum = WechatEventTypeEnum.getWechatEventTypeEnumByValue(wechatReceiveDeviceEventMessage.getEvent());
		if (WechatEventTypeEnum.订阅设备状态 == wechatEventTypeEnum) {
			WechatDeviceStatusMessage deviceStatusMessage = new WechatDeviceStatusMessage();
			deviceStatusMessage.setDevice_id(wechatReceiveDeviceEventMessage.getDeviceID());
			deviceStatusMessage.setDevice_status(WechatDeviceStatusEnum.未连接.toString());
			deviceStatusMessage.setDevice_type(wechatReceiveDeviceEventMessage.getDeviceType());
			deviceStatusMessage.setMsg_type(WechatMessageTypeEnum.发送设备状态消息.toString());
			deviceStatusMessage.setOpen_id(wechatReceiveDeviceEventMessage.getOpenID());
			wechatGatewayProvider.sendWechatDeviceStatusMessage(request.getAppType(), deviceStatusMessage);
		} else {
			deviceUserService.bindByWechat(requestHeader, wechatReceiveDeviceEventMessage);
		}
		return new SimpleResponse("");
	}

	@Override
	public boolean isAsync(WechatRequest request) {

		return true;
	}

}
