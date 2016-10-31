package com.xuehao.smartqueue.uia;

import com.xuehao.smartqueue.uia.impl.AbsRpcClientProxy;

public interface IServiceContainer extends IZkClusterServiceStatusListener {

	AbsRpcClientProxy borrowClient(String serviceName);

	void returnClient(AbsRpcClientProxy proxy);

	void destroy();
}
