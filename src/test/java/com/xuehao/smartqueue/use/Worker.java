package com.xuehao.smartqueue.use;

import com.xuehao.smartqueue.uia.IServiceContainer;
import com.xuehao.smartqueue.uia.impl.AbsRpcClientProxy;

public class Worker implements Runnable {

	private IServiceContainer facade;
	private int maxCall;
	private int sleep;
	private int index;

	public Worker(IServiceContainer facade, int maxCall, int sleep, int index) {
		super();
		this.facade = facade;
		this.maxCall = maxCall;
		this.sleep = sleep;
		this.index = index;
	}

	@Override
	public void run() {

		int i = 0;
		long be = System.currentTimeMillis();
		while (true) {
			if (index % 2 == 0) {
				if (System.currentTimeMillis() - be > 30 * 1000) {
					System.out.println("exit");
					break;
				}
			}

			AbsRpcClientProxy proxy = facade.borrowClient("sac");
			try {
				if (sleep > 0)
					Thread.sleep(sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			facade.returnClient(proxy);
		}
	}

}
