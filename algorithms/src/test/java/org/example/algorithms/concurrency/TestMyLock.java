package org.example.algorithms.concurrency;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TestMyLock {

	@Test
	public void test() throws InterruptedException {
		int[] count = {1000};
		List<Thread> threads = new ArrayList<>(100);
		var lock = new MyLock();
		for (int i = 0; i < 100; i++) {
			Thread t = new Thread(() -> {
				lock.lock();
				try {
					for (int j = 0; j < 10; j++) {
						try {
							Thread.sleep(2);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
						count[0]--;
					}
				} finally {
					lock.unlock();
				}
			});
			threads.add(t);
		}

		for (Thread thread : threads) {
			thread.start();
		}

		for (Thread thread : threads) {
			thread.join();
		}

		System.out.println(count[0]);
	}
}
