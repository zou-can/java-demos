package org.example.algorithms.collections;

public class MyHashMap<K, V> {

	@SuppressWarnings("unchecked")
	Node<K, V>[] table = (Node<K, V>[])new Node[16];

	int size = 0;

	public V put(K key, V value) {
		int keyIndex = indexOf(key);
		Node<K, V> node = table[keyIndex];

		if (node == null) {
			table[keyIndex] = new Node<>(key, value);
			size++;
			resizeIfNecessary();
			return null;
		}

		while (true) {
			if (key.equals(node.key)) {
				V oldValue = node.value;
				node.value = value;
				return oldValue;
			}
			if (node.next == null) {
				node.next = new Node<>(key, value);
				size++;
				resizeIfNecessary();
				return null;
			}
			node = node.next;
		}

	}

	public V get(K key) {
		int keyIndex = indexOf(key);
		Node<K, V> node = table[keyIndex];

		while (node != null) {
			if (key.equals(node.key)) {
				return node.value;
			}
			node = node.next;
		}

		return null;
	}

	public V remove(K key) {
		int keyIndex = indexOf(key);
		Node<K, V> head = table[keyIndex];

		if (head == null) {
			return null;
		}

		if (key.equals(head.key)) {
			table[keyIndex] = head.next;
			size--;
			return head.value;
		}

		Node<K, V> prev = head;
		Node<K, V> current = head.next;

		while (current != null) {
			if (key.equals(current.key)) {
				prev.next = current.next;
				size--;
				return current.value;
			}
			prev = current;
			current = current.next;
		}

		return null;
	}

	int indexOf(Object key) {
		return key.hashCode() & (table.length - 1);
	}

	@SuppressWarnings("unchecked")
	void resizeIfNecessary() {
		if (size < 0.75 * table.length) {
			return;
		}

		Node<K, V>[] newTable = (Node<K, V>[])new Node[table.length * 2];

		for (Node<K, V> head : table) {
			if (head == null) {
				continue;
			}
			Node<K, V> current = head;
			while (current != null) {
				int newKeyIndex = current.key.hashCode() & (newTable.length - 1);
				if (newTable[newKeyIndex] == null) {
					newTable[newKeyIndex] = current;
					Node<K, V> next = current.next;
					current.next = null;
					current = next;
				} else {
					// 头插法
					// 多个线程同时执行扩容时，同时移动同一个 Node 可能会指向自己
					// JDK8 改成了尾插法，但尾插法需要遍历整个链表，会降低效率
					Node<K, V> next = current.next;
					current.next = newTable[newKeyIndex];
					newTable[newKeyIndex] = current;
					current = next;
				}

			}

		}

		this.table = newTable;
	}

	static class Node<K, V> {
		K key;
		V value;

		Node<K, V> next;

		Node(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}
}
