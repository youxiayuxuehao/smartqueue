package com.xuehao.smartqueue.uia.impl;

import com.xuehao.smartqueue.uia.RpcClientProxy;
import com.xuehao.smartqueue.uia.ServiceMetadata;

/**
 * 客户端桩stub
 * 
 * @author 余学好(qq:398520134)
 * @date 2016年10月14日
 */
public abstract class AbsRpcClientProxy implements RpcClientProxy {

	private ServiceMetadata metadata;
	private String pathData;

	private boolean alive = true;

	@Override
	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	@Override
	public ServiceMetadata getMetadata() {
		return metadata;
	}

	protected void setMetadata(ServiceMetadata metadata) {
		this.metadata = metadata;
	}

	public String getPathData() {
		return pathData;
	}

	protected void setPathData(String pathData) {
		this.pathData = pathData;
	}

}
