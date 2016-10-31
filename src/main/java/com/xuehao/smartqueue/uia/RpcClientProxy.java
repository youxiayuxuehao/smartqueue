package com.xuehao.smartqueue.uia;

/**
 * 客户端抽象
 * 
 * @author 余学好(qq:398520134)
 * @date 2016年10月12日
 */
public interface RpcClientProxy {

	/**
	 * 配置
	 */
	void configure(String data);

	/**
	 * 初始化，初始化后应该达到可使用状态
	 */
	void init();

	/**
	 * 检测此proxy是否可用
	 * 
	 * @return
	 */
	boolean isAlive();

	/**
	 * destory
	 */
	void destory();

	/**
	 * 元数据
	 * 
	 * @return
	 */
	ServiceMetadata getMetadata();

	String getPathData();

}
