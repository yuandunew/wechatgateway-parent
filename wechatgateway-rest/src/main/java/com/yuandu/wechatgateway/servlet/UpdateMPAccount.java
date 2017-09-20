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

import com.lifesense.base.cache.command.RedisObject;
import com.lifesense.soa.wechatgateway.service.dto.MPAccount;
import com.lifesense.soa.wechatgateway.service.utils.MPAccountUtil;
import com.lifesense.soa.wechatgateway.utils.GsonUtil;
import com.lifesense.soa.wechatgateway.utils.PrintWriterUtil;
import com.lifesense.soa.wechatgateway.utils.StringUtilByWechatGateway;


/** 
 * ClassName: UpdateMPAccount
 * Function: TODO ADD FUNCTION.
 * date: 2015年12月25日 下午5:45:21
 * 
 * 向缓存中更新公众号、转发服务器的目的地址
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2015, lifesense.com All Rights Reserved.
 */
@SuppressWarnings("serial")
@WebServlet(name = "updateMPAccount", urlPatterns = {"/update_mpaccount"})
public class UpdateMPAccount extends HttpServlet 
{
	/**
	 * 向缓存中更新公众号、转发服务器的目的地址
	 * 
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		this.doPost(request, response);
	}

	/**
	 * 向缓存中更新公众号、转发服务器的目的地址
	 */
	protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException
	{
		String json = MPAccountUtil.getXML(request);

		if (StringUtilByWechatGateway.isBlankOrNull(json)) 
		{
			PrintWriterUtil.sendText("request data is empty.", response);
			return;
		}

		MPAccount mpAccount = GsonUtil.toBean(json,MPAccount.class);
		
		if (null ==  mpAccount) { // 转换失败
			PrintWriterUtil.sendText("request data is error.", response);
			return;
		}
		
		String key = MPAccountUtil.CACHED_KEY_PREFIX + mpAccount.getServiceNo();
		RedisObject redisObject=new RedisObject(key);
		
		//存入缓存，长期有效
		redisObject.set(mpAccount,-1);
		
		// 返回消息
		PrintWriterUtil.sendText("success", response);
	}
}
