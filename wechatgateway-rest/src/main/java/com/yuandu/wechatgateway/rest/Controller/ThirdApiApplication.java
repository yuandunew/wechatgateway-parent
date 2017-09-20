package com.yuandu.wechatgateway.rest.Controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Optional;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lifesense.base.beans.param.RequestHeader;
import com.lifesense.base.constant.AppType;
import com.lifesense.base.constant.ServiceName;
import com.lifesense.base.constant.UserType;
import com.lifesense.base.exception.LifesenseBaseException;
import com.lifesense.base.exception.LifesenseResultCode;
import com.lifesense.base.utils.ResourceUtils;
import com.lifesense.base.utils.ServiceAssert;
import com.lifesense.soa.wechatgateway.api.IWechatGatewayProviderV2;
import com.lifesense.soa.wechatgateway.dto.receive.WechatUser;
import com.lifesense.soa.wechatgateway.dto.receive.WechatWebpageAccessTokenResp;
import com.lifesense.soa.wechatgateway.exception.WechatGatewayErrorCode;
import com.lifesense.soa.wechatgateway.exception.WechatGatewayException;
import com.lifesense.soa.wechatgateway.rest.vo.ThirdAuthorizeParam;
import com.lifesense.support.rest.filter.auth.RequestHeaderHolder;
import com.lifesense.support.rest.filter.auth.annotation.AuthIgnore;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/third")
@Component
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
@Produces(MediaType.APPLICATION_JSON)
@Singleton
@Api(value="第三方公众号授权接口",tags="第三方公众号授权接口")
public class ThirdApiApplication {
	
	private static final Logger logger=LogManager.getLogger();
	
	private ServiceName WECHAT=ServiceName.WechatGateway;
	
	@Autowired
	private IWechatGatewayProviderV2 wechatGatewayProvider;
	
	@GET
	@Path("authorize")
	@AuthIgnore
	@ApiOperation(value = "第三方公众号授权接口")
	@ApiResponses(value = { @ApiResponse(code = 451, message = "参数不能为空") })
	@ApiImplicitParams({ @ApiImplicitParam(name = "requestId", value = "请求ID", required = true, dataType = "string", paramType = "query"), })
	public void authorize(@BeanParam ThirdAuthorizeParam request,@Context HttpServletResponse httpResponse){
		AppType appType = parseAppType(request.getAppType());
		String code=request.getCode();
		String returnUrl = request.getReturnUrl();
		ServiceAssert.notEmpty(WECHAT, "code", code);
		ServiceAssert.notEmpty(WECHAT, "returnUrl", returnUrl);
		
		authorizeAddressIsValid(returnUrl);
		
		RequestHeader requestHeader=RequestHeaderHolder.get();
		requestHeader.setUserType(UserType.USER);
		
		WechatWebpageAccessTokenResp wechatLoginResp = wechatGatewayProvider.getWechatWebpageAccessTokenByCode(appType,code);
		if(wechatLoginResp!=null){
			WechatUser wechatUser = wechatGatewayProvider.getWechatUserByAppTypeAndOpenid( appType, wechatLoginResp.getOpenid());
			String url=null;
			logger.info("wechatUser,openId[{}],nickname[{}],headImg[{}]",wechatLoginResp.getOpenid(),wechatUser.getNickname(),wechatUser.getHeadimgurl());
			try {
				url = URLDecoder.decode(UriBuilder.fromUri(returnUrl).queryParam("openId", wechatLoginResp.getOpenid()).
						queryParam("nickname",URLEncoder.encode(Optional.ofNullable(wechatUser.getNickname()).orElse(""),"UTF-8")).
						queryParam("headImg",URLEncoder.encode(Optional.ofNullable(wechatUser.getHeadimgurl()).orElse(""),"UTF-8")).
						build().toString(),"UTF-8");
			} catch (UnsupportedEncodingException | IllegalArgumentException | UriBuilderException e) {
				logger.error("build redirect url fail,",e);
			}
			//重定向
			try {
				httpResponse.sendRedirect(url);
			} catch (IOException e) {
				logger.error("redirect url[{}] fail,",url,e);
			}
		}else{
			// 授权失败
			throw new WechatGatewayException(WechatGatewayErrorCode.THIRD_AUTHORIZE_FAIL);
		}
	}
	
	private static AppType parseAppType(Integer appType){
		AppType ret=null;
		if(appType!=null && appType>0){
			ret= AppType.getAppTypeByCode(appType);
		}
		if(ret==null ){
			logger.error("appType[{}] invalid!",appType);
			throw new LifesenseBaseException(ServiceName.SESSIONS, LifesenseResultCode.APPTYPE_INVALID);
		}
		return ret;
	}
	
	private static void authorizeAddressIsValid(String redirectUrl){
		URL url=null;
		try {
			url = new URL(redirectUrl);
		} catch (MalformedURLException e) {
			logger.error("invalid url[{}]",redirectUrl,e);
			throw new WechatGatewayException(WechatGatewayErrorCode.THIRD_URL_NOT_AUTHORIZE);
		}
		String host = url.getHost();
		
		String urls = ResourceUtils.get("authorize_url", "");
		if(!Arrays.stream(urls.split(",")).filter(x->{
			return StringUtils.equals(host, x);
		}).findFirst().isPresent()){
			throw new WechatGatewayException(WechatGatewayErrorCode.THIRD_URL_NOT_AUTHORIZE);
		}
	}
}
