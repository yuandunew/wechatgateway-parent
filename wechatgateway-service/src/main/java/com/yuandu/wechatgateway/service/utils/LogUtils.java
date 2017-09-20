package com.yuandu.wechatgateway.service.utils;

public class LogUtils {
public static String getMsg(String type,long useTime){
	StringBuffer sbBuffer = new StringBuffer();
	if(useTime>1000&& useTime<2000){
		sbBuffer.append(type).append("_").append("1s_2s").append("_usetime_").append(useTime).append("ms");
	}
	else if(useTime>=2000 && useTime<5000){
		sbBuffer.append(type).append("_").append("2s_5s").append("_usetime_").append(useTime).append("ms");
	}
	else if(useTime>=5000){
		sbBuffer.append(type).append("_").append("5s_more").append("_usetime_").append(useTime).append("ms");
	}else{
		sbBuffer.append(type).append("_").append("1s_less").append("_usetime_").append(useTime).append("ms");
	}
//	else if(useTime>=10000 && useTime<20000){
//		sbBuffer.append(type).append("_").append("10s").append("_usetime_").append(useTime).append("ms");
//	}
//	else if(useTime>=20000 && useTime<50000){
//		sbBuffer.append(type).append("_").append("20s").append("_usetime_").append(useTime).append("ms");
//	}
//	else if(useTime>=50000){
//		sbBuffer.append(type).append("_").append("50s").append("_usetime_").append(useTime).append("ms");
//	}
	
	return sbBuffer.toString();
}
}
