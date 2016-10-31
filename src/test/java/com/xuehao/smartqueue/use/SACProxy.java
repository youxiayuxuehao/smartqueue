package com.xuehao.smartqueue.use;

import com.xuehao.smartqueue.uia.impl.AbsRpcClientProxy;

public class SACProxy extends AbsRpcClientProxy {

	long createTime = 0;

	@Override
	public void configure(String data) {
	}

	@Override
	public void init() {
		createTime = System.currentTimeMillis();
		System.out.println(this.toString() + ": "
				+ Thread.currentThread().getName() + ": SACProxy.init()");
	}

	@Override
	public void destory() {
		System.out.println(this.toString() + ": "
				+ Thread.currentThread().getName() + ": SACProxy.destory()");
	}

}
