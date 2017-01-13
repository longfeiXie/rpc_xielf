package com.epower.rpc.client.test;

import org.springframework.stereotype.Repository;

import com.epower.rpc.annotation.client.RpcConsumer;

@Repository
public class ClientImpl implements Client{

	@RpcConsumer("hello")
	Hello hello;
	
	public String helloServer(String name) {
		return hello.hello(name);
	}
}
