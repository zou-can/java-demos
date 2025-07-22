package org.example.flink.connector.victoria.metrics;

import static org.example.flink.connector.victoria.metrics.SinkMetrics.SinkCounter.*;

public class SinkMetricsCallback {
	private final SinkMetrics metrics;

	public SinkMetricsCallback(SinkMetrics metrics) {
		this.metrics = metrics;
	}

	private void onFailedWriteRequest(long sampleCount) {
		metrics.inc(NUM_SAMPLES_DROPPED, sampleCount);
		metrics.inc(NUM_WRITE_REQUESTS_PERMANENTLY_FAILED);
	}

	public void onSuccessfulWriteRequest(long sampleCount) {
		metrics.inc(NUM_SAMPLES_OUT, sampleCount);
		metrics.inc(NUM_WRITE_REQUESTS_OUT);
	}

	public void onFailedWriteRequestForNonRetryableError(long sampleCount) {
		metrics.inc(NUM_SAMPLES_NON_RETRYABLE_DROPPED, sampleCount);
		onFailedWriteRequest(sampleCount);
	}

	public void onFailedWriteRequestForRetryLimitExceeded(long sampleCount) {
		metrics.inc(NUM_SAMPLES_RETRY_LIMIT_DROPPED, sampleCount);
		onFailedWriteRequest(sampleCount);
	}

	public void onWriteRequestRetry() {
		metrics.inc(NUM_WRITE_REQUESTS_RETRIES);
	}
}
