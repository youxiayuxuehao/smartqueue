package com.xuehao.smartqueue.uia.impl;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.xuehao.smartqueue.uia.IRpcClientCreator;
import com.xuehao.smartqueue.uia.ServiceMetadata;

public class ClientPooledObjectFactory implements
		PooledObjectFactory<AbsRpcClientProxy> {

	private ServiceMetadata serviceMetadata;
	private IRpcClientCreator rpcClientCreator;
	private String pathData;

	public ClientPooledObjectFactory(ServiceMetadata serviceMetadata,
			IRpcClientCreator rpcClientCreator, String pathData) {
		this.serviceMetadata = serviceMetadata;
		this.rpcClientCreator = rpcClientCreator;
		this.pathData = pathData;
	}

	@Override
	public PooledObject<AbsRpcClientProxy> makeObject() throws Exception {
		AbsRpcClientProxy client = rpcClientCreator.create(serviceMetadata,
				pathData);
		client.setMetadata(serviceMetadata);
		client.setPathData(pathData);
		return new DefaultPooledObject<AbsRpcClientProxy>(client);
	}

	@Override
	public void destroyObject(PooledObject<AbsRpcClientProxy> p)
			throws Exception {
		p.getObject().destory();
	}

	@Override
	public boolean validateObject(PooledObject<AbsRpcClientProxy> p) {
		return p.getObject().isAlive();
	}

	@Override
	public void activateObject(PooledObject<AbsRpcClientProxy> p)
			throws Exception {
	}

	@Override
	public void passivateObject(PooledObject<AbsRpcClientProxy> p)
			throws Exception {
	}

}
