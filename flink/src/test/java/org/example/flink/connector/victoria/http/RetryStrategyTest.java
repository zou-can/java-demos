package org.example.flink.connector.victoria.http;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.example.flink.connector.victoria.VictoriaSinkConfiguration;
import org.example.flink.connector.victoria.metrics.VerifybleSinkMetricsCallback;
import org.junit.Test;

import static org.example.flink.connector.victoria.http.HttpClientTestUtils.*;

public class RetryStrategyTest {

	private static final int INITIAL_RETRY_DELAY_MS = 30;
	private static final int MAX_RETRY_DELAY_MS = 5000;
	private static final VictoriaSinkConfiguration.RetryConfiguration RETRY_CONFIGURATION = VictoriaSinkConfiguration.RetryConfiguration
		.builder()
		.initialRetryDelayMS(INITIAL_RETRY_DELAY_MS)
		.maxRetryDelayMS(MAX_RETRY_DELAY_MS)
		.maxRetryCount(Integer.MAX_VALUE)
		.build();

	@Test
	public void testRetry() {
		// Retryable error response
		HttpResponse httpResponse = httpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		HttpContext httpContext = httpContext();
		VerifybleSinkMetricsCallback metricsCallback = new VerifybleSinkMetricsCallback();
		RetryStrategy strategy = new RetryStrategy(RETRY_CONFIGURATION, metricsCallback);
		assertTrue(strategy.retryRequest(httpResponse, 1, httpContext));

		// IOException
		HttpRequest httpRequest = postHttpRequest();
		assertTrue(strategy.retryRequest(httpRequest, new IOException("dummy"), 1, httpContext));
	}

	@Test
	public void testNotRetry() {
		// Non-retryable error response
		HttpResponse httpResponse = httpResponse(HttpStatus.SC_FORBIDDEN);
		HttpContext httpContext = httpContext();
		VerifybleSinkMetricsCallback metricsCallback = new VerifybleSinkMetricsCallback();

		RetryStrategy strategy = new RetryStrategy(RETRY_CONFIGURATION, metricsCallback);
		assertFalse(strategy.retryRequest(httpResponse, 1, httpContext));

		// Non-retryable exceptions
		HttpRequest httpRequest = postHttpRequest();
		assertFalse(
			strategy.retryRequest(
				httpRequest, new InterruptedIOException("dummy"), 1, httpContext));
		assertFalse(
			strategy.retryRequest(
				httpRequest, new UnknownHostException("dummy"), 1, httpContext));
		assertFalse(
			strategy.retryRequest(httpRequest, new ConnectException("dummy"), 1, httpContext));
		assertFalse(
			strategy.retryRequest(
				httpRequest, new NoRouteToHostException("dummy"), 1, httpContext));
		assertFalse(strategy.retryRequest(httpRequest, new SSLException("dummy"), 1, httpContext));
	}

	@Test
	public void retryDelayShouldDecreaseExponentiallyWithExecCount() {
		HttpResponse httpResponse = httpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		HttpContext httpContext = httpContext();
		VerifybleSinkMetricsCallback metricsCallback = new VerifybleSinkMetricsCallback();

		RetryStrategy strategy = new RetryStrategy(RETRY_CONFIGURATION, metricsCallback);

		assertEquals(
			TimeValue.ofMilliseconds(INITIAL_RETRY_DELAY_MS),
			strategy.getRetryInterval(httpResponse, 1, httpContext));
		assertEquals(
			TimeValue.ofMilliseconds(INITIAL_RETRY_DELAY_MS * 2),
			strategy.getRetryInterval(httpResponse, 2, httpContext));
		assertEquals(
			TimeValue.ofMilliseconds(INITIAL_RETRY_DELAY_MS * 2 * 2),
			strategy.getRetryInterval(httpResponse, 3, httpContext));
		assertEquals(
			TimeValue.ofMilliseconds(INITIAL_RETRY_DELAY_MS * 2 * 2 * 2),
			strategy.getRetryInterval(httpResponse, 4, httpContext));
	}

	@Test
	public void retryDelayShouldNotExceedMaximumDelay() {
		VictoriaSinkConfiguration.RetryConfiguration retryConfiguration = VictoriaSinkConfiguration.RetryConfiguration
			.builder()
			.initialRetryDelayMS(30)
			.maxRetryDelayMS(5000)
			.maxRetryCount(Integer.MAX_VALUE)
			.build();
		HttpResponse httpResponse = httpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		HttpContext httpContext = httpContext();
		VerifybleSinkMetricsCallback metricsCallback = new VerifybleSinkMetricsCallback();

		RetryStrategy strategy = new RetryStrategy(retryConfiguration, metricsCallback);

		assertEquals(
			TimeValue.ofMilliseconds(5000),
			strategy.getRetryInterval(httpResponse, 10_000, httpContext));
	}
}
