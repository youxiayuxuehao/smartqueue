package com.xuehao.smartqueue.uia.impl;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.xuehao.smartqueue.uia.ServiceMetadata;
import com.xuehao.smartqueue.utils.SmartQueueException;

public class DefaultServiceMetadata implements ServiceMetadata {
	private String serviceName;
	private String servicePath;
	private String serviceClass;

	private GenericObjectPoolConfig config;

	private Class<AbsRpcClientProxy> clazz;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("serviceName=").append(serviceName).append(",");
		sb.append("servicePath=").append(servicePath).append(",");
		sb.append("serviceClass=").append(serviceClass).append(",");
		sb.append("config={");
		if (null != config) {
			sb.append("minIdle=").append(config.getMinIdle()).append(",");
			sb.append("maxIdle=").append(config.getMaxIdle()).append(",");
			sb.append("maxTotal=").append(config.getMaxTotal()).append(",");
			sb.append("evictionIdle=")
					.append(config.getTimeBetweenEvictionRunsMillis() > 0)
					.append(",");
			sb.append("blockWhenExhausted=")
					.append(config.getBlockWhenExhausted()).append(",");
			sb.append("maxWaitMillis=").append(config.getMaxWaitMillis());
		}

		sb.append("}");
		sb.append("}");
		return sb.toString();
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setServicePath(String servicePath) {
		this.servicePath = servicePath;
	}

	public void setServiceClass(String serviceClass) {
		this.serviceClass = serviceClass;
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}

	@Override
	public String getServicePath() {
		return servicePath;
	}

	@Override
	public String getServiceClass() {
		return serviceClass;
	}

	@Override
	public Class<AbsRpcClientProxy> getClazz() {
		if (null == clazz) {
			try {
				clazz = (Class<AbsRpcClientProxy>) Class.forName(serviceClass
						.trim());
			} catch (ClassNotFoundException e) {
				throw new SmartQueueException(e);
			}
		}
		return clazz;
	}

	@Override
	public GenericObjectPoolConfig getConfig() {
		return config;
	}

	public void setConfig(GenericObjectPoolConfig config) {
		this.config = config;
	}

}
