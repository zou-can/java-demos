package org.example.flink.connector.victoria;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.example.flink.connector.victoria.utils.Json;
import org.junit.jupiter.api.Test;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LokiStreamTest {

	@Test
	public void test() {
		// Empty obj
		var obj = new LokiStream(null, null);
		var json = Json.objToJson(obj);

		log.info("{} : {}", obj, json);
		var deserialized = Json.deserialize(json, LokiStream.class);
		assertEquals(obj, deserialized);

		// No other fields
		obj = new LokiStream(
			Map.of("deviceId", "123"),
			Collections.singletonList(
				Arrays.asList("0", "hello world")
			)
		);
		json = Json.objToJson(obj);
		log.info("{} : {}", obj, json);
		deserialized = Json.deserialize(json, LokiStream.class);
		assertEquals(obj, deserialized);

		// Contains other fields
		obj = new LokiStream(
			Map.of("deviceId", "123"),
			Collections.singletonList(
				Arrays.asList(
					"0",
					"hello world",
					Map.of(
						"log.level", "info",
						"log.type", "test"
					)
				)
			)
		);
		json = Json.objToJson(obj);
		log.info("{} : {}", obj, json);
		deserialized = Json.deserialize(json, LokiStream.class);
		assertEquals(obj, deserialized);
	}
}
