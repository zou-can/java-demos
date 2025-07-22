package org.example.flink.connector.victoria;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.Test;

public class VTLSinkTest {

	@Test
	public void testBuildVTLSink() {
		// Success
		var sink = VTLSink.builder()
			.url("http://localhost:8080")
			.metricGroup("VTLSink")
			.errorHandlingConfig(VictoriaSinkConfiguration.ErrorHandlingConfiguration.getDefault())
			.retryConfig(VictoriaSinkConfiguration.RetryConfiguration.getDefault())
			.writerConfig(VictoriaSinkConfiguration.WriterConfiguration
				.builder()
				.maxBatchSize(500)
				.maxBufferedRequests(1000)
				.maxBatchSizeInBytes(50 * 1024 * 1024)
				.maxRecordSizeInBytes(10 * 1024 * 1024)
				.requestTimeoutMS(Duration.ofSeconds(30).toMillis())
				.maxBufferedRequests(1000)
				.maxInFlightRequests(1)
				.build())
			.build();
		assertNotNull(sink);

		// URL is invalid
		assertThrows(
			IllegalArgumentException.class,
			() -> VTLSink.builder().url("xxx").build()
		);

		// URL is null
		assertThrows(
			IllegalArgumentException.class,
			() -> VTLSink.builder().build()
		);
	}
}
