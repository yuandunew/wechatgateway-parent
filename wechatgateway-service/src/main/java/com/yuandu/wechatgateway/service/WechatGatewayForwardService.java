/**
 * 
 */
package com.yuandu.wechatgateway.service;



import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.lifesense.base.beans.param.RequestHeader;
import com.lifesense.base.constant.AppType;
import com.lifesense.base.dto.user.User;
import com.lifesense.base.dto.wechatgateway.WechatReceiveMessage;
import com.lifesense.base.dto.wechatgateway.WechatReceiveTextMessage;
import com.lifesense.kafka.spring.TopicPublisherBean;
import com.lifesense.soa.wechatgateway.api.IWechatGatewayForwardProvider;
import com.lifesense.soa.wechatgateway.api.IWechatGatewayForwardProviderV2;
import com.lifesense.soa.wechatgateway.dto.receive.WechatReceiveAllMessage;
import com.lifesense.soa.wechatgateway.dto.receive.WechatReceiveDeviceStatusEventMessage;
/** 
 * date: 2016年1月12日 下午5:12:49
 * 收到微信消息后转发服务类
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2016, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("deprecation")
@Service("wechatGatewayForwardService")
public class WechatGatewayForwardService implements IWechatGatewayForwardProvider {
	@Autowired(required=false)
	@Qualifier("wechatGatewayTopicPublisher")//多个不同实例时，指定id
	private TopicPublisherBean topicPublisher;
	
	
	
	@Autowired
	protected IWechatGatewayForwardProviderV2 wechatGatewayForwardService;

	/**
	 * 转发微信消息到多客服系统（转发到7鱼多客服）
	 * 
	 * @param forwardUrl 接收转发消息的URL
	 * @param wechatMessage 微信消息
	 * */
	public void forwardWechatMessageToCustomer(RequestHeader requestHeader,WechatReceiveMessage wechatMessage) {
		wechatGatewayForwardService.forwardWechatMessageToCustomer(wechatMessage);
		
	}
	
	/**
	 * 把接收到微信事件消息发布到kafka
	 * @param receiveMessage 接收到的微信消息
	 * @param topicPublisher 消息发布者对象
	 * @param step 运动步数
	 * */
	public void publishWechatEventMessage(RequestHeader requestHeader,WechatReceiveAllMessage receiveMessage,Integer step,User user){
		wechatGatewayForwardService.publishWechatEventMessage(requestHeader, receiveMessage, step, user);
	}
	
	
	/**
	 * 把接收到微信语音消息发布到kafka
	 * @param receiveMessage 接收到的微信消息
	 * @param topicPublisher 消息发布者对象
	 * */
	public void publishWechatVoiceMessage(RequestHeader requestHeader,WechatReceiveMessage wechatMessage){
		wechatGatewayForwardService.publishWechatVoiceMessage(wechatMessage);
	}
	
	public void  publishDeviceStatusEvent(RequestHeader requestHeader,WechatReceiveDeviceStatusEventMessage receiveMessage){
		wechatGatewayForwardService.publishDeviceStatusEvent(receiveMessage);
	}
		
	public void publishWechatTxtEventForActivity(RequestHeader requestHeader, AppType appType,WechatReceiveTextMessage wechatReceiveTextMessage){
		wechatGatewayForwardService.publishWechatTxtEventForActivity(appType, wechatReceiveTextMessage);
	}
	
	/***
	 * 根据公众号原始ID获取应用类型
	 * */
	@Override
	public AppType getAppTypeByWechatServiceNo(String wechatServiceNo){
		return wechatGatewayForwardService.getAppTypeByWechatServiceNo(wechatServiceNo);
	}
	
	//健康检查
	@Override
	public Map<String, Object> echo(int...modes) {
		return wechatGatewayForwardService.echo(modes);
	}
}
