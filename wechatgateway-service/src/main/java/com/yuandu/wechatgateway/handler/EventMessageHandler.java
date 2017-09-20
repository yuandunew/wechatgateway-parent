package com.yuandu.wechatgateway.handler;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.lifesense.base.beans.param.RequestHeader;
import com.lifesense.base.dto.sport.PedometerRecordDayDto;
import com.lifesense.base.dto.user.User;
import com.lifesense.base.dto.wechatgateway.WechatReceiveMessage;
import com.lifesense.soa.wechatgateway.context.WechatMessageContext;
import com.lifesense.soa.wechatgateway.dto.enums.WechatEventTypeEnum;
import com.lifesense.soa.wechatgateway.dto.enums.WechatMessageTypeEnum;
import com.lifesense.soa.wechatgateway.dto.receive.WechatReceiveAllMessage;
import com.lifesense.soa.wechatgateway.request.WechatRequest;
import com.lifesense.soa.wechatgateway.response.SimpleResponse;
import com.lifesense.soa.wechatgateway.response.WechatResponse;

/**
 * 
 * 事件消息处理器<br/>
 * Created by lizhifeng on 2017年6月27日.
 *
 */
@Component
public class EventMessageHandler extends WechatMessageHandler {

	@Override
	public boolean canHandle(WechatRequest request) {
		return true;
	}

	@Override
	public WechatResponse doHandle(WechatRequest request) {
		WechatReceiveMessage message = request.getWechatReceiveMessage();
		WechatReceiveAllMessage allMessage = request.getAllMessage();
		WechatMessageTypeEnum wechatMessageTypeEnum = request.getWechatMessageTypeEnum();
		RequestHeader requestHeader = WechatMessageContext.translateRequestHeader(request);
		if (wechatMessageTypeEnum == WechatMessageTypeEnum.事件消息 && !StringUtils.isBlank(allMessage.getEventKey())
				&& allMessage.getEventKey().equals(WechatEventTypeEnum.拉取微信排行榜事件.toString())) {
			reportEvent("rankinglist_click", message.getFromUserName(),
					wechatGatewayForwardService.getAppTypeByWechatServiceNo(message.getToUserName()).code());
			return new SimpleResponse(menuRank(allMessage));
		}

		Integer step = 0;
		User user = null;
		WechatEventTypeEnum eventTypeEnum = WechatEventTypeEnum.getWechatEventTypeEnumByValue(allMessage.getEvent());
		if (WechatEventTypeEnum.模版消息送达事件.equals(eventTypeEnum)) {
			if (!"success".equals(allMessage.getStatus())) {
				logger.warn("send_template_job_fail :" + "fromUser:" + message.getFromUserName() + " toUser:" + message.getToUserName() + " msgId:"
						+ allMessage.getMsgID() + " status:" + allMessage.getStatus() + " requestId:" + requestHeader.getRequestId());
			}
		}
		// 判断事件为扫描带参二维码事件时，做如下处理
		// 关注公众号（包括无参二维码，原来只处理带参二维码eventKey!=null）都创建用户。
		if ((eventTypeEnum != null && eventTypeEnum.equals(WechatEventTypeEnum.带参二维码已经关注事件))
				|| (eventTypeEnum != null && eventTypeEnum.equals(WechatEventTypeEnum.订阅事件))) {
			// 根据serviceNo和openId去用户服务获取用户，若用户不存在，用户服务负责创建用户
			user = userService.getUserByWechatOpenId2(requestHeader, message.getToUserName(), message.getFromUserName()); // ----临时方案
			if (user != null) {
				requestHeader.setUserId(user.getId());
				PedometerRecordDayDto pedoDay = sportService.getLatestPedometerRecord(requestHeader, user.getId());
				if (pedoDay != null) {
					step = pedoDay.getStep();
				}
			}
		}

		// 非客服消息，将消息发布到Kafka,让各业务系统去订阅
		wechatGatewayForwardService.publishWechatEventMessage(requestHeader, allMessage, step, user);

		return new SimpleResponse("");
	}

	/**
	 * 用户排名
	 * 
	 * @param message
	 * @return
	 */
	private String menuRank(WechatReceiveAllMessage message) {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("<xml><ToUserName><![CDATA[%s]]></ToUserName><FromUserName><![CDATA[%s]]></FromUserName><CreateTime>%s</CreateTime>",
				message.getFromUserName(), message.getToUserName(), new Date().getTime()))
				.append("<MsgType><![CDATA[hardware]]></MsgType><HardWare><MessageView><![CDATA[myrank]]></MessageView><MessageAction><![CDATA[ranklist]]></MessageAction></HardWare><FuncFlag>0</FuncFlag></xml>");
		return sb.toString();
	}

	@Override
	public boolean isAsync(WechatRequest request) {
		WechatReceiveAllMessage allMessage = request.getAllMessage();
		WechatMessageTypeEnum wechatMessageTypeEnum = WechatMessageTypeEnum.getWechatMessageTypeEnumByValue(request.getAllMessage().getMsgType());
		if (wechatMessageTypeEnum == WechatMessageTypeEnum.事件消息 && !StringUtils.isBlank(allMessage.getEventKey())
				&& allMessage.getEventKey().equals(WechatEventTypeEnum.拉取微信排行榜事件.toString())) {
			return false;
		}
		return true;
	}

}
