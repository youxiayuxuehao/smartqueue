package com.xuehao.smartqueue.uia;

import java.util.concurrent.TimeUnit;

public interface Constants {

	static final int DEFAULT_SESSIONTIMEOUTSEC = 4;// 4 seconds

	static final int DEFAULT_MIN_IDLE = 20;

	static final int DEFAULT_MAX_TOTAL = 1000;

	static final boolean DEFAULT_BLOCK_WHEN_EXHAUSTED = true;

	static final int DEFAULT_MAX_WAITMILLIS = 10;

	static final boolean DEFAULT_EVICTABLE_IDLE = false;

	static final long MIN_EVICTABLE_IDLE_TIME_MILLIS = TimeUnit.MINUTES
			.toMillis(5);

	static final long TIME_BETWEEN_EVICTION_RUNS_MILLIS = TimeUnit.MINUTES
			.toMillis(2);

	static final int DEFAULT_ADD_NODE_MAX_RETRY = 2;

	static final long DEFAULT_ADD_NODE_WAIT = TimeUnit.SECONDS.toMillis(5);

	static final long MONITOR_MAX_BORROW_CLIENT_MS = 10;// 10ms
	static final long MONITOR_MAX_RETURN_CLIENT_MS = 10;// 10ms
}
