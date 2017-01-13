package com.epower.rpc.service;

import com.epower.rpc.entity.Request;
import com.epower.rpc.util.seralizable.RemoteMethodInfo;

public abstract class ServiceDiscovery {
	
	public abstract RemoteMethodInfo discover(Request request);
}