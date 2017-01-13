package com.epower.rpc.client;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.epower.rpc.entity.Request;
import com.epower.rpc.entity.Response;
import com.epower.rpc.util.seralizable.kryo.netty.codec.KryoDecoder;
import com.epower.rpc.util.seralizable.kryo.netty.codec.KryoEncoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

public class RpcClient {

	private static RpcClient rpcClient;
	private static Object objSync = new Object();
	
	private Object responseSync = new Object();
	private Response response;
	private Bootstrap bootstrap;
	private ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	private Map<String, ChannelId> channelIds = new ConcurrentHashMap<String, ChannelId>();


	public static RpcClient getInstance(){
		synchronized(objSync){
			if(rpcClient==null){
				rpcClient = new RpcClient();
			}
			return rpcClient;
		}
	}
	
	private RpcClient() {
		
		try {
			EventLoopGroup group = new NioEventLoopGroup();
			bootstrap = new Bootstrap();
			bootstrap.group(group)
			.option(ChannelOption.SO_KEEPALIVE, true)
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new KryoEncoder());
					ch.pipeline().addLast(new KryoDecoder());
					ch.pipeline().addLast(new RpcClientHandler());
				}
			});
		} catch (Exception e) {
			throw e;
		}
	}

	public Response remoteInvoke(String host, int port, Request request) throws Exception {

		String key = host + port;
		Channel channel = null;
		ChannelId channelId = channelIds.get(key);		
		if(channelId==null){
			ChannelFuture future = bootstrap.connect(host, port).sync();
			channel = future.channel();
			channelGroup.add(channel);
			channelIds.put(key, channel.id());
		}
		else{
			channel = channelGroup.find(channelId);
		}
		channel.writeAndFlush(request);
		synchronized (responseSync) {
			responseSync.wait();
		}
		return response;
	}

	/**
	 * 处理器
	 * 
	 * @author xielf
	 *
	 */
	private class RpcClientHandler extends ChannelHandlerAdapter {

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			response = (Response) msg;
			synchronized (responseSync) {
				responseSync.notify();
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close();
		}
	}
}
