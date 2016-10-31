package com.xuehao.smartqueue.uia.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xuehao.smartqueue.uia.ICommonDistributeServiceManager;
import com.xuehao.smartqueue.utils.SmartQueueException;

/**
 * 分布式系统注册组件<br>
 * 
 * 分布式系统，通过指定zk集群的地址，将自己以命名服务的方式注册到集群中，<br>
 * 
 * 服务调用方，通过集群的元数据查询到此服务的地址，完成服务发现与调用<br>
 * 
 * @author 余学好(qq:398520134)
 * @date 2015年12月8日
 */
public class CommonDistributeServiceManager implements
		ICommonDistributeServiceManager, Watcher {

	private static final Logger log = LoggerFactory
			.getLogger(CommonDistributeServiceManager.class);

	private String zkAddress;// zk的地址
	private ZooKeeper zk;
	private CountDownLatch lock = new CountDownLatch(1);
	private boolean firstConnetced = true;
	private String service;
	private String data;
	private int sessionTimeout = 4000;// 会话超时值

	private boolean disconnect = false;
	private boolean expired = false;

	/**
	 * 初始化方法，用于连接zk
	 */
	@Override
	public void init() {
		if (null == zkAddress) {
			log.error("error zkAddress" + zkAddress);
			throw new SmartQueueException("error zkAddress" + zkAddress);
		}
		try {
			zk = new ZooKeeper(zkAddress, sessionTimeout, this);
			lock.await(20, TimeUnit.SECONDS);// 同步等待zk的应答，如果超时，则报错
		} catch (Exception e) {
			log.error("connect to zk fail :" + e.getMessage());
			throw new SmartQueueException(e);
		}
	}

	/**
	 * 重建与zk的会话<br>
	 * 不管是第一次还是后续会话失效，都用此接口完成
	 * 
	 */
	private void reBuildSession() {
		shutdown();// 释放之前的资源
		try {
			zk = new ZooKeeper(zkAddress, sessionTimeout, this);
		} catch (Exception e) {
			throw new RuntimeException(e.getCause());
		}
	}

	/**
	 * 
	 * 注册命名服务。 连接上zk后，发起注册。
	 */
	@Override
	public boolean regist(String service, String data) {
		if (null == zk) {
			log.error("not init,init this first");
			throw new SmartQueueException("not init,init this first");
		}

		this.service = service;
		this.data = data;
		doRegist();
		return true;
	}

	/**
	 * 掉线重连后，重新注册
	 */
	private void reRegist() {
		if (null != service && null != data)
			doRegist();
	}

	/**
	 * 内部注册接口
	 */
	private void doRegist() {
		try {
			zk.create(service + "/node", data.getBytes(), Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL_SEQUENTIAL);// 瞬时节点
			log.info("regist {} to zk success", service);
		} catch (Exception e) {
			log.error("regist service error");
			throw new SmartQueueException(e);
		}
	}

	/**
	 * 关闭连接<br>
	 * zk关闭后，注册的服务会自动注销
	 * 
	 */
	@Override
	public void shutdown() {
		if (null != zk)
			try {
				zk.close();
			} catch (InterruptedException e) {
			}
	}

	/**
	 * zk事件，包括
	 * 
	 * <li>连接上通知(connected) <li>掉线通知(disconnect) <li>会话过期(expire)
	 * 
	 * 程序应该处理掉线等事件
	 * 
	 */
	@Override
	public void process(WatchedEvent event) {
		EventType eventType = event.getType();
		if (EventType.None == eventType
				&& KeeperState.SyncConnected == event.getState()) {
			if (firstConnetced) {
				log.info("connect to zk {{}} success", zkAddress);
				lock.countDown();
				firstConnetced = false;
			} else {
				if (disconnect) {
					// reRegist();
					disconnect = false;
					log.info("reconnect to server success");
				}
				if (expired) {
					reRegist();
					expired = false;
					log.info("reBuild session success");
				}
			}
		} else if (EventType.None == eventType
				&& KeeperState.Disconnected == event.getState()) {
			log.info(" disconnect to server");
			disconnect = true;
			// reBuildSession();
		} else if (KeeperState.Expired == event.getState()) {
			log.info("session expired and try to rebuild session again");
			expired = true;
			reBuildSession();
		} else {
			log.info("other event : " + event.toString());
		}
	}

	public String getZkAddress() {
		return zkAddress;
	}

	public void setZkAddress(String zkAddress) {
		this.zkAddress = zkAddress;
	}

	public int getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

}
