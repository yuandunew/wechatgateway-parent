package com.yuandu.wechatgateway.rest.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
@ApiModel(value="校验设备二维码请求")
public class VerifyQrcodeReq implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4223403434299947652L;

	@ApiModelProperty(value="设备二维码的ticket",required=true)
	private String ticket;//设备二维码的ticket
	@ApiModelProperty(value="应用类型",required=true)
	private Integer appType;

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public Integer getAppType() {
		return appType;
	}

	public void setAppType(Integer appType) {
		this.appType = appType;
	}
	
	
	
}
