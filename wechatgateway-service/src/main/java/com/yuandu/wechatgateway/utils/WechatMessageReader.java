package com.yuandu.wechatgateway.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.Files;
import com.lifesense.base.utils.SystemUtils;

public class WechatMessageReader {

	private static final Logger logger=LogManager.getLogger();
	
	private static volatile String YUNDONG_WC;
	
	private static volatile String JIANKANG_WC;
	
	public static final String readYunDong(){
		if(YUNDONG_WC==null){
			synchronized (WechatMessageReader.class) {
				if(SystemUtils.isOnlineEnv()){
					YUNDONG_WC=readClassPathFile("wc_yd.html");
				}else{
					YUNDONG_WC=readClassPathFile("wc_yd_qa.html");
				}
			}
		}
		return YUNDONG_WC;
	}
	
	public static final String readJIANKANG(){
		if(JIANKANG_WC==null){
			synchronized (WechatMessageReader.class) {
				if(SystemUtils.isOnlineEnv()){
					JIANKANG_WC=readClassPathFile("wc_jk.html");
				}else{
					JIANKANG_WC=readClassPathFile("wc_jk_qa.html");
				}
			}
		}
		return JIANKANG_WC;
	}
	
	
	private static String readClassPathFile(String path){
		try{
			String filePath = WechatMessageReader.class.getClassLoader().getResource(path).getPath();
			return StringUtils.join(Files.readLines(new File(filePath), Charset.forName("UTF-8")).toArray(new String[0]), "\n");
		}catch(IOException e){
			logger.error("load_yundong_welcome_txt_fail,",e);
		}
		return "";
	}
	public static void main(String[] args) {
		System.out.println(Charset.defaultCharset());
		System.out.println(readYunDong());
	}
}
