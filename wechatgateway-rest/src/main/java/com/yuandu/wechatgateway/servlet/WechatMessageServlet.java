package com.yuandu.wechatgateway.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lifesense.base.spring.InstanceFactory;
import com.lifesense.soa.wechatgateway.context.WechatMessageContext;
import com.lifesense.soa.wechatgateway.rest.dispatcher.WechatMessageDispatcher;

/**
 * 
 * 微信消息接受响应<br/>
 * Created by lizhifeng on 2017年6月27日.
 *
 */
@WebServlet(name = "wechatMessageServlet", urlPatterns = { "/weixin/connect" })
public class WechatMessageServlet extends HttpServlet {
	private static final long serialVersionUID = -5698323216669092981L;

	protected static final Logger logger = LoggerFactory.getLogger(WechatMessageServlet.class);

	private WechatMessageDispatcher wechatMessageDispatcher = InstanceFactory.getInstance(WechatMessageDispatcher.class);

	/**
	 * 微信服务器端请求接入乐心服务器(仅用于配置url和token时首次接入认证)
	 * 
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		WechatMessageContext.connectonCheck(request, response);
	}

	/**
	 * 接收微信服务器端的消息数据
	 * 
	 * @param xml weixin端的消息数据(xml格式)
	 * @return String 直接返回""，微信服务器不会对此作任何处理，要求2秒内必须返回，否则微信端作为超时处理（重试三次）。
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		wechatMessageDispatcher.dispatcher(request, response);
	}

}
