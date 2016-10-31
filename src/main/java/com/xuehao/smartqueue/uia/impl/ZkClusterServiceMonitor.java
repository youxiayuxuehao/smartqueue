package com.xuehao.smartqueue.uia.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xuehao.smartqueue.uia.IZkClusterServiceMonitor;
import com.xuehao.smartqueue.uia.IZkClusterServiceStatusListener;
import com.xuehao.smartqueue.uia.ServiceMetadata;
import com.xuehao.smartqueue.uia.ZkCluster;
import com.xuehao.smartqueue.utils.SmartQueueException;

public class ZkClusterServiceMonitor implements IZkClusterServiceMonitor,
		Watcher {

	private static final Logger log = LoggerFactory
			.getLogger(ZkClusterServiceMonitor.class);

	private Map<String, ServiceMetadata> images = new HashMap<String, ServiceMetadata>();// path:image

	private CountDownLatch lock = new CountDownLatch(1);
	private ZooKeeper zk;
	private boolean firstConnetced = true;// 首次链接
	private boolean disconnect = false;
	private boolean expired = false;

	private ZkCluster cluster;
	private IZkClusterServiceStatusListener listener;

	@Override
	public void joinAndMonitor(final ZkCluster cluster,
			final IZkClusterServiceStatusListener listener) {

		this.cluster = cluster;
		this.listener = listener;

		Iterator<String> names = cluster.serviceNames();
		String name = null;
		while (names.hasNext()) {
			name = names.next();
			images.put(cluster.getRpcService(name).getServicePath(),
					cluster.getRpcService(name));
		}

		try {
			log.info("begain to create session to " + cluster.getClusterName());
			zk = new ZooKeeper(cluster.getAddress(),
					cluster.getSessionTimeout() * 1000, this);
			log.info("wait zk SyncConnected notify......");
			lock.await(30, TimeUnit.SECONDS);
			log.info("OK, create zk " + cluster.getClusterName() + " success");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new SmartQueueException(e.getCause());
		}
	}

	@Override
	public void process(WatchedEvent event) {
		EventType eventType = event.getType();
		log.info("receive the message from {}, message is {}",
				cluster.getClusterName(), event.toString());
		if (EventType.None == eventType
				&& KeeperState.SyncConnected == event.getState()) {
			if (firstConnetced) {
				log.info(" connect to " + cluster.getClusterName() + " ["
						+ cluster.getAddress() + "] success");
				lock.countDown();
				firstConnetced = false;
				registAndInit();
			} else {
				if (disconnect) {
					disconnect = false;
					log.info("reconnect to server success");
				}
				if (expired) {
					expired = false;
					registAndInit();
					log.info("reBuild session success");
				}
			}

		} else if (EventType.NodeChildrenChanged == eventType) {
			log.info("NodeChildrenChanged:" + cluster.getClusterName() + ":"
					+ event.getPath());
			notify(event.getPath());
		} else if (EventType.None == eventType
				&& KeeperState.Disconnected == event.getState()) {
			disconnect = true;
			log.info(cluster.getClusterName() + " disconnect to server");
		} else if (EventType.None == eventType
				&& KeeperState.Expired == event.getState()) {
			log.info("session expired and rebuild session again");
			expired = true;
			reBuildSession();
		} else {
			log.info("other event : " + cluster.getClusterName() + ":"
					+ event.toString());
		}

	}

	private void registAndInit() {
		Iterator<String> names = cluster.serviceNames();
		String name = null;
		ServiceMetadata image = null;
		while (names.hasNext()) {
			name = names.next();
			image = cluster.getRpcService(name);

			log.info("regist " + image.getServiceName() + " to "
					+ image.getServicePath());
			notify(image.getServicePath());
		}

	}

	private void notify(String path) {
		try {
			List<String> childPaths = zk.getChildren(path, true);
			if (null == childPaths) {
				childPaths = new ArrayList<String>();
			}
			ServiceMetadata image = images.get(path);

			List<String> nodeDatas = new ArrayList<String>();

			for (String childPath : childPaths) {
				childPath = path + "/" + childPath;
				log.debug("fetch children path data {}", childPath);
				nodeDatas.add(new String(zk.getData(childPath, false, null))
						.trim());
			}

			listener.onServiceStatusChanged(image, nodeDatas);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void reBuildSession() {
		destroy();
		try {
			zk = new ZooKeeper(cluster.getAddress(),
					cluster.getSessionTimeout() * 1000, this);
		} catch (Exception e) {
			log.error("reBuild session exception");
			log.error(e.getMessage(), e);
		}
	}

	public void destroy() {
		log.info("destroy the zk " + cluster.getClusterName());
		try {
			zk.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
