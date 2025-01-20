package org.example.algorithms.collections;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestBitMap {
	@Test
	public void testBitMap() {
		BitMap bitMap = new BitMap(20);

		bitMap.set(15);
		assertTrue(bitMap.get(15));

		bitMap.set(16);
		assertTrue(bitMap.get(16));

		log.info("BitMap: {}", bitMap);

		bitMap.clear(15);
		assertFalse(bitMap.get(15));

		bitMap.clear(16);
		assertFalse(bitMap.get(16));

		log.info("BitMap: {}", bitMap);
	}
}
