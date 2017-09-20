package com.yuandu.wechatgateway.service.utils;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.lifesense.base.beans.param.RequestHeader;
import com.lifesense.base.cache.command.RedisObject;
import com.lifesense.base.constant.AppType;
import com.lifesense.base.constant.ServiceName;
import com.lifesense.base.exception.LifesenseBaseException;
import com.lifesense.base.exception.LifesenseResultCode;
import com.lifesense.base.exception.wechat.WxAppTypeNotConfiguredWechatServiceNoException;
import com.lifesense.base.exception.wechat.WxGetDeviceQrcodeErrorException;
import com.lifesense.base.exception.wechat.WxGetDeviceStateErrorException;
import com.lifesense.base.exception.wechat.WxGetParamQrcodeErrorException;
import com.lifesense.base.exception.wechat.WxGetTokenErrorException;
import com.lifesense.base.exception.wechat.WxGetWechatWebpageAccessTokenErrorException;
import com.lifesense.base.exception.wechat.WxResponeDataConvertErrorException;
import com.lifesense.base.exception.wechat.WxUserInfoConvertException;
import com.lifesense.base.exception.wechat.WxUserInfoFetchException;
import com.lifesense.base.exception.wechat.WxVerifyQrcodeErrorException;
import com.lifesense.base.utils.ResourceUtils;
import com.lifesense.base.utils.SystemUtils;
import com.lifesense.base.utils.UUIDUtils;
import com.lifesense.base.utils.code.LifesenseCheckSum;
import com.lifesense.base.utils.json.JsonUtils;
import com.lifesense.base.utils.okhttp.HttpClientManager;
import com.lifesense.soa.wechatgateway.dao.entity.ParamQrcodeEntity;
import com.lifesense.soa.wechatgateway.dto.enums.BusinessTempQrcodeType;
import com.lifesense.soa.wechatgateway.dto.enums.WechatEventTypeEnum;
import com.lifesense.soa.wechatgateway.dto.enums.WechatMessageTypeEnum;
import com.lifesense.soa.wechatgateway.dto.enums.WechatRespondErrCodeEnum;
import com.lifesense.soa.wechatgateway.dto.receive.ParamQrcodeResponse;
import com.lifesense.soa.wechatgateway.dto.receive.VerifyQrcodeResp;
import com.lifesense.soa.wechatgateway.dto.receive.WeChatIotDeviceResponse;
import com.lifesense.soa.wechatgateway.dto.receive.WechatAuthorizeDeviceResp;
import com.lifesense.soa.wechatgateway.dto.receive.WechatAuthorizeDeviceRespInfo;
import com.lifesense.soa.wechatgateway.dto.receive.WechatAuthorizeDeviceSuccessInfo;
import com.lifesense.soa.wechatgateway.dto.receive.WechatDeviceQrcode;
import com.lifesense.soa.wechatgateway.dto.receive.WechatDeviceQrcodeResponse;
import com.lifesense.soa.wechatgateway.dto.receive.WechatDeviceStatusResp;
import com.lifesense.soa.wechatgateway.dto.receive.WechatUser;
import com.lifesense.soa.wechatgateway.dto.receive.WechatWebpageAccessTokenResp;
import com.lifesense.soa.wechatgateway.dto.send.ParamQrcodeParam;
import com.lifesense.soa.wechatgateway.dto.send.WechatServiceNoInfo;
import com.lifesense.soa.wechatgateway.service.constants.WechatGratewayKey;
import com.lifesense.soa.wechatgateway.service.dto.AccessTokenDto;
import com.lifesense.soa.wechatgateway.service.dto.MPUrl;
import com.lifesense.soa.wechatgateway.service.dto.response.QrLimitSceneActonInfo;
import com.lifesense.soa.wechatgateway.service.dto.response.QrLimitSceneParam;
import com.lifesense.soa.wechatgateway.service.dto.response.QrScenActionInfo;
import com.lifesense.soa.wechatgateway.service.dto.response.QrScenStrActionInfo;
import com.lifesense.soa.wechatgateway.service.dto.response.QrSceneParam;
import com.lifesense.soa.wechatgateway.service.dto.response.QrSceneStrParam;
import com.lifesense.soa.wechatgateway.service.dto.response.SceneID;
import com.lifesense.soa.wechatgateway.service.dto.response.SceneStr;
import com.lifesense.soa.wechatgateway.utils.GsonUtil;
import com.lifesense.soa.wechatgateway.utils.StringUtilByWechatGateway;
import com.lifesense.support.context.ServiceContext;

import net.sf.json.JSONObject;
import okhttp3.Response;

public class DataUtil {
	
	private final static Logger logger = LoggerFactory.getLogger(DataUtil.class);
	
	private static String envType = SystemUtils.getEnv();
	
	
	public static final boolean QA_1_ENV = isQa1();
	public static final boolean QA_2_ENV = isQa2();
	public static final boolean ONLINE_ENV = isOnline();

	public static Integer getRandomByQrcodeType(String businessType, int times) {
		List<Integer> typeCodes = Lists.newArrayList();
		BusinessTempQrcodeType businessTempQrcodeTypes[] = BusinessTempQrcodeType.values();
		for (BusinessTempQrcodeType businessTempQrcodeType : businessTempQrcodeTypes) {
			String code = businessTempQrcodeType.code();
			if (NumberUtils.isDigits(code)) {
				typeCodes.add(Integer.valueOf(code));
			}
		}
		++times;
		Integer randomInt = RandomUtils.nextInt(200000000);
		for (Integer type : typeCodes) {
			if (randomInt.toString().startsWith(type.toString())) {
				getRandomByQrcodeType(businessType, times);
			}
		}

		return randomInt;
	}

	public static BusinessTempQrcodeType getBusinessTempQrcodeType(Long eventKey) {
		List<Integer> typeCodes = Lists.newArrayList();

		BusinessTempQrcodeType businessTempQrcodeTypes[] = BusinessTempQrcodeType.values();
		for (BusinessTempQrcodeType businessTempQrcodeType : businessTempQrcodeTypes) {
			String code = businessTempQrcodeType.code();
			if (NumberUtils.isDigits(code)) {
				typeCodes.add(Integer.valueOf(code));
			}
		}

		for (Integer code : typeCodes) {
			if (eventKey.toString().startsWith(code.toString())) {
				return BusinessTempQrcodeType.valueOf(code.toString());
			}
		}
		return null;
	}
	

	/**
	 * 初始化带参二维码
	 * 
	 * @return 带参二维码
	 */

	public static ParamQrcodeEntity initParamQrcodeEntity(ParamQrcodeParam param, ParamQrcodeResponse paramQrcodeResponse,
			long sceneId, String sceneStr, boolean useFlag) {
		// 根据应用类型获取对应的公众号原始ID
		String wechatServiceNo = MPManager.getWechatServiceNoInfoByApptype(param.getAppType()).getServiceNo();

		ParamQrcodeEntity qrcodeEntity = new ParamQrcodeEntity();
		qrcodeEntity.setWechatServiceNo(wechatServiceNo);
		qrcodeEntity.setId(UUIDUtils.uuid());
		qrcodeEntity.setActionName(param.getActionName());
		qrcodeEntity.setBusinessType(param.getBusinessType());
		qrcodeEntity.setDeleted(false);
		qrcodeEntity.setExpireseconds(paramQrcodeResponse.getExpire_seconds());
		qrcodeEntity.setSceneId(sceneId);
		qrcodeEntity.setSceneStr(sceneStr);
		qrcodeEntity.setTicket(paramQrcodeResponse.getTicket());
		qrcodeEntity.setUrl(paramQrcodeResponse.getUrl());
		qrcodeEntity.setUseFlag(useFlag);
		qrcodeEntity.setCreated(System.currentTimeMillis());

		return qrcodeEntity;
	}
	

	// 获取场景值ID为数字的请求参数
	public static  String getQrSceneJson(long sceneId, String acctonName, long expireSeconds) {
		SceneID sceneID = new SceneID();

		sceneID.setScene_id(sceneId);
		QrScenActionInfo scenActionInfo = new QrScenActionInfo();
		scenActionInfo.setScene(sceneID);
		QrSceneParam sceneParam = new QrSceneParam();
		sceneParam.setAction_info(scenActionInfo);
		sceneParam.setAction_name(acctonName);
		sceneParam.setExpire_seconds(expireSeconds);
		String jsonMessage = GsonUtil.toJson(sceneParam);

		return jsonMessage;
	}
	// 获取场景值ID为字符串的请求参数
	public static  String getQrStrSceneJson(String sceneStr, String acctonName, long expireSeconds) {
		SceneStr scene = new SceneStr();

		scene.setScene_str(sceneStr);
		QrScenStrActionInfo scenActionInfo = new QrScenStrActionInfo();
		scenActionInfo.setScene(scene);
		QrSceneStrParam sceneParam = new QrSceneStrParam();
		sceneParam.setAction_info(scenActionInfo);
		sceneParam.setAction_name(acctonName);
		sceneParam.setExpire_seconds(expireSeconds);
		String jsonMessage = GsonUtil.toJson(sceneParam);

		return jsonMessage;
	}
	// 获取场景值ID为字符串的请求参数
	public static  String getQrSceneStrJson(String sceneString, String acctonName) {
		SceneStr sceneStr = new SceneStr();

		sceneStr.setScene_str(sceneString);
		QrLimitSceneActonInfo scenActionInfo = new QrLimitSceneActonInfo();
		scenActionInfo.setScene(sceneStr);
		QrLimitSceneParam sceneParam = new QrLimitSceneParam();
		sceneParam.setAction_info(scenActionInfo);
		sceneParam.setAction_name(acctonName);
		String jsonMessage = GsonUtil.toJson(sceneParam);

		return jsonMessage;
	}

	/**
	 * 把带参二维码JSON串转换成带参二维码对象
	 * 
	 * @param jsonWechatDeviceStatusResp
	 *            带参二维码JSON串
	 * @return 带参二维码对象
	 */
	public static  ParamQrcodeResponse jsonParamQrcodeResponse(String jsonParamQrcodeResponse) {
		ParamQrcodeResponse paramQrcodeResponse = new ParamQrcodeResponse();
		try {
			// 返回JSON转换成带参二维码响应对象
			paramQrcodeResponse = GsonUtil.toBean(jsonParamQrcodeResponse, ParamQrcodeResponse.class);

			// 判断返回带参二维码的错误代码是否为0
			if (paramQrcodeResponse.getErrcode() != null
					&& paramQrcodeResponse.getErrcode() != WechatRespondErrCodeEnum.发送成功.toInteger()) {
				// 抛出获取带参二维码异常
				throw new WxGetParamQrcodeErrorException(ServiceName.WechatGateway);
			} else {
				return paramQrcodeResponse;
			}
		} catch (RuntimeException e) {
			// 转换错误时抛出异常
			throw new WxResponeDataConvertErrorException(ServiceName.WechatGateway);
		}
	}
	

	public static AccessTokenDto queryAccessTokenByOtherGateWay( Integer queryTimes, AppType appType) {
		// 如果查询3次都失败就不查询了
		if (queryTimes > 3) {
			return null;
		}
		long timestamp = new Date().getTime();
		String prequeryTokenUrl = ResourceUtils.get("query_token_url");
		
		StringBuilder queryTokenUrl = new StringBuilder(prequeryTokenUrl + "?app_type_code=" + appType.code() + "&requestId=" + UUIDUtils.uuid());
		queryTokenUrl.append("&timestamp=").append(timestamp).append("&checksum=").append(LifesenseCheckSum.create().checksum(timestamp+""));
		// 调用接口获取access_token
		String body = HttpClientManager.get(new RequestHeader(), queryTokenUrl.toString());
		if (logger.isDebugEnabled()) {
			logger.debug("getAccessTokenForMPByWechatServiceNo_" + appType + "\n body:" + body);
		}
		if (StringUtils.isBlank(body)) {
			logger.error("fetch_token_by_other_error_" + appType + "_body_" + body);
			throw new LifesenseBaseException(502, "query_token_url获取qa token失败");
		}

		JSONObject json = JSONObject.fromObject(body);

		// 获取accessToken
		String accessTokenData = json.getString(WechatGratewayKey.ACCESS_TOKEN_DATA);

		JSONObject accessTokenJson = JSONObject.fromObject(accessTokenData);
		String accessToken = accessTokenJson.getString(WechatGratewayKey.ACCESS_TOKEN);
		// 获取accessToken有效时间
		Long expiresIn = 11l;

		// 构造AccessToken对象
		AccessTokenDto tokenDto = new AccessTokenDto();
		tokenDto.setAccessToken(accessToken);
		tokenDto.setExpiresIn(expiresIn);
		tokenDto.setTimestamp(System.currentTimeMillis());

		return tokenDto;
	}
	
	
	/**
	 * 向微信获取（4-queryTimes）次access_token
	 * 
	 * @param queryTimes 获取次数
	 * @return
	 */
	public static AccessTokenDto getAccessTokenForMPByWechatServiceNo(Integer queryTimes, AppType appType) {
		AccessTokenDto tokenDto = null;

		if (determineProcessSlaveType()) {

			logger.info("fetch_accessToken_by_queryAccessTokenByOtherGateWay_" + appType);
			tokenDto = DataUtil.queryAccessTokenByOtherGateWay(queryTimes, appType);
		} else {
			logger.info("fetch_accessToken_by_queryAccessTokenByWechatInterface_" + appType);
			tokenDto = queryAccessTokenByWechatInterface( queryTimes, appType);
		}

		return tokenDto;
	}
	
	private static boolean isQa1() {
		if(StringUtils.isNotBlank(envType) && "lifesense-qa".equals(envType)) {
			return true;
		}else {
			return false;
		}
	}
	
	private static boolean isQa2(){
		if(StringUtils.isNotBlank(envType) && "lifesense-qa2".equals(envType)) {
			return true;
		}else {
			return false;
		}
	}
	
	private static boolean isOnline(){
		if(StringUtils.isNotBlank(envType) && "online".equals(envType)) {
			return true;
		}else {
			return false;
		}
	}
	
	
	
	public  static boolean determineProcessSlaveType() {
		if (QA_1_ENV || ONLINE_ENV) {
			return false;
		} else {
			return true;
		}
	}
	public static AccessTokenDto queryAccessTokenByWechatInterface(Integer queryTimes, AppType appType) {
		// 如果查询3次都失败就不查询了
		if (queryTimes > 3) {
			return null;
		}

		// 根据公众号从缓存中获取公众号相关消息
		WechatServiceNoInfo wechatServiceNoInfo = MPManager.getWechatServiceNoInfoByApptype(appType);
		if (wechatServiceNoInfo == null) {
			return null;
		}

		// 从缓存中获取微信服务器的相关链接
		MPUrl mpUrl = MPManager.getMPUrl();
		if (mpUrl == null) {
			return null;
		}

		String appid = wechatServiceNoInfo.getAppid();
		String appSecret = wechatServiceNoInfo.getSecret();
		if (logger.isDebugEnabled()) {
			logger.debug("WechatServiceNoInfo_appType_" + appType.code() + "_" + JsonUtils.toJson(wechatServiceNoInfo));
		}

		// 调用接口获取access_token
		String body = HttpClientManager.get(new RequestHeader(), MessageFormat.format(mpUrl.getTokenUrl(), appid, appSecret));
		if (logger.isDebugEnabled()) {
			logger.debug("getAccessTokenForMPByWechatServiceNo_" + wechatServiceNoInfo + "\n body:" + body);
		}

		if (StringUtils.isBlank(body)) {
			logger.error("fetch_token_by_wp_error_" + appType + "_body_" + body);
		}
		JSONObject json = JSONObject.fromObject(body);

		if (StringUtils.isEmpty(body) || !(WechatRespondErrCodeEnum.发送成功 == MPManager.checkErrcode(json))) {
			// 获取失败
			queryTimes++;
			// 递归获取
			return getAccessTokenForMPByWechatServiceNo( queryTimes, appType);
		}

		// 获取accessToken
		String accessToken = json.getString(WechatGratewayKey.ACCESS_TOKEN);
		// 获取accessToken有效时间
		Long expiresIn = json.getLong(WechatGratewayKey.EXPIRES_IN);

		// 构造AccessToken对象
		AccessTokenDto tokenDto = new AccessTokenDto();
		tokenDto.setAccessToken(accessToken);
		tokenDto.setExpiresIn(expiresIn);
		tokenDto.setTimestamp(System.currentTimeMillis());
		return tokenDto;
	}
	
	/**
	 * 更新access_token
	 * 
	 * @param appType 应用类型
	 * @return 最新的accessToken
	 */
	public static String updateAccessToken(AppType appType) {
		String wechatServiceNo = MPManager.getWechatServiceNoInfoByApptype(appType).getServiceNo();

		if (StringUtils.isEmpty(wechatServiceNo)) {
			throw new WxAppTypeNotConfiguredWechatServiceNoException(ServiceName.WechatGateway);
		}

		// 从微信服务器获取AccessToken的对象
		AccessTokenDto tokenDto = DataUtil.getAccessTokenForMPByWechatServiceNo(1, appType);
		if (tokenDto == null || StringUtils.isEmpty(tokenDto.getAccessToken())) {
			// 抛出获取AccessToken异常
			throw new WxGetTokenErrorException(ServiceName.WechatGateway);
		}

		// 把AccessToken对象存入缓存
		RedisObject redis = new RedisObject(getRedisAccessTokenKey(wechatServiceNo));
		redis.set(tokenDto, tokenDto.getExpiresIn());

		String accessToken = tokenDto.getAccessToken();
		logger.info("get_accessToken_by_wechatserver_" + wechatServiceNo + "_" + accessToken);

		return accessToken;
	}

	/**
	 * 获取AccessToken的 Redis Key
	 */
	public static String getRedisAccessTokenKey(String wechatServiceNo) {
		return wechatServiceNo + WechatGratewayKey.REDIS_ACCESS_TOKEN;
	}
	
	public static WeChatIotDeviceResponse jsonToWeChatIotDeviceResponse(String body) {
		WeChatIotDeviceResponse response = new WeChatIotDeviceResponse();
		try {
			// 返回JSON转换成带参二维码响应对象
			response = GsonUtil.toBean(body, WeChatIotDeviceResponse.class);

			// 判断返回带参二维码的错误代码是否为0
			if (response != null && response.getResp_msg() != null && response.getResp_msg().getRet_code() != null
					&& response.getResp_msg().getRet_code() == -1) {
				// 抛出获取带参二维码异常
				throw new LifesenseBaseException(ServiceName.WechatGateway, LifesenseResultCode.WX_RESPONE_DATA_CONVERT_ERROR.getCode(),
						"from_iowechat_getbindlist_res_error");
			} else {
				return response;
			}
		} catch (RuntimeException e) {
			// 转换错误时抛出异常
			throw new WxResponeDataConvertErrorException(ServiceName.WechatGateway);
		}
	}
	/**
	 * 获取Ticket的 Redis Key
	 */
	public static  String getRedisTicketKey(String wechatServiceNo) {
		return wechatServiceNo + WechatGratewayKey.REDIS_TICKET;
	}
	/**
	 * 把设备状态信息JSON串转换成设备状态对象
	 * 
	 * @param jsonWechatDeviceStatusResp 设备状态JSON串
	 * @return 设备状态对象
	 */
	public static WechatDeviceStatusResp jsonToWechatDeviceStatus(String jsonWechatDeviceStatusResp) {
		WechatDeviceStatusResp deviceStatusResp = new WechatDeviceStatusResp();
		try {
			// 返回JSON转换成设备状态对象
			deviceStatusResp = GsonUtil.toBean(jsonWechatDeviceStatusResp, WechatDeviceStatusResp.class);

			// 判断返回设备状态信息的错误代码是否为0
			if (deviceStatusResp.getErrcode() != null && deviceStatusResp.getErrcode() != WechatRespondErrCodeEnum.发送成功.toInteger()) {
				// 出获取设备状态异常
				throw new WxGetDeviceStateErrorException(ServiceName.WechatGateway);
			} else {
				return deviceStatusResp;
			}
		} catch (RuntimeException e) {
			// 转换错误时抛出异常
			throw new WxResponeDataConvertErrorException(ServiceName.WechatGateway);
		}
	}
	
	/**
	 * 把微信用户信息JSON串转换成设备二维码列表对象
	 * 
	 * @param jsonWechatDeviceQrcodeResponse 设备二维码响应JSON串
	 * @return 设备二维码列表对象
	 */
	public static List<WechatDeviceQrcode> jsonToWecharDeviceQrcode(String jsonWechatDeviceQrcodeResponse) {
		WechatDeviceQrcodeResponse deviceQrcodeResponse = new WechatDeviceQrcodeResponse();
		try {
			// 返回JSON转换成微信用户对象
			deviceQrcodeResponse = GsonUtil.toBean(jsonWechatDeviceQrcodeResponse, WechatDeviceQrcodeResponse.class);

			// 判断返回设备二维码信息的错误代码是否为0
			if (deviceQrcodeResponse.getErrcode() != null && deviceQrcodeResponse.getErrcode() != WechatRespondErrCodeEnum.发送成功.toInteger()) {
				// 抛出获取设备二维码异常
				throw new WxGetDeviceQrcodeErrorException(ServiceName.WechatGateway);
			} else {
				return deviceQrcodeResponse.getCode_list();
			}
		} catch (RuntimeException e) {
			// 转换错误时抛出异常
			throw new WxUserInfoConvertException(ServiceName.WechatGateway);
		}
	}
	/**
	 * 把微信请求响应转换成网页授权AccessToken信息
	 * 
	 * @param jsonAccessTokenResp 网页授权AccessToken信息JSON串
	 * @return 网页授权AccessTokend对象
	 */
	public static WechatWebpageAccessTokenResp jsonToWechatWebpageAccessTokenResp(String jsonAccessTokenResp) {
		WechatWebpageAccessTokenResp accessTokenResp = new WechatWebpageAccessTokenResp();
		try {
			// 返回JSON转换成网页授权AccessToken对象
			accessTokenResp = GsonUtil.toBean(jsonAccessTokenResp, WechatWebpageAccessTokenResp.class);

			// 判断返回网页授权AccessToken的错误代码是否为0
			if (accessTokenResp.getErrcode() != null && accessTokenResp.getErrcode() != WechatRespondErrCodeEnum.发送成功.toInteger()) {
				// 抛出获取网页授权AccessToken失败
				throw new WxGetWechatWebpageAccessTokenErrorException(ServiceName.WechatGateway);
			} else {
				return accessTokenResp;
			}
		} catch (RuntimeException e) {
			logger.error("jsonAccessTokenResp_error_" + JsonUtils.toJson(accessTokenResp));
			// 转换错误时抛出异常
			// throw new WxUserInfoConvertException(ServiceName.WechatGateway);
		}
		return null;
	}
	/**
	 * 根据messageType获取对应的API接口URL
	 * 
	 * @param messageType
	 * @return 访问链接
	 */
	public static String getApiUrl(String messageType) {
		String apiUrl = null;

		if (messageType.equals(WechatMessageTypeEnum.文本消息.toString()) || messageType.equals(WechatMessageTypeEnum.语音消息.toString())
				|| messageType.equals(WechatMessageTypeEnum.图文消息.toString())) {
			apiUrl = MPManager.getMPUrl().getCustomerServiceUrl();
		} else if (messageType.equals(WechatMessageTypeEnum.模板消息.toString())) {
			apiUrl = MPManager.getMPUrl().getTemplateServiceUrl();
		} else if (messageType.equals(WechatMessageTypeEnum.发送设备状态消息.toString())) {
			apiUrl = MPManager.getMPUrl().getTransmsgUrl();
		} else if (messageType.equals(WechatEventTypeEnum.绑定设备事件.toString())) {
			apiUrl = MPManager.getMPUrl().getBindDeviceUrl();
		} else if (messageType.equals(WechatEventTypeEnum.解绑设备事件.toString())) {
			apiUrl = MPManager.getMPUrl().getUnbindDeviceUrl();
		} else if (messageType.equals(WechatEventTypeEnum.强制绑定设备事件.toString())) {
			apiUrl = MPManager.getMPUrl().getCompelBindDeviceUrl();
		} else if (messageType.equals(WechatEventTypeEnum.强制解绑设备事件.toString())) {
			apiUrl = MPManager.getMPUrl().getCompelUnbindDeviceUrl();
		}
		return apiUrl;
	}
	/**
	 * 把微信用户信息JSON串转换成微信用户对象
	 * 
	 * @param jsonWechatUser 微信用户JSON串
	 * @return 微信用户对象
	 */
	public static WechatUser jsonToWechatUser(String jsonWechatUser) {
		WechatUser userInfo = new WechatUser();
		try {
			// 返回JSON转换成微信用户对象
			userInfo = GsonUtil.toBean(jsonWechatUser, WechatUser.class);

			// 判断返回微信用户信息的错误代码是否为0
			if (userInfo.getErrcode() != null && userInfo.getErrcode() != WechatRespondErrCodeEnum.发送成功.toInteger()) {
				throw new WxUserInfoFetchException(ServiceName.WechatGateway);
			} else {
				return userInfo;
			}
		} catch (RuntimeException e) {
			// 转换错误时抛出异常
			throw new WxUserInfoConvertException(ServiceName.WechatGateway);
		}
	}
	/**
	 * @param url
	 * @return 文件流
	 */
	public static byte[] getMediaStreamForByte(String url) {
		Response response = HttpClientManager.getOkHttpHandler().syncGet(url, null);
		InputStream is = response.body().byteStream();

		try {
			if (is == null) {
				return null;
			}

			byte[] voiceBytes = StringUtilByWechatGateway.input2byte(is);
			return voiceBytes;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 设备授权响应信息JSON串转换成授权成功设备列表对象
	 * 
	 * @param jsonWechatAuthorizeDeviceResp 设备授权响应JSON串
	 * @return 授权成功设备列表对象
	 */
	public static  List<WechatAuthorizeDeviceSuccessInfo> jsonToWechatAuthorizeDevice(String jsonWechatAuthorizeDeviceResp) {
		WechatAuthorizeDeviceResp authorizeDeviceResp = new WechatAuthorizeDeviceResp();
		List<WechatAuthorizeDeviceSuccessInfo> authorizeDeviceSuccessInfos = new ArrayList<>();
		try {
			// 返回JSON转换成设备授权响应对象
			authorizeDeviceResp = GsonUtil.toBean(jsonWechatAuthorizeDeviceResp, WechatAuthorizeDeviceResp.class);

			if (authorizeDeviceResp == null || authorizeDeviceResp.getResp() == null || authorizeDeviceResp.getResp().isEmpty()) {
				return null;
			} else {
				for (WechatAuthorizeDeviceRespInfo authorizeDeviceRespInfo : authorizeDeviceResp.getResp()) {
					// 判断是否授权成功
					if (authorizeDeviceRespInfo.getErrcode() == null
							|| authorizeDeviceRespInfo.getErrcode() == WechatRespondErrCodeEnum.发送成功.toInteger()) {
						authorizeDeviceSuccessInfos.add(authorizeDeviceRespInfo.getBase_info());
					}
				}
			}
			return authorizeDeviceSuccessInfos;
		} catch (RuntimeException e) {
			// 转换错误时抛出异常
			throw new WxResponeDataConvertErrorException(ServiceName.WechatGateway);
		}
	}
	
	/**
	 * 把设验证设备二维码信息JSON串转换成验证设备二维码对象
	 * 
	 * @param jsonVerifyQrcodeResp 验证设备二维码JSON串
	 * @return 验证设备二维码对象
	 */
	public static VerifyQrcodeResp jsonToVerifyQrcodeResp(String jsonVerifyQrcodeResp) {
		VerifyQrcodeResp verifyQrcodeResp = new VerifyQrcodeResp();
		try {
			// 返回JSON转换成验证设备二维码响应对象
			verifyQrcodeResp = GsonUtil.toBean(jsonVerifyQrcodeResp, VerifyQrcodeResp.class);

			// 判断返回设备状态信息的错误代码是否为0
			if (verifyQrcodeResp.getErrcode() != null && verifyQrcodeResp.getErrcode() != WechatRespondErrCodeEnum.发送成功.toInteger()) {
				// 应该抛出验证设备二维码异常
				throw new WxVerifyQrcodeErrorException(ServiceName.WechatGateway);
			} else {
				return verifyQrcodeResp;
			}
		} catch (RuntimeException e) {
			// 转换错误时抛出异常
			throw new WxResponeDataConvertErrorException(ServiceName.WechatGateway);
		}
	}
	
	/**
	 * @param url
	 * @return 文件流
	 */
	public static String getMediaStream(String url) {
		Response response = HttpClientManager.getOkHttpHandler().syncGet(url, null);
		InputStream is = response.body().byteStream();

		try {
			// 转base64
			byte[] voiceBytes = StringUtilByWechatGateway.input2byte(is);
			String voiceStr = Base64Util.encode(voiceBytes);

			return voiceStr;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 提倡不使用ServiceContext，但很多方法使用到了requestId,这里做了一个统一调用,其它地方无需使用
	 * 
	 * @return
	 */
	public static String getRequestId() {
		String requestId = ServiceContext.get().getRequestId();
		if (StringUtils.isEmpty(requestId)) {
			requestId = UUIDUtils.uuid();
		}
		return requestId;
	}

}
