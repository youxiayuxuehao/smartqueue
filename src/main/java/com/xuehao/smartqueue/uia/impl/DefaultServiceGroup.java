package com.xuehao.smartqueue.uia.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.xuehao.smartqueue.uia.Constants;
import com.xuehao.smartqueue.uia.IRpcClientCreator;
import com.xuehao.smartqueue.uia.ServiceGroup;
import com.xuehao.smartqueue.uia.ServiceMetadata;
import com.xuehao.smartqueue.utils.StringUtils;

public class DefaultServiceGroup implements ServiceGroup {

	private static final Logger log = LoggerFactory
			.getLogger(DefaultServiceGroup.class);

	private ServiceMetadata serviceMetadata;
	private IRpcClientCreator rpcClientCreator;

	private List<String> localAllNodes = Collections
			.synchronizedList(new ArrayList<String>());

	private Map<String, GenericObjectPool<AbsRpcClientProxy>> pools = new ConcurrentHashMap<String, GenericObjectPool<AbsRpcClientProxy>>();

	private AtomicLong accessPointer = new AtomicLong(0);
	private AtomicInteger nodeSize = new AtomicInteger(0);

	public DefaultServiceGroup(ServiceMetadata metadata,
			IRpcClientCreator rpcClientCreator) {
		this.serviceMetadata = metadata;
		this.rpcClientCreator = rpcClientCreator;
	}

	public AbsRpcClientProxy borrowClient(String serviceName) {
		return borrowClient(serviceName, null);
	}

	private int getAccessIndex(String sessionId) {
		int size = nodeSize.get();
		if (0 == size) {
			return -1;
		}

		if (StringUtils.isEmpty(sessionId)) {
			return (int) (accessPointer.incrementAndGet() % size);
		} else {
			HashCode hashCode = Hashing.murmur3_32().hashString(sessionId,
					Charsets.UTF_8);
			int ser = Math.abs(hashCode.asInt());
			return ser % size;
		}
	}

	@Override
	public AbsRpcClientProxy borrowClient(String serviceName, String sessionId) {
		int retry = 0;
		int index = 0;
		String node = null;

		while (retry++ < 2) {
			index = getAccessIndex(sessionId);
			if (-1 == index) {
				log.error("borrowClient {} fail,no visible node", serviceName);
				return null;
			}

			try {
				node = localAllNodes.get(index); // 节点变化时，可能存在访问失败的风险，使用重试机制
			} catch (Exception e) {
				log.error("borrowClient {} fail, suffered the node change",
						serviceName);
				continue;
			}

			GenericObjectPool<AbsRpcClientProxy> pool = pools.get(node);
			if (null != pool) {
				try {
					return pool.borrowObject();
				} catch (Exception e) {
					log.error("borrowClient {} fail, reason : {}", serviceName,
							e.getMessage());
				}
			} else {
				log.error(
						"borrowClient {} fail, the node {{}} have no visible pool",
						serviceName, node);// 临界态
				continue;
			}

			return null;
		}
		return null;
	}

	@Override
	public void returnClient(AbsRpcClientProxy proxy) {
		if (null != proxy) {
			String from = proxy.getPathData();
			GenericObjectPool<AbsRpcClientProxy> pool = pools.get(from);
			if (null != pool) {
				pool.returnObject(proxy);// 底层会检查状态
				return;
			}

			try {
				proxy.destory();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void onServiceStatusChanged(ServiceMetadata metadata,
			List<String> allDatas) {

		if (allDatas.isEmpty()) {
			log.info("service {} all node invisible", metadata.getServiceName());
		}

		Set<String> add = new HashSet<String>();// 新增的节点
		Set<String> rel = new HashSet<String>();// 移除的节点

		for (String node : allDatas) {
			if (!localAllNodes.contains(node)) {
				add.add(node);
			}
		}

		for (String node : localAllNodes) {
			if (!allDatas.contains(node)) {
				rel.add(node);
			}
		}

		removeNode(metadata, rel);
		addNode(metadata, add);

	}

	private void removeNode(ServiceMetadata image, Set<String> nodes) {
		for (String node : nodes) {
			log.info("remove node {} from service {}", node,
					image.getServiceName());
			nodeSize.decrementAndGet();
			while (true) {
				try {
					localAllNodes.remove(node);
					break;
				} catch (Exception e) {
					log.error("remove node " + node + " failure");
				}
			}

			destory(node);
		}
	}

	private void addNode(ServiceMetadata metadata, Set<String> nodes) {
		for (String nodeData : nodes) {
			log.info("add node {} to service {}", nodeData,
					metadata.getServiceName());
			ClientPooledObjectFactory factory = new ClientPooledObjectFactory(
					serviceMetadata, rpcClientCreator, nodeData);

			GenericObjectPoolConfig config = serviceMetadata.getConfig();
			config.setTestOnReturn(true);// 强制在回收的时候检查有效性

			int maxRetry = Constants.DEFAULT_ADD_NODE_MAX_RETRY;

			int i = 0;
			while (i++ < maxRetry) {
				GenericObjectPool<AbsRpcClientProxy> pool = new GenericObjectPool<AbsRpcClientProxy>(
						factory, config);
				try {
					pool.preparePool();// pre init
					pools.put(nodeData, pool);
					localAllNodes.add(nodeData);
					nodeSize.incrementAndGet();
					break;
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					destoryPool(pool);
					if (i >= maxRetry) {
						log.error(
								"could not preparePool for service {} ,node data={}",
								metadata.getServiceName(), nodeData);
						break;
					}
				}

				log.error(
						"preparePool for service {} fail,node data={}, and retry={}",
						new String[] { metadata.getServiceName(), nodeData,
								String.valueOf(i) });
				try {
					TimeUnit.MILLISECONDS
							.sleep(Constants.DEFAULT_ADD_NODE_WAIT);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private void destoryPool(GenericObjectPool<AbsRpcClientProxy> pool) {
		if (null != pool) {
			pool.close();
			pool.clear();
		}
	}

	private void destory(String node) {
		GenericObjectPool<AbsRpcClientProxy> pool = pools.remove(node);
		destoryPool(pool);
	}

	@Override
	public void destroy() {
		Set<String> nodes = pools.keySet();
		for (String node : nodes) {
			destory(node);
		}
	}
}
