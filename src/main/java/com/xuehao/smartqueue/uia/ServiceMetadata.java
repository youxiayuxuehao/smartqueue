package com.xuehao.smartqueue.uia;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.xuehao.smartqueue.uia.impl.AbsRpcClientProxy;

/**
 * 远程RPC镜像本地镜像
 * 
 * @author 余学好(qq:398520134)
 * @date 2016年10月11日
 */
public interface ServiceMetadata {

	/**
	 * 服务名称
	 * 
	 * @return
	 */
	String getServiceName();

	/**
	 * 服务zk的path
	 * 
	 * @return
	 */
	String getServicePath();
	
	/**
	 * 本地实现类
	 * 
	 * @return
	 */
	String getServiceClass();

	/**
	 * 实现类
	 * 
	 * @return
	 */
	Class<AbsRpcClientProxy> getClazz();

	/**
	 * 获取配置
	 * 
	 * @return
	 */
	GenericObjectPoolConfig getConfig();
}
