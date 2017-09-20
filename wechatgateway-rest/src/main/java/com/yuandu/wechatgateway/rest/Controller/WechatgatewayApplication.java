/**
 * 
 */
package com.yuandu.wechatgateway.rest.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.minlog.Log;
import com.lifesense.base.beans.param.RequestHeader;
import com.lifesense.base.cache.command.RedisString;
import com.lifesense.base.constant.AppType;
import com.lifesense.base.constant.ServiceName;
import com.lifesense.base.dto.wechatgateway.Article;
import com.lifesense.base.dto.wechatgateway.News;
import com.lifesense.base.dto.wechatgateway.WechatCustomServiceNewsMessage;
import com.lifesense.base.exception.check.ParamsIsNullException;
import com.lifesense.base.exception.wechat.WxAppTypeNullException;
import com.lifesense.base.exception.wechat.WxH5UrlNullException;
import com.lifesense.base.exception.wechat.WxNewsMessageNullException;
import com.lifesense.base.exception.wechat.WxNewsTypeErrorException;
import com.lifesense.base.exception.wechat.WxOpenIdNullException;
import com.lifesense.base.spring.InstanceFactory;
import com.lifesense.base.utils.ResourceUtils;
import com.lifesense.base.utils.SystemUtils;
import com.lifesense.base.utils.UUIDUtils;
import com.lifesense.base.utils.json.JsonUtils;
import com.lifesense.soa.device.api.IDeviceProvider;
import com.lifesense.soa.device.api.IDeviceTestProvider;
import com.lifesense.soa.device.dto.Device;
import com.lifesense.soa.device.enums.ProductTypeEnum;
import com.lifesense.soa.user.api.IUserProvider;
import com.lifesense.soa.wechatgateway.api.IWechatGatewayForwardProviderV2;
import com.lifesense.soa.wechatgateway.api.IWechatGatewayProviderV2;
import com.lifesense.soa.wechatgateway.dto.ParamTempQrcodeDto;
import com.lifesense.soa.wechatgateway.dto.enums.WechatMessageTypeEnum;
import com.lifesense.soa.wechatgateway.dto.receive.JSSDKSignatureInfo;
import com.lifesense.soa.wechatgateway.dto.receive.WeChatIotDeviceInfo;
import com.lifesense.soa.wechatgateway.dto.receive.WeChatIotDeviceResponse;
import com.lifesense.soa.wechatgateway.dto.receive.WechatUser;
import com.lifesense.soa.wechatgateway.dto.receive.WechatWebpageAccessTokenResp;
import com.lifesense.soa.wechatgateway.dto.send.ParamQrcodeParam;
import com.lifesense.soa.wechatgateway.dto.send.Text;
import com.lifesense.soa.wechatgateway.dto.send.WechatCustomServiceTextMessage;
import com.lifesense.soa.wechatgateway.dto.send.WechatUnBindDeviceEventMessage;
import com.lifesense.soa.wechatgateway.rest.dto.GetJSSDKSignatureParams;
import com.lifesense.soa.wechatgateway.rest.dto.GetWechatInfoBySnsParams;
import com.lifesense.soa.wechatgateway.rest.dto.GetWechatOpenIdParams;
import com.lifesense.soa.wechatgateway.rest.dto.SendNewsParams;
import com.lifesense.soa.wechatgateway.rest.dto.WechatBaseParams;
import com.lifesense.soa.wechatgateway.service.dto.QueryMPUserParam;
import com.lifesense.support.rest.filter.auth.RequestHeaderHolder;
import com.lifesense.support.rest.filter.auth.annotation.AuthIgnore;

/**
 * 微信网关服务
 * 
 * @version
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang Copyright (c)
 *         2016, lifesense.com All Rights Reserved.
 */
@Path("/")
// @Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class WechatgatewayApplication {
	protected static final Logger logger = LoggerFactory.getLogger(WechatgatewayApplication.class);
	// 通过dubbo 获取微信网关转发服务实例
	private IWechatGatewayForwardProviderV2 wechatGatewayForwardService = InstanceFactory.getInstance("wechatGatewayForwardServiceV2");

	// 通过dubbo 获取微信网关服务实例
	private IWechatGatewayProviderV2 wechatGatewayService = InstanceFactory.getInstance("wechatGatewayServiceV2");

	// 通过dubbo 获取设备用户服务实例
	private IDeviceTestProvider deviceTestService = InstanceFactory.getInstance("deviceTestService");

	// 通过dubbo 获取用户服务实例
	private IUserProvider userService = InstanceFactory.getInstance("userService");

	private static final String OPENID_BLOCK = "openid_block_%s";

	private IDeviceProvider deviceProvider = InstanceFactory.getInstance("deviceService");;

	/**
	 * 发送图文消息
	 * 
	 * @param params 发送图文消息参数
	 * @return void
	 */
	@POST
	@Path("wechat_gateway/send_news_message")
	@AuthIgnore
	public void sendWechatNewsMessage(SendNewsParams params) {
		RequestHeader requestHeader = RequestHeaderHolder.get();

		if (requestHeader.getRequestId() == null) {
			requestHeader.setRequestId(UUIDUtils.uuid());
		}
		AppType appTypeInstance = AppType.getAppTypeByCode(params.getAppType());
		if (appTypeInstance == null) {
			throw new WxAppTypeNullException(ServiceName.WechatGateway);
		}
		if (params.getNewsMessage() == null) {
			throw new WxNewsMessageNullException(ServiceName.WechatGateway);
		}

		if (StringUtils.isBlank(params.getNewsMessage().getTouser())) {
			throw new WxOpenIdNullException(ServiceName.WechatGateway);
		}

		if (StringUtils.isBlank(params.getNewsMessage().getMsgtype())
				|| !(params.getNewsMessage().getMsgtype().equals(WechatMessageTypeEnum.图文消息.toString()))) {
			throw new WxNewsTypeErrorException(ServiceName.WechatGateway);
		}

		wechatGatewayService.sendCustomServiceMessage(appTypeInstance, params.getNewsMessage());
	}

	
	
	/**
	 * 根据code获取openId
	 * 
	 * @param params 获取openId参数
	 * @return openId
	 */
	@POST
	@Path("wechat_gateway/get_openid")
	@AuthIgnore
	public String getWechatOpenId(GetWechatOpenIdParams params) {
		AppType appTypeInstance = AppType.getAppTypeByCode(params.getAppType());
		if (appTypeInstance == null) {
			throw new WxAppTypeNullException(ServiceName.WechatGateway);
		}

		if (StringUtils.isBlank(params.getCode())) {
			throw new WxOpenIdNullException(ServiceName.WechatGateway);
		}

		return wechatGatewayService.getOpenIdByCode(appTypeInstance, params.getCode());
	}

	/**
	 * 根据url获取JSSDK签名
	 * 
	 * @param params 获取签名信息参数
	 * @return 签名信息
	 */
	@POST
	@Path("wechat_gateway/get_jssdk_signature")
	@AuthIgnore
	public JSSDKSignatureInfo getJSSDKSignature(GetJSSDKSignatureParams params) {
		AppType appTypeInstance = AppType.getAppTypeByCode(params.getAppType());
		if (appTypeInstance == null) {
			throw new WxAppTypeNullException(ServiceName.WechatGateway);
		}

		if (StringUtils.isBlank(params.getUrl())) {
			throw new WxH5UrlNullException(ServiceName.WechatGateway);
		}
		return wechatGatewayService.getJSSDKSignature(appTypeInstance, params.getUrl());
	}

	/**
	 * 清除微信服务器相关资料的缓存
	 * 
	 * @param params 获取签名信息参数
	 * @return 签名信息
	 */
	// @GET
	// @Path("wechat_gateway/remove_mp_redis")
	// @AuthIgnore
	// public void removRedisForMP() {
	// wechatGatewayService.removRedisForMP();
	// }

	/**
	 * 根据url获取JSSDK签名
	 * 
	 * @param params 获取签名信息参数
	 * @return 签名信息
	 */
	@POST
	@Path("wechat_gateway/get_param_qrcode_url")
	@AuthIgnore
	public String getParamQrcodeUrl(ParamQrcodeParam params) {
		logger.info("!!!!!ParamQrcodeParam to String:" + params.toString());
		if (params.getExpireseconds() == null) {
			logger.info("!!!!!Expireseconds is null:");
			params.setExpireseconds(0L);
		}
		return wechatGatewayService.getParamQrcodeUrl(params).getUrl();
	}

	/**
	 * 根据url获取JSSDK签名
	 * 
	 * @param params 获取签名信息参数
	 * @return 签名信息
	 */
	@POST
	@Path("wechat_gateway/get_temp_param_qrcode_url")
	@AuthIgnore
	public String getTempParamQrcodeUrl(ParamTempQrcodeDto params) {
		RequestHeader requestHeader = RequestHeaderHolder.get();

		if (requestHeader.getRequestId() == null) {
			requestHeader.setRequestId(UUIDUtils.uuid());
		}
		// AppType
		// appTypeInstance=AppType.getAppTypeByCode(params.getAppType());
		// if(appTypeInstance==null)
		// {
		// throw new WxAppTypeNullException(ServiceName.WechatGateway);
		// }
		//
		// if(StringUtils.isBlank(params.getUrl()))
		// {
		// throw new WxH5UrlNullException(ServiceName.WechatGateway);
		// }
		logger.info("!!!!!ParamQrcodeParam to String:" + params.toString());
		if (params.getExpireseconds() == null) {
			logger.info("!!!!!Expireseconds is null:");
			params.setExpireseconds(0L);
		}
		// return wechatGatewayService.getTempParamQrcodeUrl(requestHeader,
		// params).getUrl();
		return null;
	}

	/**
	 * 提供dev/qa2/产线系统获取accesstoken
	 * 
	 * @param appTypeCode
	 * @return
	 */
	@GET
	@Path("wechat_gateway/get_accesstoken")
	@AuthIgnore
	public String getAccessTokenByApppType(@QueryParam("app_type_code") int appTypeCode, @QueryParam("timestamp") Long timestamp,
			@QueryParam("checksum") String checksum) {

		logger.warn("invoke_getAccessTokenByApppType");
		// ServiceAssert.notNull(ServiceName.SESSIONS, "checksum", checksum);
		// ServiceAssert.notNull(ServiceName.SESSIONS, "timestamp", timestamp);
		// if(!StringUtils.equals(LifesenseCheckSum.create().checksum(String.valueOf(timestamp)),checksum)){
		// throw new
		// WechatGatewayException(WechatGatewayErrorCode.CHECKSUM_ERROR);
		// }

		String accessToken = "";
		RequestHeader requestHeader = RequestHeaderHolder.get();

		if (requestHeader.getRequestId() == null) {
			requestHeader.setRequestId(UUIDUtils.uuid());
		}
		AppType appTypeInstance = AppType.getAppTypeByCode(appTypeCode);
		if (appTypeInstance == null) {
			throw new WxAppTypeNullException(ServiceName.WechatGateway);
		}
		accessToken = wechatGatewayService.getAccessTokenForRedis(appTypeInstance);

		return accessToken;
	}

	/**
	 * 提供给乐心运动1.0
	 * 
	 * @param appTypeCode
	 * @return
	 */
	@GET
	@Path("wechat_gateway/get_accesstoken_v1")
	@AuthIgnore
	public Map<String, String> getAccessTokenByApppTypeForV1(@QueryParam("app_type_code") int appTypeCode) {
		logger.warn("invoke_getAccessTokenByApppTypeV1");
		String accessToken = "";
		String ticket = "";
		AppType appTypeInstance = AppType.getAppTypeByCode(appTypeCode);
		if (appTypeInstance == null) {
			throw new WxAppTypeNullException(ServiceName.WechatGateway);
		}

		try {
			accessToken = wechatGatewayService.getAccessTokenForRedis(appTypeInstance);
			ticket = wechatGatewayService.getJSApiTicketByAppType(appTypeInstance);
		} catch (Exception ex) {
			Log.error("fetch_access_ticket_error " + " " + ex.getMessage(), ex);
		}

		Map<String, String> map = new HashMap<>();

		map.put("accessToken", accessToken);// 1.0
		map.put("access_token", accessToken);// 2.0
		map.put("ticket", ticket);
		return map;
	}

	@GET
	@Path("echo")
	@AuthIgnore
	public Map<String, Object> echo(@QueryParam("mode") int mode) {
		long requestTime = RequestHeaderHolder.get().getRequestTime();

		Map<String, Object> echo = new TreeMap<>();
		echo.put("rest", "ok");
		// 返回日志级别信息
		echo.put("wechatgatewayService_logger_debug", logger.isDebugEnabled());
		echo.put("wechatgatewayService_logger_info", logger.isInfoEnabled());
		echo.put("wechatgatewayService_logger_warn", logger.isWarnEnabled());
		echo.put("wechatgatewayService_logger_error", logger.isErrorEnabled());

		long innnerStart = System.currentTimeMillis();

		if (requestTime > 0)
			echo.put("filterTime", innnerStart - requestTime);
		if (mode == 1) {
			if (requestTime > 0)
				echo.put("totalTime", innnerStart - requestTime);
			return echo;
		}

		try {
			echo.put("wechatGatewayForwardService", wechatGatewayForwardService.echo(mode));
		} catch (Exception e) {
			echo.put("wechatGatewayForwardService", e.getMessage());
		}

		try {
			echo.put("wechatGatewayService", wechatGatewayService.echo(mode));
		} catch (Exception e) {
			echo.put("wechatGatewayService", e.getMessage());
		}

		try {
			echo.put("deviceService", deviceTestService.echo());
		} catch (Exception e) {
			echo.put("deviceService", e.getMessage());
		}
		
		long end = System.currentTimeMillis();
		echo.put("dubboTime", end - innnerStart);
		if (requestTime > 0)
			echo.put("totalTime", end - requestTime);

		echo.put("dubbo_version", String.valueOf(System.getProperty("lifesense.dubbo.version")));
		return echo;
	}
	
	

	/**
	 * 查看推送是否可用
	 * 
	 * @param openId
	 * @param msg
	 * @param appType
	 * @return
	 */
	@GET
	@Path("push")
	@AuthIgnore
	public Map<String, Object> testPush(@QueryParam("openId") String openId, @QueryParam("msg") String msg, @QueryParam("code") int appType) {
		WechatCustomServiceTextMessage ws = new WechatCustomServiceTextMessage();
		ws.setTouser(openId);
		ws.setMsgtype("text");
		Text t = new Text();
		t.setContent(msg);
		ws.setText(t);
		boolean r = wechatGatewayService.sendCustomServiceMessage(AppType.getAppTypeByCode(appType), ws);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("result", r);
		return m;
	}

	/**
	 * 获取微信用户信息
	 * 
	 * @param param
	 * @return
	 */
	@POST
	@Path("wechat_gateway/getWechatUser")
	@AuthIgnore
	public WechatUser getWechatUser(QueryMPUserParam param) {
		RequestHeader requestHeader = RequestHeaderHolder.get();

		if (requestHeader.getRequestId() == null) {
			requestHeader.setRequestId(UUIDUtils.uuid());
		}
		AppType appTypeInstance = AppType.getAppTypeByCode(param.getAppTypeCode());
		if (appTypeInstance == null) {
			throw new WxAppTypeNullException(ServiceName.WechatGateway);
		}

		try {

			WechatUser user = wechatGatewayService.getWechatUserByAppTypeAndOpenid(appTypeInstance, param.getOpenid());
			return user;
		} catch (Exception ex) {

			logger.error("getHeadImge_error_" + param.getOpenid(), ex);
			return null;
		}
	}

	/**
	 * 获取微信用户信息
	 * 
	 * @param param
	 * @return
	 */
	@POST
	@Path("wechat_gateway/getWechatUserByServiceNo")
	@AuthIgnore
	public WechatUser getWechatUserByServiceNo(QueryMPUserParam param) {

		try {

			WechatUser user = wechatGatewayService.getWechatUserByServiceNoAndOpenid(param.getServiceNo(), param.getOpenid());
			return user;
		} catch (Exception ex) {

			logger.error("getHeadImge_error_" + param.getOpenid(), ex);
			return null;
		}
	}

	/**
	 * 根据code获取openId
	 * 
	 * @param params 获取openId参数
	 * @return openId
	 */
	@POST
	@Path("wechat_gateway/get_webpage_access")
	@AuthIgnore
	public WechatWebpageAccessTokenResp getWechatWebpageAccessToken(GetWechatOpenIdParams params) {
		logger.warn("invoke_get_webpage_access");
		RequestHeader requestHeader = RequestHeaderHolder.get();

		if (requestHeader.getRequestId() == null) {
			requestHeader.setRequestId(UUIDUtils.uuid());
		}
		AppType appTypeInstance = AppType.getAppTypeByCode(params.getAppType());
		if (appTypeInstance == null) {
			throw new WxAppTypeNullException(ServiceName.WechatGateway);
		}

		if (StringUtils.isBlank(params.getCode())) {
			throw new WxOpenIdNullException(ServiceName.WechatGateway);
		}

		return wechatGatewayService.getWechatWebpageAccessTokenByCode(appTypeInstance, params.getCode());
	}

	/**
	 * 提供给系统定时任务调度（刷新微信accesstoken），已弃用，已改用定时任务com.lifesense.soa.wechatgateway.jobs.CheckAccessTokenJob
	 * 
	 * @param rand
	 * @return
	 */
	@POST
	@Path("wechat_gateway/task/refresh_token")
	@AuthIgnore
	public String getRefreshToken(@QueryParam("rand") String rand) {
		return "";
	}

	/**
	 * 根据code获取openId(环境共享使用)
	 * 
	 * @param params 获取openId参数
	 * @return openId
	 */
	@POST
	@Path("wechat_gateway/jump_env_openid")
	@AuthIgnore
	public Map<String, Object> jumpWechatOpenId(GetWechatOpenIdParams params) {
		Map<String, Object> resMap = new HashMap<>();

		try {

			AppType appTypeInstance = AppType.getAppTypeByCode(params.getAppType());
			if (appTypeInstance == null) {
				throw new WxAppTypeNullException(ServiceName.WechatGateway);
			}

			if (StringUtils.isBlank(params.getCode())) {
				throw new WxOpenIdNullException(ServiceName.WechatGateway);
			}

			String openId = wechatGatewayService.getOpenIdByCode(appTypeInstance, params.getCode());
			if (StringUtils.isNotBlank(openId)) {
				RedisString jumpType = new RedisString(openId, "luaredis");

				String type = jumpType.get();
				if (StringUtils.isNotBlank(type)) {
					resMap.put("type", type);
				} else {
					if (appTypeInstance.isHealthProduct() && SystemUtils.isTestEnv()) {
						resMap.put("type", "4");
					}
				}

			}

			resMap.put("openId", openId);
		} catch (Exception ex) {
			logger.error("jumpWechatOpenId_error", ex);
		}
		return resMap;

	}

	/**
	 * 解绑微信设备(仅解绑微信关系,设备服务绑定关系不改变,解决双通道问题)
	 * 
	 * @param deviceBindEventMessage
	 * @param httpRequest
	 * @return
	 */
	@POST
	@Path("wechat_gateway/unbind_wechat_device")
	@AuthIgnore
	public Map<String, Object> unbindWechatDevice(WechatUnBindDeviceEventMessage deviceBindEventMessage, @Context HttpServletRequest httpRequest) {

		RequestHeader requestHeader = RequestHeaderHolder.get();

		if (requestHeader.getRequestId() == null) {
			requestHeader.setRequestId(UUIDUtils.uuid());
		}

		Map<String, Object> resMap = new HashMap<>();
		String appType = httpRequest.getParameter("appType");

		if (appType == null) {
			throw new ParamsIsNullException(ServiceName.WechatGateway, "appType ");
		}
		AppType appTypeInstance = AppType.getAppTypeByCode(Integer.valueOf(appType));
		if (appTypeInstance == null) {
			throw new ParamsIsNullException(ServiceName.WechatGateway, "appTypeInstance");
		}

		if (deviceBindEventMessage.getOpenid() == null) {

			if (deviceBindEventMessage.getUserId() == null) {
				throw new ParamsIsNullException(ServiceName.WechatGateway, "userId");
			}

			String openId = userService.getWechatOpenId(requestHeader, Long.valueOf(deviceBindEventMessage.getUserId()), appTypeInstance);

			if (openId == null) {
				throw new ParamsIsNullException(ServiceName.WechatGateway, "userId");
			}
			deviceBindEventMessage.setOpenid(openId);
		}

		boolean isOk = wechatGatewayService.unbindWechatDevice(appTypeInstance, deviceBindEventMessage);

		// ----------------------图文推送------------------------
		if (isOk) {
			// 从缓存获取电量
			String key = String.format(OPENID_BLOCK, deviceBindEventMessage.getOpenid().toLowerCase());
			RedisString cache = new RedisString(key);
			String block = cache.get();
			if (StringUtils.isEmpty(block)) {

				try {
					WechatCustomServiceNewsMessage message = new WechatCustomServiceNewsMessage();
					News news = new News();

					List<Article> dataList = new ArrayList<>();
					Article article = new Article();
					article.setPicurl(ResourceUtils.get("WECHAT_DOMAIN", "http://static.sports.lifesense.com") + "/raw/img/push_block_bluetooth.png");
					article.setTitle("公众号连接优化说明");
					article.setUrl(ResourceUtils.get("WECHAT_DOMAIN", "http://static.sports.lifesense.com") + "/wechat/v3/index.html#!/blueToothTip");

					/// WECHAT_DOMAIN
					// http://static.sports.lifesense.com/wechat/v3/index.html#!/blueToothTip
					// http://static-qa2.lifesense.com/wechat/v3/index.html#!/blueToothTip

					dataList.add(article);
					news.setArticles(dataList);

					message.setMsgtype("news");
					message.setTouser(deviceBindEventMessage.getOpenid());
					message.setNews(news);

					boolean issuccess = wechatGatewayService.sendCustomServiceMessage(appTypeInstance, message);

					if (issuccess) {
						cache.set(deviceBindEventMessage.getOpenid().toLowerCase());
					}

					logger.info("unbind_wechat_device_push_" + deviceBindEventMessage.getDevice_id() + " _" + issuccess);
				} catch (Exception ex) {
					logger.error("unbind_wechat_device_push_error_" + deviceBindEventMessage.getDevice_id());
				}

			}

		}

		// ----------------------图文推送------------------------
		String result = isOk ? "success" : "fail";
		resMap.put("result", result);

		logger.debug("unbind_wechat_device_over_" + deviceBindEventMessage.getDevice_id() + " ticket:" + deviceBindEventMessage.getTicket()
				+ " result:" + result);
		return resMap;
	}

	/**
	 * 微信分享是获取微信用户信息（暂未使用）
	 * 
	 * @param getWechatInfoBySnsParams
	 * @return
	 */
	@POST
	@Path("wechat_gateway/getWechatUserByAccessTokenAndOpenidBySns")
	@AuthIgnore
	public WechatUser getWechatUserByAccessTokenAndOpenidBySns(GetWechatInfoBySnsParams getWechatInfoBySnsParams) {
		RequestHeader requestHeader = RequestHeaderHolder.get();

		if (requestHeader.getRequestId() == null) {
			requestHeader.setRequestId(UUIDUtils.uuid());
		}

		if (getWechatInfoBySnsParams == null) {
			throw new WxAppTypeNullException(ServiceName.WechatGateway);
		}

		if (StringUtils.isBlank(getWechatInfoBySnsParams.getAccessToken())) {
			throw new WxOpenIdNullException(ServiceName.WechatGateway);
		}

		if (StringUtils.isBlank(getWechatInfoBySnsParams.getOpenId())) {
			throw new WxOpenIdNullException(ServiceName.WechatGateway);
		}

		WechatUser wechatUser = null;
		try {
			wechatUser = wechatGatewayService.getWechatUserByAccessTokenAndOpenidBySns(getWechatInfoBySnsParams.getAccessToken(),
					getWechatInfoBySnsParams.getOpenId());

		} catch (Exception ex) {

		}

		return wechatUser;

	}

	/**
	 * app绑定,由于ticket问题,微信没有绑定的设备
	 * 
	 * @param param
	 * @return
	 */
	@POST
	@Path("wechat_gateway/getRequirdBindDeviceListForApp")
	@AuthIgnore
	public Map<String, List<Device>> getWechatBindDeviceList(WechatBaseParams param) {
		RequestHeader requestHeader = RequestHeaderHolder.get();

		Map<String, List<Device>> resultMap = new HashMap<>();
		List<Device> requireBindDeviceList = new ArrayList<>();
		if (requestHeader.getRequestId() == null) {
			requestHeader.setRequestId(UUIDUtils.uuid());
		}

		AppType appTypeInstance = AppType.getAppTypeByCode(param.getAppType());
		if (appTypeInstance == null) {
			throw new WxAppTypeNullException(ServiceName.WechatGateway);
		}

		String openId = param.getOpenId();
		if (StringUtils.isBlank(openId)) {
			throw new WxOpenIdNullException(ServiceName.WechatGateway);
		}

		String userId = param.getUserId();

		if (StringUtils.isBlank(userId)) {
			throw new WxOpenIdNullException(ServiceName.WechatGateway);
		}

		try {
			String accessToken = wechatGatewayService.getAccessTokenForRedis(appTypeInstance);

			WeChatIotDeviceResponse weChatIotDeviceResponse = wechatGatewayService.getBindDeviceList(accessToken, openId);

			if (logger.isInfoEnabled()) {
				logger.info("wx_getBindDeviceList_" + openId + "_" + JsonUtils.toJson(weChatIotDeviceResponse));
			}

			List<WeChatIotDeviceInfo> wechatDeviceList = new ArrayList<WeChatIotDeviceInfo>();
			if (weChatIotDeviceResponse != null && weChatIotDeviceResponse.getDevice_list() != null
					&& weChatIotDeviceResponse.getDevice_list().size() > 0) {
				wechatDeviceList = weChatIotDeviceResponse.getDevice_list();
			}

			List<String> iotDeviceIdList = new ArrayList<>();
			for (WeChatIotDeviceInfo iotDevice : wechatDeviceList) {
				iotDeviceIdList.add(iotDevice.getDevice_id());
			}

			List<Device> dataList = deviceProvider.getDevices(requestHeader, Long.valueOf(userId));

			if (logger.isInfoEnabled()) {
				logger.info("_getBindDeviceList_" + openId + "_" + JsonUtils.toJson(dataList));
			}

			for (Device dto : dataList) {

				//
				if (ProductTypeEnum.手环.getCode().equals(dto.getProductTypeCode())) {
					// 运动手环BonBonC 运动手环BonBon
					if ("LS410".equals(dto.getModel()) || "LS407".equals(dto.getModel())) {
						if (!iotDeviceIdList.contains(dto.getId().toUpperCase())) {
							requireBindDeviceList.add(dto);
							continue;
						}
					} else {
						continue;
					}
				} else {
					if (!iotDeviceIdList.contains(dto.getId().toUpperCase())) {
						requireBindDeviceList.add(dto);
						continue;
					}
				}
			}

		} catch (Exception ex) {

			logger.error("wx_getBindDeviceList_error" + param.getOpenId(), ex);
		}

		resultMap.put("requireBindDeviceList", requireBindDeviceList);
		logger.info("getBindDeviceList_over_" + openId + "_" + JsonUtils.toJson(requireBindDeviceList));
		return resultMap;
	}

	/**
	 * 获取微信绑定的设备类别:（蓝牙阻断用/统计使用：临时）
	 * 
	 * @param param
	 * @return
	 */
	@POST
	@Path("wechat_gateway/getWechatBindedDeviceList")
	@AuthIgnore
	public List<WeChatIotDeviceInfo> getWechatBindedDeviceList(WechatBaseParams param) {
		RequestHeader requestHeader = RequestHeaderHolder.get();

		List<WeChatIotDeviceInfo> device_list = new ArrayList<>();
		if (requestHeader.getRequestId() == null) {
			requestHeader.setRequestId(UUIDUtils.uuid());
		}

		AppType appTypeInstance = AppType.getAppTypeByCode(param.getAppType());
		if (appTypeInstance == null) {
			throw new WxAppTypeNullException(ServiceName.WechatGateway);
		}

		String openId = param.getOpenId();
		if (StringUtils.isBlank(openId)) {
			throw new WxOpenIdNullException(ServiceName.WechatGateway);
		}

		try {
			String accessToken = wechatGatewayService.getAccessTokenForRedis(appTypeInstance);

			WeChatIotDeviceResponse weChatIotDeviceResponse = wechatGatewayService.getBindDeviceList(accessToken, openId);

			if (logger.isDebugEnabled()) {
				logger.debug("wx_getBindDeviceList_" + openId + "_" + JsonUtils.toJson(weChatIotDeviceResponse));
			}

			if (weChatIotDeviceResponse != null && weChatIotDeviceResponse.getDevice_list() != null
					&& weChatIotDeviceResponse.getDevice_list().size() > 0) {
				return weChatIotDeviceResponse.getDevice_list();
			}

		} catch (Exception ex) {

			logger.error("wx_getBindDeviceList_error" + param.getOpenId(), ex);
			return device_list;
		}
		return device_list;
	}

	public static void main(String[] args) {
		if (StringUtils.isNotBlank("qa") && ("qa".equalsIgnoreCase("qa") || "online".equalsIgnoreCase("qa"))) {
			System.err.println("ok");
		} else {
			System.err.println("not ok");
		}
	}
}
