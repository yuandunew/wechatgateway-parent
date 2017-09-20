package com.yuandu.wechatgateway.rest.Controller;

import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.collections.CollectionUtils;

import com.lifesense.base.beans.param.RequestHeader;
import com.lifesense.base.constant.AppType;
import com.lifesense.base.constant.ServiceName;
import com.lifesense.base.spring.InstanceFactory;
import com.lifesense.base.utils.ServiceAssert;
import com.lifesense.soa.wechatgateway.api.IWechatGatewayProviderV2;
import com.lifesense.soa.wechatgateway.dto.receive.VerifyQrcodeResp;
import com.lifesense.soa.wechatgateway.dto.receive.WechatAuthorizeDeviceSuccessInfo;
import com.lifesense.soa.wechatgateway.dto.receive.WechatDeviceIdAndQrcode;
import com.lifesense.soa.wechatgateway.dto.receive.WechatDeviceQrcode;
import com.lifesense.soa.wechatgateway.dto.receive.WechatDeviceStatusResp;
import com.lifesense.soa.wechatgateway.dto.send.AuthorizeDevice;
import com.lifesense.soa.wechatgateway.dto.send.VerifyQrcodeParam;
import com.lifesense.soa.wechatgateway.dto.send.WechatAuthorizeDevice;
import com.lifesense.soa.wechatgateway.dto.send.WechatDeviceQrcodeParam;
import com.lifesense.soa.wechatgateway.exception.WechatGatewayErrorCode;
import com.lifesense.soa.wechatgateway.exception.WechatGatewayException;
import com.lifesense.soa.wechatgateway.rest.dto.DeviceAuthorizeReq;
import com.lifesense.soa.wechatgateway.rest.dto.DeviceAuthorizeResp;
import com.lifesense.soa.wechatgateway.rest.dto.GenQrcodeReq;
import com.lifesense.soa.wechatgateway.rest.dto.PlCheckSum;
import com.lifesense.soa.wechatgateway.rest.dto.VerifyQrcodeReq;
import com.lifesense.soa.wechatgateway.rest.dto.WechatDeviceStatusReq;
import com.lifesense.soa.wechatgateway.rest.vo.CreateDeviceQrcodeVo;
import com.lifesense.support.rest.filter.auth.annotation.AuthIgnore;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 提供给产线用,奇葩产线，不走dubbo
 * @author tim
 *
 */
@Path("/pl")
//@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
@Api(value="产线接口")
public class ProductLineApplication {
	// 通过dubbo 获取微信网关服务实例
	private IWechatGatewayProviderV2 wechatGatewayService = InstanceFactory.getInstance("wechatGatewayServiceV2");
	/**
	 * 生成设备二维码
	 * @param req
	 * @return
	 */
	@POST
	@Path("createDeviceQrcode")
	@AuthIgnore
	@ApiOperation(value="生成设备二维码")
	@ApiResponses({@ApiResponse(code=451,message="参数不能为空"),@ApiResponse(code=200,message="OK",response=WechatDeviceIdAndQrcode.class)})
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "requestId", value = "请求ID", required = true, dataType = "string", paramType = "query"),
	    @ApiImplicitParam(name = "timestamp", value = "时间截", required = true, dataType = "string", paramType = "query"),
	    @ApiImplicitParam(name = "checksum", value = "checksum", required = true, dataType = "string", paramType = "query")
	  })
	public WechatDeviceIdAndQrcode createDeviceQrcode(@BeanParam PlCheckSum checksum,GenQrcodeReq req){
		if(!checksum.verifyChecksum()){
			throw new WechatGatewayException(WechatGatewayErrorCode.CHECKSUM_ERROR);
		}
		AppType appType = AppType.getAppTypeByCode(req.getAppType());
		ServiceAssert.notNull(ServiceName.WechatGateway, "appType", appType);
		WechatDeviceIdAndQrcode wechatDeviceIdAndQrcode = wechatGatewayService.getWechatDeviceIdAndQrcode(appType);
		return wechatDeviceIdAndQrcode;
	}

	@POST
	@Path("authorizeDevice")
	@AuthIgnore
	@ApiOperation(value="设备授权")
	@ApiResponses({@ApiResponse(code=451,message="参数不能为空"),@ApiResponse(code=200,message="OK",response=DeviceAuthorizeResp.class)})
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "requestId", value = "请求ID", required = true, dataType = "string", paramType = "query"),
	    @ApiImplicitParam(name = "timestamp", value = "时间截", required = true, dataType = "string", paramType = "query"),
	    @ApiImplicitParam(name = "checksum", value = "checksum", required = true, dataType = "string", paramType = "query")
	  })
	public DeviceAuthorizeResp authorizeDevice(@BeanParam PlCheckSum checksum,DeviceAuthorizeReq req){
		if(!checksum.verifyChecksum()){
			throw new WechatGatewayException(WechatGatewayErrorCode.CHECKSUM_ERROR);
		}
		List<AuthorizeDevice> device_list = req.getDevice_list();
		String op_type = req.getOp_type();
		String product_id = req.getProduct_id();
		AppType appType = AppType.getAppTypeByCode(req.getAppType());
		ServiceAssert.notNull(ServiceName.WechatGateway, "appType", appType);
		ServiceAssert.notNull(ServiceName.WechatGateway, "product_id", product_id);
		if(CollectionUtils.isEmpty(device_list)){
			return null;
		}
		WechatAuthorizeDevice wechatAuthorizeDevice = new WechatAuthorizeDevice();
		wechatAuthorizeDevice.setDevice_list(device_list);
		wechatAuthorizeDevice.setDevice_num(String.valueOf(device_list.size()));
		wechatAuthorizeDevice.setOp_type(op_type);
		wechatAuthorizeDevice.setProduct_id(product_id);
		
		List<WechatAuthorizeDeviceSuccessInfo> authorizeDevice = wechatGatewayService.authorizeDevice(appType, wechatAuthorizeDevice);
		DeviceAuthorizeResp deviceAuthorizeResp = new DeviceAuthorizeResp();
		deviceAuthorizeResp.setAuthorizeList(authorizeDevice);
		return deviceAuthorizeResp;
	}
	/**
	 * 根据微信公众号，设备ID获取设备状态信息
	 * @param appType 应用类型
	 * @param device_id 设备ID
	 * @return 设状态信息
	 * */
	@POST
	@Path("getWechatDeviceStatus")
	@AuthIgnore
	@ApiOperation(value="获取设备状态信息")
	@ApiResponses({@ApiResponse(code=451,message="参数不能为空"),@ApiResponse(code=200,message="OK",response=WechatDeviceStatusResp.class)})
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "requestId", value = "请求ID", required = true, dataType = "string", paramType = "query"),
	    @ApiImplicitParam(name = "timestamp", value = "时间截", required = true, dataType = "string", paramType = "query"),
	    @ApiImplicitParam(name = "checksum", value = "checksum", required = true, dataType = "string", paramType = "query")
	  })
	public WechatDeviceStatusResp getWechatDeviceStatus(@BeanParam PlCheckSum checksum,WechatDeviceStatusReq req){
		if(!checksum.verifyChecksum()){
			throw new WechatGatewayException(WechatGatewayErrorCode.CHECKSUM_ERROR);
		}
		AppType appType = AppType.getAppTypeByCode(req.getAppType());
		String device_id = req.getDevice_id();
		ServiceAssert.notEmpty(ServiceName.WechatGateway, "device_id", device_id);
		ServiceAssert.notNull(ServiceName.WechatGateway, "appType", appType);
		WechatDeviceStatusResp wechatDeviceStatus = wechatGatewayService.getWechatDeviceStatus(appType, device_id);
		return wechatDeviceStatus;
	}
	/**
	 * 验证设备二维码
	 * @param appType 应用类型
	 * @param authorizeDevice 验证设备二维码参数
	 * @return 验证设备二维码响应
	 * */
	@POST
	@Path("verifyQrcode")
	@AuthIgnore
	@ApiOperation(value="验证设备二维码")
	@ApiResponses({@ApiResponse(code=451,message="参数不能为空"),@ApiResponse(code=200,message="OK",response=VerifyQrcodeResp.class)})
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "requestId", value = "请求ID", required = true, dataType = "string", paramType = "query"),
	    @ApiImplicitParam(name = "timestamp", value = "时间截", required = true, dataType = "string", paramType = "query"),
	    @ApiImplicitParam(name = "checksum", value = "checksum", required = true, dataType = "string", paramType = "query")
	  })
	public VerifyQrcodeResp verifyQrcode(@BeanParam PlCheckSum checksum,VerifyQrcodeReq req){
		if(!checksum.verifyChecksum()){
			throw new WechatGatewayException(WechatGatewayErrorCode.CHECKSUM_ERROR);
		}
		AppType appType = AppType.getAppTypeByCode(req.getAppType());
		String ticket = req.getTicket();
		ServiceAssert.notNull(ServiceName.WechatGateway, "appType", appType);
		ServiceAssert.notEmpty(ServiceName.WechatGateway, "ticket", ticket);
		VerifyQrcodeParam verifyQrcodeParam = new VerifyQrcodeParam();
		verifyQrcodeParam.setTicket(ticket);
		VerifyQrcodeResp verifyQrcode = wechatGatewayService.verifyQrcode(appType, verifyQrcodeParam);
		return verifyQrcode;
	}
	
	
	/**
	 * 验证设备二维码
	 * @param appType 应用类型
	 * @param authorizeDevice 验证设备二维码参数
	 * @return 验证设备二维码响应
	 * */
	@SuppressWarnings("deprecation")
	@POST
	@Path("createDeviceQrcodeV0")
	@AuthIgnore
	@ApiOperation(value="创建设备二维码（旧接口，已废弃，兼容老设备）")
	@ApiResponses({@ApiResponse(code=451,message="参数不能为空"),@ApiResponse(code=200,message="OK",response=CreateDeviceQrcodeVo.class)})
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "requestId", value = "请求ID", required = true, dataType = "string", paramType = "query"),
	    @ApiImplicitParam(name = "timestamp", value = "时间截", required = true, dataType = "string", paramType = "query"),
	    @ApiImplicitParam(name = "checksum", value = "checksum", required = true, dataType = "string", paramType = "query")
	  })
	public CreateDeviceQrcodeVo createDeviceQrcodeV0(@BeanParam PlCheckSum checksum,WechatDeviceQrcodeParam param){
		if(!checksum.verifyChecksum()){
			throw new WechatGatewayException(WechatGatewayErrorCode.CHECKSUM_ERROR);
		}
		AppType appType = AppType.getAppTypeByCode(param.getAppType());
		ServiceAssert.notNull(ServiceName.WechatGateway, "appType", appType);
		
		RequestHeader requestHeader=new RequestHeader();
		requestHeader.setAppType(appType);
		
		List<WechatDeviceQrcode> createDeviceQrcode = wechatGatewayService.createDeviceQrcode(appType, param);
		CreateDeviceQrcodeVo vo=new CreateDeviceQrcodeVo();
		vo.setDeviceQrcodes(createDeviceQrcode);
		return vo;
	}
	
}
