package com.xuehao.smartqueue.uia;

import com.xuehao.smartqueue.uia.impl.AbsRpcClientProxy;

public interface IServiceContainer extends IZkClusterServiceStatusListener {

	AbsRpcClientProxy borrowClient(String serviceName);

	/**
	 * 实现会话绑定
	 * 
	 * @param serviceName
	 * @param sessionId
	 * @return
	 */
	AbsRpcClientProxy borrowClient(String serviceName, String sessionId);

	void returnClient(AbsRpcClientProxy proxy);

	void destroy();
}
