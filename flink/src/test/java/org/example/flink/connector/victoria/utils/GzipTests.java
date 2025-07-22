package org.example.flink.connector.victoria.utils;

import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GzipTests {

	@Test
	public void testCompress() throws IOException {
		Random random = new Random();
		int size = 100000;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append((char)(random.nextInt(95) + 32));
		}

		byte[] data = sb.toString().getBytes();
		byte[] compressed = Gzip.compress(data);

		log.info("original data length = {}, compress data length = {}", data.length, compressed.length);
	}
}
