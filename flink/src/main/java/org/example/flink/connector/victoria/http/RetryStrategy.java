package org.example.flink.connector.victoria.http;

import static org.example.flink.connector.victoria.http.HttpResponseType.RETRYABLE_ERROR;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLException;

import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.example.flink.connector.victoria.VictoriaSinkConfiguration;
import org.example.flink.connector.victoria.metrics.SinkMetricsCallback;

import lombok.extern.slf4j.Slf4j;

import static org.example.flink.connector.victoria.http.HttpResponseClassifier.*;

@Slf4j
public class RetryStrategy implements HttpRequestRetryStrategy {
	/**
	 * List of exceptions considered non-recoverable (non-retryable).
	 */
	private static final List<Class<? extends IOException>> NON_RECOVERABLE_EXCEPTIONS = List.of(
		InterruptedIOException.class,
		UnknownHostException.class,
		ConnectException.class,
		NoRouteToHostException.class,
		SSLException.class
	);

	private final long initialRetryDelayMs;
	private final long maxRetryDelayMs;
	private final int maxRetryCount;
	private final SinkMetricsCallback metricCallback;

	public RetryStrategy(
		@Nonnull VictoriaSinkConfiguration.RetryConfiguration retryConfiguration,
		@Nonnull SinkMetricsCallback metricCallback) {
		this.initialRetryDelayMs = retryConfiguration.initialRetryDelayMS();
		this.maxRetryDelayMs = retryConfiguration.maxRetryDelayMS();
		this.maxRetryCount = retryConfiguration.maxRetryCount();
		this.metricCallback = metricCallback;
	}

	@Override
	public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
		// Retry on any IOException except those considered non-recoverable
		boolean retry = (execCount <= maxRetryCount)
			&& !(NON_RECOVERABLE_EXCEPTIONS.contains(exception.getClass()));
		log.debug(
			"{} retry on {}, at execution {}",
			(retry) ? "DO" : "DO NOT",
			exception.getClass(),
			execCount);
		countRetry(retry);
		return retry;
	}

	@Override
	public boolean retryRequest(HttpResponse response, int execCount, HttpContext context) {
		boolean retry = (execCount <= maxRetryCount) && (classify(response) == RETRYABLE_ERROR);
		log.debug(
			"{} retry on response {} {}, at execution {}",
			(retry) ? "DO" : "DO NOT",
			response.getCode(),
			response.getReasonPhrase(),
			execCount);
		countRetry(retry);
		return retry;
	}

	@Override
	public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
		long calculatedDelay = initialRetryDelayMs << (execCount - 1);
		return TimeValue.ofMilliseconds(Math.min(calculatedDelay, maxRetryDelayMs));
	}

	private void countRetry(boolean retry) {
		if (retry) {
			metricCallback.onWriteRequestRetry();
		}
	}
}
