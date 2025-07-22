package org.example.flink.connector.victoria;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.connector.base.sink.writer.config.AsyncSinkWriterConfiguration;
import org.apache.flink.util.Preconditions;

import lombok.Builder;

import static org.example.flink.connector.victoria.VictoriaSinkConfiguration.OnErrorBehavior.*;

@Builder
public record VictoriaSinkConfiguration(
										String url,
										String metricGroup,
										WriterConfiguration writerConfig,
										ErrorHandlingConfiguration errorHandlingConfig,
										RetryConfiguration retryConfig) implements Serializable {

	public static final long DEFAULT_SOCKET_TIMEOUT = Duration.ofSeconds(5).toMillis();

	public VictoriaSinkConfiguration {
		// Validation
		Preconditions.checkArgument(StringUtils.isNotBlank(url), "Url must not empty.");
		try {
			URI.create(url).toURL();
		} catch (MalformedURLException mue) {
			throw new IllegalArgumentException("Invalid URL: " + url, mue);
		}

		Preconditions.checkArgument(StringUtils.isNotBlank(metricGroup), "Metric group must not empty.");
		Preconditions.checkNotNull(writerConfig, "WriterConfig must not be null.");
		Preconditions.checkNotNull(errorHandlingConfig, "ErrorHandlingConfig must not be null.");
		Preconditions.checkNotNull(retryConfig, "RetryConfig must not be null.");
	}

	public enum OnErrorBehavior {
		DISCARD, FAIL
	}

	@Builder
	public record WriterConfiguration(
										long socketTimeout,
										int maxBatchSize,
										int maxInFlightRequests,
										int maxBufferedRequests,
										long maxBatchSizeInBytes,
										long maxTimeInBufferMS,
										long maxRecordSizeInBytes,
										long requestTimeoutMS,
										boolean failOnTimeout) implements Serializable {

		public static final long DEFAULT_SOCKET_TIMEOUT_MS = Duration.ofSeconds(30).toMillis();

		// Max buffered requests(RequestEntry)
		public static final int DEFAULT_MAX_BATCH_SIZE = 1000;
		// Max buffered requests(RequestEntry)
		public static final int DEFAULT_MAX_BUFFERED_REQUESTS = 2000;

		// Max in-flight requests is always = 1, to retain ordering
		public static final int DEFAULT_MAX_IN_FLIGHT_REQUESTS = 1;

		// MAX batch size
		public static final int DEFAULT_MAX_BATCH_SIZE_IN_BYTES = 50 * 1024 * 1024;

		// Max time a record is buffered
		public static final long DEFAULT_MAX_TIME_IN_BUFFER_MS = 5000;

		// MAX request size(RequestEntry)
		public static final int DEFAULT_MAX_RECORD_SIZE_IN_BYTES = 10 * 1024 * 1024;

		public static final long DEFAULT_REQUEST_TIMEOUT_MS = Duration.ofMinutes(1).toMillis();

		public static final boolean DEFAULT_FAIL_ON_TIMEOUT = false;

		public static WriterConfiguration getDefault() {
			return VictoriaSinkConfiguration.WriterConfiguration.builder()
				.socketTimeout(DEFAULT_SOCKET_TIMEOUT)
				.maxBatchSize(DEFAULT_MAX_BATCH_SIZE)
				.maxBatchSizeInBytes(DEFAULT_MAX_BATCH_SIZE_IN_BYTES)
				.maxInFlightRequests(DEFAULT_MAX_IN_FLIGHT_REQUESTS)
				.maxBufferedRequests(DEFAULT_MAX_BUFFERED_REQUESTS)
				.maxTimeInBufferMS(DEFAULT_MAX_TIME_IN_BUFFER_MS)
				.maxRecordSizeInBytes(DEFAULT_MAX_RECORD_SIZE_IN_BYTES)
				.requestTimeoutMS(DEFAULT_REQUEST_TIMEOUT_MS)
				.failOnTimeout(DEFAULT_FAIL_ON_TIMEOUT)
				.build();
		}

		public AsyncSinkWriterConfiguration toAsyncSinkWriterConfiguration() {
			return AsyncSinkWriterConfiguration.builder()
				.setMaxBatchSize(maxBatchSize)
				.setMaxBatchSizeInBytes(maxBatchSizeInBytes)
				.setMaxInFlightRequests(maxInFlightRequests)
				.setMaxBufferedRequests(maxBufferedRequests)
				.setMaxTimeInBufferMS(maxTimeInBufferMS)
				.setMaxRecordSizeInBytes(maxRecordSizeInBytes)
				.setFailOnTimeout(failOnTimeout)
				.setRequestTimeoutMS(requestTimeoutMS)
				.build();
		}

	}

	@Builder
	public record ErrorHandlingConfiguration(
												OnErrorBehavior onMaxRetryExceeded,
												OnErrorBehavior onOtherError) implements Serializable {

		public ErrorHandlingConfiguration {
			Preconditions.checkNotNull(onMaxRetryExceeded, "OnMaxRetryExceeded not be null.");
			Preconditions.checkNotNull(onOtherError, "OnOtherError must not be null.");
		}

		public static ErrorHandlingConfiguration getDefault() {
			return new ErrorHandlingConfiguration(DISCARD, DISCARD);
		}
	}

	@Builder
	public record RetryConfiguration(
										long initialRetryDelayMS, long maxRetryDelayMS, int maxRetryCount) implements
		Serializable {
		public static final long DEFAULT_INITIAL_RETRY_DELAY_MS = 30L;
		public static final long DEFAULT_MAX_RETRY_DELAY_MS = 5000L;
		public static final int DEFAULT_MAX_RETRY_COUNT = 5;

		public RetryConfiguration {
			Preconditions.checkArgument(initialRetryDelayMS > 0, "Initial retry delay must be > 0");
			Preconditions.checkArgument(maxRetryDelayMS > 0, "Max retry delay must be > 0");
			Preconditions.checkArgument(
				maxRetryDelayMS >= initialRetryDelayMS,
				"Max retry delay must be >= Initial retry delay");
			Preconditions.checkArgument(maxRetryCount > 0, "Max retry count must be > 0");
		}

		public static RetryConfiguration getDefault() {
			return new RetryConfiguration(
				DEFAULT_INITIAL_RETRY_DELAY_MS,
				DEFAULT_MAX_RETRY_DELAY_MS,
				DEFAULT_MAX_RETRY_COUNT);
		}
	}
}
