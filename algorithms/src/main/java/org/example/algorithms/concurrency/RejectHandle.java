package org.example.algorithms.concurrency;

public interface RejectHandle {

	void reject(Runnable command, MyThreadPool threadPool);
}
