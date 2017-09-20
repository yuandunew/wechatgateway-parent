/**
 * 
 */
package com.yuandu.wechatgateway.service.dto;

import java.io.Serializable;

/** 
 * ClassName: MPUrl
 * Function: TODO ADD FUNCTION.
 * date: 2015年12月23日 下午6:23:43
 * 
 * 微信服务器相关链接类
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2015, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class MPUrl implements Serializable
{
	/**
	 * 获取access token的URL，http请求方式: GET
	 */
	public String tokenUrl;

	/**
	 * 发送客服消息URL
	 */
	public  String customerServiceUrl;

	/**
	 * 第三方发送消息给设备主人的微信端，并最终送达设备的URL
	 */
	public String transmsgUrl;

	/**
	 * 获取设备绑定openid的URL
	 */
	public String getDeviceOpenIdUrl;

	/**
	 * 上传媒体文件
	 */
	public String uploadMediaUrl;
	
	/**
	 * 通过code换取网页授权access_token的URL
	 */
	public String getAccessTokenUrl;
	
	/**
	 * 刷新access_token，由于access_token拥有较短的有效期，当access_token超时后，
	 * 可以使用refresh_token进行刷新，refresh_token拥有较长的有效期（7天、30天、60天、90天），
	 * 当refresh_token失效的后，需要用户重新授权。 
	 */
	public String refreshTokenUrl;
	
	/**
	 * 获取用户信息(需scope为snsapi_userinfo)
	 */
	public String getUserInfoBySnsUrl;
	
	/**
	 * 创建二维码ticket
	 */
	public String createTicketUrl;
	
	/**
	 * 批量创建二维码
	 */
	public String createOrcodeUrl;
	
	/**
	 * 生成微信的设备二维码和设备ID
	 */
	public String createOrcodeUrl2;
	
	
	/**
	 * 设备授权url
	 */
	public String authorizeDeviceUrl;
	
	/**
	 * 设备状态查询
	 */
	public String getDeviceStat;
	
	/**
	 * 网页授权获取用户信息
	 */
	public String authorizeUserInfoUrl;
	
	/**
	 * 发送模板消息URL
	 */
	public String templateServiceUrl;
	
	/**
	 * 获取微信用户信息
	 */
	public String getUserInfoUrl;
	
	// 获取生成临时带参数二维码的ticket
	public String  wxGetQrcodeTicketUrl;
	
	// 获取生成临时带参数二维码
	public String wxGetQrcodeUrl;
	
	// 获得jsapi_ticket
	public String  getJsapiTicketUrl;

	// （微信）下载多媒体
	public String getDownloadUrl;

	public String getUploadUrl;
	
	/**-----------微信设备接口-----------start**/
	
	// 强制绑定用户和设备
	public String compelBindDeviceUrl;
	
	// 强制解绑用户和设备
	public String compelUnbindDeviceUrl;
	
	// 获取绑定的设备id
	public String getBindedDevice;
	
	//绑定用户和设备
	public String bindDeviceUrl;
	
	//解绑用户和设备
	public String unbindDeviceUrl;
	
	//验证二维码
	public String verifyQrcode;

	//获取微信服务器IP地址（目前用于验证token是否有效）
	public String getcallbackip;
	
	/**-----------微信设备接口-----------end**/

	public String getTokenUrl() {
		return tokenUrl;
	}

	public void setTokenUrl(String tokenUrl) {
		this.tokenUrl = tokenUrl;
	}

	public String getCustomerServiceUrl() {
		return customerServiceUrl;
	}

	public void setCustomerServiceUrl(String customerServiceUrl) {
		this.customerServiceUrl = customerServiceUrl;
	}

	public String getTransmsgUrl() {
		return transmsgUrl;
	}

	public void setTransmsgUrl(String transmsgUrl) {
		this.transmsgUrl = transmsgUrl;
	}

	public String getGetDeviceOpenIdUrl() {
		return getDeviceOpenIdUrl;
	}

	public void setGetDeviceOpenIdUrl(String getDeviceOpenIdUrl) {
		this.getDeviceOpenIdUrl = getDeviceOpenIdUrl;
	}

	public String getUploadMediaUrl() {
		return uploadMediaUrl;
	}

	public void setUploadMediaUrl(String uploadMediaUrl) {
		this.uploadMediaUrl = uploadMediaUrl;
	}

	public String getGetAccessTokenUrl() {
		return getAccessTokenUrl;
	}

	public void setGetAccessTokenUrl(String getAccessTokenUrl) {
		this.getAccessTokenUrl = getAccessTokenUrl;
	}

	public String getRefreshTokenUrl() {
		return refreshTokenUrl;
	}

	public void setRefreshTokenUrl(String refreshTokenUrl) {
		this.refreshTokenUrl = refreshTokenUrl;
	}

	public String getGetUserInfoBySnsUrl() {
		return getUserInfoBySnsUrl;
	}

	public void setGetUserInfoBySnsUrl(String getUserInfoBySnsUrl) {
		this.getUserInfoBySnsUrl = getUserInfoBySnsUrl;
	}

	public String getCreateOrcodeUrl() {
		return createOrcodeUrl;
	}

	public void setCreateOrcodeUrl(String createOrcodeUrl) {
		this.createOrcodeUrl = createOrcodeUrl;
	}

	public String getCreateOrcodeUrl2() {
		return createOrcodeUrl2;
	}

	public void setCreateOrcodeUrl2(String createOrcodeUrl2) {
		this.createOrcodeUrl2 = createOrcodeUrl2;
	}

	public String getAuthorizeDeviceUrl() {
		return authorizeDeviceUrl;
	}

	public void setAuthorizeDeviceUrl(String authorizeDeviceUrl) {
		this.authorizeDeviceUrl = authorizeDeviceUrl;
	}

	public String getGetDeviceStat() {
		return getDeviceStat;
	}

	public void setGetDeviceStat(String getDeviceStat) {
		this.getDeviceStat = getDeviceStat;
	}

	public String getAuthorizeUserInfoUrl() {
		return authorizeUserInfoUrl;
	}

	public void setAuthorizeUserInfoUrl(String authorizeUserInfoUrl) {
		this.authorizeUserInfoUrl = authorizeUserInfoUrl;
	}

	public String getTemplateServiceUrl() {
		return templateServiceUrl;
	}

	public void setTemplateServiceUrl(String templateServiceUrl) {
		this.templateServiceUrl = templateServiceUrl;
	}

	public String getGetUserInfoUrl() {
		return getUserInfoUrl;
	}

	public void setGetUserInfoUrl(String getUserInfoUrl) {
		this.getUserInfoUrl = getUserInfoUrl;
	}

	public String getWxGetQrcodeTicketUrl() {
		return wxGetQrcodeTicketUrl;
	}

	public void setWxGetQrcodeTicketUrl(String wxGetQrcodeTicketUrl) {
		this.wxGetQrcodeTicketUrl = wxGetQrcodeTicketUrl;
	}

	public String getWxGetQrcodeUrl() {
		return wxGetQrcodeUrl;
	}

	public void setWxGetQrcodeUrl(String wxGetQrcodeUrl) {
		this.wxGetQrcodeUrl = wxGetQrcodeUrl;
	}

	public String getGetJsapiTicketUrl() {
		return getJsapiTicketUrl;
	}

	public void setGetJsapiTicketUrl(String getJsapiTicketUrl) {
		this.getJsapiTicketUrl = getJsapiTicketUrl;
	}

	public String getGetDownloadUrl() {
		return getDownloadUrl;
	}

	public void setGetDownloadUrl(String getDownloadUrl) {
		this.getDownloadUrl = getDownloadUrl;
	}

	public String getGetUploadUrl() {
		return getUploadUrl;
	}

	public void setGetUploadUrl(String getUploadUrl) {
		this.getUploadUrl = getUploadUrl;
	}

	public String getCompelBindDeviceUrl() {
		return compelBindDeviceUrl;
	}

	public void setCompelBindDeviceUrl(String compelBindDeviceUrl) {
		this.compelBindDeviceUrl = compelBindDeviceUrl;
	}

	public String getCompelUnbindDeviceUrl() {
		return compelUnbindDeviceUrl;
	}

	public void setCompelUnbindDeviceUrl(String compelUnbindDeviceUrl) {
		this.compelUnbindDeviceUrl = compelUnbindDeviceUrl;
	}

	public String getGetBindedDevice() {
		return getBindedDevice;
	}

	public void setGetBindedDevice(String getBindedDevice) {
		this.getBindedDevice = getBindedDevice;
	}

	public String getCreateTicketUrl() {
		return createTicketUrl;
	}

	public void setCreateTicketUrl(String createTicketUrl) {
		this.createTicketUrl = createTicketUrl;
	}

	public String getBindDeviceUrl() {
		return bindDeviceUrl;
	}

	public void setBindDeviceUrl(String bindDeviceUrl) {
		this.bindDeviceUrl = bindDeviceUrl;
	}

	public String getUnbindDeviceUrl() {
		return unbindDeviceUrl;
	}

	public void setUnbindDeviceUrl(String unbindDeviceUrl) {
		this.unbindDeviceUrl = unbindDeviceUrl;
	}

	public String getVerifyQrcode() {
		return verifyQrcode;
	}

	public void setVerifyQrcode(String verifyQrcode) {
		this.verifyQrcode = verifyQrcode;
	}

	public String getGetcallbackip() {
		return getcallbackip;
	}

	public void setGetcallbackip(String getcallbackip) {
		this.getcallbackip = getcallbackip;
	}
	
	
}
