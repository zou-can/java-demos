package org.example.algorithms.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MyThreadPool {

	private final int corePoolSize;
	private final int maxPoolSize;
	private final int keepAliveTime;
	private final TimeUnit unit;
	private final BlockingQueue<Runnable> blockingQueue;
	private final RejectHandle rejectHandle;

	private final List<Thread> coreThreads = new ArrayList<>();
	private final List<Thread> extraThreads = new ArrayList<>();

	public MyThreadPool(int corePoolSize, int maxPoolSize, int keepAliveTime, TimeUnit unit,
		BlockingQueue<Runnable> blockingQueue, RejectHandle rejectHandle) {
		this.corePoolSize = corePoolSize;
		this.maxPoolSize = maxPoolSize;
		this.keepAliveTime = keepAliveTime;
		this.unit = unit;
		this.blockingQueue = blockingQueue;
		this.rejectHandle = rejectHandle;
	}

	/**
	 * 1. threads < core pool size: 创建新线程
	 * 2. 将任务添加到阻塞队列
	 * 3. 如果阻塞队列满了，尝试创建额外的线程
	 * 4. 再尝试添加后仍然满了，执行拒绝策略
	 * 注意：以下实现只是为了记录思路，有很多线程不安全操作。
	 */
	public void execute(Runnable command) {
		if (coreThreads.size() < corePoolSize) {
			Thread t = new CoreThread();
			coreThreads.add(t);
			t.start();
		}

		if (blockingQueue.offer(command)) {
			return;
		}

		if (coreThreads.size() + extraThreads.size() < maxPoolSize) {
			Thread t = new ExtraThread();
			extraThreads.add(t);
			t.start();
		}

		if (!blockingQueue.offer(command)) {
			rejectHandle.reject(command, this);
		}
	}

	class CoreThread extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					Runnable task = blockingQueue.take();
					task.run();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	class ExtraThread extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					Runnable task = blockingQueue.poll(keepAliveTime, unit);
					if (task == null) {
						break;
					}
					task.run();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			System.out.println(Thread.currentThread() + "线程结束了！");
		}
	}
}
