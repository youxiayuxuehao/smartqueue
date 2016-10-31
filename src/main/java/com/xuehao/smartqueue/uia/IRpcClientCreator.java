package com.xuehao.smartqueue.uia;

import com.xuehao.smartqueue.uia.impl.AbsRpcClientProxy;


/**
 * Proxy客户端工厂
 * 
 * @author 余学好(qq:398520134)
 * @date 2016年10月12日
 */
public interface IRpcClientCreator {

	AbsRpcClientProxy create(ServiceMetadata metadata, String data);
}
