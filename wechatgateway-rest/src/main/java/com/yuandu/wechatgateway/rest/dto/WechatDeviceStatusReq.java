package com.yuandu.wechatgateway.rest.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
@ApiModel("设备状态查询请求")
public class WechatDeviceStatusReq implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4329803991427257579L;

	@ApiModelProperty(value="应用类型",required=true)
	private Integer appType;
	@ApiModelProperty(value="设备ID",required=true)
	private String device_id;

	public Integer getAppType() {
		return appType;
	}

	public void setAppType(Integer appType) {
		this.appType = appType;
	}

	public String getDevice_id() {
		return device_id;
	}

	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}
	
	
}
