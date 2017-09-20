package com.yuandu.wechatgateway.service.dto;

public class QueryMPUserParam
{
    private String openid;
    
    private int appTypeCode;
    
    private String serviceNo;

    public String getOpenid()
    {
        return openid;
    }

    public void setOpenid(String openid)
    {
        this.openid = openid;
    }

    public int getAppTypeCode()
    {
        return appTypeCode;
    }

    public void setAppTypeCode(int appTypeCode)
    {
        this.appTypeCode = appTypeCode;
    }

	public String getServiceNo() {
		return serviceNo;
	}

	public void setServiceNo(String serviceNo) {
		this.serviceNo = serviceNo;
	}
    
}
