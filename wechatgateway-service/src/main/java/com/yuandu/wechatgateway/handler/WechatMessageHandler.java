package com.yuandu.wechatgateway.handler;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import com.lifesense.base.constant.KafkaTopic;
import com.lifesense.base.constant.OpenAccountType;
import com.lifesense.base.constant.ServiceName;
import com.lifesense.base.dto.wechatgateway.WechatReceiveMessage;
import com.lifesense.base.exception.LifesenseBaseException;
import com.lifesense.base.exception.LifesenseResultCode;
import com.lifesense.base.utils.LsDigestUtils;
import com.lifesense.base.utils.SystemUtils;
import com.lifesense.kafka.message.BaseMessage;
import com.lifesense.kafka.message.ObjectMessage;
import com.lifesense.kafka.spring.TopicPublisherBean;
import com.lifesense.log.core.LogConstant;
import com.lifesense.soa.device.api.IDeviceUserProvider;
import com.lifesense.soa.device.gateway.api.IDeviceGatewayWechatProvider;
import com.lifesense.soa.service.api.IEventsReportProvider;
import com.lifesense.soa.service.dto.Events;
import com.lifesense.soa.sport.api.ISportProvider;
import com.lifesense.soa.user.api.IUserProvider;
import com.lifesense.soa.wechatgateway.api.IWechatGatewayForwardProvider;
import com.lifesense.soa.wechatgateway.api.IWechatGatewayForwardProviderV2;
import com.lifesense.soa.wechatgateway.api.IWechatGatewayProvider;
import com.lifesense.soa.wechatgateway.api.IWechatGatewayProviderV2;
import com.lifesense.soa.wechatgateway.context.WechatMessageContext;
import com.lifesense.soa.wechatgateway.msg.SimpleWechatMessage;
import com.lifesense.soa.wechatgateway.request.WechatRequest;
import com.lifesense.soa.wechatgateway.response.SimpleResponse;
import com.lifesense.soa.wechatgateway.response.WechatResponse;
import com.lifesense.soa.wechatgateway.service.WechatGatewayAdapter;
import com.lifesense.soa.wechatgateway.utils.Constants;

/**
 * 
 * 微信消息处理器 <br/>
 * Created by lizhifeng on 2017年6月27日.
 * 
 * @param <M> 具体的消息对象
 */
public abstract class WechatMessageHandler {
	protected static final Logger logger = LoggerFactory.getLogger(WechatMessageHandler.class);
	@Autowired
	protected IUserProvider userService;
	@Autowired
	protected IEventsReportProvider eventProvider;
	@Autowired
	protected IWechatGatewayForwardProviderV2 wechatGatewayForwardService;
	@Autowired
	protected WechatGatewayAdapter wechatGatewayAdapter;
	@Autowired
	protected IWechatGatewayProviderV2 wechatGatewayProvider;
	@Autowired
	protected WechatMessageContext wechatMessageContext;
	@Autowired
	protected IDeviceUserProvider deviceUserService;
	@Autowired
	protected IDeviceGatewayWechatProvider deviceGatewayWechatService;
	@Autowired
	protected ISportProvider sportService;
	@Autowired
	protected TaskExecutor taskExecutor;
	@Autowired
	protected TopicPublisherBean topicPublishBean;

	public boolean preHandle(WechatRequest request) {
		logger.info("receive wechat message:msgType is {}, xml is\n{}", request.getAllMessage().getMsgType(), request.getXmlMessage());
		return true;
	}

	/**
	 * 能否处理请求
	 *
	 * @param request 微信请求
	 * @param message 微信消息
	 * @return true则执行doHandle
	 */
	public abstract boolean canHandle(WechatRequest request);

	/**
	 * 是否异步处理，如果是，将马上返回空的报文并异步进行消息处理<br/>
	 * 微信服务器要求消息响应在2秒内响应，建议存在业务的消息异步进行处理<br/>
	 * 没有相关业务处理的默认都是同步，因为异步操作会占用线程资源
	 * 
	 * @param request
	 * @param message
	 * @return
	 */
	public abstract boolean isAsync(WechatRequest request);

	public WechatResponse handle(WechatRequest request) {
		Long startTime = System.currentTimeMillis();

		if (!preHandle(request)) {
			return new SimpleResponse("");
		}

		if (!canHandle(request)) {
			return new SimpleResponse("");
		}
		WechatResponse wechatResponse = null;

		if (isAsync(request)) {
			wechatResponse = new SimpleResponse("");
			SimpleWechatMessage simpleWechatMessage = WechatMessageContext.translateSimleWechatMessage(request);
			wechatGatewayAdapter.publicTopic(request.getRequestId(), KafkaTopic.微信网关异步消息,  simpleWechatMessage);

		} else {
			try {
				wechatResponse = doHandle(request);
			} catch (Exception e) {
				wechatResponse = reportError(request, e);
			}
		}

		// 记录日志、执行时间跟进等
		afterHandle(request);
		long endTime = System.currentTimeMillis();
		if (endTime - startTime > 2000) {
			logger.warn("handle wechat message spent time more than 2000ms msgType-xml:{}-\n{} ", request.getWechatMessageTypeEnum().getMessageType(),
					request.getXmlMessage());
		}
		return wechatResponse;
	}

	/**
	 * 处理请求
	 *
	 * @param request 微信请求
	 * @param message 微信消息
	 * @param nodeNames 节点名称集合
	 * @return 回复内容
	 */
	public abstract WechatResponse doHandle(WechatRequest request);

	protected void afterHandle(WechatRequest request) {
		MDC.put(LogConstant.PROJECT, ServiceName.WechatGateway.name() + "-rest");
		MDC.put(LogConstant.REQUEST_ID, request.getRequestId());
		MDC.put(LogConstant.ENV, SystemUtils.getEnv());
	}

	/**
	 * 异常日志打印，对于微信消息接口统一不返回异常信息
	 * 
	 * @param request
	 * @param e
	 * @return
	 */
	protected WechatResponse reportError(WechatRequest request, Exception e) {
		WechatReceiveMessage message = request.getWechatReceiveMessage();
		logger.error("error happend when receive wechat message: msgType is :{}, xmlMessage is\n:{}, error is \n{}", message.getMsgType(), request.getXmlMessage(),e);
		return new SimpleResponse("");
	}

	protected void reportEvent(String eventId, String openId, Integer appType) {
		try {
			Events events = new Events();
			long timestamp = System.currentTimeMillis();
			String secret = "b0083e3837634635837f3c17a78eb11d";
			events.setChecksum(LsDigestUtils.md5(timestamp + secret));
			events.setTimestamp(timestamp);
			long userId = userService.getUserIdByOpenId(openId, OpenAccountType.WECHAT.code());
			events.setCommons("userId", String.valueOf(userId));
			events.setCommons("appType", appType.intValue());
			events.setCommons("systemType", 3);
			events.setCommons("platform", "wechat");
			com.lifesense.soa.service.dto.Event event = new com.lifesense.soa.service.dto.Event();
			event.setEventID(eventId);
			event.setTimestamp(timestamp);
			events.setEvents(Arrays.asList(event));
			eventProvider.report(events);
		} catch (Exception e) {
			// 开发环境此服务不能够调用，忽略此错误
			logger.info("reportEvent:exception:" + e.getMessage());
		}
	}

	protected void reportMsgEvent(String openId, Integer appType) {
		reportEvent("typecontent_click", openId, appType);
	}

}
