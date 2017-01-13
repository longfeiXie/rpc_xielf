package com.epower.rpc.server.test;

import com.epower.rpc.annotation.server.RpcService;

@RpcService("hello")
public class HelloImp implements Hello{

	public String hello(String str) {
		return "hello;;;;;;;;;;;;" + str;
	}
}
