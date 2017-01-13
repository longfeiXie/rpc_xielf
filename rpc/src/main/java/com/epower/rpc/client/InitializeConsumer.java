package com.epower.rpc.client;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.epower.rpc.annotation.client.RpcConsumer;
import com.epower.rpc.entity.Request;
import com.epower.rpc.entity.Response;
import com.epower.rpc.service.ServiceDiscovery;
import com.epower.rpc.service.impl.ZkServiceDiscoveryImpl;
import com.epower.rpc.util.seralizable.RemoteMethodInfo;

/**
 * 初始化服务器
 * 
 * @author xielf
 *
 */
@SuppressWarnings("unchecked")
public class InitializeConsumer implements ApplicationContextAware, InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(InitializeConsumer.class);
	private String registryAddress;
	private ServiceDiscovery discovery;
	private ApplicationContext applicationContext;

	public String getRegistryAddress() {
		return registryAddress;
	}

	public void setRegistryAddress(String registryAddress) {
		this.registryAddress = registryAddress;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	private <T> T create(final Class<T> clazz, final String beanName) {

		return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, new InvocationHandler() {

			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

				Request request = new Request();
				String methodName = method.getName();
				Class<?>[] clazz = method.getParameterTypes();
				Class<?> resultType = method.getReturnType();
				request.setArgs(args);
				request.setMethodName(methodName);
				request.setParameterTypes(clazz);
				request.setServiceBeanName(beanName);
				request.setResultType(resultType);

				// 目前只有zookeeper
				if (discovery == null) {
					discovery = new ZkServiceDiscoveryImpl(registryAddress);
				}
				RemoteMethodInfo remoteMethodInfo = discovery.discover(request);
				String address = remoteMethodInfo.getAddress();
				int port = remoteMethodInfo.getPort();

				Response response = RpcClient.getInstance().remoteInvoke(address, port, request);
				if (response.getError() != null) {
					throw response.getError();
				} else {
					return response.getResult();
				}
			}
		});
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		if (!StringUtils.isEmpty(registryAddress)) {

			Map<String, Object> rpcServiceBeanMap = applicationContext.getBeansWithAnnotation(Component.class);

			for (Object serviceBean : rpcServiceBeanMap.values()) {

				LOG.info("class name: " + serviceBean.getClass().getName());
				Field[] fields = serviceBean.getClass().getDeclaredFields();
				for (Field field : fields) {

					LOG.info("field name: " + field.getName());
					if (field.getType().isInterface()) {
						RpcConsumer rpcConsumer = field.getAnnotation(RpcConsumer.class);
						if (rpcConsumer != null) {
							String value = rpcConsumer.value();
							value = StringUtils.isEmpty(value) ? field.getType().getSimpleName() : value;
							try {
								LOG.info("consumer name: " + value);
								field.setAccessible(true);
								field.set(serviceBean, create(field.getType(), value));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			LOG.info("initalize rpc client success");
		}
	}
	public static void main(String[] args) throws IOException {
		
		
		Properties properties = new Properties();
		properties.load(InitializeConsumer.class.getClassLoader().getResourceAsStream("com/epower/rpc/client/file.properties"));
		System.out.println(InitializeConsumer.class.getClassLoader().getResource("").getPath());
		System.out.println(properties.get("a"));
		
		
	}
}
