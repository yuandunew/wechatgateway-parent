/**
 * 
 */
package com.yuandu.wechatgateway.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.lifesense.base.beans.param.RequestHeader;
import com.lifesense.base.cache.command.RedisObject;
import com.lifesense.base.cache.command.RedisString;
import com.lifesense.base.constant.AppType;
import com.lifesense.base.constant.ServiceName;
import com.lifesense.base.dto.wechatgateway.WechatCustomServiceImageMessage;
import com.lifesense.base.dto.wechatgateway.WechatCustomServiceMessage;
import com.lifesense.base.dto.wechatgateway.WechatCustomServiceNewsMessage;
import com.lifesense.base.dto.wechatgateway.WechatCustomServiceVoiceMessage;
import com.lifesense.base.dto.wechatgateway.WechatTemplateMessage;
import com.lifesense.base.exception.LifesenseBaseException;
import com.lifesense.base.exception.check.ParamsIsNullException;
import com.lifesense.base.exception.wechat.WsSetStepErrorException;
import com.lifesense.base.exception.wechat.WxAppTypeNotConfiguredWechatServiceNoException;
import com.lifesense.base.exception.wechat.WxAppTypeNullException;
import com.lifesense.base.exception.wechat.WxGetDeviceAuthorizeErrorException;
import com.lifesense.base.exception.wechat.WxGetDeviceQrcodeErrorException;
import com.lifesense.base.exception.wechat.WxGetOpenidErrorException;
import com.lifesense.base.exception.wechat.WxPublicNoNotExistsException;
import com.lifesense.base.exception.wechat.WxUserInfoFetchException;
import com.lifesense.base.exception.wechat.WxVerifyQrcodeErrorException;
import com.lifesense.base.spring.InstanceFactory;
import com.lifesense.base.utils.DateUtils;
import com.lifesense.base.utils.ServiceAssert;
import com.lifesense.base.utils.json.JsonUtils;
import com.lifesense.base.utils.okhttp.FileExtensionEnum;
import com.lifesense.base.utils.okhttp.HttpClientManager;
import com.lifesense.soa.wechatgateway.api.IWechatGatewayProviderV2;
import com.lifesense.soa.wechatgateway.dao.mapper.ParamQrcodeEntityMapper;
import com.lifesense.soa.wechatgateway.dto.ParamTempQrcodeDto;
import com.lifesense.soa.wechatgateway.dto.enums.ParamQrcodeActionTypeEnum;
import com.lifesense.soa.wechatgateway.dto.enums.WechatEventTypeEnum;
import com.lifesense.soa.wechatgateway.dto.enums.WechatMediaFileTypeEnum;
import com.lifesense.soa.wechatgateway.dto.enums.WechatMessageTypeEnum;
import com.lifesense.soa.wechatgateway.dto.enums.WechatRespondErrCodeEnum;
import com.lifesense.soa.wechatgateway.dto.receive.JSSDKSignatureInfo;
import com.lifesense.soa.wechatgateway.dto.receive.ParamQrcodeResponse;
import com.lifesense.soa.wechatgateway.dto.receive.VerifyQrcodeResp;
import com.lifesense.soa.wechatgateway.dto.receive.WeChatIotDeviceResponse;
import com.lifesense.soa.wechatgateway.dto.receive.WechatAuthorizeDeviceSuccessInfo;
import com.lifesense.soa.wechatgateway.dto.receive.WechatDeviceIdAndQrcode;
import com.lifesense.soa.wechatgateway.dto.receive.WechatDeviceQrcode;
import com.lifesense.soa.wechatgateway.dto.receive.WechatDeviceStatusResp;
import com.lifesense.soa.wechatgateway.dto.receive.WechatIOTQrcodeResp;
import com.lifesense.soa.wechatgateway.dto.receive.WechatUser;
import com.lifesense.soa.wechatgateway.dto.receive.WechatWebpageAccessTokenResp;
import com.lifesense.soa.wechatgateway.dto.send.ParamQrcodeParam;
import com.lifesense.soa.wechatgateway.dto.send.VerifyQrcodeParam;
import com.lifesense.soa.wechatgateway.dto.send.WechatAuthorizeDevice;
import com.lifesense.soa.wechatgateway.dto.send.WechatBindDeviceEventMessage;
import com.lifesense.soa.wechatgateway.dto.send.WechatCustomServiceTextMessage;
import com.lifesense.soa.wechatgateway.dto.send.WechatDeviceEventMessage;
import com.lifesense.soa.wechatgateway.dto.send.WechatDeviceQrcodeParam;
import com.lifesense.soa.wechatgateway.dto.send.WechatDeviceStatusMessage;
import com.lifesense.soa.wechatgateway.dto.send.WechatDeviveContentMessage;
import com.lifesense.soa.wechatgateway.dto.send.WechatServiceNoInfo;
import com.lifesense.soa.wechatgateway.service.constants.WechatGratewayKey;
import com.lifesense.soa.wechatgateway.service.dto.NotSendMessage;
import com.lifesense.soa.wechatgateway.service.dto.NotSendMessageQueue;
import com.lifesense.soa.wechatgateway.service.utils.DataUtil;
import com.lifesense.soa.wechatgateway.service.utils.MPManager;
import com.lifesense.soa.wechatgateway.utils.AppTypeAdapter;
import com.lifesense.soa.wechatgateway.utils.Constants;
import com.lifesense.soa.wechatgateway.utils.GsonUtil;
import com.lifesense.soa.wechatgateway.utils.SignatureUtil;
import com.lifesense.soa.wechatgateway.utils.StringUtilByWechatGateway;

import net.sf.json.JSONObject;

/**
 * 
 * 微信网关服务实现
 * 
 * @version
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang Copyright (c)
 *         2016, lifesense.com All Rights Reserved.
 */
@Service("wechatGatewayServiceV2")
public class WechatGatewayServiceV2 implements IWechatGatewayProviderV2 {
	private static final Logger logger = LoggerFactory.getLogger(WechatGatewayServiceV2.class);
	
	@Autowired
	TaskExecutor taskExecutor;

	@Autowired
	WechatGatewayAdapter wechatGatewayAdapter;

	private String isAsyncRank = "true";

	private Date lastDate = new Date();


	/***********************************************
	 * 向微信公众号请求信息 start
	 *************************************************************/
	/**
	 * 根据微信公众号，OPENID获取微信用户信息
	 * 
	 * @param appType 应用类型
	 * @param openid
	 * @return 微信用户信息
	 */
	@Override
	public WechatUser getWechatUserByAppTypeAndOpenid(AppType appType, String openid) {
		// 获取用户资料，第一次请求
		WechatUser wechatUser = wechatGatewayAdapter.getWechatUserForMPByWechatServiceNo(1, appType, openid);
		return wechatUser;
	}

	/**
	 * 根据微信公众号，OPENID获取微信用户信息
	 * 
	 * @param appType 应应用类型
	 * @param openid
	 * @return 微信用户信息
	 */
	@Override
	public WechatUser getWechatUserByServiceNoAndOpenid(String wechatServiceNo, String openid) {
		AppType appType = MPManager.getAppTypeByWechatServiceNo(wechatServiceNo);
		if (appType == null) {
			throw new WxAppTypeNullException(ServiceName.WechatGateway);
		}

		return this.getWechatUserByAppTypeAndOpenid(appType, openid);
	}

	/**
	 * 根据微信公众号ACCESS_TOKEN，OPENID获取微信用户信息
	 * 
	 * @param accessToken 微信公众号访问令牌
	 * @param openid
	 * @return 微信用户信息
	 */
	@Override
	public WechatUser getWechatUserByAccessTokenAndOpenid(String accessToken, String openid) {
		// 通过HTTP到微信服务器请求微信用户信息
		String body = HttpClientManager.get(null, MessageFormat.format(MPManager.getMPUrl().getGetUserInfoUrl(), accessToken, openid));
		JSONObject json = JSONObject.fromObject(body);
		if (logger.isInfoEnabled())
			logger.info("!!!!!!!!!!!!getWechatUser:" + body);

		// 判断返回内容是否为空，或内容中是否包含错误代码
		if (StringUtils.isEmpty(body) || !(WechatRespondErrCodeEnum.发送成功 == MPManager.checkErrcode(json))) {
			throw new WxUserInfoFetchException(ServiceName.WechatGateway);
		}

		return DataUtil.jsonToWechatUser(body);
	}

	/**
	 * 根据微信公众号ACCESS_TOKEN，OPENID获取微信用户信息(用于微信开放平台)
	 * 
	 * @param accessToken 调用凭证
	 * @param openid
	 * @return 微信用户信息
	 */
	@Override
	public WechatUser getWechatUserByAccessTokenAndOpenidBySns(String accessToken, String openid) {
		// 通过HTTP到微信服务器请求微信用户信息
		String body = HttpClientManager.get(null, MessageFormat.format(MPManager.getMPUrl().getGetUserInfoBySnsUrl(), accessToken, openid));
		JSONObject json = JSONObject.fromObject(body);
		if (logger.isInfoEnabled())
			logger.info("!!!!!!!!!!!!getWechatUser:" + body);

		// 判断返回内容是否为空，或内容中是否包含错误代码
		if (StringUtils.isEmpty(body) || !(WechatRespondErrCodeEnum.发送成功 == MPManager.checkErrcode(json))) {
			throw new WxUserInfoFetchException(ServiceName.WechatGateway);
		}

		return DataUtil.jsonToWechatUser(body);
	}

	/**
	 * 根据应用类型获取微信JSApiTicket
	 * 
	 * @param appType 应用类型
	 * @return jsApiTicket
	 */
	@Override
	public String getJSApiTicketByAppType(AppType appType) {
		return wechatGatewayAdapter.getJSApiTicketByAppType(appType);
	}

	/**
	 * 根据AppType从缓存中获取微信AccessToken
	 * 
	 * @param appType 应用类型
	 * @return accessToken
	 */
	@Override
	public String getAccessTokenForRedis(AppType appType) {
		return wechatGatewayAdapter.getAccessTokenForRedis(appType);
	}

	/**
	 * 根据AppType获取微信AccessToken
	 * 
	 * @param appType 应用类型
	 * @return accessToken
	 */
	@Override
	public String getAccessTokenByAppType(AppType appType) {
		return wechatGatewayAdapter.getAccessTokenByAppType(appType);
	}

	@Override
	public String getAccessTokenByURL(AppType appType) {
		return wechatGatewayAdapter.getAccessTokenByURL(appType);
	}

	/**
	 * 根据参数获取带参数二维码(乐心健康公众号专用)
	 * 
	 * @param param 获取参数
	 * @return 二维码解析后URL
	 */
	@Override
	public String getParamQrcodeUrlForHealth(ParamQrcodeParam param) {
		String paramQrcodeUrl = "";

		String wechatServiceNo = MPManager.getWechatServiceNoInfoByApptype(AppType.乐心健康WECHAT).getServiceNo();
		if (StringUtilByWechatGateway.isBlankOrNull(wechatServiceNo)) {
			throw new WxAppTypeNotConfiguredWechatServiceNoException(ServiceName.WechatGateway);
		}

		// 用字符串做场景值ID时
		if (param.getActionName().equals(ParamQrcodeActionTypeEnum.永久二维码_字符串.toString())) {
			paramQrcodeUrl = wechatGatewayAdapter.geQrLimitStrScene(param, wechatServiceNo).getUrl();
		} else {
			paramQrcodeUrl = this.getParamQrcodeUrl(param).getUrl();
		}
		return paramQrcodeUrl;
	}

	/**
	 * 根据公众号原始ID获取公众号信息
	 * 
	 * @param wechatServiceNo 公众号原始ID
	 * @return 公号信息
	 */
	@Override
	public WechatServiceNoInfo getWechatServiceNoInfoByApptype(AppType appType) {
		return MPManager.getWechatServiceNoInfoByApptype(appType);
	}

	/**
	 * 临时二维码新接口
	 * 
	 * @param requestHeader
	 * @param paramTempQrcodeDto
	 * @return
	 */
	@Override
	public ParamQrcodeResponse getTempParamQrcodeUrl(ParamTempQrcodeDto paramTempQrcodeDto) {
		// 二维码仅公众号才有的功能
		paramTempQrcodeDto.setAppType(AppTypeAdapter.onlyWechat(paramTempQrcodeDto.getAppType()));
		ParamQrcodeParam param = new ParamQrcodeParam();
		param.setAppType(paramTempQrcodeDto.getAppType());
		param.setBusinessType(paramTempQrcodeDto.getBusinessType());
		param.setExpireseconds(paramTempQrcodeDto.getExpireseconds());
		ParamQrcodeResponse paramQrcodeResponse = null;
		String id = "";
		RedisString redisString = null;
		if (StringUtils.isEmpty(paramTempQrcodeDto.getSceneStr())) {
			id = DataUtil.getRandomByQrcodeType(paramTempQrcodeDto.getBusinessType(), 0).longValue() +"";
			param.setSceneId(NumberUtils.toLong(id));
			param.setActionName("QR_SCENE");
		}else {
			id= param.getSceneStr();
			param.setSceneStr(paramTempQrcodeDto.getSceneStr());
			param.setActionName("QR_SCENE_STR");
		}
		redisString = new RedisString( "wechat:temp:qrcode:" + id);
		paramQrcodeResponse = wechatGatewayAdapter.getQrSceneParamQrcodeUrl(param);
		Map<String, Object> map = Maps.newHashMap();
		map.put("SceneId", id);
		map.put("paramQrcode", paramQrcodeResponse);
		redisString.set(JsonUtils.toJson(map));
		redisString.setExpire(3600 * 24 * 7);
		return paramQrcodeResponse;
	}

	





	/**
	 * 根据应用类型和网页授权Code获取OpenId
	 * 
	 * @param appType 应用类型
	 * @param code 网页授权Code
	 * @return openId
	 */
	@Override
	public String getOpenIdByCode(AppType appType, String code) {

		WechatWebpageAccessTokenResp accessTokenResp = getWechatWebpageAccessTokenByCode(appType, code);

		if (accessTokenResp != null) {
			return accessTokenResp.getOpenid();
		}
		return null;
	}

	@Override
	public WechatWebpageAccessTokenResp getWechatWebpageAccessTokenByCode(AppType appType, String code) {
		WechatServiceNoInfo serviceNoInfo = MPManager.getWechatServiceNoInfoByApptype(appType);
		if (serviceNoInfo == null) {
			// 抛出应用类型还未配置相应对应的微信公众号
			throw new WxAppTypeNotConfiguredWechatServiceNoException(ServiceName.WechatGateway);
		}

		// 通过HTTP到微信服务器请求网页授权AccessToken信息
		String body = HttpClientManager.get(null,
				MessageFormat.format(MPManager.getMPUrl().getGetAccessTokenUrl(), serviceNoInfo.getAppid(), serviceNoInfo.getSecret(), code));
		// 判断返回内容是否为空，或内容中是否包含错误代码
		if (StringUtils.isEmpty(body)) {
			// 抛出获取OpenId失败
			throw new WxGetOpenidErrorException(ServiceName.WechatGateway);
		}

		JSONObject json = JSONObject.fromObject(body);

		// 判断返回内容是否为空，或内容中是否包含错误代码
		if (!(WechatRespondErrCodeEnum.发送成功 == MPManager.checkErrcode(json))) {
			// 抛出获取OpenId失败
			throw new WxGetOpenidErrorException(ServiceName.WechatGateway);
		}

		WechatWebpageAccessTokenResp accessTokenResp = DataUtil.jsonToWechatWebpageAccessTokenResp(body);
		if (accessTokenResp == null) {
			// 抛出获取OpenId失败
			throw new WxGetOpenidErrorException(ServiceName.WechatGateway);
		}

		return accessTokenResp;
	}



	/**
	 * 获取JSSDK签名
	 * 
	 * @param appType 应用类型
	 * @param url 当前网页的URL
	 * @return JSSDK签名信息
	 */
	@Override
	public JSSDKSignatureInfo getJSSDKSignature(AppType appType, String url) {
		if (appType == null) {
			// 抛出应用类型为空的异常
			throw new WxAppTypeNullException(ServiceName.WechatGateway);
		}

		WechatServiceNoInfo serviceNoInfo = MPManager.getWechatServiceNoInfoByApptype(appType);
		if (serviceNoInfo == null) {
			// 抛出该应用类型的公众号信息没有配置异常
			throw new WxAppTypeNotConfiguredWechatServiceNoException(ServiceName.WechatGateway);
		}
		JSSDKSignatureInfo signatureInfo = new JSSDKSignatureInfo();
		signatureInfo.setAppId(serviceNoInfo.getAppid());
		String jsapi_ticket = this.getJSApiTicketByAppType(appType);
		// 获取一个16位的随机数
		String noncestr = SignatureUtil.getCharAndNumr(16);
		long timestamp = System.currentTimeMillis() / 1000;
		String jsSDKSignature = SignatureUtil.generateJSSDKSignature(jsapi_ticket, url, noncestr, timestamp);
		signatureInfo.setNonceStr(noncestr);
		signatureInfo.setSignature(jsSDKSignature);
		signatureInfo.setTimestamp(timestamp);

		return signatureInfo;
	}

	/**
	 * @param appType 应用类型
	 * @return boolean
	 */
	@Override
	public boolean checkAccessToken(AppType appType, String accessToken) {

		String key = String.format(WechatGratewayKey.CHECKTOKEN_TIMES, appType.code());
		RedisString redis = new RedisString(key);
		if (redis.exists()) {
			logger.warn("checkAccessToken超频访问");
			Boolean r = false;
			try {
				r = Boolean.valueOf(redis.get());
			} catch (LifesenseBaseException e) {
				logger.error("checkAccessToken对象转换异常：", e);
			}
			return r;
		}

		// 通过HTTP到微信服务器请求微信用户信息

		String url = MPManager.getMPUrl().getGetcallbackip();
		String body = HttpClientManager.get(new RequestHeader(), MessageFormat.format(url, accessToken));
		JSONObject json = JSONObject.fromObject(body);
		WechatRespondErrCodeEnum returnCode = MPManager.checkErrcode(json);
		if (returnCode.equals(WechatRespondErrCodeEnum.AcessToken超时)) {
			logger.warn("checkAccessTokenBody_timeOut " + body + ",appType:" + appType.code());
			redis.set("true", 10);// 缓存10秒
			return true;
		} else if (returnCode.equals(WechatRespondErrCodeEnum.接口调用超过限制)) {
			logger.warn("checkAccessTokenBody_limit_out " + body + ",appType:" + appType.code());
			redis.set("false", 10);// 缓存10秒
			return false;
		} else {
			if (logger.isDebugEnabled())
				logger.info("checkAccessTokenBody " + body + ",appType:" + appType.code());
			redis.set("false", 10);// 缓存10秒
			return false;
		}
	}

	/***********************************************
	 * 向微信公众号请求信息接口 start
	 *************************************************************/

	/***********************************************
	 * 向微信公众号发送信息接口 start
	 *************************************************************/

	/**
	 * 发送微信客服消息
	 * 
	 * @param appType 应用类型
	 * @param customServiceMessage 微信客服消息
	 * @return 发送是否成功标志
	 */
	@Override
	public boolean sendCustomServiceMessage(AppType appType, WechatCustomServiceMessage customServiceMessage) {
		String wechatServiceNo = MPManager.getWechatServiceNoInfoByApptype(appType).getServiceNo();

		if (StringUtils.isEmpty(wechatServiceNo) || customServiceMessage == null || StringUtils.isEmpty(customServiceMessage.getMsgtype())) {
			// 抛出公众号为null异常
			throw new WxPublicNoNotExistsException(ServiceName.WechatGateway);
		}

		String jsonMessage = "";

		// 根据消息类型获取消息发送的链接
		String apiUrl = DataUtil.getApiUrl(customServiceMessage.getMsgtype());

		// 获取下消息类型枚举
		WechatMessageTypeEnum wechatMessageTypeEnum = WechatMessageTypeEnum.getWechatMessageTypeEnumByValue(customServiceMessage.getMsgtype());
		if (wechatMessageTypeEnum == null) {
			return false;
		}

		switch (wechatMessageTypeEnum) {
		case 文本消息:
			// 强制类型转换为微信文本消息对象
			WechatCustomServiceTextMessage textMessage = (WechatCustomServiceTextMessage) customServiceMessage;
			// 把消息对象转换成Json串
			jsonMessage = GsonUtil.toJson(textMessage);
			if (logger.isDebugEnabled())
				logger.debug("send textMessage:" + jsonMessage + ",apiUrl=" + apiUrl);
			// 发送消息
			return wechatGatewayAdapter.sendMessage(jsonMessage, appType, apiUrl);
		case 语音消息:
			// 强制类型转换为微信语音消息对象
			WechatCustomServiceVoiceMessage voiceMessage = (WechatCustomServiceVoiceMessage) customServiceMessage;
			// 把消息对象转换成Json串
			jsonMessage = GsonUtil.toJson(voiceMessage);
			// 发送消息
			return wechatGatewayAdapter.sendMessage(jsonMessage, appType, apiUrl);
		case 图文消息:
			// 强制类型转换为微信图文消息对象
			WechatCustomServiceNewsMessage newsMessage = (WechatCustomServiceNewsMessage) customServiceMessage;
			// 把消息对象转换成Json串
			if (newsMessage.getNews().getArticles() != null && newsMessage.getNews().getArticles().size() > 0) {
				logger.info("!!!!!!!!!sendCustomServiceNewsMessage:" + "newsMessageObj=" + newsMessage.getNews().getArticles().get(0).getUrl());
			}

			jsonMessage = GsonUtil.toJson(newsMessage);
			if (logger.isDebugEnabled())
				logger.debug("send newsMessage:" + "newsMessage=" + jsonMessage);
			// 发送消息
			return wechatGatewayAdapter.sendMessage(jsonMessage, appType, apiUrl);
		case 图片消息:
			// 强制类型转换为微信图文消息对象
			WechatCustomServiceImageMessage imageMessage = (WechatCustomServiceImageMessage) customServiceMessage;
			// 把消息对象转换成Json串
			jsonMessage = GsonUtil.toJson(imageMessage);
			// 发送消息
			return wechatGatewayAdapter.sendMessage(jsonMessage, appType, apiUrl);
		default:
			if (logger.isDebugEnabled())
				logger.debug("!!!!!!!!!senddefaultMessage:" + "textMessage=" + jsonMessage + ",apiUrl=" + apiUrl);
			return false;
		}
	}

	/**
	 * 根据微信公众号原始Id发送微信客服消息
	 * 
	 * @param wechatServiceNo 微信公众号原始ID
	 * @param customServiceMessage 微信客服消息
	 * @return 发送是否成功标志
	 */
	@Override
	public boolean sendCustomServiceMessageByServiceNo(String wechatServiceNo, WechatCustomServiceMessage customServiceMessage) {
		AppType appType = MPManager.getAppTypeByWechatServiceNo(wechatServiceNo);
		if (appType == null) {
			logger.info("!!!!!!!!!send text message2");
			return false;
		}

		return this.sendCustomServiceMessage(appType, customServiceMessage);
	}

	/**
	 * 根据微信公众号原始ID发送微信模板消息
	 * 
	 * @param wechatServiceNo 微信公众号原始ID
	 * @param templateMessage 模板消息
	 * @return 发送是否成功标志
	 */
	@Override
	public boolean sendTemplateMessageByServiceNo(String wechatServiceNo, WechatTemplateMessage templateMessage) {
		AppType appType = MPManager.getAppTypeByWechatServiceNo(wechatServiceNo);
		if (appType == null) {
			return false;
		}

		return this.sendTemplateMessage(appType, templateMessage);
	}

	/**
	 * 发送微信模板消息
	 * 
	 * @param appType 应用类型
	 * @param templateMessage 模板消息
	 * @return 发送是否成功标志
	 */
	@Override
	public boolean sendTemplateMessage(AppType appType, WechatTemplateMessage templateMessage) {
		String wechatServiceNo = MPManager.getWechatServiceNoInfoByApptype(appType).getServiceNo();
		if (templateMessage == null || StringUtils.isEmpty(wechatServiceNo)) {
			// 抛出公众号为空异常
			throw new WxPublicNoNotExistsException(ServiceName.WechatGateway);
		}

		// 把模板消息对象转换成JSON字符串
		String jsonMessage = GsonUtil.toJson(templateMessage);
		// 根据消息类型获取发送链接
		String apiUrl = DataUtil.getApiUrl(WechatMessageTypeEnum.模板消息.toString());

		// 发送模板消息
		return wechatGatewayAdapter.sendMessage(jsonMessage, appType, apiUrl);
	}

	/**
	 * 上传步数到微信排行榜
	 * 
	 * @param appType 应用类型
	 * @param openId 微信用户openId
	 * @param step 适时步数
	 */
	@Override
	public void setStepForWechatRank(AppType appType, String openId, int step) {
		if (logger.isDebugEnabled()) {
			logger.debug("wechatgateway_begin_to_set_step_to_wechat _for_{}_{}", openId, step);
		}
		if (DateUtils.getDiffSeconds(lastDate, new Date()) > 60) {
			lastDate = new Date();
			RedisString redis = new RedisString("is_async_rank");
			boolean isExist = redis.exists();
			if (isExist) {
				isAsyncRank = redis.get();
			}
		}

		if (StringUtils.isBlank(isAsyncRank)) {
			isAsyncRank = "true";
		}
		if ("true".equals(isAsyncRank)) {
			taskExecutor.execute(() -> wechatGatewayAdapter.processRank(appType, openId, step));
		} else {
			wechatGatewayAdapter.processRank(appType, openId, step);
		}
	}



	/***********************************************
	 * 向微信公众号发送信息接口 end
	 *************************************************************/

	/*************************************************
	 * 设备相关接口 start
	 *****************************************************************/

	/**
	 * 发送设备消息(包括设备内容消息、wifi设备连接状态消息)
	 * 
	 * @param appType 应用类型
	 * @param deviceStatusMessage 设备消息
	 * @return 发送是否成功标志
	 */
	@Override
	public boolean sendWechatDeviceStatusMessage(AppType appType, WechatDeviceStatusMessage deviceStatusMessage) {
		return wechatGatewayAdapter.sendWechatDeviceMessage(appType, deviceStatusMessage);
	}

	/**
	 * 主动发消息给设备
	 * 
	 * @param appType 应用类型
	 * @param deviceMessage 设备状态消息
	 * @return 发送是否成功标志
	 */
	@Override
	public boolean sendWechatDeviceContentMessage(AppType appType, WechatDeviveContentMessage deviceContentMessage) {

		if (deviceContentMessage != null) {
			deviceContentMessage.setDevice_type(MPManager.getWechatServiceNoInfoByApptype(appType).getServiceNo());
		}
		return wechatGatewayAdapter.sendWechatDeviceMessage(appType, deviceContentMessage);
	}


	/**
	 * 强制绑定设备
	 * 
	 * @param appType 应用类型
	 * @param deviceEventMessage 设备事件消息
	 * @return 发送是否成功标志
	 */
	@Override
	public boolean compelBindWechatDevice(AppType appType, WechatDeviceEventMessage deviceEventMessage) {
		String wechatServiceNo = MPManager.getWechatServiceNoInfoByApptype(appType).getServiceNo();

		if (deviceEventMessage == null || StringUtils.isEmpty(wechatServiceNo)) {
			// 抛出公众号不能为空的异常
			throw new WxPublicNoNotExistsException(ServiceName.WechatGateway);
		}

		// 把设备事件消息转换成JSON串
		String jsonMessage = GsonUtil.toJson(deviceEventMessage);
		// 根据消息类型获取消息发送链接
		String apiUrl = DataUtil.getApiUrl(WechatEventTypeEnum.强制绑定设备事件.toString());

		return wechatGatewayAdapter.sendMessage(jsonMessage, appType, apiUrl);
	}

	/**
	 * 强制解绑设备
	 * 
	 * @param appType 应用类型
	 * @param deviceEventMessage 设备事件消息
	 * @return 发送是否成功标志
	 */
	@Override
	public boolean compelUnbindWechatDevice(AppType appType, WechatDeviceEventMessage deviceEventMessage) {
		String wechatServiceNo = MPManager.getWechatServiceNoInfoByApptype(appType).getServiceNo();
		if (deviceEventMessage == null || StringUtils.isEmpty(wechatServiceNo)) {
			// 抛出公众号不能为空的异常
			throw new WxPublicNoNotExistsException(ServiceName.WechatGateway);
		}

		// 把设备事件消息转换成JSON串
		String jsonMessage = GsonUtil.toJson(deviceEventMessage);
		// 根据消息类型获取消息发送链接
		String apiUrl = DataUtil.getApiUrl(WechatEventTypeEnum.强制解绑设备事件.toString());

		return wechatGatewayAdapter.sendMessage(jsonMessage, appType, apiUrl);
	}

	/**
	 * 绑定设备
	 * 
	 * @param appType 应用类型
	 * @param deviceBindEventMessage 设备绑定事件消息
	 * @return 发送是否成功标志
	 */
	@Override
	public boolean bindWechatDevice(AppType appType, WechatBindDeviceEventMessage deviceBindEventMessage) {
		String wechatServiceNo = MPManager.getWechatServiceNoInfoByApptype(appType).getServiceNo();
		if (deviceBindEventMessage == null || StringUtils.isEmpty(wechatServiceNo)) {
			// 抛出公众号不能为空的异常
			throw new WxPublicNoNotExistsException(ServiceName.WechatGateway);
		}

		// 把设备事件消息转换成JSON串
		String jsonMessage = GsonUtil.toJson(deviceBindEventMessage);
		// 根据消息类型获取消息发送链接
		String apiUrl = DataUtil.getApiUrl(WechatEventTypeEnum.绑定设备事件.toString());

		return wechatGatewayAdapter.sendMessage(jsonMessage, appType, apiUrl);
	}

	/**
	 * 解绑设备
	 * 
	 * @param appType 应用类型
	 * @param deviceBindEventMessage 设备绑定事件消息
	 * @return 发送是否成功标志
	 */
	@Override
	public boolean unbindWechatDevice(AppType appType, WechatBindDeviceEventMessage deviceBindEventMessage) {
		if (deviceBindEventMessage == null || appType == null) {
			// 抛出公众号不能为空的异常
			throw new WxPublicNoNotExistsException(ServiceName.WechatGateway);
		}

		// 把设备事件消息转换成JSON串
		String jsonMessage = GsonUtil.toJson(deviceBindEventMessage);
		// 根据消息类型获取消息发送链接
		String apiUrl = DataUtil.getApiUrl(WechatEventTypeEnum.解绑设备事件.toString());
		boolean isunbind = wechatGatewayAdapter.sendMessage(jsonMessage, appType, apiUrl);
		return isunbind;
	}

	/**
	 * 根据设备ID获取微信设备二维码（建议每次获取数量不超过5个）
	 * 
	 * @param appType 应用类型
	 * @param deviceQrcodeParam 获取微信设备二维码参数
	 * @return 微信设备二维码列表
	 */
	@Override
	public List<WechatDeviceQrcode> createDeviceQrcode(AppType appType, WechatDeviceQrcodeParam deviceQrcodeParam) {

		// 调用接口获取access_token
		String accessToken = this.getAccessTokenForRedis(appType);

		// 通过HTTP到微信服务器请求设备二维码
		String jsonMessage = GsonUtil.toJson(deviceQrcodeParam);
		String body = HttpClientManager.postJson(null, MessageFormat.format(MPManager.getMPUrl().getCreateOrcodeUrl(), accessToken), jsonMessage);

		JSONObject json = JSONObject.fromObject(body);
		if (logger.isDebugEnabled())
			logger.debug("!!!!!!!!!!!!getWechatUser:" + body);

		// 判断返回内容是否为空，或内容中是否包含错误代码
		if (StringUtils.isEmpty(body) || !(WechatRespondErrCodeEnum.发送成功 == MPManager.checkErrcode(json))) {
			// 抛出获取设备二维码异常
			throw new WxGetDeviceQrcodeErrorException(ServiceName.WechatGateway);
		}

		return DataUtil.jsonToWecharDeviceQrcode(body);
	}

	@Override
	public WechatDeviceIdAndQrcode getWechatDeviceIdAndQrcode(AppType appType) {
		RequestHeader requestHeader = new RequestHeader();
		// 调用接口获取access_token
		String accessToken = this.getAccessTokenForRedis(appType);

		// 通过HTTP到微信服务器请求设备二维码
		String body = HttpClientManager.get(requestHeader, MessageFormat.format(MPManager.getMPUrl().getCreateOrcodeUrl2(), accessToken));
		WechatIOTQrcodeResp resp = GsonUtil.toBean(body, WechatIOTQrcodeResp.class);
		if (resp == null || !(WechatRespondErrCodeEnum.发送成功 == resp.checkErrorCode())) {
			// 抛出获取设备二维码异常
			throw new WxGetDeviceQrcodeErrorException(ServiceName.WechatGateway);
		}
		return resp.lifesenseResponse();
	}



	/**
	 * 设备授权
	 * 
	 * @param appType 应用类型
	 * @param authorizeDevice 需要授权的设备资料
	 * @return 授权成功的设备列表
	 */
	@Override
	public List<WechatAuthorizeDeviceSuccessInfo> authorizeDevice(AppType appType, WechatAuthorizeDevice authorizeDevice) {
		// 调用接口获取access_token
		String accessToken = this.getAccessTokenForRedis(appType);

		// 通过HTTP到微信服务器请求设备授权
		String jsonMessage = GsonUtil.toJson(authorizeDevice);
		String body = HttpClientManager.postJson(null, MessageFormat.format(MPManager.getMPUrl().getAuthorizeDeviceUrl(), accessToken), jsonMessage);

		JSONObject json = JSONObject.fromObject(body);
		if (logger.isDebugEnabled())
			logger.debug("!!!!!!!!!!!!getWechatUser:" + body);

		// 判断返回内容是否为空，或内容中是否包含错误代码
		if (StringUtils.isEmpty(body) || !(WechatRespondErrCodeEnum.发送成功 == MPManager.checkErrcode(json))) {
			// 抛出获取设备授权信息异常
			throw new WxGetDeviceAuthorizeErrorException(ServiceName.WechatGateway);
		}

		return DataUtil.jsonToWechatAuthorizeDevice(body);
	}



	/**
	 * 根据微信公众号，设备ID获取设备状态信息
	 * 
	 * @param appType 应用类型
	 * @param device_id 设备ID
	 * @return 设状态信息
	 */
	@Override
	public WechatDeviceStatusResp getWechatDeviceStatus(AppType appType, String device_id) {
		// 通过HTTP到微信服务器请求微信用户信息
		String accessToken = this.getAccessTokenForRedis(appType);
		String body = HttpClientManager.get(null, MessageFormat.format(MPManager.getMPUrl().getGetUserInfoUrl(), accessToken, device_id));
		JSONObject json = JSONObject.fromObject(body);
		if (logger.isDebugEnabled())
			logger.debug("!!!!!!!!!!!!getWechatUser:" + body);

		// 判断返回内容是否为空，或内容中是否包含错误代码
		if (StringUtils.isEmpty(body) || !(WechatRespondErrCodeEnum.发送成功 == MPManager.checkErrcode(json))) {
			throw new WxUserInfoFetchException(ServiceName.WechatGateway);
		}

		return DataUtil.jsonToWechatDeviceStatus(body);
	}



	/**
	 * 验证设备二维码
	 * 
	 * @param appType 应用类型
	 * @param authorizeDevice 验证设备二维码参数
	 * @return 验证设备二维码响应
	 */
	@Override
	public VerifyQrcodeResp verifyQrcode(AppType appType, VerifyQrcodeParam verifyQrcodeParam) {
		// 调用接口获取access_token
		String accessToken = this.getAccessTokenForRedis(appType);

		// 通过HTTP到微信服务器请验证设备二维码
		String jsonMessage = GsonUtil.toJson(verifyQrcodeParam);
		String body = HttpClientManager.postJson(null, MessageFormat.format(MPManager.getMPUrl().getVerifyQrcode(), accessToken), jsonMessage);

		JSONObject json = JSONObject.fromObject(body);

		// 判断返回内容是否为空，或内容中是否包含错误代码
		if (StringUtils.isEmpty(body) || !(WechatRespondErrCodeEnum.发送成功 == MPManager.checkErrcode(json))) {
			logger.error("verifyQrcodeResp:{}", body);
			// 抛出验证设备二维码异常
			throw new WxVerifyQrcodeErrorException(ServiceName.WechatGateway);
		}

		return DataUtil.jsonToVerifyQrcodeResp(body);
	}



	/*************************************************
	 * 设备相关接口 end
	 *****************************************************/

	/************************************************
	 * 媒体相关文件接口 start
	 *****************************************************/
	/**
	 * 上传文件，并返回MEDIA_ID
	 * 
	 * @return MEDIA_ID
	 * 
	 * @throws UnsupportedEncodingException
	 */
	@Override
	public String uploadWechatMediaFile(AppType appType, String fullFilePath, WechatMediaFileTypeEnum fileType) throws IOException {
		ServiceAssert.notEmpty(ServiceName.WechatGateway, "fullFilePath", fullFilePath);

		ServiceAssert.notNull(ServiceName.WechatGateway, "fileType", fileType);

		String jsonStr = wechatGatewayAdapter.uploadWechatFile(appType, fullFilePath, fileType, 1, null);
		if (StringUtils.isBlank(jsonStr)) {
			return null;
		}
		JSONObject json = JSONObject.fromObject(jsonStr);
		if (json.containsKey(Constants.MEDIA_ID)) {
			if (logger.isDebugEnabled())
				logger.debug("MEDIA_ID=" + json.getString(Constants.MEDIA_ID));
			return json.getString(Constants.MEDIA_ID);
		} else {

			return jsonStr;
		}

	}

	/**
	 * 上传文件，并返回MEDIA_ID,带文件后缀
	 * 
	 * @return MEDIA_ID
	 * 
	 * @throws UnsupportedEncodingException
	 */
	@Override
	public String uploadWechatMediaFile(AppType appType, String fullFilePath, WechatMediaFileTypeEnum fileType, FileExtensionEnum fileExtensionEnum)
			throws IOException {
		ServiceAssert.notEmpty(ServiceName.WechatGateway, "fullFilePath", fullFilePath);

		ServiceAssert.notNull(ServiceName.WechatGateway, "fileType", fileType);

		// 第一次上传
		String jsonStr = wechatGatewayAdapter.uploadWechatFile(appType, fullFilePath, fileType, 1, fileExtensionEnum);
		JSONObject json = JSONObject.fromObject(jsonStr);
		if (json.containsKey(Constants.MEDIA_ID)) {
			if (logger.isDebugEnabled())
				logger.debug("MEDIA_ID=" + json.getString(Constants.MEDIA_ID));
			return json.getString(Constants.MEDIA_ID);
		} else {

			return jsonStr;
		}
	}


	/**
	 * 从微信服务器下载文件
	 * 
	 * @param mediaId 媒体ID
	 * @return 文件流
	 */
	@Override
	public String downloadWechatMediaFile(AppType appType, String mediaId) throws IOException {
		String accessToken = this.getAccessTokenForRedis(appType);
		String url = MessageFormat.format(MPManager.getMPUrl().getGetDownloadUrl(), accessToken, mediaId);
		String mediaStr = DataUtil.getMediaStream(url);
		return mediaStr;
	}

	

	/**
	 * 从微信服务器下载文件
	 * 
	 * @param mediaId 媒体ID
	 * @return 字节流
	 */
	@Override
	public byte[] downloadWechatMediaFileForByte(AppType appType, String mediaId) throws IOException {
		String accessToken = this.getAccessTokenForRedis(appType);
		String url = MessageFormat.format(MPManager.getMPUrl().getGetDownloadUrl(), accessToken, mediaId);
		/*
		 * 如果下载的附件为空，则再重复下载3次
		 */
		int downloadCount = 0;
		byte[] result = null;
		while (downloadCount < 4) {
			logger.info("downloadWechatMediaFileForByte_mediaId_" + mediaId + "_downloadCount_" + downloadCount);
			downloadCount++;
			result = DataUtil.getMediaStreamForByte(url);
			if (result != null && result.length > 1) {
				return result;
			}
			logger.info("downloadWechatMediaFileForByte_mediaId_" + mediaId + "_downloadCount_" + downloadCount + "_resul is null");
		}
		return result;
	}



	/**
	 * 根据文件serverId从微信下载文件
	 * 
	 * @param serverId
	 * @return 文件流字符串
	 * @throws IOException
	 */
	@Override
	public String downloadWechatMediaFileByServerId(AppType appType, String serverId) throws IOException {
		String accessToken = this.getAccessTokenForRedis(appType);
		String url = MessageFormat.format(MPManager.getMPUrl().getGetDownloadUrl(), accessToken, serverId);
		String mediaStr = DataUtil.getMediaStream(url);
		return mediaStr;
	}

	/************************************************
	 * 媒体相关文件接口 end
	 *****************************************************/

	

	

	/**
	 * 补发文本、图文、模板等消息
	 */
	@Override
	public void reissueMessage() {
		RequestHeader requestHeader = new RequestHeader();
		// 从缓存中获取未发送成功消息的队列
		RedisObject redisObject = new RedisObject(WechatGratewayKey.REDIS_NOT_SEND_MESSAGE_QUEUE);
		NotSendMessageQueue<NotSendMessage> nsmq = redisObject.get();

		if (null == nsmq || nsmq.empty() || nsmq.size() == 0) {
			// 测试定时器
			if (logger.isDebugEnabled())
				logger.debug("!!!!!!!REDIS_NOT_SEND_MESSAGE_QUEUE:" + System.currentTimeMillis());
			return;
		}

		NotSendMessageQueue<NotSendMessage> nsmq2 = new NotSendMessageQueue<>();
		for (int i = 0; i < nsmq.size(); i = 0) {
			NotSendMessage sm = nsmq.remove();

			// 最多补发5次
			if (sm.getReissueCount() < 6) {
				// 更新token
				String accessToken = this.getAccessTokenForRedis(sm.getAppType());
				// 发送消息
				String body = HttpClientManager.postJson(requestHeader, MessageFormat.format(sm.getSendUrl(), accessToken), sm.getJsonMessage());
				if (logger.isDebugEnabled())
					logger.debug("!!!!!!!resend message:" + body);

				JSONObject json = JSONObject.fromObject(body);

				// 从返回代码判断是否发送成功,发送失败重新插入队列并存入缓存
				if (!(WechatRespondErrCodeEnum.发送成功 == MPManager.checkErrcode(json))) {
					int reissueCount = sm.getReissueCount();
					// 已补发次数增加一次
					sm.setReissueCount(reissueCount + 1);
					// 把发送失败的消息插入新队列的队尾
					nsmq2.offer(sm);
				}
			}
		}

		// 存入缓存 长期有效
		redisObject.set(nsmq2, -1);
	}

	// 清理微信服务相关的缓存
	public void removRedisForMP() {
		MPManager.removRedisForMP();
	}

	/**
	 * 
	 * 根据openid获取用户的设备列表
	 * 
	 * @param accessToken
	 * @param openId
	 * @return
	 */
	@Override
	public WeChatIotDeviceResponse getBindDeviceList(String accessToken, String openId) {
		RequestHeader requestHeader = new RequestHeader();
		if (StringUtils.isEmpty(accessToken)) {
			throw new ParamsIsNullException(ServiceName.WechatGateway, "accessToken");
		}

		if (StringUtils.isEmpty(openId)) {
			throw new ParamsIsNullException(ServiceName.WechatGateway, "openId");
		}

		String body = HttpClientManager.get(requestHeader, MessageFormat.format(MPManager.getMPUrl().getBindedDevice, accessToken, openId));

		// 判断返回内容是否为空，或内容中是否包含错误代码
		if (StringUtils.isEmpty(body)) {
			throw new WsSetStepErrorException(ServiceName.WechatGateway);
		}

		JSONObject json = JSONObject.fromObject(body);
		logger.info("!!!!!!!!!!!!getBindDeviceList:" + body);

		// 判断返回内容是否为空，或内容中是否包含错误代码
		if (!(WechatRespondErrCodeEnum.发送成功 == MPManager.checkErrcode(json))) {
			throw new WsSetStepErrorException(ServiceName.WechatGateway);
		}

		WeChatIotDeviceResponse accessTokenResp = DataUtil.jsonToWeChatIotDeviceResponse(body);
		if (accessTokenResp == null) {
			throw new WsSetStepErrorException(ServiceName.WechatGateway);
		}

		return accessTokenResp;
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
			RedisString redisString = new RedisString("echo");
			redisString.set("ok");
			result.put("redis", redisString.get());
		} catch (Exception e) {
			result.put("redis", e.getMessage());
		}

		try {
			InstanceFactory.getInstance(ParamQrcodeEntityMapper.class).findByBusinessTypeAndWechatServiceNo("", "");
			result.put("mysql", "ok");
		} catch (Exception e) {
			result.put("mysql", e.getMessage());
		}

		result.put("time", System.currentTimeMillis() - start);
		return result;
	}

	@Override
	public ParamQrcodeResponse getParamQrcodeUrl(ParamQrcodeParam param) {
		ParamQrcodeResponse paramQrcodeResponse = wechatGatewayAdapter.getQrSceneParamQrcodeUrl(param);
		return paramQrcodeResponse;
	}

}
