package org.example.flink.connector.victoria;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.flink.connector.base.sink.writer.ResultHandler;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.example.flink.connector.victoria.metrics.VerifybleSinkMetricsCallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

import static org.example.flink.connector.victoria.VictoriaSinkConfiguration.OnErrorBehavior.*;

@Slf4j
public class VictoriaRequestCallbackTest {
	private static final int SAMPLE_COUNT = 1;

	private VerifybleSinkMetricsCallback metricsCallback;

	private ResultHandler<VTLRequestEntry> resultHandler;

	@BeforeEach
	public void setUp() throws Exception {
		metricsCallback = new VerifybleSinkMetricsCallback();

		resultHandler = new ResultHandler<>() {
			@Override
			public void complete() {
				// do nothing
			}

			@Override
			public void completeExceptionally(Exception e) {
				log.error("VTL request complete with exception", e);
				if (e instanceof VictoriaSinkException ve) {
					throw ve;
				}
			}

			@Override
			public void retryForEntries(List<VTLRequestEntry> requestEntriesToRetry) {
				// do nothing
			}
		};
	}

	@Test
	public void testDiscard() {
		var config = VictoriaSinkConfiguration.ErrorHandlingConfiguration.getDefault();

		VictoriaRequestCallback<VTLRequestEntry> callback = new VictoriaRequestCallback<>(
			"http://localhost:9471/insert/loki/api/v1/push",
			SAMPLE_COUNT,
			resultHandler,
			metricsCallback,
			config);

		// Success
		callback.completed(new SimpleHttpResponse(HttpStatus.SC_OK));

		// Fatal error
		assertThrows(
			VictoriaSinkException.class,
			() -> callback.completed(new SimpleHttpResponse(HttpStatus.SC_NOT_FOUND))
		);

		// Non-recoverable error
		callback.completed(new SimpleHttpResponse(HttpStatus.SC_BAD_REQUEST));

		// Recoverable error
		callback.completed(new SimpleHttpResponse(HttpStatus.SC_SERVER_ERROR));

		// Unexpected/unhandled
		assertThrows(
			VictoriaSinkException.class,
			() -> callback.completed(new SimpleHttpResponse(100))
		);
	}

	@Test
	public void testFailed() {
		var config = VictoriaSinkConfiguration.ErrorHandlingConfiguration.builder()
			.onOtherError(FAIL)
			.onMaxRetryExceeded(FAIL)
			.build();

		VictoriaRequestCallback<VTLRequestEntry> callback = new VictoriaRequestCallback<>(
			"http://localhost:9471/insert/loki/api/v1/push",
			SAMPLE_COUNT,
			resultHandler,
			metricsCallback,
			config);

		// Success
		callback.completed(new SimpleHttpResponse(HttpStatus.SC_OK));

		// Fatal error
		assertThrows(
			VictoriaSinkException.class,
			() -> callback.completed(new SimpleHttpResponse(HttpStatus.SC_NOT_FOUND))
		);

		// Non-recoverable error
		assertThrows(
			VictoriaSinkException.class,
			() -> callback.completed(new SimpleHttpResponse(HttpStatus.SC_BAD_REQUEST))
		);

		// Recoverable error
		assertThrows(
			VictoriaSinkException.class,
			() -> callback.completed(new SimpleHttpResponse(HttpStatus.SC_SERVER_ERROR))
		);

		// Unexpected/unhandled
		assertThrows(
			VictoriaSinkException.class,
			() -> callback.completed(new SimpleHttpResponse(100))
		);
	}
}
