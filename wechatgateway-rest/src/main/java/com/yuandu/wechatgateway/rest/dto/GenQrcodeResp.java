package com.yuandu.wechatgateway.rest.dto;

import java.io.Serializable;
import java.util.List;

import com.lifesense.soa.wechatgateway.dto.receive.WechatDeviceQrcode;

public class GenQrcodeResp implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1903254740782805841L;
	private List<WechatDeviceQrcode> qrcodes;

	public List<WechatDeviceQrcode> getQrcodes() {
		return qrcodes;
	}

	public void setQrcodes(List<WechatDeviceQrcode> qrcodes) {
		this.qrcodes = qrcodes;
	}
	
}
