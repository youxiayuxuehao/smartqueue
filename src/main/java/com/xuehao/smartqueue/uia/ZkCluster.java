package com.xuehao.smartqueue.uia;

import java.util.Iterator;

/**
 * 代表一个Zookeeper集群组网
 * 
 * @author 余学好(qq:398520134)
 * @date 2016年10月11日
 */
public interface ZkCluster {

	/**
	 * 获取集群名称
	 */
	String getClusterName();

	/**
	 * 获取集群地址
	 * 
	 * @return
	 */
	String getAddress();

	/**
	 * 获取集群会话超时值
	 * 
	 * @return
	 */
	int getSessionTimeout();

	/**
	 * 获取该集群下的所有服务名
	 * 
	 * @return
	 */
	Iterator<String> serviceNames();

	/**
	 * 根据服务名获取服务镜像
	 * 
	 * @param serviceName
	 * @return
	 */
	ServiceMetadata getRpcService(String serviceName);
}
