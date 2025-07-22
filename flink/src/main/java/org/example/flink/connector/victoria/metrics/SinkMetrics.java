package org.example.flink.connector.victoria.metrics;

import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.MetricGroup;

import lombok.Getter;

public class SinkMetrics {
	private final Counter[] counters;

	private SinkMetrics(Counter[] counters) {
		this.counters = counters;
	}

	public void inc(SinkCounter counter, long value) {
		counters[counter.ordinal()].inc(value);
	}

	public void inc(SinkCounter counter) {
		counters[counter.ordinal()].inc();
	}

	/**
	 * Register all custom sink metrics and return an of this wrapper class.
	 */
	public static SinkMetrics registerSinkMetrics(MetricGroup metrics) {
		// Register all counters
		Counter[] counters = new Counter[SinkCounter.values().length];
		for (SinkCounter metric : SinkCounter.values()) {
			counters[metric.ordinal()] = metrics.counter(metric.getMetricName());
		}
		return new SinkMetrics(counters);
	}

	/**
	 * Enum defining all sink counters.
	 */
	@Getter
	public enum SinkCounter {

		/**
		 * Total number of Samples that were dropped because of causing non-retryable errors.
		 */
		NUM_SAMPLES_NON_RETRYABLE_DROPPED("numSamplesNonRetryableDropped"),

		/**
		 * Number of Samples dropped after reaching retry limit on retryable errors.
		 */
		NUM_SAMPLES_RETRY_LIMIT_DROPPED("numSamplesRetryLimitDropped"),

		/**
		 * Total number of Samples dropped due to any reasons: retryable errors reaching retry
		 * limit, non-retryable errors, unexpected IO errors.
		 */
		NUM_SAMPLES_DROPPED("numSamplesDropped"),

		/**
		 * Number of Samples successfully written to Prometheus.
		 */
		NUM_SAMPLES_OUT("numSamplesOut"),

		/**
		 * Number of WriteRequests successfully sent to Prometheus.
		 */
		NUM_WRITE_REQUESTS_OUT("numWriteRequestsOut"),

		/**
		 * Number of permanently failed WriteRequests.
		 */
		NUM_WRITE_REQUESTS_PERMANENTLY_FAILED("numWriteRequestsPermanentlyFailed"),

		/**
		 * Number of WriteRequests retries.
		 */
		NUM_WRITE_REQUESTS_RETRIES("numWriteRequestsRetries");

		private final String metricName;

		SinkCounter(String metricName) {
			this.metricName = metricName;
		}
	}
}
