package com.yuandu.wechatgateway.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lifesense.base.beans.param.RequestHeader;
import com.lifesense.base.cache.command.RedisNumber;
import com.lifesense.base.cache.command.RedisString;
import com.lifesense.base.constant.AppType;
import com.lifesense.base.constant.OpenAccountType;
import com.lifesense.base.constant.ServiceName;
import com.lifesense.base.dto.sport.PedometerRecordDayDto;
import com.lifesense.base.dto.user.User;
import com.lifesense.base.dto.wechatgateway.WechatReceiveMessage;
import com.lifesense.base.dto.wechatgateway.WechatReceiveTextMessage;
import com.lifesense.base.spring.InstanceFactory;
import com.lifesense.base.utils.LsDigestUtils;
import com.lifesense.base.utils.ResourceUtils;
import com.lifesense.base.utils.SystemUtils;
import com.lifesense.base.utils.UUIDUtils;
import com.lifesense.base.utils.json.JsonUtils;
import com.lifesense.log.core.LogConstant;
import com.lifesense.soa.device.api.IDeviceUserProvider;
import com.lifesense.soa.device.gateway.api.IDeviceGatewayWechatProvider;
import com.lifesense.soa.service.api.IEventsReportProvider;
import com.lifesense.soa.service.dto.Event;
import com.lifesense.soa.service.dto.Events;
import com.lifesense.soa.sport.api.ISportProvider;
import com.lifesense.soa.user.api.IUserProvider;
import com.lifesense.soa.wechatgateway.api.IWechatGatewayForwardProvider;
import com.lifesense.soa.wechatgateway.api.IWechatGatewayProvider;
import com.lifesense.soa.wechatgateway.dto.enums.WechatDeviceStatusEnum;
import com.lifesense.soa.wechatgateway.dto.enums.WechatEventTypeEnum;
import com.lifesense.soa.wechatgateway.dto.enums.WechatMessageTypeEnum;
import com.lifesense.soa.wechatgateway.dto.receive.WechatReceiveAllMessage;
import com.lifesense.soa.wechatgateway.dto.receive.WechatReceiveDeviceEventMessage;
import com.lifesense.soa.wechatgateway.dto.receive.WechatReceiveDeviceStatusEventMessage;
import com.lifesense.soa.wechatgateway.dto.send.Text;
import com.lifesense.soa.wechatgateway.dto.send.WechatCustomServiceTextMessage;
import com.lifesense.soa.wechatgateway.dto.send.WechatDeviceStatusMessage;
import com.lifesense.soa.wechatgateway.service.utils.MPAccountUtil;
import com.lifesense.soa.wechatgateway.utils.Constants;
import com.lifesense.soa.wechatgateway.utils.PrintWriterUtil;
import com.lifesense.soa.wechatgateway.utils.SignatureUtil;
import com.lifesense.soa.wechatgateway.utils.StringUtilByWechatGateway;
import com.lifesense.soa.wechatgateway.utils.ThreadPoolRankUtils;
import com.lifesense.soa.wechatgateway.utils.XstreamUtil;

/** 
 * ClassName: WechatMessageReceiver
 * Function: TODO ADD FUNCTION.
 * date: 2015年12月25日 下午2:53:39
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2015, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
@WebServlet(name = "weixinMessageReceiver", urlPatterns = {"/weixin/connect/old"})
public class WechatMessageReceiver extends HttpServlet
{
	protected static final Logger logger = LoggerFactory.getLogger(WechatMessageReceiver.class);
	
	//通过dubbo 获取微信网关转发服务实例
	private IWechatGatewayForwardProvider wechatGatewayForwardService=InstanceFactory.getInstance("wechatGatewayForwardService");
		
	//通过dubbo 获取设备收取数据服务实例
	private IDeviceGatewayWechatProvider deviceGatewayWechatService=InstanceFactory.getInstance("deviceGatewayWechatService");
	
	//通过dubbo 获取设备用户服务实例
	private IDeviceUserProvider deviceUserService=InstanceFactory.getInstance("deviceUserService");
	
	//通过dubbo 获取用户服务实例
	private IUserProvider userService=InstanceFactory.getInstance("userService");
	
	//通过dubbo 获取运动服务实例
	private ISportProvider sportService=InstanceFactory.getInstance("sportService");
	
	   //通过dubbo 获取微信网关转发服务实例
    private IWechatGatewayProvider wechatGatewayProvider=InstanceFactory.getInstance("wechatGatewayService");
    
    private IEventsReportProvider eventProvider=InstanceFactory.getInstance("eventsReportService");
	
	/**
	 * 微信服务器端请求接入乐心服务器(仅用于配置url和token时首次接入认证)
	 * 
	 */
	protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException
	{	
		MDC.put(LogConstant.PROJECT, ServiceName.WechatGateway.name() + "-rest");
		MDC.put(LogConstant.REQUEST_ID, request.getParameter("signature"));
		MDC.put(LogConstant.ENV, SystemUtils.getEnv());
		
		String signature = request.getParameter("signature");
		long timestamp = StringUtilByWechatGateway.toLong(request.getParameter("timestamp"), 0L);
		long nonce = StringUtilByWechatGateway.toLong(request.getParameter("nonce"), 0L);
		String echostr = request.getParameter("echostr");
		
		StringBuilder sb = new StringBuilder();
		sb.append("weixin connect to lifesense server");
		sb.append("==========================================");
		sb.append("signature from weixin > " + signature);
		sb.append("timestamp from weixin > " + timestamp);
		sb.append("nonce from weixin > " + nonce);
		sb.append("echostr from weixin > " + echostr);
		sb.append("==========================================");
		
		logger.debug(sb.toString());
		
		boolean result = SignatureUtil.checkSignature(signature, timestamp, nonce);

		if (result) 
		{
			PrintWriterUtil.sendText(echostr, response);
		}
	}
	
	/**
	 * 接收微信服务器端的消息数据
	 * 
	 * @param xml weixin端的消息数据(xml格式)
	 * @return String 直接返回""，微信服务器不会对此作任何处理，要求2秒内必须返回，否则微信端作为超时处理（重试三次）。
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
	    String signature = request.getParameter("signature");
	    
	    RequestHeader requestHeader=new RequestHeader();
		if(signature!=null && !"".equals(signature)){
		    requestHeader.setRequestId(signature);
		    logger.debug("signature_is_data_"+signature);
		}else{
		    requestHeader.setRequestId(UUIDUtils.uuid());
		    logger.debug("signature_is_null");
		}
		
		MDC.put(LogConstant.PROJECT, ServiceName.WechatGateway.name() + "-rest");
		MDC.put(LogConstant.REQUEST_ID, requestHeader.getRequestId());
		MDC.put(LogConstant.ENV, SystemUtils.getEnv());
		
		// 获得post过来的xml
		String xml = MPAccountUtil.getXML(request);
		
		// 日志记录原始MP消息
		logger.info("weixin_mp_msg: "+"requestId "+requestHeader.getRequestId() +" "+ xml);
		
		// 解析微信服务端发送的消息数据，TODO:考虑不转json格式，原样xml格式转发？
		WechatReceiveAllMessage message = XstreamUtil.parseWechatReceiveAllMessage(xml);

		//根据消息类型获取消息类型枚举
		WechatMessageTypeEnum wechatMessageTypeEnum = WechatMessageTypeEnum.getWechatMessageTypeEnumByValue(message.getMsgType()); 
		
		if(wechatMessageTypeEnum==null){
			return;
		}
		
		if(wechatMessageTypeEnum == WechatMessageTypeEnum.设备文本消息){
            //把设备数据发送的设备服务
        	Long start = System.currentTimeMillis();
        	
            String responseXml=deviceGatewayWechatService.receiveDeviceData(requestHeader,xml);
            if(logger.isInfoEnabled())
            	logger.info("wechat_receiveDeviceData_requestId_dev:"+requestHeader.getRequestId()+" useTime:"+(System.currentTimeMillis()-start) +"\n"+responseXml);
                logger.info("wechat_receiveDeviceData_requestId:"+requestHeader.getRequestId()+" useTime:"+(System.currentTimeMillis()-start) +"\n"+responseXml);
            //回包
            PrintWriterUtil.sendText(responseXml, response);
            
            //好友榜
		}else if(wechatMessageTypeEnum == WechatMessageTypeEnum.事件消息&&!StringUtils.isBlank(message.getEventKey())&&message.getEventKey().equals(WechatEventTypeEnum.拉取微信排行榜事件.toString())){
	            logger.info("PEDOMETER_TOP "+"requestID:"+requestHeader.getRequestId()+" "+menuRank(message));
	            reportEvent("rankinglist_click", message.getFromUserName(), wechatGatewayForwardService.getAppTypeByWechatServiceNo(message.getToUserName()).code());
	            PrintWriterUtil.sendText(menuRank(message), response);
		}else{
			ThreadPoolRankUtils.threadSendMessagePool.submit(new Runnable() {
				@Override
				public void run() {
					try{
						if(logger.isDebugEnabled()){
							logger.debug("async_wechatgate_process_start"+"requestId:"+requestHeader.getRequestId());
						}
						long start = System.currentTimeMillis();
						processWeChatRequest(response, requestHeader, xml, message, wechatMessageTypeEnum);
						logger.info("async_wechatgate_process_"+"requestId:"+requestHeader.getRequestId()+" useTime:"+(System.currentTimeMillis()-start));
					}catch(Exception ex){
						
					}
				}
			});
			// 返回消息
			PrintWriterUtil.sendText("", response);
		}
		
		logger.info("wechatgate_way_is_over>>>>>>"+xml + " requestId:"+requestHeader.getRequestId());
	}

	protected void processWeChatRequest(HttpServletResponse response, RequestHeader requestHeader, String xml,
			WechatReceiveAllMessage message, WechatMessageTypeEnum wechatMessageTypeEnum) {
		MDC.put(LogConstant.PROJECT, ServiceName.WechatGateway.name() + "-rest");
		MDC.put(LogConstant.REQUEST_ID, requestHeader.getRequestId());
		MDC.put(LogConstant.ENV, SystemUtils.getEnv());
		
		//获取appType
        AppType appType = wechatGatewayForwardService.getAppTypeByWechatServiceNo(message.getToUserName());
        requestHeader.setAppType(appType);
		
		boolean isDeviceEvent = false;
		try{
		    switch (wechatMessageTypeEnum) {
	            case 文本消息:
	            	reportMsgEvent(message.getFromUserName(),appType.code());
	                processTextEvent(requestHeader, xml, message);
	                WechatReceiveMessage wechatReceiveMessage = message.toWechatReceiveMessage();
	                if(appType.equals(AppType.乐心健康WECHAT)){
	                	wechatGatewayForwardService.forwardWechatMessageToCustomer(requestHeader,message.toWechatReceiveMessage());
	                }
	                
	                // 处理运营活动
	                this.dealActivity(requestHeader, wechatReceiveMessage);
	              
	                break;
	            case 语音消息:
	            	reportMsgEvent(message.getFromUserName(),appType.code());
//	                将消息发布到kafka
	            	wechatGatewayForwardService.publishWechatVoiceMessage(requestHeader,message.toWechatReceiveMessage());
	              
	            	//回包
	            	//PrintWriterUtil.sendText("", response);
	                break;
	            case 图片消息:
	            	reportMsgEvent(message.getFromUserName(),appType.code());
	            	//将消息转发到多客服系统
	            	 if(appType.equals(AppType.乐心健康WECHAT)){
	            		 wechatGatewayForwardService.forwardWechatMessageToCustomer(requestHeader,message.toWechatReceiveMessage());
		             }
	               
	                //回包
	                //PrintWriterUtil.sendText("", response);
	                break;
	            case 视频消息:
	            	reportMsgEvent(message.getFromUserName(),appType.code());
	                //将消息转发到多客服系统
//	              wechatGatewayForwardService.forwardWechatMessageToCustomer(requestHeader,mpAccount.getVoiceForwardUrl(),message.toWechatReceiveMessage(),response);
	                break;
	            case 设备文本消息:                
	                //把设备数据发送的设备服务
	            	Long start = System.currentTimeMillis();
	            	
	                String responseXml=deviceGatewayWechatService.receiveDeviceData(requestHeader,xml);
	                if(logger.isInfoEnabled())
	                    logger.info("wechat_receiveDeviceData_deviceId_"+message.getDeviceID()+" json:"+JsonUtils.toJson(message)+" requestid:"+requestHeader.getRequestId()+" useTime:"+(System.currentTimeMillis()-start) +"\n"+responseXml);
	                //回包
	                //PrintWriterUtil.sendText(responseXml, response);
	                
	                break;
	            case 事件消息:
	                processEventMessage(response, requestHeader, message);
	                break;
	            case 设备事件消息:
	                isDeviceEvent = true;
	                
//	                requestHeader.setAppType(appType);
	                //调用设备服务中的微信设备事件服务
	                WechatReceiveDeviceEventMessage wechatReceiveDeviceEventMessage = (WechatReceiveDeviceEventMessage)message.toWechatReceiveMessage();
	                
	                WechatEventTypeEnum wechatEventTypeEnum = WechatEventTypeEnum.getWechatEventTypeEnumByValue(wechatReceiveDeviceEventMessage.getEvent());
	                if(WechatEventTypeEnum.订阅设备状态==wechatEventTypeEnum){
	                    logger.info("wifi_device_status :"+requestHeader.getRequestId() +" deviceId"+wechatReceiveDeviceEventMessage.getDeviceID()+" "+wechatReceiveDeviceEventMessage.getOpenID() +"appType:"+requestHeader.getAppType().code()+" serverNo:"+message.getToUserName());
	                    WechatDeviceStatusMessage  deviceStatusMessage= new WechatDeviceStatusMessage();
	                    deviceStatusMessage.setDevice_id(wechatReceiveDeviceEventMessage.getDeviceID());
	                    deviceStatusMessage.setDevice_status(WechatDeviceStatusEnum.未连接.toString());
	                    deviceStatusMessage.setDevice_type(wechatReceiveDeviceEventMessage.getDeviceType());
	                    deviceStatusMessage.setMsg_type(WechatMessageTypeEnum.发送设备状态消息.toString());
	                    deviceStatusMessage.setOpen_id(wechatReceiveDeviceEventMessage.getOpenID());
	                    wechatGatewayProvider.sendWechatDeviceStatusMessage(requestHeader, appType, deviceStatusMessage);
	                    
	                    logger.info("wifi_device_over :"+requestHeader.getRequestId() +" deviceId"+wechatReceiveDeviceEventMessage.getDeviceID()+" "+wechatReceiveDeviceEventMessage.getOpenID() +"appType:"+requestHeader.getAppType().code()+" serverNo:"+message.getToUserName());
	                }else{
	                    Long startProcessBind =System.currentTimeMillis();
	                    logger.info("WxBindByWechat："+requestHeader.getRequestId() +" deviceId"+wechatReceiveDeviceEventMessage.getDeviceID()+" "+wechatReceiveDeviceEventMessage.getOpenID() +"appType:"+requestHeader.getAppType().code()+" serverNo:"+message.getToUserName());
	                    deviceUserService.bindByWechat(requestHeader,wechatReceiveDeviceEventMessage);
	                    
	                    Long endProcessBind =System.currentTimeMillis();
	                    logger.info("WxBindByWechatover "+requestHeader.getRequestId() +" useTime:"+(endProcessBind-startProcessBind)+" deviceId"+wechatReceiveDeviceEventMessage.getDeviceID()+" "+wechatReceiveDeviceEventMessage.getOpenID());
	                }
	                //回包
	                //PrintWriterUtil.sendText("", response);
	                break;
	                
	            case 设备状态消息:
	                
	                 //获取appType
//	                requestHeader.setAppType(wechatGatewayForwardService.getAppTypeByWechatServiceNo(message.getToUserName()));
	                //调用设备服务中的微信设备事件服务
	                
	                
	                WechatReceiveDeviceStatusEventMessage receiveDeviceStatusEventMessage = (WechatReceiveDeviceStatusEventMessage)message.toWechatReceiveMessage();
	                
	                if(logger.isDebugEnabled()){
	                    logger.info("subscribe_status_json_invoke1>>>>>>>"+JsonUtils.toJson(receiveDeviceStatusEventMessage) + " requestId:"+requestHeader.getRequestId());
	                }
	                wechatGatewayForwardService.publishDeviceStatusEvent(requestHeader,receiveDeviceStatusEventMessage);
	                
	                if(logger.isDebugEnabled()){
	                    logger.info("subscribe_status_json_invoke_over>>>>>>>"+JsonUtils.toJson(receiveDeviceStatusEventMessage) + " requestId:"+requestHeader.getRequestId());
	                }
	                //回包
	                //PrintWriterUtil.sendText("", response);
	                break;
	            default:
	                //PrintWriterUtil.sendText("", response);
	                break;
	        }
		    
		}catch(Exception ex){
		    logger.error("wechatgate_way_is_over_ex_requestId_"+requestHeader.getRequestId()+ " error :" + ex.getMessage()+" xml : "+xml,ex);
		    if(isDeviceEvent){
		        logger.error("device_event_error_requestId_"+requestHeader.getRequestId()+"\n" + xml);
		    }
		}
		
		
		logger.info("async_wechatgate_way_is_over>>>>>>"+xml + " requestId:"+requestHeader.getRequestId());
	}

	/**
	 * 
	 * @param requestHeader
	 * @param wechatReceiveMessage
	 */
    private void dealActivity(RequestHeader requestHeader, WechatReceiveMessage wechatReceiveMessage) {
    	//  运销活动是否关闭
    	String isOpenMarking = ResourceUtils.get("is_open_marking", "true");
    	// 两次操作时间健康不能小于10秒
        String stayTime = ResourceUtils.get("is_marking_stay_time", "10");
        
        AppType appType = requestHeader.getAppType();
        WechatReceiveTextMessage message = (WechatReceiveTextMessage)wechatReceiveMessage;
        logger.info("marking_out_health_"+Constants.getHealthActiveKeyWords());
        logger.info("marking_out_sport_"+Constants.getSprotsActiveKeyWords());
        // 检索是否是运动或者健康的运营关键字
        if(((Constants.getHealthActiveKeyWords().contains(message.getContent())&&appType.equals(AppType.乐心健康WECHAT))
        		|| ("我自律我最美".equals(message.getContent()) && appType.equals(AppType.乐心运动WECHAT)))
        	    && "true".equals(isOpenMarking)){
        	// 统计各个公众号各个关键字的发送次数
    		RedisNumber activeCount = new RedisNumber("marking:jiankang:"+appType.toString()+":message.getContent()");
    		activeCount.increase(1);
    		
    		// 缓存用户是否在短时间内发送过关键字
    		RedisString markingLimitRedisKey = new RedisString("marking:openid:" + wechatReceiveMessage.getFromUserName());
    		if(markingLimitRedisKey.exists()){
    			logger.info("processWeChatRequest_activity_txt_wecaht_limit_jiankan_"+wechatReceiveMessage.getFromUserName() +" json: "+JsonUtils.toJson(message));
    			sendMarkingMessage(requestHeader,wechatReceiveMessage.getFromUserName(),appType,"亲爱哒，您发送得太频繁了，歇会儿再来，大奖还在滴！么么哒~");
    			return;
    		}
    		
			markingLimitRedisKey.set("1", Long.valueOf(stayTime));
			logger.info("processWeChatRequest_activity_txt_wecaht_jiankan_"+wechatReceiveMessage.getFromUserName() +" json: "+JsonUtils.toJson(message));
			// 发送kafka给运营活动服务器
			wechatGatewayForwardService.publishWechatTxtEventForActivity(requestHeader, appType, (WechatReceiveTextMessage)wechatReceiveMessage);
        }
	}

	private void processEventMessage(HttpServletResponse response, RequestHeader requestHeader,
        WechatReceiveAllMessage message)
        throws IOException
    {
		MDC.put(LogConstant.PROJECT, ServiceName.WechatGateway.name() + "-rest");
		MDC.put(LogConstant.REQUEST_ID, requestHeader.getRequestId());
		MDC.put(LogConstant.ENV, SystemUtils.getEnv());
		
        if(logger.isDebugEnabled()){
            logger.debug("processEventMessage :"+"fromUser:"+message.getFromUserName()+" toUser:"+message.getToUserName() +" msgId:"+message.getMsgID()+" status:"+message.getStatus() +" requestId:"+requestHeader.getRequestId());
        }
        if(!StringUtils.isBlank(message.getEventKey())&&message.getEventKey().equals(WechatEventTypeEnum.拉取微信排行榜事件.toString()))
        {
            logger.info("PEDOMETER_TOP "+"requestID:"+requestHeader.getRequestId()+" "+menuRank(message));
            PrintWriterUtil.sendText(menuRank(message), response);
        }
        else
        {
            Integer step=0;
            User user=null;
            
            WechatEventTypeEnum eventTypeEnum=WechatEventTypeEnum.getWechatEventTypeEnumByValue(message.getEvent());
            
            if(WechatEventTypeEnum.模版消息送达事件.equals(eventTypeEnum)){
                if(!"success".equals(message.getStatus())){
                    logger.warn("send_template_job_fail :"+"fromUser:"+message.getFromUserName()+" toUser:"+message.getToUserName() +" msgId:"+message.getMsgID()+" status:"+message.getStatus() +" requestId:"+requestHeader.getRequestId());
                }
            }
            //判断事件为扫描带参二维码事件时，做如下处理
            //关注公众号（包括无参二维码，原来只处理带参二维码eventKey!=null）都创建用户。
            if((eventTypeEnum!=null&&eventTypeEnum.equals(WechatEventTypeEnum.带参二维码已经关注事件))
                ||(eventTypeEnum!=null&&eventTypeEnum.equals(WechatEventTypeEnum.订阅事件)))
            {
                //根据serviceNo和openId去用户服务获取用户，若用户不存在，用户服务负责创建用户
                logger.info("WxGetUserByWechatOpenId："+requestHeader.getRequestId());
                user=userService.getUserByWechatOpenId2(requestHeader,message.getToUserName(),message.getFromUserName());   //----临时方案 灰度
                if(logger.isDebugEnabled()){
                   if(user==null){
                       logger.warn("userService_getUserByWechatOpenId2_error requestID:"+requestHeader.getRequestId() +" fromUserName"+message.getFromUserName());
                   }else{
                       
                       logger.debug("userService_getUserByWechatOpenId2_ok  requestID:"+requestHeader.getRequestId() +" WechatUnionId"+user.getWechatUnionId());
                   } 
                }
                //user=userService.getUserByWechatOpenId(requestHeader,message.getToUserName(),message.getFromUserName());
                if(user!=null)
                {
                    requestHeader.setUserId(user.getId());
                    logger.info("WxGetLatestPedometerRecord："+requestHeader.getRequestId());
                    PedometerRecordDayDto pedoDay = sportService.getLatestPedometerRecord(requestHeader,user.getId());
                    if(pedoDay!=null)
                    {
                        step=pedoDay.getStep();
                        logger.info("获取当天步数pedoDay："+pedoDay);
                    }
                }
            }
            
            //非客服消息，将消息发布到Kafka,让各业务系统去订阅
            wechatGatewayForwardService.publishWechatEventMessage(requestHeader,message,step,user);
            //PrintWriterUtil.sendText("", response);
        }
    }

    private void processTextEvent(RequestHeader requestHeader, String xml, WechatReceiveAllMessage message)
    {
//        if(logger.isDebugEnabled()){
//            logger.debug("1000000000001");
//        }
//        logger.info("textmessage_json_invoke1>>>>>>>"+xml + " requestId:"+requestHeader.getRequestId());
//        
//        
//        String content = message.getContent();
//        boolean isSendcustom = true;;
//        if(!StringUtils.isBlank(content)){
//            if(grayWordList == null){
//                grayWordList = new ArrayList<>();
//                String wordGrays = ResourceUtils.get("auto_reploy_gray_word", "反馈/灰度测试/灰度/问题反馈/测试/问题");
//                if(!StringUtils.isBlank(wordGrays)){
//                    String wordGrayArr[] = wordGrays.split("/");
//                    if(wordGrayArr.length >0){
//                        
//                        for(String word : wordGrayArr){
//                            
//                            grayWordList.add(word);
//                        }
//                    }
//                }
//            }
//            
//            if(grayWordList !=null&&!grayWordList.isEmpty()){
//                
//                StringBuffer sbTxt = new StringBuffer();
//                for(String word : grayWordList){
//                    
//                    if(content.indexOf(word) !=-1){
//                        sbTxt.append("感谢您参与线上灰度测试！\n");
//                        sbTxt.append("<a id=\"a1\" href=\"https://jinshuju.net/f/bpOD6u\">"+"→问题反馈←"+"</a>").append(" \n");
//                        sbTxt.append("<a id=\"a2\" href=\"http://dwz.cn/3ljfwZ\">"+"→测试说明←"+"</a>");
//                        isSendcustom=false;
//                        break;
//                    }
//                }
//                WechatCustomServiceTextMessage textMessage = new WechatCustomServiceTextMessage();
//                textMessage.setTouser(message.getFromUserName());
//                textMessage.setMsgtype("text");
//                
//                Text text = new Text();
//                text.setContent(sbTxt.toString());
//                textMessage.setText(text);
//                
//                wechatGatewayProvider.sendCustomServiceMessage(requestHeader, wechatGatewayForwardService.getAppTypeByWechatServiceNo(message.getToUserName()), textMessage);
//                
//                
//                if(logger.isDebugEnabled()){
//                    logger.debug("2139890234textmessage_json_invoke1_over:"+xml + " requestId:"+requestHeader.getRequestId());
//                }
//            }
            
//            if(isSendcustom){
//                WechatCustomServiceTextMessage textMessage = new WechatCustomServiceTextMessage();
//                textMessage.setTouser(message.getFromUserName());
//                textMessage.setMsgtype("text");
//                
//                Text text = new Text();
//                StringBuffer sbTxt = new StringBuffer();
//                sbTxt.append("欢迎使用乐心运动！\n");
//                sbTxt.append("如遇问题，欢迎致电：4006002323");
//                text.setContent(sbTxt.toString());
//                textMessage.setText(text);
//                
//                wechatGatewayProvider.sendCustomServiceMessage(requestHeader, wechatGatewayForwardService.getAppTypeByWechatServiceNo(message.getToUserName()), textMessage);
//                
//                if(logger.isDebugEnabled()){
//                    logger.debug("2139890234textmessage_json_invoke2_over:"+xml + " requestId:"+requestHeader.getRequestId());
//                }
//                
//            }
//        }
    }
    
	protected void sendMarkingMessage(RequestHeader requestHeader, String openid , AppType appType,
			String content ) {
			WechatCustomServiceTextMessage textMessage = new WechatCustomServiceTextMessage();
			textMessage.setTouser(openid);
			textMessage.setMsgtype("text");

			Text text = new Text();
			text.setContent(content);
			textMessage.setText(text);
			wechatGatewayProvider.sendCustomServiceMessage(requestHeader, appType, textMessage);

	}
	
	
	/**
	 * 用户排名
	 * 
	 * @param message
	 * @return
	 */
	private String menuRank(WechatReceiveAllMessage message) 
	{
		StringBuilder sb = new StringBuilder();

		sb.append(
				String.format("<xml><ToUserName><![CDATA[%s]]></ToUserName><FromUserName><![CDATA[%s]]></FromUserName><CreateTime>%s</CreateTime>", message.getFromUserName(), message.getToUserName(),
						new Date().getTime()))
				.append("<MsgType><![CDATA[hardware]]></MsgType><HardWare><MessageView><![CDATA[myrank]]></MessageView><MessageAction><![CDATA[ranklist]]></MessageAction></HardWare><FuncFlag>0</FuncFlag></xml>");
		return sb.toString();
	}
	
	private void reportEvent(String eventId,String openId ,Integer appType){
		Events events=new Events();
		long timestamp=System.currentTimeMillis();
		String secret="b0083e3837634635837f3c17a78eb11d";
		events.setChecksum(LsDigestUtils.md5(timestamp + secret));
		events.setTimestamp(timestamp);
		long userId = userService.getUserIdByOpenId(openId, OpenAccountType.WECHAT.code());
		events.setCommons("userId", String.valueOf(userId));
		events.setCommons("appType", appType.intValue());
		events.setCommons("systemType", 3);
		events.setCommons("platform", "wechat");
		Event e=new Event();
		e.setEventID(eventId);
		e.setTimestamp(timestamp);
		events.setEvents(Arrays.asList(e));
		eventProvider.report(events);
	}
	
	private void reportMsgEvent(String openId,Integer appType){
		reportEvent("typecontent_click", openId, appType);
	}
}
