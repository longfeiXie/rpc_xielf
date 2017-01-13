package com.epower.rpc.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import com.epower.rpc.entity.Request;
import com.epower.rpc.service.ServiceDiscovery;
import com.epower.rpc.util.Constants;
import com.epower.rpc.util.seralizable.RemoteMethodInfo;
import com.epower.rpc.util.seralizable.protostuff.ProtostuffUtils;

import io.netty.util.internal.ThreadLocalRandom;

public class ZkServiceDiscoveryImpl extends ServiceDiscovery{
	
	private String connectString;

	private CountDownLatch countDownLatch = new CountDownLatch(1);

	private volatile List<RemoteMethodInfo> methodInfos = new ArrayList<RemoteMethodInfo>();

	public ZkServiceDiscoveryImpl(String connectString) {
		this.connectString = connectString;
		watcherNode(initConnect());
	}

	public RemoteMethodInfo discover(Request request) {

		List<RemoteMethodInfo> methodInfoTempList = new ArrayList<RemoteMethodInfo>();
		for (RemoteMethodInfo remoteMethodInfo : methodInfos) {
			if (remoteMethodInfo.getMethodName().equals(request.getMethodName())
					&& Arrays.equals(remoteMethodInfo.getParametersType(),request.getParameterTypes())
					&& remoteMethodInfo.getResultType().equals(request.getResultType())) {
				methodInfoTempList.add(remoteMethodInfo);
			}
		}
		int size = methodInfoTempList.size();
		
		switch (size) {
		case 0:
			return null;
		case 1:
			return methodInfoTempList.get(0);
		default:
			int index = ThreadLocalRandom.current().nextInt(size);
			return methodInfoTempList.get(index);
		}
	}

	private ZooKeeper initConnect() {

		ZooKeeper zkClient = null;
		try {
			zkClient = new ZooKeeper(connectString, Constants.ZK_SESSION_TIMEOUT, new Watcher() {
				public void process(WatchedEvent event) {
					countDownLatch.countDown();
				}
			});
			countDownLatch.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return zkClient;
	}

	private void watcherNode(final ZooKeeper zkClient) {

		try {
			List<String> nodesList = zkClient.getChildren(Constants.ZK_SERVICE_REGISTRY_NODE, new Watcher() {

				public void process(WatchedEvent event) {
					if (event.getType() == Event.EventType.NodeChildrenChanged) {
						watcherNode(zkClient);
					}
				}
			});

			List<RemoteMethodInfo> methodInfos = new ArrayList<RemoteMethodInfo>();

			for (String nodeName : nodesList) {

				byte[] bytes = zkClient.getData(Constants.ZK_SERVICE_REGISTRY_NODE + "/" + nodeName, null, null);
				RemoteMethodInfo remoteMethodInfo = ProtostuffUtils.deserialize(bytes, RemoteMethodInfo.class);
				methodInfos.add(remoteMethodInfo);
			}
			this.methodInfos = methodInfos;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
