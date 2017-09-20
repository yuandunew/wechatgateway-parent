package com.yuandu.wechatgateway.rest.dto;

import java.io.Serializable;
import java.util.List;

import com.lifesense.soa.wechatgateway.dto.receive.WechatAuthorizeDeviceSuccessInfo;

public class DeviceAuthorizeResp implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9125946505316027704L;
	private List<WechatAuthorizeDeviceSuccessInfo> authorizeList;

	public List<WechatAuthorizeDeviceSuccessInfo> getAuthorizeList() {
		return authorizeList;
	}

	public void setAuthorizeList(List<WechatAuthorizeDeviceSuccessInfo> authorizeList) {
		this.authorizeList = authorizeList;
	}
	
}
