package com.yuandu.wechatgateway.rest.dto;

import javax.ws.rs.QueryParam;

import org.apache.commons.lang3.StringUtils;

import com.lifesense.base.utils.code.LifesenseCheckSum;

public class PlCheckSum {

	@QueryParam("checksum")
	private String checksum;
	@QueryParam("timestamp")
	private String timestamp;

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public boolean verifyChecksum(){
		return StringUtils.equals(this.checksum, LifesenseCheckSum.create().checksum(this.timestamp));
	}
	
}
