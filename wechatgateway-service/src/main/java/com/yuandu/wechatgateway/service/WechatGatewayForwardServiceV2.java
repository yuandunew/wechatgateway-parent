/**
 * 
 */
package com.yuandu.wechatgateway.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.lifesense.base.beans.param.RequestHeader;
import com.lifesense.base.cache.command.RedisString;
import com.lifesense.base.constant.AppType;
import com.lifesense.base.constant.KafkaTopic;
import com.lifesense.base.dto.device.WehatDeviceIdBindDto;
import com.lifesense.base.dto.health.QrcodeRedisKey;
import com.lifesense.base.dto.user.User;
import com.lifesense.base.dto.wechatgateway.WechatActivityDto;
import com.lifesense.base.dto.wechatgateway.WechatHealthQrcodeKafkaDto;
import com.lifesense.base.dto.wechatgateway.WechatJoinGroupKafkaDto;
import com.lifesense.base.dto.wechatgateway.WechatReceiveImageMessage;
import com.lifesense.base.dto.wechatgateway.WechatReceiveMessage;
import com.lifesense.base.dto.wechatgateway.WechatReceiveTextMessage;
import com.lifesense.base.dto.wechatgateway.WechatReceiveVoiceMessage;
import com.lifesense.base.utils.ResourceUtils;
import com.lifesense.base.utils.json.JsonUtils;
import com.lifesense.kafka.message.ObjectMessage;
import com.lifesense.kafka.spring.TopicPublisherBean;
import com.lifesense.soa.device.api.IDeviceUserProvider;
import com.lifesense.soa.user.api.IUserProvider;
import com.lifesense.soa.wechatgateway.api.IWechatGatewayForwardProviderV2;
import com.lifesense.soa.wechatgateway.dto.enums.BusinessTempQrcodeType;
import com.lifesense.soa.wechatgateway.dto.enums.WechatEventTypeEnum;
import com.lifesense.soa.wechatgateway.dto.enums.WechatMessageTypeEnum;
import com.lifesense.soa.wechatgateway.dto.receive.WeChatIotDeviceInfo;
import com.lifesense.soa.wechatgateway.dto.receive.WeChatIotDeviceResponse;
import com.lifesense.soa.wechatgateway.dto.receive.WechatReceiveAllMessage;
import com.lifesense.soa.wechatgateway.dto.receive.WechatReceiveDeviceStatusEventMessage;
import com.lifesense.soa.wechatgateway.dto.send.Text;
import com.lifesense.soa.wechatgateway.dto.send.WechatCustomServiceTextMessage;
import com.lifesense.soa.wechatgateway.dto.send.WechatEventMessage;
import com.lifesense.soa.wechatgateway.service.utils.DataUtil;
import com.lifesense.soa.wechatgateway.service.utils.MPManager;
import com.lifesense.soa.wechatgateway.utils.WechatMessageReader;

/**
 * date: 2016年1月12日 下午5:12:49 收到微信消息后转发服务类
 * 
 * @version
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang Copyright (c)
 *         2016, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("deprecation")
@Service("wechatGatewayForwardServiceV2")
public class WechatGatewayForwardServiceV2 implements IWechatGatewayForwardProviderV2 {
	private static final Logger logger = LoggerFactory.getLogger(WechatGatewayForwardServiceV2.class);

	@Autowired(required = false)
	@Qualifier("wechatGatewayTopicPublisher") // 多个不同实例时，指定id
	private TopicPublisherBean topicPublisher;

	private String voiceRemoveRepeatKey = "wechat:voice:%s";

	@Autowired
	private WechatGatewayServiceV2 wechatGatewayServiceV2;

	@Autowired
	private IDeviceUserProvider deviceUserService;

	@Autowired
	private IUserProvider userProvider;
	
	@Autowired
	WechatGatewayAdapter wechatGatewayAdapter;

	/**
	 * 转发微信消息到多客服系统（转发到7鱼多客服）
	 * 
	 * @param forwardUrl 接收转发消息的URL
	 * @param wechatMessage 微信消息
	 */
	@Override
	public void forwardWechatMessageToCustomer(WechatReceiveMessage wechatMessage) {
		// 获取下消息类型枚举
		WechatMessageTypeEnum wechatMessageTypeEnum = WechatMessageTypeEnum.getWechatMessageTypeEnumByValue(wechatMessage.getMsgType());
		if (wechatMessageTypeEnum == null) {
			return;
		}

		switch (wechatMessageTypeEnum) {
		case 文本消息:
			// 强制类型转换为微信文本消息对象
			WechatReceiveTextMessage textMessage = (WechatReceiveTextMessage) wechatMessage;
			// 发送健康文本客服消息
			topicPublisher.publish(KafkaTopic.健康文本客服消息.code(), new ObjectMessage(textMessage));
			break;
		case 图片消息:
			// 强制类型转换为微信图片消息对象
			WechatReceiveImageMessage imageMessage = (WechatReceiveImageMessage) wechatMessage;
			// 发送健康文本客服消息
			topicPublisher.publish(KafkaTopic.健康图片客服消息.code(), new ObjectMessage(imageMessage));
			break;
		default:
			break;
		}

	}

	
	@Override
	public void publishWechatEventMessage(RequestHeader requestHeader, WechatReceiveAllMessage receiveMessage, Integer step, User user) {
		WechatMessageTypeEnum wechatMessageTypeEnum = WechatMessageTypeEnum.getWechatMessageTypeEnumByValue(receiveMessage.getMsgType());
		if (wechatMessageTypeEnum == null || !wechatMessageTypeEnum.equals(WechatMessageTypeEnum.事件消息)) {
			return;
		}
		WechatEventTypeEnum eventTypeEnum = WechatEventTypeEnum.getWechatEventTypeEnumByValue(receiveMessage.getEvent());
		AppType appType = this.getAppTypeByWechatServiceNo(receiveMessage.getToUserName());
		if (AppType.乐心健康WECHAT.equals(appType)) {
			publishWechatHealthEventMessage(requestHeader, receiveMessage, step, user);
			return;
		}else {
			WechatEventMessage wechatEventMessage = new WechatEventMessage();
			wechatEventMessage.setAppType(appType);
			wechatEventMessage.setEventKey(receiveMessage.getEventKey());
			wechatEventMessage.setEventType(eventTypeEnum);
			wechatEventMessage.setStep(step);
			wechatEventMessage.setFromUserName(receiveMessage.getFromUserName());
			wechatEventMessage.setToUserName(receiveMessage.getToUserName());
			wechatGatewayAdapter.publicTopic(UUID.randomUUID().toString(), KafkaTopic.微信网关事件消息, wechatEventMessage);;
			if(WechatEventTypeEnum.订阅事件.equals(eventTypeEnum)) {
				// 推送扫描二维码后图文消息
				this.sendSubscribeEventNewsMessage(receiveMessage, requestHeader, appType);
			}
			
		}
	}
	
	/**
	 * 发送健康事件消息
	 * 
	 * @param requestHeader
	 * @param receiveMessage
	 * @param step
	 * @param user
	 */
	private void publishWechatHealthEventMessage(RequestHeader requestHeader, WechatReceiveAllMessage receiveMessage, Integer step, User user) {
		// 获取消息类型枚举
		WechatMessageTypeEnum wechatMessageTypeEnum = WechatMessageTypeEnum.getWechatMessageTypeEnumByValue(receiveMessage.getMsgType());
		if (wechatMessageTypeEnum == null || !wechatMessageTypeEnum.equals(WechatMessageTypeEnum.事件消息)) {
			return;
		}

		WechatEventTypeEnum eventTypeEnum = WechatEventTypeEnum.getWechatEventTypeEnumByValue(receiveMessage.getEvent());

		AppType appType = this.getAppTypeByWechatServiceNo(receiveMessage.getToUserName());

		if (eventTypeEnum == null) {
			return;
		}
		switch (eventTypeEnum) {
		case 带参二维码已经关注事件:
			if (AppType.乐心健康WECHAT.equals(appType)) {
				if (!StringUtils.isBlank(receiveMessage.getEventKey()) && receiveMessage.getEventKey().startsWith(QrcodeRedisKey.公共设备.code())) {
					User userInfo = userProvider.getUserByWechatOpenId(requestHeader, receiveMessage.getToUserName(),
							receiveMessage.getFromUserName());

					int i = receiveMessage.getEventKey().indexOf(QrcodeRedisKey.公共设备.code());
					if (i == 0) {
						deviceUserService.bindByWechatForPublicDevice(requestHeader, userInfo.getId(), receiveMessage.getEventKey().substring(22));
					}

				} else {
					this.publishWechatHealthQrcodeKafka(receiveMessage, receiveMessage.getEventKey(), appType,
							receiveMessage.getToUserName());
				}
			}
			break;

		// 扫码未关注
		case 订阅事件:
			// 判断是否为扫描带参二维码关注事件
			if (StringUtils.isNotBlank(receiveMessage.getEventKey())) {
				// 截取场景值ID 事件KEY值，qrscene_为前缀，后面为二维码的参数值
				String sceneId = receiveMessage.getEventKey().substring(8);

				if (appType.equals(AppType.乐心健康WECHAT)) {
					if (StringUtils.isNotBlank(receiveMessage.getEventKey())
							&& receiveMessage.getEventKey().startsWith("qrscene_" + QrcodeRedisKey.公共设备.code())) {
						User userInfo = userProvider.getUserByWechatOpenId(requestHeader,
								receiveMessage.getToUserName(), receiveMessage.getFromUserName());

						int i = receiveMessage.getEventKey().indexOf("qrscene_" + QrcodeRedisKey.公共设备.code());
						if (i == 0) {
							deviceUserService.bindByWechatForPublicDevice(requestHeader, userInfo.getId(),
									receiveMessage.getEventKey().substring(30));
						}
					} else {
						// 发送健康端扫描二维码kafka
						this.publishWechatHealthQrcodeKafka(receiveMessage, sceneId, appType,
								receiveMessage.getToUserName());
					}

				}
			}

			// 推送扫描二维码后图文消息
			this.sendSubscribeEventNewsMessage(receiveMessage, requestHeader, appType);
			break;

		default:
			break;
		}
	}
	
	
	/**
	 * 把接收到微信事件消息发布到kafka(旧代码供参考)
	 * 
	 * @param receiveMessage 接收到的微信消息
	 * @param topicPublisher 消息发布者对象
	 * @see publishWechatEventMessage
	 * @param step 运动步数
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private void publishWechatEventMessage2(RequestHeader requestHeader, WechatReceiveAllMessage receiveMessage, Integer step, User user) {
		// 获取消息类型枚举
		WechatMessageTypeEnum wechatMessageTypeEnum = WechatMessageTypeEnum.getWechatMessageTypeEnumByValue(receiveMessage.getMsgType());
		if (wechatMessageTypeEnum == null || !wechatMessageTypeEnum.equals(WechatMessageTypeEnum.事件消息)) {
			return;
		}

		WechatEventTypeEnum eventTypeEnum = WechatEventTypeEnum.getWechatEventTypeEnumByValue(receiveMessage.getEvent());

		AppType appType = this.getAppTypeByWechatServiceNo(receiveMessage.getToUserName());

		if (eventTypeEnum == null) {
			return;
		}
		switch (eventTypeEnum) {
		case 带参二维码已经关注事件:
			if (AppType.乐心健康WECHAT.equals(appType)) {
				if (!StringUtils.isBlank(receiveMessage.getEventKey()) && receiveMessage.getEventKey().startsWith(QrcodeRedisKey.公共设备.code())) {
					User userInfo = userProvider.getUserByWechatOpenId(requestHeader, receiveMessage.getToUserName(),
							receiveMessage.getFromUserName());

					int i = receiveMessage.getEventKey().indexOf(QrcodeRedisKey.公共设备.code());
					if (i == 0) {
						deviceUserService.bindByWechatForPublicDevice(requestHeader, userInfo.getId(), receiveMessage.getEventKey().substring(22));
					}

				} else {

					this.publishWechatHealthQrcodeKafka(receiveMessage, receiveMessage.getEventKey(), appType,
							receiveMessage.getToUserName());
				}
			}

			if (AppType.乐心运动WECHAT.equals(appType)) {
				long groupId = 0L;
				String eventKey = receiveMessage.getEventKey();
				if (eventKey != null) {
					int start = eventKey.lastIndexOf("_");
					if (start > 0) {
						eventKey = eventKey.substring(start + 1);
						groupId = Long.valueOf(eventKey);

					} else {
						groupId = Long.valueOf(receiveMessage.getEventKey());
					}
				}
				RedisString redisString = new RedisString("wechat:temp:qrcode:" + groupId);
				if (redisString.exists()) {
					String mapString = redisString.get();
					@SuppressWarnings("unchecked")
					Map<String, Object> valueMap = JsonUtils.toObject(mapString, Map.class);
					// 发送加入群组kafka
					if (BusinessTempQrcodeType.IRON_BIND == DataUtil.getBusinessTempQrcodeType(groupId)) {
						this.bindIronDevice(requestHeader, receiveMessage.getToUserName(), user.getWechatOpenId(),
								valueMap.get("SceneId").toString());
					} else {
						this.bindIronDevice(requestHeader, receiveMessage.getToUserName(), receiveMessage.getFromUserName(),
								valueMap.get("SceneId").toString());
					}
				} else {
					// 发送加入群组kafka
					this.publishJoinGroupKafkaByGroupId(receiveMessage, step, user, groupId);
				}
			}
			break;

		// 扫码未关注
		case 订阅事件:
			// 判断是否为扫描带参二维码关注事件
			if (StringUtils.isNotBlank(receiveMessage.getEventKey())) {
				// 截取场景值ID 事件KEY值，qrscene_为前缀，后面为二维码的参数值
				String sceneId = receiveMessage.getEventKey().substring(8);

				if (appType.equals(AppType.乐心健康WECHAT)) {
					if (StringUtils.isNotBlank(receiveMessage.getEventKey())
							&& receiveMessage.getEventKey().startsWith("qrscene_" + QrcodeRedisKey.公共设备.code())) {
						User userInfo = userProvider.getUserByWechatOpenId(requestHeader, receiveMessage.getToUserName(),
								receiveMessage.getFromUserName());

						int i = receiveMessage.getEventKey().indexOf("qrscene_" + QrcodeRedisKey.公共设备.code());
						if (i == 0) {
							deviceUserService.bindByWechatForPublicDevice(requestHeader, userInfo.getId(),
									receiveMessage.getEventKey().substring(30));
						}
					} else {
						// 发送健康端扫描二维码kafka
						this.publishWechatHealthQrcodeKafka(receiveMessage, sceneId, appType, receiveMessage.getToUserName());
					}

				}

				if (appType.equals(AppType.乐心运动WECHAT)) {
					// 发送加入群组kafka
					String eventKey = sceneId;
					long groupId2 = 0L;
					if (eventKey != null) {
						int start = eventKey.lastIndexOf("_");
						if (start > 0) {
							eventKey = eventKey.substring(start + 1);
							groupId2 = Long.valueOf(eventKey);
						} else {
							groupId2 = Long.valueOf(eventKey);
						}
					}

					RedisString redisString = new RedisString("wechat:temp:qrcode:" + groupId2);
					if (redisString.exists()) {
						String mapString = redisString.get();
						@SuppressWarnings("unchecked")
						Map<String, Object> valueMap = JsonUtils.toObject(mapString, Map.class);
						// 发送加入群组kafka
						if (BusinessTempQrcodeType.IRON_BIND == DataUtil.getBusinessTempQrcodeType(groupId2)) {
							this.bindIronDevice(requestHeader, receiveMessage.getToUserName(), user.getWechatOpenId(),
									valueMap.get("SceneId").toString());
						} else {
							this.bindIronDevice(requestHeader, receiveMessage.getToUserName(), receiveMessage.getFromUserName(),
									valueMap.get("SceneId").toString());
						}
					} else {
						this.publishJoinGroupKafkaByGroupId(receiveMessage, step, user, groupId2);
					}
				}

			}

			// 推送扫描二维码后图文消息
			this.sendSubscribeEventNewsMessage(receiveMessage, requestHeader, appType);

			break;

		default:
			break;
		}
	}
	/**
	 * 发送健康端扫描二维码事件
	 * */
	private void publishWechatHealthQrcodeKafka(WechatReceiveAllMessage receiveMessage,
			String sceneIdStr,AppType appType,String serviceNo){
		WechatHealthQrcodeKafkaDto qrcodeKafkaDto=new WechatHealthQrcodeKafkaDto();
		
		//判断场景值ID是否为数字，为数字时指临时二维码
		if(StringUtils.isNumeric(sceneIdStr)){
			qrcodeKafkaDto.setSceneId(Long.valueOf(sceneIdStr));
		}
		else {
			//为字符串时，用用于永久二维码
			qrcodeKafkaDto.setSceneIdStr(sceneIdStr);
		}
		
		qrcodeKafkaDto.setAppType(appType);
		qrcodeKafkaDto.setOpenId(receiveMessage.getFromUserName());
		
		qrcodeKafkaDto.setServiceNo(serviceNo);
		
		topicPublisher.publish(KafkaTopic.健康端扫描带参二维码消息.code(), new ObjectMessage(DataUtil.getRequestId(),qrcodeKafkaDto));
	}
	/**
	 * 把接收到微信语音消息发布到kafka
	 * 
	 * @param receiveMessage 接收到的微信消息
	 * @param topicPublisher 消息发布者对象
	 */
	@Override
	public void publishWechatVoiceMessage(WechatReceiveMessage wechatMessage) {
		// 获取下消息类型枚举
		WechatMessageTypeEnum wechatMessageTypeEnum = WechatMessageTypeEnum.getWechatMessageTypeEnumByValue(wechatMessage.getMsgType());
		if (wechatMessageTypeEnum == null) {
			return;
		}

		if (wechatMessageTypeEnum.equals(WechatMessageTypeEnum.语音消息)) {
			// 强制类型转换为微信语音消息对象
			WechatReceiveVoiceMessage voiceMessage = (WechatReceiveVoiceMessage) wechatMessage;
			String key = String.format(voiceRemoveRepeatKey, voiceMessage.getMediaId());
			RedisString voiceRedisString = new RedisString(key);
			if ("1".equals(voiceRedisString.get())) {
				logger.info("receivce_voice_repeat_" + DataUtil.getRequestId() + " " + JsonUtils.toJson(voiceMessage));
				return;
			} else {
				logger.info("receivce_voice_" + DataUtil.getRequestId() + " " + JsonUtils.toJson(voiceMessage));
				voiceRedisString.set("1", 20);
			}
			topicPublisher.publish(KafkaTopic.微信语音消息.code(), new ObjectMessage(DataUtil.getRequestId(), voiceMessage), false);
		}
	}

	/**
	 * 把接收到微信事件消息发布到kafka
	 * 
	 * @param joinGroupKafkaDto 接收到的加入群组微信事件消息
	 * @param topicPublisher 消息发布者对象
	 */
	private void publishjoinGroupKafka(WechatJoinGroupKafkaDto joinGroupKafkaDto, TopicPublisherBean topicPublisher) {
		if (joinGroupKafkaDto.getGroupId() > 1000000000) {
			joinGroupKafkaDto.setGroupId(joinGroupKafkaDto.getGroupId() - 1000000000);
			topicPublisher.publish(KafkaTopic.微信公众号加入企业群组消息.code(), new ObjectMessage(DataUtil.getRequestId(), joinGroupKafkaDto));
		} else {
			topicPublisher.publish(KafkaTopic.微信公众号加入个人群组消息.code(), new ObjectMessage(DataUtil.getRequestId(), joinGroupKafkaDto));
		}
	}

	/**
	 * Iron绑定
	 * 
	 * @param requestHeader
	 * @param userId
	 * @param deviceId
	 */
	private void bindIronDevice(RequestHeader requestHeader, String serviceNo, String openId, String sn) {
		logger.info("iron_bind_wechatgateway_start_deviceid_" + sn + "_openId_" + openId);
		// sn生成临时二维码时将前面的0去掉了，这里需要补足16位
		if(StringUtils.isNotEmpty(sn) && sn.length()<16) {
			sn = StringUtils.leftPad(sn, 16, "0");
		}
		deviceUserService.bindByWecahtForIron(requestHeader, serviceNo, openId, sn);
		logger.info("iron_bind_wechatgateway_end_deviceid_" + sn + "_userid_" + openId);
	}
	@Override
	public void publishDeviceStatusEvent(WechatReceiveDeviceStatusEventMessage receiveMessage) {
		try {
			AppType appType = getAppTypeByWechatServiceNo(receiveMessage.getFromUserName());
			String accessToken = wechatGatewayServiceV2.getAccessTokenForRedis(appType);
			WeChatIotDeviceResponse weChatIotDeviceResponse = wechatGatewayServiceV2.getBindDeviceList(accessToken,
					receiveMessage.getToUserName());
			WehatDeviceIdBindDto dto = new WehatDeviceIdBindDto();
			dto.setAppType(appType);

			List<String> deviceList = new ArrayList<String>();

			List<WeChatIotDeviceInfo> dList = weChatIotDeviceResponse.getDevice_list();
			for (WeChatIotDeviceInfo info : dList) {
				deviceList.add(info.getDevice_id());
			}
			dto.setDeviceId(deviceList);

			publishWechatDeviceEventMessage(dto, topicPublisher);
		} catch (Exception ex) {
			logger.error("subscribe_status_json_error_request" + DataUtil.getRequestId(), ex);
		}
	}

	private void publishWechatDeviceEventMessage( WehatDeviceIdBindDto wehatDeviceIdBindDto,
			TopicPublisherBean topicPublisher) {
		topicPublisher.publish(KafkaTopic.微信正在绑定的设备ID事件.code(), new ObjectMessage(DataUtil.getRequestId(), wehatDeviceIdBindDto));
	}
	@Override
	public void publishWechatTxtEventForActivity( AppType appType, WechatReceiveTextMessage wechatReceiveTextMessage) {
		WechatActivityDto activityDto = new WechatActivityDto();
		activityDto.setOpenId(wechatReceiveTextMessage.getFromUserName());
		activityDto.setAppType(appType);
		activityDto.setContent(wechatReceiveTextMessage.getContent());
		topicPublisher.publish(KafkaTopic.公众号运营活动.code(), new ObjectMessage(DataUtil.getRequestId(), activityDto));
	}

	/***
	 * 根据公众号原始ID获取应用类型
	 */
	@Override
	public AppType getAppTypeByWechatServiceNo(String wechatServiceNo) {
		AppType appType = MPManager.getAppTypeByWechatServiceNo(wechatServiceNo);
		if (appType == AppType.乐心运动APP) {
			appType = AppType.乐心运动WECHAT;
		}

		if (appType == AppType.乐心健康医生APP端) {
			appType = AppType.乐心健康WECHAT;
		}

		if (appType == AppType.乐心健康医生PC端) {
			appType = AppType.乐心健康WECHAT;
		}

		if (appType == AppType.乐心健康APP) {
			appType = AppType.乐心健康WECHAT;
		}

		return appType;
	}

	/**
	 * 发布加入群组
	 */
	private void publishJoinGroupKafkaByGroupId(WechatReceiveAllMessage receiveMessage, Integer step, User user, long groupId) {
		WechatJoinGroupKafkaDto joinGroupKafkaDto = new WechatJoinGroupKafkaDto();
		joinGroupKafkaDto.setGroupId(groupId);
		joinGroupKafkaDto.setOpenId(receiveMessage.getFromUserName());
		joinGroupKafkaDto.setServiceNo(receiveMessage.getToUserName());
		if (user != null) {
			joinGroupKafkaDto.setUserId(user.getId());
			joinGroupKafkaDto.setUserHeadImg(user.getHeadImg());
			joinGroupKafkaDto.setUserName(user.getName());
		}
		joinGroupKafkaDto.setStep(step);

		// 发布加入群组
		this.publishjoinGroupKafka(joinGroupKafkaDto, topicPublisher);
	}

	/**
	 * 发送扫描带参二维码后图文消息
	 */
	private void sendSubscribeEventNewsMessage(WechatReceiveAllMessage receiveMessage, RequestHeader requestHeader, AppType appType) {
		if (logger.isDebugEnabled()) {
			logger.debug("publish_subscribe_event_requestId_welcome " + "openId:" + receiveMessage.getFromUserName() + " requesetID:"
					+ requestHeader.getRequestId());
		}

		if (appType.equals(AppType.乐心运动WECHAT)) {
			WechatCustomServiceTextMessage textMessage = new WechatCustomServiceTextMessage();
			textMessage.setTouser(receiveMessage.getFromUserName());
			textMessage.setMsgtype("text");

			Text text = new Text();
			StringBuffer sbTxt = new StringBuffer();

			String pushTxt = ResourceUtils.get("wechat_sport_auto_push", "");

			if (logger.isDebugEnabled()) {
				logger.debug("push_welcome_" + pushTxt);
			}

			if (StringUtils.isEmpty(pushTxt)) {
				sbTxt.append(WechatMessageReader.readYunDong());
			} else {
				sbTxt.append(pushTxt);
			}
			text.setContent(sbTxt.toString());
			textMessage.setText(text);

			boolean isOk = wechatGatewayServiceV2.sendCustomServiceMessage(this.getAppTypeByWechatServiceNo(receiveMessage.getToUserName()),
					textMessage);
			if(!isOk) {
				logger.warn("sendSubscribeEventNewsMessage send message failure");
			}
		}

		if (appType.equals(AppType.乐心健康WECHAT)) {
			WechatCustomServiceTextMessage textMessage = new WechatCustomServiceTextMessage();
			textMessage.setTouser(receiveMessage.getFromUserName());
			textMessage.setMsgtype("text");

			Text text = new Text();
			StringBuffer sbTxt = new StringBuffer();
			String pushTxt = ResourceUtils.get("wechat_jiankang_auto_push", "");

			if (StringUtils.isEmpty(pushTxt)) {
				sbTxt.append(WechatMessageReader.readJIANKANG());
			} else {
				sbTxt.append(pushTxt);
			}
			text.setContent(sbTxt.toString());
			textMessage.setText(text);

			boolean isOk = wechatGatewayServiceV2.sendCustomServiceMessage(this.getAppTypeByWechatServiceNo(receiveMessage.getToUserName()),
					textMessage);
			if(!isOk) {
				logger.warn("sendSubscribeEventNewsMessage send message failure");
			}
		}
	}
	

	// 健康检查
	@Override
	public Map<String, Object> echo(int... modes) {
		long start = System.currentTimeMillis();
		int mode = modes != null && modes.length > 0 ? modes[0] : 0;
		Map<String, Object> result = new TreeMap<>();
		if (mode == 2) {
			result.put("time", System.currentTimeMillis() - start);
			return result;
		}
		try {
			new RedisString("echo").get();
			result.put("redis", "ok");
		} catch (Exception e) {
			result.put("redis", e.getMessage());
		}

		result.put("time", System.currentTimeMillis() - start);
		return result;
	}
}
