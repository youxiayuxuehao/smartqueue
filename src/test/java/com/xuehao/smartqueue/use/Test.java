package com.xuehao.smartqueue.use;

import com.xuehao.smartqueue.uia.impl.ServiceContainer;

public class Test {

	public static void main(String[] args) throws InterruptedException {
		ServiceContainer facade = new ServiceContainer();
		facade.setConfigPath("src\\main\\resources\\example.xml");
		facade.init();

		Thread.sleep(3000);

		int threadNum = 0;
		int maxCall = Integer.MAX_VALUE;
		int sleep = 10;

		for (int i = 0; i < threadNum; i++) {
			new Thread(new Worker(facade, maxCall, sleep,i)).start();
		}

		Thread.sleep(Integer.MAX_VALUE);
		facade.destroy();
	}

}
