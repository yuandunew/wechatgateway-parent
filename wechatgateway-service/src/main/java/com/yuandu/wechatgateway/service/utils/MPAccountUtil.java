package com.yuandu.wechatgateway.service.utils;

import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import com.lifesense.base.cache.command.RedisObject;
import com.lifesense.soa.wechatgateway.service.dto.MPAccount;

/** 
 * ClassName: MPAccount
 * Function: TODO ADD FUNCTION.
 * date: 2015年12月25日 下午4:23:39
 * 
 * 微信公号列表
 * 
 * @version  
 * @since JDK 1.8
 * @author <a href="mailto:wanghanchao@lifesense.com">davidwang 
 * Copyright (c) 2015, lifesense.com All Rights Reserved.
 */
public class MPAccountUtil 
{
//	private static final Logger logger = LogManager.getLogger(MessageForwardingToBusessSystemService.class);
	// 公众号转发服务器，缓存key的前缀
	public static final String CACHED_KEY_PREFIX = "wxgateway_";
		
	/**
	 * 根据公众号原始ID获取公众号转发对象信息
	 * @param serviceNo
	 * @return
	 */
	public static MPAccount getMPAccount(String serviceNo)
	{		
		String key = CACHED_KEY_PREFIX + serviceNo;
			
		MPAccount forwardUrl=new RedisObject(key).get();
		if (forwardUrl==null) 
		{ 
			//key存在，则返回转发的目的服务器地址
			throw new RuntimeException("[公众号原始ID：" + serviceNo+ "]转发的目的服务器地址不存在，请在缓存中添加！");
		}
		return forwardUrl;
	}

	/**
	 * 根据公众号原始ID获取关注、绑定、扫描等消息转发地址
	 * @param serviceNo
	 * @return
	 */
	public static String getMsgForwardUrl(String serviceNo) 
	{
		return getMPAccount(serviceNo).getMsgForwardUrl();
	}
	
	/**
	 * 从request里边获取post过来的xml
	 * 
	 * @param request
	 * @return
	 */
	public static String getXML(HttpServletRequest request) 
	{
		// TODO:修改为其他方式获取数据
		String xml = "";
		byte[] dataOrigin = new byte[request.getContentLength()];
		ServletInputStream in = null;
		try
		{
			in = request.getInputStream();
			in.read(dataOrigin); // 根据长度，将消息实体的内容读入字节数组dataOrigin中
			xml = new String(dataOrigin, "UTF-8"); // 从字节数组中得到表示实体的字符串
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
//			logger.error(e);
		} 
		finally 
		{
			try
			{
				if (null != in)
				{
					in.close();// 关闭数据流
					in = null;
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
//				logger.error(e);
			}
		}
		return xml.replace("MsgId", "MsgID");
	}
}
