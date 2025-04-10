package org.example.algorithms.concurrency;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class TestMyThreadPool {
	@Test
	public void test() throws InterruptedException {
		RejectHandle rejectHandle = (command, pool) -> {
			throw new RuntimeException("阻塞队列满了！");
		};

		MyThreadPool myThreadPool = new MyThreadPool(
			2,
			4,
			1,
			TimeUnit.SECONDS,
			new ArrayBlockingQueue<>(64),
			rejectHandle);
		for (int i = 0; i < 5; i++) {
			myThreadPool.execute(() -> {
				try {
					TimeUnit.SECONDS.sleep(1);
					System.out.println("当前线程为：" + Thread.currentThread());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException(e);
				}
			});
		}

		System.out.println("主线程未阻塞");

		TimeUnit.SECONDS.sleep(10);
	}
}
