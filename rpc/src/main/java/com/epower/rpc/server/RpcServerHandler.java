package com.epower.rpc.server;

import java.lang.reflect.Method;
import java.util.Map;

import com.epower.rpc.entity.Request;
import com.epower.rpc.entity.Response;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class RpcServerHandler extends ChannelHandlerAdapter {

	private Map<String, Object> handlerMap = null;

	public RpcServerHandler(Map<String, Object> handlerMap) {
		this.handlerMap = handlerMap;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		Response response = new Response();
		try {
			Request request = (Request) msg;
			response.setRequestId(request.getRequestId());
			Object result = handle(request);
			response.setResult(result);
		} catch (Exception e) {
			e.printStackTrace();
			response.setError(e);
		}
		ctx.writeAndFlush(response);
	}

	private Object handle(Request request) throws Exception{
		
		String serviceName = request.getServiceBeanName();
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getArgs();

		Object serviceBean = handlerMap.get(serviceName);
		if (serviceBean == null) {
			throw new NullPointerException("此服务不存在:" + serviceName);
		} else {
			Method method = serviceBean.getClass().getMethod(methodName, parameterTypes);
			if (method == null) {
				throw new NullPointerException("服务方法不存在:" + serviceName + "=>" + methodName);
			} else {
				return method.invoke(serviceBean, parameters);
			}
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
