package org.example.flink.connector.victoria;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.example.flink.connector.victoria.utils.Json;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class VTMRequestEntryHandlerTest {

	private VTMRequestEntryHandler handler;

	@BeforeEach
	public void setUp() {
		handler = new VTMRequestEntryHandler();
	}

	@Test
	public void test() throws IOException {
		var requestEntries = RequestGenerator.metricEntries(2);

		// Merge request
		byte[] body = handler.mergeRequests(requestEntries);
		List<?> metrics = Json.deserialize(body, List.class);
		assertEquals(requestEntries.size(), metrics.size());

		// Get sample count
		assertEquals(requestEntries.size(), handler.getSampleCount(requestEntries));

		// Get size in bytes
		var requestEntity = requestEntries.get(0);
		assertEquals(requestEntity.sizeInBytes(), handler.getSizeInBytes(requestEntity));
	}
}
