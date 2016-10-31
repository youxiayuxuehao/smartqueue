package com.xuehao.smartqueue.regist;

import com.xuehao.smartqueue.uia.ICommonDistributeServiceManager;
import com.xuehao.smartqueue.uia.impl.CommonDistributeServiceManager;

public class Test {

	public static void main(String[] args) throws InterruptedException {
		ICommonDistributeServiceManager serviceManager = new CommonDistributeServiceManager();
		String zkAddress = "192.168.1.28:2181,192.168.1.22:2181";
		String path = "/services/geo";
		String data = "192.168.4.145:8080";

		serviceManager.setZkAddress(zkAddress);
		serviceManager.init();
		serviceManager.regist(path, data);

		Thread.sleep(20 * 1000);
		serviceManager.shutdown();
		Thread.sleep(20 * 1000);

	}
}
