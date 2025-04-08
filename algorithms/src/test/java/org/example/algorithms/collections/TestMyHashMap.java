package org.example.algorithms.collections;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestMyHashMap {
	@Test
	public void testMyHashMap() {
		MyHashMap<String, String> myHashMap = new MyHashMap<>();

		int count = 100000;
		for (int i = 0; i < count; i++) {
			myHashMap.put(String.valueOf(i), String.valueOf(i));
		}

		assertEquals(count, myHashMap.size);

		for (int i = 0; i < count; i++) {
			myHashMap.remove(String.valueOf(i));
		}

		assertEquals(0, myHashMap.size);
	}
}
