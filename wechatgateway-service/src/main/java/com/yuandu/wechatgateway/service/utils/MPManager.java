/**
 * 
 */
package com.yuandu.wechatgateway.service.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lifesense.base.constant.AppType;
import com.lifesense.base.exception.LifesenseBaseException;
import com.lifesense.base.utils.ResourceUtils;
import com.lifesense.base.utils.json.JsonUtils;
import com.lifesense.soa.wechatgateway.dto.enums.WechatRespondErrCodeEnum;
import com.lifesense.soa.wechatgateway.dto.send.WechatServiceNoInfo;
import com.lifesense.soa.wechatgateway.service.dto.MPUrl;
import com.lifesense.soa.wechatgateway.utils.StringUtilByWechatGateway;

import net.sf.json.JSONObject;

/** 
 * ClassName: WechatServiceNoManager
 * date: 2015年12月23日 下午4:47:35
 * 
 * 微信服务器工具类
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2015, lifesense.com All Rights Reserved.
 */
public class MPManager
{
	private static final Logger logger = LoggerFactory.getLogger(MPManager.class);
	
	/**
	 * <b>公众账号token刷新开关</b><br>
	 * false:token来自于url(包括ticket)；<br>
	 * true:token通过appid和Secret刷新</br>
	 * 
	 ***/
	public static boolean TokenSwitch = true;
	
	public static volatile boolean isInit = true;
	
	private static Map<String,WechatServiceNoInfo> wechatServiceNoInfoMap = new HashMap<>();
	
	private static Map<String,MPUrl> mpUrlMap = new HashMap<>();
	/**
	 * <b>运动公众账号根据URL对token刷新开关</b>
	 ***/
	public static Map<String, String> RefreshTokenUrlMaps = new HashMap<String, String>(); 
	
	
	public static void initConf() {
		StringBuffer sf = new StringBuffer();
		sf.append("===========清空公众号缓存==start=========\n");
		try{
			for (AppType at : AppType.values()) {
				wechatServiceNoInfoMap.remove("wechatServiceNoInfo_"+at.code());
				//new RedisObject(String.format(WechatGratewayKey.WECHAT_SERVICENO_INFO,at.code()),WechatGratewayKey.WECHAT_GROUP_NAME).remove();
			}
		}catch(LifesenseBaseException e){
			sf.append("清空公众号缓存发送异常:"+e.getMessage()).append("\n");
			logger.error("清空公众号缓存发送异常",e);
		}
		try{
			mpUrlMap.remove("mpUrl2");
		}catch(LifesenseBaseException e){
			sf.append("清空mpUrl2缓存发送异常:"+e.getMessage()+"\n");
			logger.error("清空mpUrl2缓存发送异常",e);
		}
		sf.append("===========清空公众号缓存==end=========\n");
		
		
		sf.append("===========读取公众号token刷新配置==start=========\n");
		try {
			TokenSwitch = Boolean.parseBoolean(ResourceUtils.get("tokenSwitch", "true"));
			sf.append("读取tokenSwitch值["+TokenSwitch+"]"+ResourceUtils.get("tokenSwitch")+"\n");
		} catch (LifesenseBaseException e) {
			logger.error("读取tokenSwitch配置异常：",e);
		}
		String sno = null;
		for (AppType appType : AppType.values()) {
			try {
				sno = MPManager.getWechatServiceNoInfoByApptype(appType).getServiceNo();
				RefreshTokenUrlMaps.put(appType.code().toString(), ResourceUtils.get(sno+"_RefreshTokenUrl"));
				MPManager.getWechatServiceNoInfoByApptype(appType).getServiceNo();
			} catch (LifesenseBaseException e) {
				logger.error("读取RefreshTokenUrl等配置异常：",e);
			}
		}
		sf.append("===========读取公众号token刷新配置==start=========\n");
		
		logger.info(sf.toString());
		
	}
	/**
	 * 根据公众号原始ID获取公众号信息
	 * @param wechatServiceNo 公众号原始ID
	 * @return 公号信息
	 * */
	public static WechatServiceNoInfo getWechatServiceNoInfoByApptype(AppType appType)
	{
		//根据应用类型获取公众号原始ID
		int code=appType.code();
		
		if(appType.equals(AppType.未知))
		{
			code=9;
		}
		
		WechatServiceNoInfo wechatServiceNoInfo=wechatServiceNoInfoMap.get("wechatServiceNoInfo_"+code);
		
		if(wechatServiceNoInfo==null||StringUtils.isBlank(wechatServiceNoInfo.getServiceNo()))
		{
			//从配置文件中户获取公众号信息
			wechatServiceNoInfo=getWechatServiceNoInfoByProperties(appType);
			
			//存入缓存，缓存时间为一个周
			wechatServiceNoInfoMap.put("wechatServiceNoInfo_"+code, wechatServiceNoInfo);
		}
		return wechatServiceNoInfo;
	}
	
	
	/***
	 * 根据公众号原始ID获取应用类型
	 * */
	public static AppType getAppTypeByWechatServiceNo(String wechatServiceNo)
	{
		logger.info("!!!!getAppTypeByWechatServiceNo:"+wechatServiceNo);
		WechatServiceNoInfo wechatServiceNoInfo=getWechatServiceNoInfoByApptype(AppType.乐心健康APP);
		if(wechatServiceNoInfo!=null&&wechatServiceNoInfo.getServiceNo().equals(wechatServiceNo))
		{
			return AppType.乐心健康APP;
		}else{
			logger.info("wechatServiceNoInfo_jiank_app_serviceNo:"+wechatServiceNo+","+JsonUtils.toJson(wechatServiceNoInfo));
		}
		
		wechatServiceNoInfo=getWechatServiceNoInfoByApptype(AppType.乐心健康WECHAT);
		if(wechatServiceNoInfo!=null&&wechatServiceNoInfo.getServiceNo().equals(wechatServiceNo))
		{
			return AppType.乐心健康WECHAT;
		}
		
		wechatServiceNoInfo=getWechatServiceNoInfoByApptype(AppType.乐心健康医生APP端);
		if(wechatServiceNoInfo!=null&&wechatServiceNoInfo.getServiceNo().equals(wechatServiceNo))
		{
			return AppType.乐心健康医生APP端;
		}
		
		wechatServiceNoInfo=getWechatServiceNoInfoByApptype(AppType.乐心健康医生PC端);
		if(wechatServiceNoInfo!=null&&wechatServiceNoInfo.getServiceNo().equals(wechatServiceNo))
		{
			return AppType.乐心健康医生PC端;
		}
		
		wechatServiceNoInfo=getWechatServiceNoInfoByApptype(AppType.乐心运动APP);
		if(wechatServiceNoInfo!=null&&wechatServiceNoInfo.getServiceNo().equals(wechatServiceNo))
		{
			return AppType.乐心运动APP;
		}
		
		wechatServiceNoInfo=getWechatServiceNoInfoByApptype(AppType.乐心运动WECHAT);
		logger.info("!!!!wechatServiceNoInfo serviceNo:"+wechatServiceNo+","+wechatServiceNoInfo.getServiceNo());
		if(wechatServiceNoInfo!=null&&wechatServiceNoInfo.getServiceNo().equals(wechatServiceNo))
		{
			return AppType.乐心运动WECHAT;
		}
		
		wechatServiceNoInfo=getWechatServiceNoInfoByApptype(AppType.企业后台);
		if(wechatServiceNoInfo!=null&&wechatServiceNoInfo.getServiceNo().equals(wechatServiceNo))
		{
			return AppType.企业后台;
		}
		
		wechatServiceNoInfo=getWechatServiceNoInfoByApptype(AppType.未知);
		if(wechatServiceNoInfo!=null&&wechatServiceNoInfo.getServiceNo().equals(wechatServiceNo))
		{
			return AppType.未知;
		}
		
		wechatServiceNoInfo=getWechatServiceNoInfoByApptype(AppType.基础平台PC端);
		if(wechatServiceNoInfo!=null&&wechatServiceNoInfo.getServiceNo().equals(wechatServiceNo))
		{
			return AppType.基础平台PC端;
		}
		
		wechatServiceNoInfo=getWechatServiceNoInfoByApptype(AppType.乐心官网);
		if(wechatServiceNoInfo!=null && wechatServiceNoInfo.getServiceNo()!=null &&wechatServiceNoInfo.getServiceNo().equals(wechatServiceNo))
		{
			return AppType.乐心官网;
		}
		
		return null;
	}
	
	/**
	 * 获取微信服务器相关链接
	 * @return 微信服务服务器相关链接
	 * */
	public static MPUrl getMPUrl()
	{
		
		MPUrl mpUrl= mpUrlMap.get("mpUrl2");
		
		if(mpUrl==null||StringUtilByWechatGateway.isBlankOrNull(mpUrl.getAccessTokenUrl))
		{
			//从配置文件中户获取微信服务器相关连接
			mpUrl=getMPUrlByProperties();
			
			//存入缓存，缓存时间为一个周
			mpUrlMap.put("mpUrl2",mpUrl);	
		}
		return mpUrl;
	}
	
	/**
	 * 判断微信服务器返回 errcode结果代码
	 * 
	 * @param errcode
	 * @return int 0-成功，1-accessToken超时或失效后重发3次，2-未知错误
	 */
	public static WechatRespondErrCodeEnum checkErrcode(JSONObject json) 
	{
		if (json.containsKey("base_resp")) 
		{
			json = json.getJSONObject("base_resp");
		}
		
		// errcode判断结果代码
		// 兼容其他返回值
		int errcode = 0;
		
		if (json.containsKey("errcode")) 
		{
			errcode = json.getInt("errcode");
		}
		else if (json.containsKey("ret")) 
		{
			errcode = json.getInt("ret");
		}
		
		if (0 == errcode || 43004 == errcode) 
		{ 
			// 已发送成功，说明43004表示需要接收者关注（即用户已取消关注）
			return WechatRespondErrCodeEnum.发送成功;
		}
		else if (42001 == errcode || 40001 == errcode || 40002 == errcode|| 40014 == errcode) 
		{ 
			// accessToken超时或失效后重发3次
			return WechatRespondErrCodeEnum.AcessToken超时;
		} 
		else if(45015 == errcode)
		{
			// 回复时间超过限制(客服接口，用户与公众号超48小时没有交互)
			return WechatRespondErrCodeEnum.用户与微信公众号超过48小时没有交互;
		} else if(45009 == errcode)
			return WechatRespondErrCodeEnum.接口调用超过限制;
			
		else 
		{ 
			// 未知错误
			return WechatRespondErrCodeEnum.未知错误;
		}
	}
	
	
	/**
	 * 从配置文件中根据公众号原始ID读取公众号信息
	 * @param wechatServiceNo 公众号原始ID
	 * @return 公号信息
	 * */
	private static WechatServiceNoInfo getWechatServiceNoInfoByProperties(AppType appType)
	{
			WechatServiceNoInfo wechatServiceNoInfo=new WechatServiceNoInfo();
			
//			logger.debug("env-config/wechart_service_no_"+wechatServiceNo+".properties");
			
//			Map<String, String> propMap = PropertiesUtil.readPropertiesForMap("env-config/wechart_service_no_"+wechatServiceNo+".properties");
			
			Integer code=appType.code();
			
			if(appType.equals(AppType.未知))
			{
				code=9;
			}
			
			wechatServiceNoInfo.setServiceNo(ResourceUtils.get("service_no_"+code.toString()));
			wechatServiceNoInfo.setAppid(ResourceUtils.get("appid_"+code.toString()));
			wechatServiceNoInfo.setSecret(ResourceUtils.get("secret_"+code.toString()));
			wechatServiceNoInfo.setStepTemplateId(ResourceUtils.get("stepTemplateId_"+code.toString()));
			wechatServiceNoInfo.setWeightTemplateId(ResourceUtils.get("weightTemplateId_"+code.toString()));
			
			return wechatServiceNoInfo;
	}
	
	/**
	 * 从配置文件中获取微信服务器相关链接
	 * @return 微信服务服务器相关链接
	 * */
	private static MPUrl getMPUrlByProperties()
	{	
			MPUrl mpUrl=new MPUrl();
			
//			Map<String, String> propMap = PropertiesUtil.readPropertiesForMap("env-config/mp_url.properties");
			
			//初始化设备授权URL
//			mpUrl.setAuthorizeDeviceUrl(propMap.get("AUTHORIZE_DEVICE_URL"));
			mpUrl.setAuthorizeDeviceUrl(ResourceUtils.get("AUTHORIZE_DEVICE_URL"));
			
			//初始化网页授权获取用户信息URL
//			mpUrl.setAuthorizeUserInfoUrl(propMap.get("AUTHORIZE_USERINFO_URL"));
			mpUrl.setAuthorizeUserInfoUrl(ResourceUtils.get("AUTHORIZE_USERINFO_URL"));
			
			//初始化强制绑定用户和设备URL
//			mpUrl.setCompelBindDeviceUrl(propMap.get("COMPEL_BIND_DEVICE_URL"));
			mpUrl.setCompelBindDeviceUrl(ResourceUtils.get("COMPEL_BIND_DEVICE_URL"));
			
			//初始化强制解绑用户和设备URL
//			mpUrl.setCompelUnbindDeviceUrl(propMap.get("COMPEL_UNBIND_DEVICE_URL"));
			mpUrl.setCompelUnbindDeviceUrl(ResourceUtils.get("COMPEL_UNBIND_DEVICE_URL"));
			
			//初始化批量创建二维码URL
//			mpUrl.setCreateOrcodeUrl(propMap.get("CREATE_QRCODE_URL"));
			mpUrl.setCreateOrcodeUrl(ResourceUtils.get("CREATE_QRCODE_URL"));
			
			//生成微信的设备二维码和设备ID
//			mpUrl.setCreateOrcodeUrl(propMap.get("CREATE_QRCODE_URL"));
			mpUrl.setCreateOrcodeUrl2(ResourceUtils.get("CREATE_QRCODE_URL2"));
			
			
			//初始化发送客服消息URL
//			mpUrl.setCustomerServiceUrl(propMap.get("CUSTOMER_SERVICE_URL"));
			mpUrl.setCustomerServiceUrl(ResourceUtils.get("CUSTOMER_SERVICE_URL"));
			
			//初始化通过code换取网页授权access_token的URL
//			mpUrl.setGetAccessTokenUrl(propMap.get("GET_ACCESS_TOKEN_URL"));
			mpUrl.setGetAccessTokenUrl(ResourceUtils.get("GET_ACCESS_TOKEN_URL"));
			
			//初始化获取用户绑定设备URL
//			mpUrl.setGetBindedDevice(propMap.get("GET_BINDED_DEVICE"));
			mpUrl.setGetBindedDevice(ResourceUtils.get("GET_BINDED_DEVICE"));
			
			//初始化获取设备绑定openidURL
//			mpUrl.setGetDeviceOpenIdUrl(propMap.get("GET_DEVICE_OPENID_URL"));
			mpUrl.setGetDeviceOpenIdUrl(ResourceUtils.get("GET_DEVICE_OPENID_URL"));
			
			//初始化获取设备状态URL
//			mpUrl.setGetDeviceStat(propMap.get("GET_DEVICE_STAT"));
			mpUrl.setGetDeviceStat(ResourceUtils.get("GET_DEVICE_STAT"));
			
			//初始化获下载多媒体URL
//			mpUrl.setGetDownloadUrl(propMap.get("GET_DOWNLOAD_URL"));
			mpUrl.setGetDownloadUrl(ResourceUtils.get("GET_DOWNLOAD_URL"));
			
			//初始化获得jsapi_ticketURL
//			mpUrl.setGetJsapiTicketUrl(propMap.get("GET_JSAPI_TICKET_URL"));
			mpUrl.setGetJsapiTicketUrl(ResourceUtils.get("GET_JSAPI_TICKET_URL"));
			
			//初始化创建二维码ticketURL
//			mpUrl.setCreateTicketUrl(propMap.get("CREATE_TICKET_URL"));
			mpUrl.setCreateTicketUrl(ResourceUtils.get("CREATE_TICKET_URL"));
			
			//初始化获取上传多媒体URL
//			mpUrl.setGetUploadUrl(propMap.get("GET_UPLOAD_URL"));
			mpUrl.setGetUploadUrl(ResourceUtils.get("GET_UPLOAD_URL"));
			
			//初始化获取用户信息(需scope为snsapi_userinfo)URL
//			mpUrl.setGetUserInfoBySnsUrl(propMap.get("GET_USERINFO_URL"));
			mpUrl.setGetUserInfoBySnsUrl(ResourceUtils.get("GET_USERINFO_URL"));
			
			//初始化获取微信用户信息URL
//			mpUrl.setGetUserInfoUrl(propMap.get("GET_USER_INFO_URL"));
			mpUrl.setGetUserInfoUrl(ResourceUtils.get("GET_USER_INFO_URL"));
			
			//初始化刷新access_tokenURL
//			mpUrl.setRefreshTokenUrl(propMap.get("REFRESH_TOKEN_URL"));
			mpUrl.setRefreshTokenUrl(ResourceUtils.get("REFRESH_TOKEN_URL"));
			
			//初始化发送模板消息URL
//			mpUrl.setTemplateServiceUrl(propMap.get("TEMPLATE_SERVICE_URL"));
			mpUrl.setTemplateServiceUrl(ResourceUtils.get("TEMPLATE_SERVICE_URL"));
			
			//初始化获取access token的URL
//			mpUrl.setTokenUrl(propMap.get("TOKEN_URL"));
			mpUrl.setTokenUrl(ResourceUtils.get("TOKEN_URL"));
			
			//初始化第三方发送消息给设备主人的微信端，并最终送达设备的URL
//			mpUrl.setTransmsgUrl(propMap.get("TRANSMSG_URL"));
			mpUrl.setTransmsgUrl(ResourceUtils.get("TRANSMSG_URL"));
			
			//初始化上传媒体文件URL
//			mpUrl.setUploadMediaUrl(propMap.get("UPLOAD_MEDIA_URL"));
			mpUrl.setUploadMediaUrl(ResourceUtils.get("UPLOAD_MEDIA_URL"));
			
			//初始化获取生成临时带参数二维码的ticketURL
//			mpUrl.setWxGetQrcodeTicketUrl(propMap.get("WX_GET_QRCODE_TICKET_URL"));
			mpUrl.setWxGetQrcodeTicketUrl(ResourceUtils.get("WX_GET_QRCODE_TICKET_URL"));
			
			//初始化获取生成临时带参数二维码URL
//			mpUrl.setWxGetQrcodeUrl(propMap.get("WX_GET_QRCODE_URL"));
			mpUrl.setWxGetQrcodeUrl(ResourceUtils.get("WX_GET_QRCODE_URL"));
			
			//初始化绑定用户和设备URL
//			mpUrl.setBindDeviceUrl(propMap.get("BIND_DEVICE_URL"));
			mpUrl.setBindDeviceUrl(ResourceUtils.get("BIND_DEVICE_URL"));
			
			//初始化解绑用户和设备息URL
//			mpUrl.setUnbindDeviceUrl(propMap.get("UNBIND_DEVICE_URL"));
			mpUrl.setUnbindDeviceUrl(ResourceUtils.get("UNBIND_DEVICE_URL"));
			
			//初始化验证设备二维码URL
//			mpUrl.setVerifyQrcode("VERIFY_QRCODE");
			mpUrl.setVerifyQrcode(ResourceUtils.get("VERIFY_QRCODE"));
			
			//获取微信服务器IP地址（目前用于验证token是否过期）
			mpUrl.setGetcallbackip(ResourceUtils.get("GET_CALLBACK_IP","https://api.weixin.qq.com/cgi-bin/getcallbackip?access_token={0}"));
			
			return mpUrl;
	}
	
	//清除缓存中微信公众号相关的信息
	public static void removRedisForMP()
	{
		mpUrlMap.remove("mpUrl2");
		//mpRrlRedisObject.remove();
		
		for(int i=1;i<10;i++)
		{
			WechatServiceNoInfo wechatServiceNoInfo=wechatServiceNoInfoMap.remove("wechatServiceNoInfo_"+i);
		}
	}
	
//	public static void main(String[] str)
//	{
//		AppType appType=getAppTypeByWechatServiceNo("gh_4bc17495d97a");
//		System.out.println(appType.toString());
//	}
//	
//	private static void test(Long t)
//	{
//		System.out.println(t);
//	}
}
