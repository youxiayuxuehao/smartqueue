package com.xuehao.smartqueue.uia.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import com.xuehao.smartqueue.uia.ConfigLoader;
import com.xuehao.smartqueue.uia.Constants;
import com.xuehao.smartqueue.uia.ServiceMetadata;
import com.xuehao.smartqueue.uia.ZkCluster;
import com.xuehao.smartqueue.utils.ConfigException;
import com.xuehao.smartqueue.utils.StringUtils;

/**
 * 配置加载器
 * 
 * @author 余学好(qq:398520134)
 * @date 2016年10月12日
 */
public class DefaultConfigLoader implements ConfigLoader {

	@Override
	public List<ZkCluster> loadFromFile(String filePath) {
		return loadFromFile(new File(filePath));
	}

	@Override
	public List<ZkCluster> loadFromFile(File file) {
		List<ZkCluster> list = new ArrayList<ZkCluster>();

		SAXBuilder builder = new SAXBuilder();
		try {
			Document document = (Document) builder.build(file);
			Element rootNode = document.getRootElement();

			Element root = rootNode.getChild("zookeepers");
			if (null == root) {
				throw new ConfigException(
						"Illegal format xml,no zookeepers node");
			}

			// 读取所有集群默认配置
			Element defaultCfg = root.getChild("defaultCfg");
			int sessionTimeoutSec = Constants.DEFAULT_SESSIONTIMEOUTSEC;
			if (null != defaultCfg) {
				String _sessionTimeoutSec = defaultCfg
						.getChildText("sessionTimeoutSec");
				if (!StringUtils.isEmpty(_sessionTimeoutSec)) {
					sessionTimeoutSec = Integer.parseInt(_sessionTimeoutSec);
					if (sessionTimeoutSec < 0) {
						throw new ConfigException(
								"Illegal defaultCfg.sessionTimeoutSec");
					}
				}
			}

			Map<String, ServiceMetadata> tmpMap = new HashMap<String, ServiceMetadata>();
			Set<String> serviceNameSet = new HashSet<String>();
			Set<String> servicePathSet = new HashSet<String>();

			// 读取各个集群
			List<Element> zookeepers = root.getChildren("zookeeper");
			for (Element zookeeper : zookeepers) {
				String name = zookeeper.getChildText("name");
				String address = zookeeper.getChildText("address");
				String sessionTimeoutSec1 = zookeeper
						.getChildText("sessionTimeoutSec");
				if (StringUtils.isEmpty(name)) {
					throw new ConfigException("zookeeper have no name");
				}
				if (StringUtils.isEmpty(address)) {
					throw new ConfigException("zookeeper " + name
							+ " have no address");
				}

				DefaultZkCluster zk = new DefaultZkCluster();
				zk.setClusterName(name);
				zk.setAddress(address.trim());
				if (!StringUtils.isEmpty(sessionTimeoutSec1)) {
					if (Integer.parseInt(sessionTimeoutSec1.trim()) < 0) {
						throw new ConfigException(
								"Illegal zookeeper.sessionTimeoutSec");
					}

					zk.setSessionTimeout(Integer.parseInt(sessionTimeoutSec1
							.trim()));
				} else {
					zk.setSessionTimeout(sessionTimeoutSec);
				}

				// 读取每个集群下的服务镜像
				Element services_root = zookeeper.getChild("services");
				List<Element> services = services_root.getChildren("service");

				for (Element service : services) {
					String serviceName = service.getChildText("serviceName");
					String servicePath = service.getChildText("servicePath");
					if (StringUtils.isEmpty(serviceName)) {
						throw new ConfigException("service>serviceName empty");
					}

					if (StringUtils.isEmpty(servicePath)) {
						throw new ConfigException("service>servicePath empty");
					}
					if (serviceNameSet.contains(serviceName)) {
						throw new ConfigException("serviceName " + serviceName
								+ " not unique");
					}
					serviceNameSet.add(serviceName);

					if (servicePathSet.contains(servicePath)) {
						throw new ConfigException("servicePath " + servicePath
								+ " not unique");
					}
					servicePathSet.add(servicePath);

					DefaultServiceMetadata image = new DefaultServiceMetadata();
					image.setServiceName(serviceName);
					image.setServicePath(servicePath);
					zk.addRpcService(image);

					tmpMap.put(serviceName, image);
				}

				list.add(zk);
			}

			// ----------------分割线------------------------------------------------
			// 读取provider 模块
			Element providers_root = rootNode.getChild("providers");
			if (null == providers_root) {
				throw new ConfigException(
						"Illegal format xml,no providers node");
			}
			defaultCfg = providers_root.getChild("defaultCfg");
			int global_min_idle = Constants.DEFAULT_MIN_IDLE;
			int global_max_idle = Constants.DEFAULT_MAX_TOTAL;

			boolean global_blockWhenExhausted = Constants.DEFAULT_BLOCK_WHEN_EXHAUSTED;
			boolean global_evictable_idle = Constants.DEFAULT_EVICTABLE_IDLE;

			int global_maxWaitMillis = Constants.DEFAULT_MAX_WAITMILLIS;

			if (null != defaultCfg) {
				String tmp = defaultCfg.getChildText("minConn");
				if (!StringUtils.isEmpty(tmp)) {
					global_min_idle = Integer.parseInt(tmp);
					if (global_min_idle < 0) {
						throw new ConfigException("Illegal defaultCfg.minConn");
					}
				}

				tmp = defaultCfg.getChildText("maxConn");
				if (!StringUtils.isEmpty(tmp)) {
					global_max_idle = Integer.parseInt(tmp);
					if (global_max_idle < 0) {
						throw new ConfigException("Illegal defaultCfg.maxConn");
					}
				}

				tmp = defaultCfg.getChildText("blockWhenExhausted");
				if (!StringUtils.isEmpty(tmp)) {
					if (!StringUtils.isBoolean(tmp)) {
						throw new ConfigException(
								"Illegal defaultCfg.blockWhenExhausted");
					}
					global_blockWhenExhausted = Boolean.valueOf(tmp);
				}

				tmp = defaultCfg.getChildText("maxWaitMillis");
				if (!StringUtils.isEmpty(tmp)) {
					global_maxWaitMillis = Integer.parseInt(tmp);
					if (global_maxWaitMillis < 0) {
						throw new ConfigException(
								"Illegal defaultCfg.maxWaitMillis");
					}
				}

				tmp = defaultCfg.getChildText("evictionIdle");
				if (!StringUtils.isEmpty(tmp)) {
					if (!StringUtils.isBoolean(tmp)) {
						throw new ConfigException(
								"Illegal defaultCfg.evictionIdle");
					}
					global_evictable_idle = Boolean.valueOf(tmp);
				}

			}

			List<Element> providers = providers_root.getChildren("provider");
			for (Element provider : providers) {
				String serviceName = provider.getChildText("serviceName");
				String serviceClass = provider.getChildText("serviceClass");

				String _minIdle = provider.getChildText("minConn");
				String _maxIdle = provider.getChildText("maxConn");

				String _blockWhenExhausted = provider
						.getChildText("blockWhenExhausted");
				String _maxWaitMillis = provider.getChildText("maxWaitMillis");

				String _evictionIdle = provider.getChildText("evictionIdle");

				if (StringUtils.isEmpty(serviceName)) {
					throw new ConfigException("provider>serviceName empty");
				}
				if (StringUtils.isEmpty(serviceClass)) {
					throw new ConfigException("provider>serviceClass empty");
				}

				DefaultServiceMetadata image = (DefaultServiceMetadata) tmpMap
						.get(serviceName);
				if (null == image) {
					throw new ConfigException("have no service." + serviceName
							+ " configed");
				}
				image.setServiceClass(serviceClass);

				GenericObjectPoolConfig config = new GenericObjectPoolConfig();
				image.setConfig(config);

				int minIdle = global_min_idle;
				int maxIdle = global_max_idle;

				if (!StringUtils.isEmpty(_minIdle)) {
					if (Integer.parseInt(_minIdle.trim()) < 0) {
						throw new ConfigException("Illegal provider.minIdle");
					}
					minIdle = Integer.parseInt(_minIdle.trim());
				}

				if (!StringUtils.isEmpty(_maxIdle)) {
					if (Integer.parseInt(_maxIdle.trim()) < 0) {
						throw new ConfigException("Illegal provider.maxIdle");
					}
					maxIdle = Integer.parseInt(_maxIdle.trim());
				}

				if (minIdle > maxIdle) {
					throw new ConfigException(
							"Illegal config,minConn <= maxConn");
				}
				config.setMinIdle(minIdle);
				config.setMaxIdle(maxIdle);
				config.setMaxTotal(config.getMaxIdle() + 1);

				boolean blockWhenExhausted = global_blockWhenExhausted;
				if (!StringUtils.isEmpty(_blockWhenExhausted)) {
					if (!StringUtils.isBoolean(_blockWhenExhausted)) {
						throw new ConfigException(
								"Illegal provider.blockWhenExhausted");

					}
					blockWhenExhausted = Boolean
							.parseBoolean(_blockWhenExhausted);
				}

				int maxWaitMillis = global_maxWaitMillis;
				if (!StringUtils.isEmpty(_maxWaitMillis)) {
					if (Integer.parseInt(_maxWaitMillis.trim()) < 0) {
						throw new ConfigException(
								"Illegal provider.maxWaitMillis");
					}
					maxWaitMillis = Integer.parseInt(_maxWaitMillis.trim());
				}

				boolean evictionIdle = global_evictable_idle;
				if (!StringUtils.isEmpty(_evictionIdle)) {
					if (!StringUtils.isBoolean(_evictionIdle)) {
						throw new ConfigException(
								"Illegal provider.evictionIdle");

					}
					evictionIdle = Boolean.parseBoolean(_evictionIdle);
				}

				config.setBlockWhenExhausted(blockWhenExhausted);
				config.setMaxWaitMillis(maxWaitMillis);
				if (evictionIdle) {
					config.setTimeBetweenEvictionRunsMillis(Constants.TIME_BETWEEN_EVICTION_RUNS_MILLIS);
					config.setSoftMinEvictableIdleTimeMillis(Constants.MIN_EVICTABLE_IDLE_TIME_MILLIS);
				}

			}

			if (null == list || list.isEmpty()) {
				throw new ConfigException("no avaliable config");
			}

		} catch (ConfigException e) {
			throw e;
		} catch (Exception e) {
			throw new ConfigException(e);
		}
		return list;
	}
}
