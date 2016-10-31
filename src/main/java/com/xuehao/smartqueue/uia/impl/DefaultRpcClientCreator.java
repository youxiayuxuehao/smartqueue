package com.xuehao.smartqueue.uia.impl;

import com.xuehao.smartqueue.uia.IRpcClientCreator;
import com.xuehao.smartqueue.uia.ServiceMetadata;
import com.xuehao.smartqueue.utils.SmartQueueException;

/**
 * 默认的客户端工厂，界面类可以实现此接口，采用spring等其他方式提供bean的创建
 * 
 * @author 余学好(qq:398520134)
 * @date 2016年10月13日
 */
public class DefaultRpcClientCreator implements IRpcClientCreator {
	@Override
	public AbsRpcClientProxy create(ServiceMetadata metadata, String pathData) {
		try {
			AbsRpcClientProxy proxy = metadata.getClazz().newInstance();
			proxy.setMetadata(metadata);
			proxy.configure(pathData);
			proxy.init();
			return proxy;
		} catch (Throwable e) {
			throw new SmartQueueException(e);
		}
	}

}
