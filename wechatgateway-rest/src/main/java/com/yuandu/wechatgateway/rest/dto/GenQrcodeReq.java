package com.yuandu.wechatgateway.rest.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
@ApiModel(value="生成设备二维码请求")
public class GenQrcodeReq implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4954083961938947180L;
	@ApiModelProperty(required=true,value="应用类型")
	private Integer appType;

	public Integer getAppType() {
		return appType;
	}

	public void setAppType(Integer appType) {
		this.appType = appType;
	}
	
}
