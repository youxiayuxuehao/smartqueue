package com.xuehao.smartqueue.uia.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.xuehao.smartqueue.uia.ServiceMetadata;
import com.xuehao.smartqueue.uia.ZkCluster;

public class DefaultZkCluster implements ZkCluster {

	private String clusterName;
	private String address;
	private int sessionTimeout;
	private List<ServiceMetadata> serviceGroups = new ArrayList<ServiceMetadata>();
	private List<String> names = new ArrayList<String>();
	private Map<String, ServiceMetadata> map = new HashMap<String, ServiceMetadata>();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("clusterName=").append(clusterName).append(",");
		sb.append("address=").append(address).append(",");
		sb.append("sessionTimeout=").append(sessionTimeout).append(",");
		sb.append("serviceGroups=");
		sb.append("[");
		for (ServiceMetadata image : serviceGroups) {
			sb.append(image.toString()).append(",");
		}
		sb.append("]");
		sb.append("}");
		return sb.toString();
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	@Override
	public String getClusterName() {
		return clusterName;
	}

	@Override
	public String getAddress() {
		return address;
	}

	@Override
	public int getSessionTimeout() {
		return sessionTimeout;
	}

	@Override
	public Iterator<String> serviceNames() {
		return names.iterator();
	}

	@Override
	public ServiceMetadata getRpcService(String serviceName) {
		return map.get(serviceName);
	}

	public void addRpcService(ServiceMetadata image) {
		if (null != image) {
			serviceGroups.add(image);
			names.add(image.getServiceName());
			map.put(image.getServiceName(), image);
		}
	}

}
