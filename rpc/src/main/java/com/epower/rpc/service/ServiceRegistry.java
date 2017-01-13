package com.epower.rpc.service;

import com.epower.rpc.service.impl.ZKServiceRegistryImpl;

/**
 * 服务器注册
 * @author xielf
 *
 */
public abstract class ServiceRegistry {

	public final static String ZOOKEEPER="zookeeper";
	
	public static ServiceRegistry getInstance(String connectString, int serverBindPort, String registerType){
		
		ServiceRegistry serviceRegistry = null;
		
		switch (registerType) {
		case ZOOKEEPER:
			serviceRegistry = new ZKServiceRegistryImpl(connectString, serverBindPort);
			break;
		default:
			throw new IllegalArgumentException("不支持的注册类型");
		}
		return serviceRegistry;
	}
	
	public abstract void registry(String methodName, Class<?> resultType, Class<?>[] parametersType);
}
