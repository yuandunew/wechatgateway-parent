/**
 * 
 */
package com.yuandu.wechatgateway.service.dto.message;

import java.io.Serializable;

import com.lifesense.base.dto.wechatgateway.Data;
import com.lifesense.base.dto.wechatgateway.ValueData;

/** 
 * ClassName: BpData
 * Function: TODO ADD FUNCTION.
 * date: 2015年12月26日 下午4:07:26
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2015, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
public class BpData extends Data implements Serializable 
{
	/**
	 * 高压
	 */
	private ValueData highPressure;
	
	/**
	 * 低压
	 */
	private ValueData lowPressure;
	
	/**
	 * 心率
	 */
	private ValueData heart;
	
	/**
	 * 血压等级
	 */
	private ValueData bloodPressureLevel;

	public ValueData getHighPressure() {
		return highPressure;
	}

	public void setHighPressure(ValueData highPressure) {
		this.highPressure = highPressure;
	}

	public ValueData getLowPressure() {
		return lowPressure;
	}

	public void setLowPressure(ValueData lowPressure) {
		this.lowPressure = lowPressure;
	}

	public ValueData getHeart() {
		return heart;
	}

	public void setHeart(ValueData heart) {
		this.heart = heart;
	}

	public ValueData getBloodPressureLevel() {
		return bloodPressureLevel;
	}

	public void setBloodPressureLevel(ValueData bloodPressureLevel) {
		this.bloodPressureLevel = bloodPressureLevel;
	}
}
