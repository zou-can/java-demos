package org.example.algorithms.concurrency;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class MyLock {

	AtomicBoolean flag = new AtomicBoolean(false);

	/**
	 * 为了避免被非持有锁的线程解锁
	 */
	Thread owner = null;

	/**
	 * 将阻塞的线程保存在链表尾部，唤醒时取头部节点
	 */
	AtomicReference<Node> head = new AtomicReference<>(new Node());

	AtomicReference<Node> tail = new AtomicReference<>(head.get());

	/*
	 * 公平锁：取消第一步 if 尝试获取锁的步骤
	 * 可重入锁：将 flag 变为 AtomicInteger 计数器
	 */
	public void lock() {
		if (flag.compareAndSet(false, true)) {
			// 获取了锁，记录获取锁的线程，并返回
			owner = Thread.currentThread();
			return;
		}

		// 没有获取锁，把线程保存到队列尾部，并阻塞线程

		Node current = new Node();
		current.thread = Thread.currentThread();
		while (true) {
			Node currentTail = tail.get();
			if (tail.compareAndSet(currentTail, current)) {
				current.prev = currentTail;
				currentTail.next = current;
				break;
			}
		}

		while (true) {
			// 再尝试一次是否能拿到锁，拿不到锁再阻塞。
			// 如果先阻塞在执行重新获取锁的逻辑，
			// 假设解锁过程在阻塞之前执行（比如在前面加入链表尾部的时候），可能该线程永远不会被唤醒。

			// Head -> A -> B -> C
			// 尝试重新获取锁
			if (current.prev == head.get() && flag.compareAndSet(false, true)) {
				// 当前线程是第一个线程，且成功获取了锁

				// 1. 记录获取锁的线程
				owner = Thread.currentThread();

				// 2. 更新头节点，这里不需要 CAS，因为线程已经获取锁了
				head.set(current);
				// 断开与原来 head 的连接
				current.prev.next = null;
				current.prev = null;
				return;
			}

			LockSupport.park();
		}

	}

	public void unlock() {
		if (owner != Thread.currentThread()) {
			throw new IllegalStateException("Lock is not owned by this thread");
		}

		Node headNode = head.get();
		Node next = headNode.next;
		// 当前线程一定是持有锁的线程，因此也不需要 CAS 操作
		// 这行必须在获取头节点之后执行，因为一旦释放锁，其他线程可能会获取锁，此时头节点可能会变动。
		flag.set(false);
		if (next != null) {
			LockSupport.unpark(next.thread);
		}
	}

	/**
	 * 双向链表，保存阻塞的线程
	 */
	class Node {
		Node prev;
		Node next;
		Thread thread;
	}

}


