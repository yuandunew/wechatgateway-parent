package com.yuandu.wechatgateway.service;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AccessTokenResult implements Serializable{
		private static final long serialVersionUID = -7870202296342116804L;
		private int code;
		private String msg;
		private ResultToken data;
		public AccessTokenResult(){
		}
		public int getCode() {
			return code;
		}
		public void setCode(int code) {
			this.code = code;
		}
		public String getMsg() {
			return msg;
		}
		public void setMsg(String msg) {
			this.msg = msg;
		}
		public ResultToken getData() {
			return data;
		}
		public void setData(ResultToken data) {
			this.data = data;
		}
	}

class ResultToken implements Serializable{
	private String accessToken;
	private String ticket;
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getTicket() {
		return ticket;
	}
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}
}