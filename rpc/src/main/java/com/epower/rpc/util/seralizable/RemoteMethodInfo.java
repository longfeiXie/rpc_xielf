package com.epower.rpc.util.seralizable;

public class RemoteMethodInfo {

	private String address;
	private int port;
	private String methodName;
	private Class<?> resultType;
	private Class<?>[] parametersType;
	
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public Class<?> getResultType() {
		return resultType;
	}
	public void setResultType(Class<?> resultType) {
		this.resultType = resultType;
	}
	public Class<?>[] getParametersType() {
		return parametersType;
	}
	public void setParametersType(Class<?>[] parametersType) {
		this.parametersType = parametersType;
	}
}
