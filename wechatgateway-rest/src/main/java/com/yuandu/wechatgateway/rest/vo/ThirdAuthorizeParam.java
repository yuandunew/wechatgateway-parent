package com.yuandu.wechatgateway.rest.vo;

import java.io.Serializable;

import javax.ws.rs.QueryParam;

import io.swagger.annotations.ApiParam;

public class ThirdAuthorizeParam implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4981248002775314266L;
	@QueryParam("appType")
	@ApiParam(value="appType",required=true)
	private Integer appType;
	@QueryParam("code")
	@ApiParam(value="授权码",required=true)
	private String code;
	@QueryParam("returnUrl")
	@ApiParam(value="登录成功后重定向地址",required=true)
	private String returnUrl;
	public Integer getAppType() {
		return appType;
	}
	public void setAppType(Integer appType) {
		this.appType = appType;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getReturnUrl() {
		return returnUrl;
	}
	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}
	
}
