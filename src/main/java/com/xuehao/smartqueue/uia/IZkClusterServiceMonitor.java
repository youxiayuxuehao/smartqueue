package com.xuehao.smartqueue.uia;

/**
 * 集群服务监视器
 * 
 * @author 余学好(qq:398520134)
 * @date 2016年10月12日
 */
public interface IZkClusterServiceMonitor {

	/**
	 * 加入并监视集群服务
	 * 
	 * @param cluster
	 * @param listener
	 */
	void joinAndMonitor(final ZkCluster cluster,
			final IZkClusterServiceStatusListener listener);

	void destroy();
}
