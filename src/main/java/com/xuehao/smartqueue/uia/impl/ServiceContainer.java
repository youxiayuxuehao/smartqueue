package com.xuehao.smartqueue.uia.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xuehao.smartqueue.uia.ConfigLoader;
import com.xuehao.smartqueue.uia.Constants;
import com.xuehao.smartqueue.uia.IRpcClientCreator;
import com.xuehao.smartqueue.uia.IServiceContainer;
import com.xuehao.smartqueue.uia.IZkClusterServiceMonitor;
import com.xuehao.smartqueue.uia.ServiceGroup;
import com.xuehao.smartqueue.uia.ServiceMetadata;
import com.xuehao.smartqueue.uia.ZkCluster;

/**
 * 客户编程界面
 * 
 * @author 余学好(qq:398520134)
 * @date 2016年10月14日
 */
public class ServiceContainer implements IServiceContainer {

	private static final Logger log = LoggerFactory
			.getLogger(ServiceContainer.class);

	private volatile boolean ready;
	private volatile boolean close;

	private Map<String, ServiceGroup> serviceGroups = new ConcurrentHashMap<String, ServiceGroup>();

	/* 配置文件和加载器 */
	private String configPath;// 配置文件
	private ConfigLoader configLoader;

	/* 集群监听器。 每个集群一个单独的监听 */
	private List<IZkClusterServiceMonitor> zkClusterServiceMonitors = new ArrayList<IZkClusterServiceMonitor>();

	private IRpcClientCreator rpcClientCreator;

	private void validate() {
		if (null == rpcClientCreator) {
			rpcClientCreator = new DefaultRpcClientCreator();
		}
	}

	public void init() {
		long now = System.currentTimeMillis();

		validate();

		List<ZkCluster> clusters = loadCfg();

		for (ZkCluster zkCluster : clusters) {
			log.debug("loadConfig: {}", zkCluster.toString());
		}

		/* init the imageGroups */
		ServiceGroup group = null;
		Iterator<String> serviceNames = null;
		String serviceName = null;
		for (ZkCluster zkCluster : clusters) {
			serviceNames = zkCluster.serviceNames();
			while (serviceNames.hasNext()) {
				serviceName = serviceNames.next();
				group = new DefaultServiceGroup(
						zkCluster.getRpcService(serviceName), rpcClientCreator);
				serviceGroups.put(serviceName, group);
			}
		}

		/* join to zookeeper */
		log.debug("created the ServiceGroups and size is {}",
				serviceGroups.size());

		IZkClusterServiceMonitor monitor = null;
		for (ZkCluster zkCluster : clusters) {
			monitor = new ZkClusterServiceMonitor();
			log.info("create ZkClusterServiceMonitor for Cluster {} ",
					zkCluster.getClusterName());
			zkClusterServiceMonitors.add(monitor);
			monitor.joinAndMonitor(zkCluster, this);
		}

		ready = true;

		long consume = System.currentTimeMillis() - now;
		log.info("{} init over, and consume {} ms", getClass().getSimpleName(),
				consume);
	}

	private List<ZkCluster> loadCfg() {
		if (null == configPath) {
			throw new IllegalArgumentException("no configFile");
		}
		if (null == configLoader) {
			log.debug("load cfg use DefaultConfigLoader");
			configLoader = new DefaultConfigLoader();
		}
		return configLoader.loadFromFile(configPath);
	}

	@Override
	public AbsRpcClientProxy borrowClient(String serviceName) {
		long now = System.currentTimeMillis();
		try {
			if (!ready) {
				throw new IllegalStateException("not ready");
			}
			if (isClose()) {
				throw new IllegalStateException("had closed");
			}
			ServiceGroup group = getServiceGroup(serviceName);
			if (null != group) {
				return group.borrowClient(serviceName);
			}
			log.error("borrowClient {} fail, no ServiceGroup", serviceName);
			return null;
		} finally {
			long consume = System.currentTimeMillis() - now;
			if (consume > Constants.MONITOR_MAX_BORROW_CLIENT_MS) {
				log.warn("borrowClient {} consume {} ms", serviceName, consume);
			}
		}
	}

	private ServiceGroup getServiceGroup(String serviceName) {
		return serviceGroups.get(serviceName);
	}

	@Override
	public void returnClient(AbsRpcClientProxy proxy) {
		long now = System.currentTimeMillis();
		try {
			if (null == proxy) {
				return;
			}

			ServiceGroup group = getServiceGroup(proxy.getMetadata()
					.getServiceName());

			if (null != group) {
				group.returnClient(proxy);
				return;
			}
			if (isClose()) {
				proxy.destory();
				return;
			}

			log.error("returnClient {} fail", proxy.getMetadata()
					.getServiceName());
		} finally {
			long consume = System.currentTimeMillis() - now;
			if (consume > Constants.MONITOR_MAX_RETURN_CLIENT_MS) {
				log.warn("returnClient {} consume {} ms", proxy.getMetadata()
						.getServiceName(), consume);
			}
		}
	}

	@Override
	public void onServiceStatusChanged(ServiceMetadata metadata,
			List<String> nodeDatas) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		if (null != nodeDatas && !nodeDatas.isEmpty()) {
			for (String string : nodeDatas) {
				sb.append(string).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("]");

		log.info("onServiceStatusChanged,serviceName={},datas={}",
				metadata.getServiceName(), sb.toString());

		ServiceGroup group = getServiceGroup(metadata.getServiceName());
		group.onServiceStatusChanged(metadata, nodeDatas);
	}

	public void destroy() {

		setClose(true);

		for (IZkClusterServiceMonitor zkClusterServiceMonitor : zkClusterServiceMonitors) {
			zkClusterServiceMonitor.destroy();
		}

		Set<Entry<String, ServiceGroup>> set = serviceGroups.entrySet();
		for (Entry<String, ServiceGroup> entry : set) {
			entry.getValue().destroy();
		}
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	public ConfigLoader getConfigLoader() {
		return configLoader;
	}

	public void setConfigLoader(ConfigLoader configLoader) {
		this.configLoader = configLoader;
	}

	public IRpcClientCreator getRpcClientCreator() {
		return rpcClientCreator;
	}

	public void setRpcClientCreator(IRpcClientCreator rpcClientCreator) {
		this.rpcClientCreator = rpcClientCreator;
	}

	private boolean isClose() {
		return close;
	}

	private void setClose(boolean close) {
		this.close = close;
	}

}
