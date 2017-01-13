package com.epower.rpc.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.epower.rpc.annotation.server.RpcService;
import com.epower.rpc.service.ServiceRegistry;
import com.epower.rpc.util.seralizable.kryo.netty.codec.KryoDecoder;
import com.epower.rpc.util.seralizable.kryo.netty.codec.KryoEncoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 初始化服务器
 * @author xielf
 *
 */
public class InitializeProducer implements ApplicationContextAware, InitializingBean{

	private static final Logger LOG = LoggerFactory.getLogger(InitializeProducer.class);
	private Map<String,Object> serverHandlerMap = new HashMap<String, Object>();
	private String registryAddress;
	private int serverBindPort;
	private ApplicationContext applicationContext;
	
	public String getRegistryAddress() {
		return registryAddress;
	}
	public void setRegistryAddress(String registryAddress) {
		this.registryAddress = registryAddress;
	}
	public int getServerBindPort() {
		return serverBindPort;
	}
	public void setServerBindPort(int serverBindPort) {
		this.serverBindPort = serverBindPort;
	}
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	public void afterPropertiesSet() throws Exception {
		
		Map<String, Object> rpcServiceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
		
		Thread thread = new Thread(new Runnable() {
			public void run() {
				LOG.info("initializing producer");
				EventLoopGroup bossGroup = new NioEventLoopGroup();
				EventLoopGroup workerGroup = new NioEventLoopGroup();
				
				try {
					ServerBootstrap bootstrap = new ServerBootstrap();
					
					bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new KryoEncoder());
							ch.pipeline().addLast(new KryoDecoder());
							ch.pipeline().addLast(new RpcServerHandler(serverHandlerMap));
						}
					});
					
					ChannelFuture channelFuture = bootstrap.bind(serverBindPort).sync();
					LOG.info("initialized producer");
					channelFuture.channel().closeFuture().sync();
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					bossGroup.shutdownGracefully();
					workerGroup.shutdownGracefully();
				}
			}
		});
		thread.setName("rpc producer initialize thread");
		thread.start();
		
		for (Object serviceBean : rpcServiceBeanMap.values()) {
			String serviceBeanName = serviceBean.getClass().getAnnotation(RpcService.class).value();
			serverHandlerMap.put(serviceBeanName, serviceBean);
			ServiceRegistry registry = ServiceRegistry.getInstance(registryAddress,serverBindPort,ServiceRegistry.ZOOKEEPER);
			Method[] methods = serviceBean.getClass().getDeclaredMethods();
			for (Method method : methods) {
				registry.registry(method.getName(), method.getReturnType(), method.getParameterTypes());
			}
		}
	}
}
