package com.yuandu.wechatgateway.rest.vo;

import java.io.Serializable;
import java.util.List;

import com.lifesense.soa.wechatgateway.dto.receive.WechatDeviceQrcode;

public class CreateDeviceQrcodeVo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3070356965001924202L;
	private List<WechatDeviceQrcode> deviceQrcodes;

	public List<WechatDeviceQrcode> getDeviceQrcodes() {
		return deviceQrcodes;
	}

	public void setDeviceQrcodes(List<WechatDeviceQrcode> deviceQrcodes) {
		this.deviceQrcodes = deviceQrcodes;
	}
	
	
	
}
