package com.xuehao.smartqueue.uia;
/**
 * 通用分布式集群客户端，注册服务<br>
 * 任意分布式系统，提供此接口的实现，将服务已命名的方式注册到zk集群中，从而达到服务集群化的目的。
 * 
 * @author 余学好(qq:398520134)
 * @date 2015年12月8日
 */
public interface ICommonDistributeServiceManager {

	void setZkAddress(String zkAddress);

	void init();

	boolean regist(String servicePath, String data);

	void shutdown();
}
