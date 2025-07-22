package org.example.flink.connector.victoria;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.example.flink.connector.victoria.utils.Json;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VTLRequestEntryHandlerTest {

	private VTLRequestEntryHandler handler;

	@BeforeEach
	public void setUp() {
		handler = new VTLRequestEntryHandler();
	}

	@Test
	public void test() throws IOException {
		var requestEntries = RequestGenerator.logEntries(2);

		// Merge request
		byte[] body = handler.mergeRequests(requestEntries);
		LokiStreams lokiStreams = Json.deserialize(body, LokiStreams.class);
		assertEquals(requestEntries.size(), lokiStreams.streams().size());

		// Get sample count
		assertEquals(requestEntries.size(), handler.getSampleCount(requestEntries));

		// Get size in bytes
		var requestEntity = requestEntries.get(0);
		assertEquals(requestEntity.sizeInBytes(), handler.getSizeInBytes(requestEntity));
	}

}
