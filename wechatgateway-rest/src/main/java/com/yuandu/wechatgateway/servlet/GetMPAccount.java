/**
 * 
 */
package com.yuandu.wechatgateway.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lifesense.soa.wechatgateway.service.dto.MPAccount;
import com.lifesense.soa.wechatgateway.service.utils.MPAccountUtil;
import com.lifesense.soa.wechatgateway.utils.PrintWriterUtil;
import com.lifesense.soa.wechatgateway.utils.StringUtilByWechatGateway;

/** 
 * ClassName: GetMPAccount
 * Function: TODO ADD FUNCTION.
 * date: 2015年12月25日 下午6:01:46
 * 
 * 查询缓存中的公众号、转发服务器的目的地址
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2015, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
@WebServlet(name="getMPAccount", urlPatterns={"/get_mpaccount"})
public class GetMPAccount extends HttpServlet 
{
	/**
	 * 向缓存中更新公众号、转发服务器的目的地址
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{

		this.doPost(request, response);
	}

	/**
	 * 向缓存中更新公众号、转发服务器的目的地址
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException 
	{
		
		String serviceNo = request.getParameter("serviceNo");

		// 判断传参是否为空
		if (StringUtilByWechatGateway.isBlankOrNull(serviceNo)) 
		{
			PrintWriterUtil.sendText("param is empty.", response);
			return;
		}
		
		// 从缓存中获取公众号配置信息
		MPAccount mpAccount = MPAccountUtil.getMPAccount(serviceNo);
		
		if (null == mpAccount) 
		{
			PrintWriterUtil.sendText("data is empty.", response);
			return;
		}
		
		// 返回消息
		PrintWriterUtil.sendJson(mpAccount, response);
	}
}
