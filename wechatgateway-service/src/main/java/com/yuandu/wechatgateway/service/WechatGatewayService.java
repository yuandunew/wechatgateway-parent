/**
 * 
 */
package com.yuandu.wechatgateway.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lifesense.base.beans.param.RequestHeader;
import com.lifesense.base.constant.AppType;
import com.lifesense.base.dto.wechatgateway.WechatCustomServiceMessage;
import com.lifesense.base.dto.wechatgateway.WechatTemplateMessage;
import com.lifesense.base.utils.okhttp.FileExtensionEnum;
import com.lifesense.soa.wechatgateway.api.IWechatGatewayProvider;
import com.lifesense.soa.wechatgateway.dto.ParamTempQrcodeDto;
import com.lifesense.soa.wechatgateway.dto.enums.WechatMediaFileTypeEnum;
import com.lifesense.soa.wechatgateway.dto.receive.JSSDKSignatureInfo;
import com.lifesense.soa.wechatgateway.dto.receive.ParamQrcodeResponse;
import com.lifesense.soa.wechatgateway.dto.receive.VerifyQrcodeResp;
import com.lifesense.soa.wechatgateway.dto.receive.WeChatIotDeviceResponse;
import com.lifesense.soa.wechatgateway.dto.receive.WechatAuthorizeDeviceSuccessInfo;
import com.lifesense.soa.wechatgateway.dto.receive.WechatDeviceIdAndQrcode;
import com.lifesense.soa.wechatgateway.dto.receive.WechatDeviceQrcode;
import com.lifesense.soa.wechatgateway.dto.receive.WechatDeviceStatusResp;
import com.lifesense.soa.wechatgateway.dto.receive.WechatUser;
import com.lifesense.soa.wechatgateway.dto.receive.WechatWebpageAccessTokenResp;
import com.lifesense.soa.wechatgateway.dto.send.ParamQrcodeParam;
import com.lifesense.soa.wechatgateway.dto.send.VerifyQrcodeParam;
import com.lifesense.soa.wechatgateway.dto.send.WechatAuthorizeDevice;
import com.lifesense.soa.wechatgateway.dto.send.WechatBindDeviceEventMessage;
import com.lifesense.soa.wechatgateway.dto.send.WechatDeviceEventMessage;
import com.lifesense.soa.wechatgateway.dto.send.WechatDeviceQrcodeParam;
import com.lifesense.soa.wechatgateway.dto.send.WechatDeviceStatusMessage;
import com.lifesense.soa.wechatgateway.dto.send.WechatDeviveContentMessage;
import com.lifesense.soa.wechatgateway.dto.send.WechatServiceNoInfo;
import com.lifesense.soa.wechatgateway.service.utils.MPManager;

/**
 * 
 * 微信网关服务实现
 * 
 * @version
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang Copyright (c)
 *         2016, lifesense.com All Rights Reserved.
 */
@Service("wechatGatewayService")
public class WechatGatewayService implements IWechatGatewayProvider {
	@Autowired
	WechatGatewayServiceV2 wechatGatewayService;

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
	public WechatUser getWechatUserByAppTypeAndOpenid(RequestHeader requestHeader, AppType appType, String openid) {
		return wechatGatewayService.getWechatUserByAppTypeAndOpenid(appType, openid);
	}

	/**
	 * 根据微信公众号，OPENID获取微信用户信息
	 * 
	 * @param appType 应应用类型
	 * @param openid
	 * @return 微信用户信息
	 */
	public WechatUser getWechatUserByServiceNoAndOpenid(RequestHeader requestHeader, String wechatServiceNo,
			String openid) {
		return wechatGatewayService.getWechatUserByServiceNoAndOpenid(wechatServiceNo, openid);
	}

	/**
	 * 根据微信公众号ACCESS_TOKEN，OPENID获取微信用户信息
	 * 
	 * @param accessToken 微信公众号访问令牌
	 * @param openid
	 * @return 微信用户信息
	 */
	@Override
	public WechatUser getWechatUserByAccessTokenAndOpenid(RequestHeader requestHeader, String accessToken,
			String openid) {
		return wechatGatewayService.getWechatUserByAccessTokenAndOpenid(accessToken, openid);
	}

	/**
	 * 根据微信公众号ACCESS_TOKEN，OPENID获取微信用户信息(用于微信开放平台)
	 * 
	 * @param accessToken 调用凭证
	 * @param openid
	 * @return 微信用户信息
	 */
	@Override
	public WechatUser getWechatUserByAccessTokenAndOpenidBySns(RequestHeader requestHeader, String accessToken,
			String openid) {
		return wechatGatewayService.getWechatUserByAccessTokenAndOpenidBySns(accessToken, openid);
	}

	/**
	 * 根据应用类型获取微信JSApiTicket
	 * 
	 * @param appType 应用类型
	 * @return jsApiTicket
	 */
	@Override
	public String getJSApiTicketByAppType(RequestHeader requestHeader, AppType appType) {
		return wechatGatewayService.getJSApiTicketByAppType(appType);
	}

	/**
	 * 根据AppType从缓存中获取微信AccessToken
	 * 
	 * @param appType 应用类型
	 * @return accessToken
	 */
	@Override
	public String getAccessTokenForRedis(RequestHeader requestHeader, AppType appType) {
		return wechatGatewayService.getAccessTokenForRedis(appType);
	}

	/**
	 * 根据AppType获取微信AccessToken
	 * 
	 * @param appType 应用类型
	 * @return accessToken
	 */
	@Override
	public String getAccessTokenByAppType(RequestHeader requestHeader, AppType appType) {
		return wechatGatewayService.getAccessTokenByAppType(appType);
	}

	@Override
	public String getAccessTokenByURL(AppType appType) {

		return wechatGatewayService.getAccessTokenByURL(appType);
	}

	/**
	 * 根据参数获取带参数二维码(乐心健康公众号专用)
	 * 
	 * @param param 获取参数
	 * @return 二维码解析后URL
	 */
	@Override
	public String getParamQrcodeUrlForHealth(RequestHeader requestHeader, ParamQrcodeParam param) {
		return wechatGatewayService.getParamQrcodeUrlForHealth(param);
	}

	/**
	 * 根据公众号原始ID获取公众号信息
	 * 
	 * @param wechatServiceNo 公众号原始ID
	 * @return 公号信息
	 */
	@Override
	public WechatServiceNoInfo getWechatServiceNoInfoByApptype(AppType appType) {
		return wechatGatewayService.getWechatServiceNoInfoByApptype(appType);
	}

	/**
	 * 临时二维码新接口
	 * 
	 * @param requestHeader
	 * @param paramTempQrcodeDto
	 * @return
	 */
	public ParamQrcodeResponse getTempParamQrcodeUrl(RequestHeader requestHeader,
			ParamTempQrcodeDto paramTempQrcodeDto) {

		return wechatGatewayService.getTempParamQrcodeUrl(paramTempQrcodeDto);
	}

	/**
	 * 根据应用类型和网页授权Code获取OpenId
	 * 
	 * @param appType 应用类型
	 * @param code 网页授权Code
	 * @return openId
	 */
	@Override
	public String getOpenIdByCode(RequestHeader requestHeader, AppType appType, String code) {
		return wechatGatewayService.getOpenIdByCode(appType, code);
	}

	@Override
	public WechatWebpageAccessTokenResp getWechatWebpageAccessTokenByCode(RequestHeader requestHeader, AppType appType,
			String code) {
		return wechatGatewayService.getWechatWebpageAccessTokenByCode(appType, code);
	}

	/**
	 * 获取JSSDK签名
	 * 
	 * @param appType 应用类型
	 * @param url 当前网页的URL
	 * @return JSSDK签名信息
	 */
	@Override
	public JSSDKSignatureInfo getJSSDKSignature(RequestHeader requestHeader, AppType appType, String url) {
		return wechatGatewayService.getJSSDKSignature(appType, url);
	}

	/**
	 * @param appType 应用类型
	 * @return boolean
	 */
	@Override
	public boolean checkAccessToken(RequestHeader requestHeader, AppType appType, String accessToken) {
		return wechatGatewayService.checkAccessToken(appType, accessToken);
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
	public boolean sendCustomServiceMessage(RequestHeader requestHeader, AppType appType,
			WechatCustomServiceMessage customServiceMessage) {
		return wechatGatewayService.sendCustomServiceMessage(appType, customServiceMessage);
	}

	/**
	 * 根据微信公众号原始Id发送微信客服消息
	 * 
	 * @param wechatServiceNo 微信公众号原始ID
	 * @param customServiceMessage 微信客服消息
	 * @return 发送是否成功标志
	 */
	public boolean sendCustomServiceMessageByServiceNo(RequestHeader requestHeader, String wechatServiceNo,
			WechatCustomServiceMessage customServiceMessage) {
		return wechatGatewayService.sendCustomServiceMessageByServiceNo(wechatServiceNo, customServiceMessage);
	}

	/**
	 * 根据微信公众号原始ID发送微信模板消息
	 * 
	 * @param wechatServiceNo 微信公众号原始ID
	 * @param templateMessage 模板消息
	 * @return 发送是否成功标志
	 */
	public boolean sendTemplateMessageByServiceNo(RequestHeader requestHeader, String wechatServiceNo,
			WechatTemplateMessage templateMessage) {
		return wechatGatewayService.sendTemplateMessageByServiceNo(wechatServiceNo, templateMessage);
	}

	/**
	 * 发送微信模板消息
	 * 
	 * @param appType 应用类型
	 * @param templateMessage 模板消息
	 * @return 发送是否成功标志
	 */
	@Override
	public boolean sendTemplateMessage(RequestHeader requestHeader, AppType appType,
			WechatTemplateMessage templateMessage) {
		return wechatGatewayService.sendTemplateMessage(appType, templateMessage);
	}

	/**
	 * 上传步数到微信排行榜
	 * 
	 * @param appType 应用类型
	 * @param openId 微信用户openId
	 * @param step 适时步数
	 */
	@Override
	public void setStepForWechatRank(RequestHeader requestHeader, AppType appType, String openId, int step) {
		wechatGatewayService.setStepForWechatRank(appType, openId, step);
		;
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
	public boolean sendWechatDeviceStatusMessage(RequestHeader requestHeader, AppType appType,
			WechatDeviceStatusMessage deviceStatusMessage) {
		return wechatGatewayService.sendWechatDeviceStatusMessage(appType, deviceStatusMessage);
	}

	/**
	 * 主动发消息给设备
	 * 
	 * @param appType 应用类型
	 * @param deviceMessage 设备状态消息
	 * @return 发送是否成功标志
	 */
	@Override
	public boolean sendWechatDeviceContentMessage(RequestHeader requestHeader, AppType appType,
			WechatDeviveContentMessage deviceContentMessage) {

		return wechatGatewayService.sendWechatDeviceContentMessage(appType, deviceContentMessage);
	}

	/**
	 * 强制绑定设备
	 * 
	 * @param appType 应用类型
	 * @param deviceEventMessage 设备事件消息
	 * @return 发送是否成功标志
	 */
	@Override
	public boolean compelBindWechatDevice(RequestHeader requestHeader, AppType appType,
			WechatDeviceEventMessage deviceEventMessage) {
		return wechatGatewayService.compelBindWechatDevice(appType, deviceEventMessage);
	}

	/**
	 * 强制解绑设备
	 * 
	 * @param appType 应用类型
	 * @param deviceEventMessage 设备事件消息
	 * @return 发送是否成功标志
	 */
	@Override
	public boolean compelUnbindWechatDevice(RequestHeader requestHeader, AppType appType,
			WechatDeviceEventMessage deviceEventMessage) {
		return wechatGatewayService.compelUnbindWechatDevice(appType, deviceEventMessage);
	}

	/**
	 * 绑定设备
	 * 
	 * @param appType 应用类型
	 * @param deviceBindEventMessage 设备绑定事件消息
	 * @return 发送是否成功标志
	 */
	@Override
	public boolean bindWechatDevice(RequestHeader requestHeader, AppType appType,
			WechatBindDeviceEventMessage deviceBindEventMessage) {
		return wechatGatewayService.bindWechatDevice(appType, deviceBindEventMessage);
	}

	/**
	 * 解绑设备
	 * 
	 * @param appType 应用类型
	 * @param deviceBindEventMessage 设备绑定事件消息
	 * @return 发送是否成功标志
	 */
	@Override
	public boolean unbindWechatDevice(RequestHeader requestHeader, AppType appType,
			WechatBindDeviceEventMessage deviceBindEventMessage) {
		return wechatGatewayService.unbindWechatDevice(appType, deviceBindEventMessage);
	}

	/**
	 * 根据设备ID获取微信设备二维码（建议每次获取数量不超过5个）
	 * 
	 * @param appType 应用类型
	 * @param deviceQrcodeParam 获取微信设备二维码参数
	 * @return 微信设备二维码列表
	 */
	@Override
	public List<WechatDeviceQrcode> createDeviceQrcode(RequestHeader requestHeader, AppType appType,
			WechatDeviceQrcodeParam deviceQrcodeParam) {
		return wechatGatewayService.createDeviceQrcode(appType, deviceQrcodeParam);
	}

	@Override
	public WechatDeviceIdAndQrcode getWechatDeviceIdAndQrcode(AppType appType) {
		return wechatGatewayService.getWechatDeviceIdAndQrcode(appType);
	}

	/**
	 * 设备授权
	 * 
	 * @param appType 应用类型
	 * @param authorizeDevice 需要授权的设备资料
	 * @return 授权成功的设备列表
	 */
	@Override
	public List<WechatAuthorizeDeviceSuccessInfo> authorizeDevice(RequestHeader requestHeader, AppType appType,
			WechatAuthorizeDevice authorizeDevice) {
		return wechatGatewayService.authorizeDevice(appType, authorizeDevice);
	}

	/**
	 * 根据微信公众号，设备ID获取设备状态信息
	 * 
	 * @param appType 应用类型
	 * @param device_id 设备ID
	 * @return 设状态信息
	 */
	@Override
	public WechatDeviceStatusResp getWechatDeviceStatus(RequestHeader requestHeader, AppType appType,
			String device_id) {
		return wechatGatewayService.getWechatDeviceStatus(appType, device_id);
	}

	/**
	 * 验证设备二维码
	 * 
	 * @param appType 应用类型
	 * @param authorizeDevice 验证设备二维码参数
	 * @return 验证设备二维码响应
	 */
	@Override
	public VerifyQrcodeResp verifyQrcode(RequestHeader requestHeader, AppType appType,
			VerifyQrcodeParam verifyQrcodeParam) {
		return wechatGatewayService.verifyQrcode(appType, verifyQrcodeParam);
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
	public String uploadWechatMediaFile(RequestHeader requestHeader, AppType appType, String fullFilePath,
			WechatMediaFileTypeEnum fileType) throws IOException {
		return wechatGatewayService.uploadWechatMediaFile(appType, fullFilePath, fileType);
	}

	/**
	 * 上传文件，并返回MEDIA_ID,带文件后缀
	 * 
	 * @return MEDIA_ID
	 * 
	 * @throws UnsupportedEncodingException
	 */
	@Override
	public String uploadWechatMediaFile(RequestHeader requestHeader, AppType appType, String fullFilePath,
			WechatMediaFileTypeEnum fileType, FileExtensionEnum fileExtensionEnum) throws IOException {
		return wechatGatewayService.uploadWechatMediaFile(appType, fullFilePath, fileType, fileExtensionEnum);
	}

	/**
	 * 从微信服务器下载文件
	 * 
	 * @param mediaId 媒体ID
	 * @return 文件流
	 */
	@Override
	public String downloadWechatMediaFile(RequestHeader requestHeader, AppType appType, String mediaId)
			throws IOException {
		return wechatGatewayService.downloadWechatMediaFile(appType, mediaId);
	}

	/**
	 * 从微信服务器下载文件
	 * 
	 * @param mediaId 媒体ID
	 * @return 字节流
	 */
	@Override
	public byte[] downloadWechatMediaFileForByte(RequestHeader requestHeader, AppType appType, String mediaId)
			throws IOException {
		return wechatGatewayService.downloadWechatMediaFileForByte(appType, mediaId);
	}

	/**
	 * 根据文件serverId从微信下载文件
	 * 
	 * @param serverId
	 * @return 文件流字符串
	 * @throws IOException
	 */
	@Override
	public String downloadWechatMediaFileByServerId(RequestHeader requestHeader, AppType appType, String serverId)
			throws IOException {
		return wechatGatewayService.downloadWechatMediaFileByServerId(appType, serverId);
	}

	/************************************************
	 * 媒体相关文件接口 end
	 *****************************************************/

	/**
	 * 补发文本、图文、模板等消息
	 */
	@Override
	public void reissueMessage() {
		wechatGatewayService.reissueMessage();
	}

	// 清理微信服务相关的缓存
	public void removRedisForMP() {
		MPManager.removRedisForMP();
	}

	// -------------add by shijy----
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
		return wechatGatewayService.getBindDeviceList(accessToken, openId);
	}

	// -------------end by shijy----

	// 健康检查
	@Override
	public Map<String, Object> echo(int... modes) {
		return wechatGatewayService.echo(modes);
	}

	@Override
	public ParamQrcodeResponse getParamQrcodeUrl(RequestHeader requestHeader, ParamQrcodeParam param) {
		return wechatGatewayService.getParamQrcodeUrl(param);
	}

}
