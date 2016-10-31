package com.xuehao.smartqueue.uia;

import java.util.List;

/**
 * Zookeeper状态监听器
 * 
 * @author 余学好(qq:398520134)
 * @date 2016年10月11日
 */
public interface IZkClusterServiceStatusListener {

	/**
	 * 节点状态变化通知
	 * 
	 * @param image
	 *            变化的服务
	 * @param nodeDatas
	 */
	void onServiceStatusChanged(ServiceMetadata image, List<String> nodeDatas);

}
