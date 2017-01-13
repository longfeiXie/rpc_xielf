package com.epower.rpc.service.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epower.rpc.service.ServiceRegistry;
import com.epower.rpc.util.Constants;
import com.epower.rpc.util.seralizable.RemoteMethodInfo;
import com.epower.rpc.util.seralizable.protostuff.ProtostuffUtils;

/**
 * 服务器注册
 * @author xielf
 *
 */
public class ZKServiceRegistryImpl extends ServiceRegistry{

	private String connectString;
	private int serverBindPort;
	private static final Logger LOG = LoggerFactory.getLogger("zookeeper 注册类");
	private CountDownLatch latch = new CountDownLatch(1);

	public ZKServiceRegistryImpl(String connectString, int serverBindPort) {
		this.connectString = connectString;
		this.serverBindPort = serverBindPort;
	}

	/**
	 * 注册服务器
	 * @param connectString
	 * @param data
	 */
	public void registry(String methodName, Class<?> resultType, Class<?>[] parametersType) {
		ZooKeeper zkClient = connectServer();
		if (zkClient != null) {
			try {
				RemoteMethodInfo info = new RemoteMethodInfo();
				info.setAddress(InetAddress.getLocalHost().getHostAddress());
				info.setMethodName(methodName);
				info.setPort(serverBindPort);
				info.setResultType(resultType);
				info.setParametersType(parametersType);
				createNode(zkClient, ProtostuffUtils.serialize(info));
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 连接zk
	 * 
	 * @throws IOException
	 */
	private ZooKeeper connectServer() {
		ZooKeeper zkClient = null;
		try {
			zkClient = new ZooKeeper(connectString, Constants.ZK_SESSION_TIMEOUT, new Watcher() {
				public void process(WatchedEvent event) {
					if (event.getState() == Event.KeeperState.SyncConnected) {
						latch.countDown();
					}
				}
			});
			latch.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return zkClient;
	}

	/**
	 * 创建节点
	 * 
	 * @param data
	 */
	private void createNode(ZooKeeper zkClient, byte[] data) {

		try {
			if (zkClient.exists(Constants.ZK_SERVICE_REGISTRY_NODE, null) == null) {
				zkClient.create(Constants.ZK_SERVICE_REGISTRY_NODE, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			String path = zkClient.create(Constants.ZK_SERVICE_DATA_NODE, data, Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL_SEQUENTIAL);
			LOG.info("create zookeeper node" + path);
		} catch (Exception e) {
			LOG.info("create zookeeper error", e);
		}
	}
}
