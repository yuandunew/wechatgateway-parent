package com.yuandu.wechatgateway.service;

import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifesense.base.beans.param.RequestHeader;
import com.lifesense.base.cache.command.RedisObject;
import com.lifesense.base.cache.command.RedisString;
import com.lifesense.base.constant.AppType;
import com.lifesense.base.constant.KafkaTopic;
import com.lifesense.base.constant.ServiceName;
import com.lifesense.base.exception.LifesenseBaseException;
import com.lifesense.base.exception.wechat.WsSetStepErrorException;
import com.lifesense.base.exception.wechat.WxAccessTokenTimeoutException;
import com.lifesense.base.exception.wechat.WxAppTypeNotConfiguredWechatServiceNoException;
import com.lifesense.base.exception.wechat.WxAppTypeNullException;
import com.lifesense.base.exception.wechat.WxGetParamQrcodeErrorException;
import com.lifesense.base.exception.wechat.WxPublicNoNotExistsException;
import com.lifesense.base.exception.wechat.WxReplyNullException;
import com.lifesense.base.exception.wechat.WxSendMessageUnkonwnErrorException;
import com.lifesense.base.exception.wechat.WxUserInfoFetchException;
import com.lifesense.base.utils.ResourceUtils;
import com.lifesense.base.utils.SystemUtils;
import com.lifesense.base.utils.UUIDUtils;
import com.lifesense.base.utils.code.LifesenseCheckSum;
import com.lifesense.base.utils.http.HttpUtils;
import com.lifesense.base.utils.json.JsonUtils;
import com.lifesense.base.utils.okhttp.FileExtensionEnum;
import com.lifesense.base.utils.okhttp.HttpClientManager;
import com.lifesense.kafka.message.ObjectMessage;
import com.lifesense.kafka.spring.TopicPublisherBean;
import com.lifesense.log.core.LogConstant;
import com.lifesense.soa.wechatgateway.dao.entity.ParamQrcodeEntity;
import com.lifesense.soa.wechatgateway.dao.mapper.ParamQrcodeEntityMapper;
import com.lifesense.soa.wechatgateway.dto.enums.ParamQrcodeActionTypeEnum;
import com.lifesense.soa.wechatgateway.dto.enums.WechatMediaFileTypeEnum;
import com.lifesense.soa.wechatgateway.dto.enums.WechatMessageTypeEnum;
import com.lifesense.soa.wechatgateway.dto.enums.WechatRespondErrCodeEnum;
import com.lifesense.soa.wechatgateway.dto.receive.ParamQrcodeResponse;
import com.lifesense.soa.wechatgateway.dto.receive.WechatUser;
import com.lifesense.soa.wechatgateway.dto.send.ParamQrcodeParam;
import com.lifesense.soa.wechatgateway.dto.send.WechatDeviceMessage;
import com.lifesense.soa.wechatgateway.dto.send.WechatServiceNoInfo;
import com.lifesense.soa.wechatgateway.service.constants.WechatGratewayKey;
import com.lifesense.soa.wechatgateway.service.dto.AccessTokenDto;
import com.lifesense.soa.wechatgateway.service.dto.MPUrl;
import com.lifesense.soa.wechatgateway.service.dto.NotSendMessage;
import com.lifesense.soa.wechatgateway.service.dto.NotSendMessageQueue;
import com.lifesense.soa.wechatgateway.service.dto.TicketDto;
import com.lifesense.soa.wechatgateway.service.utils.DataUtil;
import com.lifesense.soa.wechatgateway.service.utils.LogUtils;
import com.lifesense.soa.wechatgateway.service.utils.MPManager;
import com.lifesense.soa.wechatgateway.service.utils.ThreadPoolUtils;
import com.lifesense.soa.wechatgateway.utils.AppTypeAdapter;
import com.lifesense.soa.wechatgateway.utils.Constants;
import com.lifesense.soa.wechatgateway.utils.GsonUtil;
import com.lifesense.soa.wechatgateway.utils.StringUtilByWechatGateway;

import net.sf.json.JSONObject;

/**
 * 
 * WechatGatewayServiceV2 处理太多业务，短时间内没办法拆开，这里分担部分业务处理<br/>
 * 1、分担WechatGatewayServiceV2内部被重复调用的方法<br/>
 * 2、分担跟微信打交道的方法，将微信返回对象组装为业务对象<br/>
 * 3、accessToken处理<br/>
 * Created by lizhifeng on 2017年7月5日.
 *
 */
@Service
public class WechatGatewayAdapter {
	private static final Logger logger = LoggerFactory.getLogger(WechatGatewayAdapter.class);
	@Autowired
	TaskExecutor taskExecutor;

	@Autowired
	private ParamQrcodeEntityMapper paramQrcodeEntityMapper;
	@Autowired
	protected TopicPublisherBean topicPublishBean;
	
	@SuppressWarnings("deprecation")
	public  void  publicTopic(String requestId, KafkaTopic kafaTopic, Serializable serializable) {
		topicPublishBean.publish(kafaTopic.code(), new ObjectMessage(requestId, serializable));
	}
	
	public String getJSApiTicketByAppType(AppType appType) {
		String wechatServiceNo = MPManager.getWechatServiceNoInfoByApptype(appType).getServiceNo();
		if (StringUtils.isEmpty(wechatServiceNo)) {
			// 抛出公众号不能为空的异常
			throw new WxPublicNoNotExistsException(ServiceName.WechatGateway);
		}

		// 从缓存中获取TICKET对象
		TicketDto ticketDto = new RedisObject(DataUtil.getRedisTicketKey(wechatServiceNo)).get();

		if (ticketDto == null) {
			// 更新jsApiTicket
			String jsapiTicket = updateJsApiTicket(appType);
			logger.info("!!!get jsapiTicket by wechatserver= " + jsapiTicket);
			return jsapiTicket;
		} else {

			if (DataUtil.determineProcessSlaveType()) {
				String accessToken = this.getAccessTokenForRedis(appType);
				logger.info("!!!get accessToken by wechatserver = " + accessToken);
				String jsapiTicket = ticketDto.getJsapiTicket();
				logger.info("!!!get jsapiTicket by redis= " + jsapiTicket);

				return jsapiTicket;
			} else {
				// 获取ticket对象的时间戳
				Long timestamp = ticketDto.getTimestamp();
				// 计算ticket的剩余有效时间，单位：秒
				Long diff = (System.currentTimeMillis() - timestamp) / 1000;

				// 检查jsApiTicket是否过期
				if (diff > ticketDto.getExpiresIn() - 2500) {
					// 更新jsApiTicket
					String jsapiTicket = updateJsApiTicket(appType);
					logger.info("!!!get jsapiTicket by wechatserver= " + jsapiTicket);
					return jsapiTicket;
				} else {
					String accessToken = this.getAccessTokenForRedis(appType);
					logger.info("!!!get accessToken by wechatserver = " + accessToken);
					String jsapiTicket = ticketDto.getJsapiTicket();
					logger.info("!!!get jsapiTicket by redis= " + jsapiTicket);

					return jsapiTicket;
				}
			}
		}
	}
	
	/**
	 * 根据AppType从缓存中获取微信AccessToken
	 * 
	 * @param appType 应用类型
	 * @return accessToken
	 */
	public String getAccessTokenForRedis(AppType appType) {
		String wechatServiceNo = MPManager.getWechatServiceNoInfoByApptype(appType).getServiceNo();

		if (StringUtils.isEmpty(wechatServiceNo)) {
			throw new WxAppTypeNotConfiguredWechatServiceNoException(ServiceName.WechatGateway);
		}
		// 从缓存中获取AccessToken对象
		AccessTokenDto tokenDto = new RedisObject(DataUtil.getRedisAccessTokenKey(wechatServiceNo)).get();

		if (tokenDto == null) {
			// 返回accessToken为空串
			return this.getAccessTokenByAppType(appType);
		}

		String accessToken = tokenDto.getAccessToken();

		return accessToken;
	}

	/**
	 * 根据AppType获取微信AccessToken
	 * 
	 * @param appType 应用类型
	 * @return accessToken
	 */
	public String getAccessTokenByAppType(AppType appType) {
		if (!MPManager.TokenSwitch) {
			logger.debug("getAccessTokenByURL");
			return getAccessTokenByURL(appType);
		}

		String wechatServiceNo = MPManager.getWechatServiceNoInfoByApptype(appType).getServiceNo();
		String accessToken = "";

		if (StringUtils.isEmpty(wechatServiceNo)) {
			throw new WxAppTypeNotConfiguredWechatServiceNoException(ServiceName.WechatGateway);
		}

		// 从缓存中获取AccessToken对象
		AccessTokenDto tokenDto = new RedisObject(DataUtil.getRedisAccessTokenKey(wechatServiceNo)).get();

		if (tokenDto == null) {
			// 更新accessToken
			accessToken = DataUtil.updateAccessToken(appType);

			logger.info("updateAccessToken_step1 " + wechatServiceNo);
			// 同时刷新JSAPITicket
			this.updateJsApiTicket(appType);

			return accessToken;
		}

		// 从微信服务器判断AccessToken是否失效
		if (this.checkAccessToken(appType, tokenDto.getAccessToken())) {

			logger.info("updateAccessToken_step2 " + wechatServiceNo);
			accessToken = DataUtil.updateAccessToken(appType);

			// 同时刷新JSAPITicket
			this.updateJsApiTicket(appType);

			return accessToken;
		}

		// 获取AccessToken时间戳
		Long timestamp = tokenDto.getTimestamp() / 1000;

		// 计算当前时间与AccessToken时间戳直接的时间（单位为：秒）
		Long currentTs = System.currentTimeMillis() / 1000;

		// Long diff = (currentTs - timestamp)/1000;

		// 检查AccessToken是否过期，提前3600秒去更新AccessToken
		if (currentTs - timestamp > 3600) {
			logger.info("updateAccessToken_step3 " + wechatServiceNo + ">currentTs:" + currentTs + ",timestamp:" + timestamp);
			// 更新accessToken
			accessToken = DataUtil.updateAccessToken(appType);

			// 同时刷新JSAPITicket
			this.updateJsApiTicket(appType);

			return accessToken;
		} else {
			accessToken = tokenDto.getAccessToken();
			logger.info("get_accessToken_by_redis " + wechatServiceNo + ">" + accessToken);
			return accessToken;
		}
	}

	public String getAccessTokenByURL(AppType appType) {
		String wechatServiceNo = MPManager.getWechatServiceNoInfoByApptype(appType).getServiceNo();
		String url = MPManager.RefreshTokenUrlMaps.get(appType.code().toString());
		try {
			if (StringUtils.isBlank(url))
				return null;
			String body = HttpUtils.doGet(url, null);
			AccessTokenResult r = JsonUtils.toObject(body, AccessTokenResult.class);

			AccessTokenDto tokenDto = new AccessTokenDto();
			tokenDto.setAccessToken(r.getData().getAccessToken());
			tokenDto.setExpiresIn(1000L);
			tokenDto.setTimestamp(System.currentTimeMillis());

			// 把AccessToken对象存入缓存
			RedisObject redis = new RedisObject(DataUtil.getRedisAccessTokenKey(wechatServiceNo));
			redis.set(tokenDto, tokenDto.getExpiresIn());

			TicketDto ticketDto = new TicketDto();
			ticketDto.setJsapiTicket(r.getData().getTicket());
			ticketDto.setExpiresIn(1000L);
			ticketDto.setTimestamp(System.currentTimeMillis());

			// 把ticket对象存入缓存
			redis = new RedisObject(DataUtil.getRedisTicketKey(wechatServiceNo));
			redis.set(ticketDto, ticketDto.getExpiresIn());
			return r.getData().getAccessToken();
		} catch (IOException e) {
			logger.error(wechatServiceNo + "根据url刷新token和ticket异常:", e);
		}

		return null;
	}
	/**
	 * 更新access_token
	 * 
	 * @param appType 应用类型
	 * @return 最新的jsApiTicket
	 */
	public String updateJsApiTicket(AppType appType) {
		String wechatServiceNo = MPManager.getWechatServiceNoInfoByApptype(appType).getServiceNo();
		if (StringUtils.isEmpty(wechatServiceNo)) {
			// 抛出微信公众号不能为空的异常
			throw new WxPublicNoNotExistsException(ServiceName.WechatGateway);
		}

		TicketDto ticketDto = this.getJsapiTicketForMPByWechatServiceNo(1, appType);
		if (ticketDto == null) {
			return null;
		}

		// 在ticket对象设当前时间戳
		ticketDto.setTimestamp(System.currentTimeMillis());

		if (StringUtils.isEmpty(ticketDto.getJsapiTicket())) {
			return null;
		}

		// 把Ticket存入缓存
		RedisObject redis = new RedisObject(DataUtil.getRedisTicketKey(wechatServiceNo));
		redis.set(ticketDto, ticketDto.getExpiresIn());

		// ---end by shijy 临时使用

		if (DataUtil.determineProcessSlaveType()) {
			return ticketDto.getJsapiTicket();
		}

		return ticketDto.getJsapiTicket();
	}
	
	/**
	 * 向微信获取（4-queryTimes）次jsApiTicket
	 * 
	 * @param queryTimes 获取次数
	 * @return
	 */
	private TicketDto getJsapiTicketForMPByWechatServiceNo(Integer queryTimes, AppType appType) {

		TicketDto ticketDto = null;

		if (DataUtil.determineProcessSlaveType()) {

			logger.info("fetch_ticket_by_getJsTiketByOtherWechatGateWay_" + appType);
			ticketDto = getJsTiketByOtherWechatGateWay(queryTimes, appType);
		} else {
			logger.info("fetch_ticket_by_getJsTiketByWechatInterface_" + appType);
			ticketDto = getJsTiketByWechatInterface(queryTimes, appType);
		}

		return ticketDto;
	}

	protected TicketDto getJsTiketByOtherWechatGateWay(Integer queryTimes, AppType appType) {

		// 如果查询3次都失败就不查询了
		if (queryTimes > 3) {
			return null;
		}

		WechatServiceNoInfo wechatServiceNoInfo = MPManager.getWechatServiceNoInfoByApptype(appType);
		MPUrl mpUrl = MPManager.getMPUrl();
		if (wechatServiceNoInfo == null || mpUrl == null) {
			// 抛出微信公众号不能为空的异常
			throw new WxPublicNoNotExistsException(ServiceName.WechatGateway);
		}

		// 调用接口获取access_token
		String accessToken = this.getAccessTokenForRedis(appType);
		logger.info("get_accessToken_by_wechatserver_apptype_" + appType + "_" + accessToken);

		String prequeryTokenUrl = ResourceUtils.get("query_token_url");
		long timestamp = new Date().getTime();
		StringBuilder queryTokenUrl = new StringBuilder(prequeryTokenUrl + "?app_type_code=" + appType.code() + "&requestId=" + UUIDUtils.uuid());
		queryTokenUrl.append("&timestamp=").append(timestamp).append("&checksum=").append(LifesenseCheckSum.create().checksum(timestamp+""));
		
		
		logger.info("queryTokenUrl " + queryTokenUrl);
		// 调用接口获取access_token
		String body = HttpClientManager.get(null, queryTokenUrl.toString());

		logger.info("!!!get jsApiTicket body = " + body);

		if (StringUtils.isEmpty(body)) {
			// 获取ticket失败，递归获取
			queryTimes++;
			return getJsapiTicketForMPByWechatServiceNo(queryTimes, appType);
		}

		JSONObject json = JSONObject.fromObject(body);

		String ticketData = json.getString(WechatGratewayKey.ACCESS_TOKEN_DATA);

		JSONObject ticketJson = JSONObject.fromObject(ticketData);

		// 获取ticket
		String jsApiTicket = ticketJson.getString(WechatGratewayKey.TICKET);
		// 获取ticket的时效
		Long expiresIn = 11l;

		// 组装Ticket对象
		TicketDto ticketDto = new TicketDto();
		ticketDto.setJsapiTicket(jsApiTicket);
		ticketDto.setExpiresIn(expiresIn);

		return ticketDto;
	}

	protected TicketDto getJsTiketByWechatInterface(Integer queryTimes, AppType appType) {
		// 如果查询3次都失败就不查询了
		if (queryTimes > 3) {
			return null;
		}

		WechatServiceNoInfo wechatServiceNoInfo = MPManager.getWechatServiceNoInfoByApptype(appType);
		MPUrl mpUrl = MPManager.getMPUrl();
		if (wechatServiceNoInfo == null || mpUrl == null) {
			// 抛出微信公众号不能为空的异常
			throw new WxPublicNoNotExistsException(ServiceName.WechatGateway);
		}

		// 调用接口获取access_token
		String accessToken = this.getAccessTokenForRedis(appType);
		logger.info("get_accessToken_by_wechatserver_apptype_ " + appType + "_" + accessToken);
		String body = HttpClientManager.get(null, MessageFormat.format(mpUrl.getGetJsapiTicketUrl(), accessToken));

		logger.info("get_jsApiTicket_body_" + body);

		if (StringUtils.isEmpty(body)) {
			// 获取ticket失败，递归获取
			queryTimes++;
			return getJsapiTicketForMPByWechatServiceNo(queryTimes, appType);
		}

		JSONObject json = JSONObject.fromObject(body);

		// 判断微信返回结果结果代码是否成功
		if (WechatRespondErrCodeEnum.发送成功 != MPManager.checkErrcode(json)) {
			// 获取失败
			queryTimes++;
			// 更新AccessToken,并存入缓存
			this.getAccessTokenForRedis(appType);

			// 递归获取ticekt
			return getJsapiTicketForMPByWechatServiceNo(queryTimes, appType);
		}

		// 获取ticket
		String jsApiTicket = json.getString(WechatGratewayKey.TICKET);
		// 获取ticket的时效
		Long expiresIn = json.getLong(WechatGratewayKey.EXPIRES_IN);

		// 组装Ticket对象
		TicketDto ticketDto = new TicketDto();
		ticketDto.setJsapiTicket(jsApiTicket);
		ticketDto.setExpiresIn(expiresIn);
		return ticketDto;
	}
	/**
	 * @param appType 应用类型
	 * @return boolean
	 */
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
	public void processRank(AppType appType, String openId, int step) {

		MDC.put(LogConstant.PROJECT, ServiceName.WechatGateway.name() + "-rest");
		MDC.put(LogConstant.REQUEST_ID, DataUtil.getRequestId());
		MDC.put(LogConstant.ENV, SystemUtils.getEnv());


		String accessToken = this.getAccessTokenForRedis(appType);

		String setStepForWechatUrl = "https://api.weixin.qq.com/hardware/bracelet/setstep?access_token={0}&openid={1}&timestamp={2}&step={3}";

		long time = System.currentTimeMillis() / 1000;

		// 通过HTTP到微信服务器请求网页授权AccessToken信息
		int tryTimes = 0;
		String body = null;
		do {
			setStepForWechatUrl = MessageFormat.format(setStepForWechatUrl, accessToken, openId, time + "", step + "");
			body = HttpClientManager.get(null, setStepForWechatUrl);
		} while (body == null && ++tryTimes < 3);

		// 判断返回内容是否为空，或内容中是否包含错误代码
		if (StringUtils.isEmpty(body)) {
            logger.error("调用微信服务上传步数返回body为空,{}-{}", openId, step);
			throw new WsSetStepErrorException(ServiceName.WechatGateway);
		}

		JSONObject json = JSONObject.fromObject(body);
        logger.info("processRank_errocoe_setStepForWechatUrl:request:{},reponse:{}", setStepForWechatUrl, json);

		// 判断返回内容是否为空，或内容中是否包含错误代码
		if (!(WechatRespondErrCodeEnum.发送成功 == MPManager.checkErrcode(json))) {
			throw new WsSetStepErrorException(ServiceName.WechatGateway);
		}
		

		
	}
	/**
	 * 根据参数获取带参数二维码
	 */
	public ParamQrcodeResponse getParamQrcodeByMP(ParamQrcodeParam param, String wechatServiceNo, String jsonMessage) {
		RequestHeader requestHeader = new RequestHeader();
		// 调用接口获取access_token
		String accessToken = this.getAccessTokenForRedis(param.getAppType());

		// 通过HTTP到微信服务器请求设备二维码
		String body = HttpClientManager.postJson(requestHeader, MessageFormat.format(MPManager.getMPUrl().getCreateTicketUrl(), accessToken),
				jsonMessage);

		JSONObject json = JSONObject.fromObject(body);

		// 判断返回内容是否为空
		if (StringUtils.isEmpty(body)) {
			// 抛出获取带参二维码异常
			throw new WxGetParamQrcodeErrorException(ServiceName.WechatGateway);
		}

		// 如果是AcessToken超时，重新更新AccessToken再发一次请求
		if (WechatRespondErrCodeEnum.AcessToken超时 == MPManager.checkErrcode(json)) {
			accessToken = this.getAccessTokenForRedis(param.getAppType());
			body = HttpClientManager.postJson(requestHeader, MessageFormat.format(MPManager.getMPUrl().getCreateTicketUrl(), accessToken),
					jsonMessage);
			json = JSONObject.fromObject(body);
		}

		// 判断返回内容是否为空，或内容中是否包含错误代码
		if (StringUtils.isEmpty(body) || !(WechatRespondErrCodeEnum.发送成功 == MPManager.checkErrcode(json))) {
			// 抛出获取带参二维码异常
			throw new WxGetParamQrcodeErrorException(ServiceName.WechatGateway);
		}

		return DataUtil.jsonParamQrcodeResponse(body);
	}
	
	/**
	 * 根据参数获取带参数临时二维码
	 */
	public ParamQrcodeResponse getQrScenemp(ParamQrcodeParam param, String wechatServiceNo) {
		if (param.getSceneId() == null) {
			// 抛出场景值ID为空
			return null;
		}

		String jsonMessage = DataUtil.getQrSceneJson(param.getSceneId(), param.getActionName(), param.getExpireseconds());

		ParamQrcodeResponse qrcodeResponse = this.getParamQrcodeByMP(param, wechatServiceNo, jsonMessage);

		if (qrcodeResponse == null || qrcodeResponse.getExpire_seconds() == 0) {
			return null;
		}

		// 当前时间戳
		long currentTs = System.currentTimeMillis() / 1000;
		// 微信返回有效时间
		long expireSecondes = qrcodeResponse.getExpire_seconds();

		// 设置过期时间
		qrcodeResponse.setExpire_seconds(currentTs + expireSecondes);

		return qrcodeResponse;
	}
	/**
	 * 根据参数获取带参数二维码(场景值为数字)
	 * 
	 * @param param 获取参数
	 * @return 二维码解析后URL
	 */
	public ParamQrcodeResponse getQrSceneParamQrcodeUrl(ParamQrcodeParam param) {
		ParamQrcodeResponse paramQrcodeResponse = new ParamQrcodeResponse();

		if (param == null || param.getAppType() == null) {
			throw new WxAppTypeNullException(ServiceName.WechatGateway);
		}

		// 二维码仅公众号有的功能
		param.setAppType(AppTypeAdapter.onlyWechat(param.getAppType()));
		if (StringUtils.isBlank(param.getActionName())) {
			return null;
		}

		String wechatServiceNo = MPManager.getWechatServiceNoInfoByApptype(param.getAppType()).getServiceNo();
		if (StringUtilByWechatGateway.isBlankOrNull(wechatServiceNo)) {
			throw new WxAppTypeNotConfiguredWechatServiceNoException(ServiceName.WechatGateway);
		}

		switch (ParamQrcodeActionTypeEnum.getParamQrcodeActionTypeEnumByValue(param.getActionName())) {
		case 临时二维码:
			paramQrcodeResponse = this.getQrScenemp(param, wechatServiceNo);
			break;
		case 临时二维码_字符串:
			paramQrcodeResponse = this.getQrStrScenemp(param, wechatServiceNo);
			break;
		case 永久二维码:
			if (param.getExpireseconds() == null) {
				param.setExpireseconds(0l);
			}
			paramQrcodeResponse = this.geQrLimitScene(param, wechatServiceNo);
			break;
		case 永久二维码_字符串:
			paramQrcodeResponse = this.geQrLimitStrScene(param, wechatServiceNo);
			break;
		default:
			break;
		}

		return paramQrcodeResponse;
	}

	/**
	 * 根据参数获取带参数临时二维码(字符串)
	 */
	public ParamQrcodeResponse getQrStrScenemp(ParamQrcodeParam param, String wechatServiceNo) {
		if (StringUtils.isEmpty(param.getSceneStr())) {
			// 抛出场景值ID为空
			return null;
		}
		String jsonMessage = DataUtil.getQrStrSceneJson( param.getSceneStr(), param.getActionName(), param.getExpireseconds());
		ParamQrcodeResponse qrcodeResponse = this.getParamQrcodeByMP(param, wechatServiceNo, jsonMessage);
		if (qrcodeResponse == null || qrcodeResponse.getExpire_seconds() == 0) {
			return null;
		}

		// 当前时间戳
		long currentTs = System.currentTimeMillis() / 1000;
		// 微信返回有效时间
		long expireSecondes = qrcodeResponse.getExpire_seconds();

		// 设置过期时间
		qrcodeResponse.setExpire_seconds(currentTs + expireSecondes);

		return qrcodeResponse;
	}
	/**
	 * 根据参数获取带参数永久二维码(场景值ID为1-100000数字)
	 */
	public ParamQrcodeResponse geQrLimitScene(ParamQrcodeParam param, String wechatServiceNo) {
		ParamQrcodeResponse paramQrcodeResponse = new ParamQrcodeResponse();

		// 查询对应的场景值ID和公众号下是否有可用的二维码
		ParamQrcodeEntity paramQrcodeEntity = paramQrcodeEntityMapper.findBySceneIdAndWechatServiceNo(param.getSceneId(), wechatServiceNo);
		if (paramQrcodeEntity == null) {
			// 若没有对应场景值的的二维码
			String jsonMessage = DataUtil.getQrSceneJson(param.getSceneId(), param.getActionName(), param.getExpireseconds());

			// 去微信服务器获取带参二维码
			paramQrcodeResponse = this.getParamQrcodeByMP(param, wechatServiceNo, jsonMessage);
			// 存储带参二维码，并设置为已使用状态
			this.saveParamQrcode(paramQrcodeResponse, param, param.getSceneId(), "", true);
		} else {
			// 更新数据库中的带参数二维码，标示为已使用
			paramQrcodeResponse.setUrl(paramQrcodeEntity.getUrl());
			paramQrcodeResponse.setTicket(paramQrcodeEntity.getTicket());
			paramQrcodeEntity.setUseFlag(true);
			paramQrcodeEntityMapper.updateByPrimaryKey(paramQrcodeEntity);
		}

		// 查询某公众号在数据库中可使用的带参二维码数量
		Long count = paramQrcodeEntityMapper.selectCountByWechatServiceNo(wechatServiceNo);
		if (count == 0) {
			// 异步将获取10个带参二维码存入数据库，并标识为未使用
			ThreadPoolUtils.excuteCachedThreadPool(() -> this.batchGetParamQrcodeAndSave(param, wechatServiceNo, false, 10));
		}

		if (count == 5) {
			// 若数据库中只剩5个符合条件的带参二维码，将再异步将获取5个带参二维码存入数据库，并标识为未使用
			taskExecutor.execute(() -> this.batchGetParamQrcodeAndSave(param, wechatServiceNo, false, 5));
		}

		return paramQrcodeResponse;
	}
	/**
	 * 将带参二维码存入数据库
	 * 
	 * @return 带参二维码
	 */
	@Transactional
	private void saveParamQrcode(ParamQrcodeResponse paramQrcodeResponse, ParamQrcodeParam param, long sceneId, String sceneStr, boolean useFlag) {
		if (paramQrcodeResponse != null && param != null && param.getAppType() != null) {
			ParamQrcodeEntity qrcodeEntity = DataUtil.initParamQrcodeEntity(param, paramQrcodeResponse, sceneId, sceneStr, useFlag);
			paramQrcodeEntityMapper.insert(qrcodeEntity);
		}
	}
	/**
	 * 根据参数获取带参数永久二维码(场景值ID为字符串)
	 */
	public ParamQrcodeResponse geQrLimitStrScene(ParamQrcodeParam param, String wechatServiceNo) {
		ParamQrcodeResponse paramQrcodeResponse = new ParamQrcodeResponse();

		// 组合场景值ID，业务类型+唯一标示
		String jsonMessage = DataUtil.getQrSceneStrJson(param.getBusinessType() + "_" + param.getSceneStr(), param.getActionName());
		// 去微信服务请求一个带参的二维码
		paramQrcodeResponse = this.getParamQrcodeByMP(param, wechatServiceNo, jsonMessage);
		if (paramQrcodeResponse == null) {
			throw new WxGetParamQrcodeErrorException(ServiceName.WechatGateway);
		}

		return paramQrcodeResponse;
	}

	/**
	 * 批量获取带参数二维码并存入数据库
	 * 
	 * @return 微信用户信息
	 */
	@Transactional
	private void batchGetParamQrcodeAndSave(ParamQrcodeParam param, String wechatServiceNo, boolean useFlag, int batchCount) {
		for (int i = 0; i < batchCount; i++) {
			// 场景ID自增
			long sceneId = param.getSceneId() + 1;
			param.setSceneId(sceneId);

			Long count = paramQrcodeEntityMapper.selectCountBySceneIdAndWechatServiceNo(sceneId, wechatServiceNo);
			if (count == 0) {
				String jsonMessage = DataUtil.getQrSceneJson(sceneId, param.getActionName(), param.getExpireseconds());
				ParamQrcodeResponse paramQrcodeResponse = this.getParamQrcodeByMP(param, wechatServiceNo, jsonMessage);
				ParamQrcodeEntity qrcodeEntity = DataUtil.initParamQrcodeEntity(param, paramQrcodeResponse, sceneId, "", useFlag);
				paramQrcodeEntityMapper.insert(qrcodeEntity);
			}
		}
	}

	/**
	 * 批量获取带参数二维码并存入数据库
	 * 
	 * @return 微信用户信息
	 */
	@Transactional
	private void batchGetParamQrcodeAndSaveByStr(ParamQrcodeParam param, String wechatServiceNo, boolean useFlag, int batchCount) {
		for (int i = 0; i < batchCount; i++) {
			String sceneStr = param.getBusinessType() + "_" + UUIDUtils.uuid();
			String jsonMessage = DataUtil.getQrSceneStrJson(sceneStr, param.getActionName());
			ParamQrcodeResponse paramQrcodeResponse = this.getParamQrcodeByMP(param, wechatServiceNo, jsonMessage);
			ParamQrcodeEntity qrcodeEntity = DataUtil.initParamQrcodeEntity(param, paramQrcodeResponse, 0, sceneStr, useFlag);
			paramQrcodeEntityMapper.insert(qrcodeEntity);
		}
	}
	
	
	



	/**
	 * 发送微信消息
	 * 
	 * @param jsonMessage 微信消息json字符串
	 * @param wechatServiceNo 服务号
	 * @param apiUrl 消息消息发送链接
	 */
	public boolean sendMessage(String jsonMessage, AppType appType, String apiUrl) {
		// 发送结果标识，true-成功，false-失败
		boolean flag = false;

		// 获取AccessToken
		String accessToken = this.getAccessTokenForRedis(appType);

		logger.info("!!!!!!!sendMessage json:" + jsonMessage + "," + "accessToken:" + accessToken + " \n requestId:"
				+ DataUtil.getRequestId());
		logger.info("!!!!!!!sendMessage apiUrl:" + apiUrl);
		// 用POST方式发送消息
		Long start = System.currentTimeMillis();
		String body = HttpClientManager.postJson(null, MessageFormat.format(apiUrl, accessToken), jsonMessage);
		Long end = System.currentTimeMillis();
		Long use = end - start;
		String requestId = DataUtil.getRequestId();
		if (use > 1000 && use < 2000) {
			logger.info("sendmessage_http_max_1s" + " \n requestId" + requestId + " useTime " + use);
		}
		if (use > 2000 && use < 3000) {
			logger.info("sendmessage_http_max_2s" + " \n requestId" + requestId + " useTime " + use);
		}
		if (use > 3000 && use < 4000) {
			logger.info("sendmessage_http_max_3s" + " \n requestId" + requestId + " useTime " + use);
		}
		if (use > 5000 && use < 10000) {
			logger.info("sendmessage_http_max_5s" + " \n requestId" + requestId + " useTime " + use);
		}
		if (use > 10000) {
			logger.info("sendmessage_http_max_10s" + " \n requestId" + requestId + " useTime " + use);
		}
		if (body == null) {
			logger.error("sendmessage_response_null" + " \n requestId" + requestId + " useTime " + use);
			// 抛出异发送消息响应为null异常
			throw new WxReplyNullException(ServiceName.WechatGateway);
		}

		// 把发送返回消息转换成JSON对象
		JSONObject json = JSONObject.fromObject(body);

		if (logger.isInfoEnabled()) {
			int msg_id = 0;
			if (json.containsKey("msg_id")) {
				msg_id = json.getInt("msg_id");
			} else if (json.containsKey("msgid")) {
				msg_id = json.getInt("msgid");
			}
			logger.info("!!!!!!!errcode:" + MPManager.checkErrcode(json).toString() + ",body:" + body + "," + "accessToken:" + accessToken
					+ " \n requestId" + requestId + " msgId:" + msg_id);
		}

		// 获取返回消息错误代码
		if (WechatRespondErrCodeEnum.发送成功 == MPManager.checkErrcode(json)) {
			flag = true;
		} else if (WechatRespondErrCodeEnum.AcessToken超时 == MPManager.checkErrcode(json)) {
			// accessToken超时或失效后重发3次
			flag = resendMessage(apiUrl, jsonMessage, appType);
		} else if (WechatRespondErrCodeEnum.用户与微信公众号超过48小时没有交互 == MPManager.checkErrcode(json)) {
			// 回复时间超过限制(客服接口，用户与公众号超48小时没有交互)
			flag = false;
		}
		return flag;
	}

	/**
	 * 重发送微信消息（1次）
	 * 
	 * @param url 发送链接
	 * @param jsonMessage 微信消息json字符串
	 * @param serviceNo 服务号
	 */
	public boolean resendMessage(String url, String jsonMessage, AppType appType) {
		// 更新token
		String accessToken = this.getAccessTokenForRedis(appType);
		String body = HttpClientManager.postJson(null, MessageFormat.format(url, accessToken), jsonMessage);
		if (logger.isDebugEnabled())
			logger.debug("!!!!!!!resend message:" + body);

		if (body != null) {
			// 抛出异发送消息响应为null异常
			throw new WxReplyNullException(ServiceName.WechatGateway);
		}

		JSONObject json = JSONObject.fromObject(body);

		// 从返回代码判断是否发送成功
		if (WechatRespondErrCodeEnum.发送成功 == MPManager.checkErrcode(json)) {
			// 已发送成功
			return true;
		} else if (WechatRespondErrCodeEnum.AcessToken超时 == MPManager.checkErrcode(json)) {
			// 把未发送成功的消息存入队列并缓存
			this.saveNotSendMessageToQueue(appType, url, jsonMessage);
			if (logger.isDebugEnabled())
				logger.debug("accessToken超时或失效，发送失败");
			throw new WxAccessTokenTimeoutException(ServiceName.WechatGateway);
		} else {
			// 把未发送成功的消息存入队列并缓存
			this.saveNotSendMessageToQueue(appType, url, jsonMessage);
			if (logger.isDebugEnabled())
				logger.debug("未知错误，后台待补发");
			// 抛出发送消息失败，未知错误异常
			throw new WxSendMessageUnkonwnErrorException(ServiceName.WechatGateway);
		}
	}

	/**
	 * 把发送失败的消息放入队列并存入缓存
	 * 
	 * @param serviceNo 服务号
	 * @param sendUrl 发送链接
	 * @param jsonMessage 消息内容
	 */
	public void saveNotSendMessageToQueue(AppType appType, String sendUrl, String jsonMessage) {
		// 从缓存中获取未发送成功消息的队列
		RedisObject redisObject = new RedisObject(WechatGratewayKey.REDIS_NOT_SEND_MESSAGE_QUEUE);
		NotSendMessageQueue<NotSendMessage> nsmq = redisObject.get();

		if (nsmq == null) {
			nsmq = redisObject.get();
		}

		NotSendMessage sm = new NotSendMessage();
		sm.setJsonMessage(jsonMessage);
		// 设置补发次数为初始值0
		sm.setReissueCount(0);

		sm.setSendUrl(sendUrl);
		sm.setAppType(appType);
		// 把未发送成功的消息插入队尾
		nsmq.offer(sm);

		// 存入缓存 长期有效
		redisObject.set(nsmq, -1);
	}

	/**
	 * 发送设备消息(包括设备内容消息、wifi设备连接状态消息)
	 * 
	 * @param appType 应用类型
	 * @param deviceStatusMessage 设备消息
	 * @return 发送是否成功标志
	 */
	public boolean sendWechatDeviceMessage(AppType appType, WechatDeviceMessage deviceMessage) {

		Long start = System.currentTimeMillis();
		taskExecutor.execute(() -> {
			Long firstStart = System.currentTimeMillis();
			try {
				String wechatServiceNo = MPManager.getWechatServiceNoInfoByApptype(appType).getServiceNo();

				if (deviceMessage == null || StringUtils.isEmpty(wechatServiceNo)) {
					// 抛出公众号为null异常
					throw new WxPublicNoNotExistsException(ServiceName.WechatGateway);
				}

				// 把设备状态消息转换成Json串
				String jsonMessage = GsonUtil.toJson(deviceMessage);

				Long start2 = System.currentTimeMillis();
				// 根据消息类型获取消息发送链接
				String apiUrl = DataUtil.getApiUrl(WechatMessageTypeEnum.发送设备状态消息.toString());

				// 发送消息
				sendMessage(jsonMessage, appType, apiUrl);
				Long end2 = System.currentTimeMillis();
				logger.info(LogUtils.getMsg("send_wechat_device_exe_message", (end2 - start2)));
			} catch (Exception ex) {
				logger.error(LogUtils.getMsg("send_wechat_device_exe_error", (System.currentTimeMillis() - firstStart)), ex);
			}

		});

		Long end = System.currentTimeMillis();

		logger.info(LogUtils.getMsg("async_send_wechat_device_message", (end - start)));
		return true;
	}

	/**
	 * 向微信服务器上传多媒体文件
	 * 
	 * @param queryTimes 获取次数
	 * @return
	 */
	public  String uploadWechatFile(AppType appType, String fullFilePath, WechatMediaFileTypeEnum fileType, Integer queryTimes,
			FileExtensionEnum fileExtensionEnum) {
		// 如果发送3次都失败就不查询了
		if (queryTimes > 3) {
			logger.info("uploadFile_json_repeat_error_" + DataUtil.getRequestId() + " fullFilePath_" + fullFilePath);
			return null;
		}
		// 调用接口获取access_token
		String accessToken = getAccessTokenForRedis(appType);
		String url = MessageFormat.format(MPManager.getMPUrl().getGetUploadUrl(), accessToken, fileType.getType());
		String result = HttpClientManager.uploadFile(null, url, fullFilePath, fileExtensionEnum);
		JSONObject json = JSONObject.fromObject(result);
		if (StringUtils.isEmpty(result) || !(WechatRespondErrCodeEnum.发送成功 == MPManager.checkErrcode(json))) {
			// 获取失败
			queryTimes++;
			// 更新AccessToken
			accessToken = this.getAccessTokenForRedis(appType);
			// 递归获取
			result = uploadWechatFile(appType, fullFilePath, fileType, queryTimes, fileExtensionEnum);

			json = JSONObject.fromObject(result);
		}
		if (StringUtils.isEmpty(result)) {
			throw new WxUserInfoFetchException(ServiceName.WechatGateway);
		}
		if (json.containsKey(Constants.MEDIA_ID)) {
			return result;
		} else {
			return Constants.UPLOAD_FAIL + fullFilePath;
		}
	}
	
	/**
	 * 向微信获取（4-queryTimes）次微信用户信息
	 * 
	 * @param queryTimes 获取次数
	 * @return
	 */
	public WechatUser getWechatUserForMPByWechatServiceNo(Integer queryTimes, AppType appType, String openid) {
		// 如果查询3次都失败就不查询了
		if (queryTimes > 3) {
			return null;
		}

		// 调用接口获取access_token
		String accessToken = this.getAccessTokenForRedis(appType);
		String url = MessageFormat.format(MPManager.getMPUrl().getGetUserInfoUrl(), accessToken, openid);
		String body = HttpClientManager.get(null, url);
		JSONObject json = JSONObject.fromObject(body);

		if (StringUtils.isEmpty(body)) {
			throw new WxUserInfoFetchException(ServiceName.WechatGateway);
		}

		if (!(WechatRespondErrCodeEnum.发送成功 == MPManager.checkErrcode(json))) {
			logger.error("getWechatUserForMPByWechatServiceNo_errr_url:" + url + ",body:" + body);
			// 获取失败
			queryTimes++;
			// 更新AccessToken
			accessToken = this.getAccessTokenForRedis(appType);
			// 递归获取
			return getWechatUserForMPByWechatServiceNo(queryTimes, appType, openid);
		}

		// 将JSON串转换成WechatUser对象
		return DataUtil.jsonToWechatUser(body);
	}
}
