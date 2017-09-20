package com.yuandu.wechatgateway.rest.dto;

import java.io.Serializable;
import java.util.List;

import com.lifesense.soa.wechatgateway.dto.send.AuthorizeDevice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
@ApiModel("设备授权请求")
public class DeviceAuthorizeReq implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 509754515090688544L;
	/**
	 * 设备列表
	 * */
	@ApiModelProperty(required=true,value="设备列表")
	private List<AuthorizeDevice> device_list;
	/**
	 * 请求操作的类型，
	 * 限定取值为：0：设备授权（缺省值为0） 1：设备更新（更新已授权设备的各属性值）
	 * */
	@ApiModelProperty(required=true,value="请求操作的类型",allowableValues="0：设备授权（缺省值为0） 1：设备更新（更新已授权设备的各属性值）")
	private String op_type;
	/**
	 * 设备的产品编号（由微信硬件平台分配）。
	 * 可在公众号设备功能管理页面查询。
	 * */
	@ApiModelProperty(required=true,value="设备的产品编号",notes="由微信硬件平台分配,可在公众号设备功能管理页面查询")
	private String product_id;
	@ApiModelProperty(required=true,value="应用类型")
	private Integer appType;
	
	public List<AuthorizeDevice> getDevice_list() {
		return device_list;
	}
	public void setDevice_list(List<AuthorizeDevice> device_list) {
		this.device_list = device_list;
	}
	public String getOp_type() {
		return op_type;
	}
	public void setOp_type(String op_type) {
		this.op_type = op_type;
	}
	public String getProduct_id() {
		return product_id;
	}
	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}
	public Integer getAppType() {
		return appType;
	}
	public void setAppType(Integer appType) {
		this.appType = appType;
	}
	
	
	
}
