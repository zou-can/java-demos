package org.example.flink.connector.victoria;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VictoriaSinkConfigurationTest {

	@Test
	public void test() {
		var writeConfig = VictoriaSinkConfiguration.WriterConfiguration.builder()
			.maxBatchSize(500)
			.maxBatchSizeInBytes(50 * 1024 * 1024)
			.maxBufferedRequests(1000)
			.maxRecordSizeInBytes(10 * 1024 * 1024)
			.maxInFlightRequests(1)
			.requestTimeoutMS(60000)
			.failOnTimeout(false)
			.build();

		var sinkConfig = VictoriaSinkConfiguration.builder()
			.url("http://localhost:8080")
			.metricGroup("test")
			.writerConfig(writeConfig)
			.errorHandlingConfig(VictoriaSinkConfiguration.ErrorHandlingConfiguration.getDefault())
			.retryConfig(VictoriaSinkConfiguration.RetryConfiguration.getDefault())
			.build();

		log.info("Sink config: {}", sinkConfig);
	}
}
