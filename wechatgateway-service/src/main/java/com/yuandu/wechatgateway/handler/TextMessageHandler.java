package com.yuandu.wechatgateway.handler;

import org.springframework.stereotype.Component;

import com.lifesense.base.beans.param.RequestHeader;
import com.lifesense.base.cache.command.RedisNumber;
import com.lifesense.base.cache.command.RedisString;
import com.lifesense.base.constant.AppType;
import com.lifesense.base.dto.wechatgateway.WechatReceiveMessage;
import com.lifesense.base.dto.wechatgateway.WechatReceiveTextMessage;
import com.lifesense.base.utils.ResourceUtils;
import com.lifesense.base.utils.json.JsonUtils;
import com.lifesense.soa.wechatgateway.context.WechatMessageContext;
import com.lifesense.soa.wechatgateway.dto.send.Text;
import com.lifesense.soa.wechatgateway.dto.send.WechatCustomServiceTextMessage;
import com.lifesense.soa.wechatgateway.request.WechatRequest;
import com.lifesense.soa.wechatgateway.response.SimpleResponse;
import com.lifesense.soa.wechatgateway.response.WechatResponse;
import com.lifesense.soa.wechatgateway.utils.Constants;

/**
 * 
 * 文本消息处理器<br/>
 * Created by lizhifeng on 2017年6月27日.
 *
 */
@Component
public class TextMessageHandler extends WechatMessageHandler {

	@Override
	public boolean canHandle(WechatRequest request) {
		return true;
	}

	@Override
	public WechatResponse doHandle(WechatRequest request) {
		WechatReceiveMessage message = request.getWechatReceiveMessage();
		AppType appType = request.getAppType();
		reportMsgEvent(message.getFromUserName(), request.getAppType().code());
		WechatReceiveMessage wechatReceiveMessage = request.getWechatReceiveMessage();
		if (appType.equals(AppType.乐心健康WECHAT)) {
			wechatGatewayForwardService.forwardWechatMessageToCustomer( wechatReceiveMessage);
		}
		// 处理运营活动
		dealActivity( wechatReceiveMessage, request.getAppType());
		return new SimpleResponse("");
	}

	/**
	 * 
	 * @param requestHeader
	 * @param wechatReceiveMessage
	 */
	private void dealActivity( WechatReceiveMessage wechatReceiveMessage, AppType appType) {
		// 运销活动是否关闭
		String isOpenMarking = ResourceUtils.get("is_open_marking", "true");
		// 两次操作时间健康不能小于10秒
		String stayTime = ResourceUtils.get("is_marking_stay_time", "10");
		WechatReceiveTextMessage message = (WechatReceiveTextMessage) wechatReceiveMessage;
		// 检索是否是运动或者健康的运营关键字
		if (((Constants.getHealthActiveKeyWords().contains(message.getContent()) && appType.equals(AppType.乐心健康WECHAT))
				|| ("我自律我最美".equals(message.getContent()) && appType.equals(AppType.乐心运动WECHAT))) && "true".equals(isOpenMarking)) {
			// 统计各个公众号各个关键字的发送次数
			RedisNumber activeCount = new RedisNumber("marking:jiankang:" + appType.toString() + ":message.getContent()");
			activeCount.increase(1);

			// 缓存用户是否在短时间内发送过关键字
			RedisString markingLimitRedisKey = new RedisString("marking:openid:" + wechatReceiveMessage.getFromUserName());
			if (markingLimitRedisKey.exists()) {
				logger.info("processWeChatRequest_activity_txt_wecaht_limit_jiankan_" + wechatReceiveMessage.getFromUserName() + " json: "
						+ JsonUtils.toJson(message));
				sendMarkingMessage( wechatReceiveMessage.getFromUserName(), appType, "亲爱哒，您发送得太频繁了，歇会儿再来，大奖还在滴！么么哒~");
				return;
			}

			markingLimitRedisKey.set("1", Long.valueOf(stayTime));
			logger.info("processWeChatRequest_activity_txt_wecaht_jiankan_" + wechatReceiveMessage.getFromUserName() + " json: "
					+ JsonUtils.toJson(message));
			// 发送kafka给运营活动服务器
			wechatGatewayForwardService.publishWechatTxtEventForActivity(appType, (WechatReceiveTextMessage) wechatReceiveMessage);
		}
	}

	protected void sendMarkingMessage(String openid, AppType appType, String content) {
		WechatCustomServiceTextMessage textMessage = new WechatCustomServiceTextMessage();
		textMessage.setTouser(openid);
		textMessage.setMsgtype("text");

		Text text = new Text();
		text.setContent(content);
		textMessage.setText(text);
		wechatGatewayProvider.sendCustomServiceMessage(appType, textMessage);

	}

	@Override
	public boolean isAsync(WechatRequest request) {

		return true;
	}

}
