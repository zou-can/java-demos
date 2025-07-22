package org.example.flink.connector.victoria.http;

import java.util.Optional;

import org.apache.flink.util.Preconditions;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.example.flink.connector.victoria.VictoriaSinkConfiguration;
import org.example.flink.connector.victoria.metrics.SinkMetricsCallback;

public class HttpClientBuilder {

	private Long socketTimeoutMs;
	private VictoriaSinkConfiguration.RetryConfiguration retryConfiguration;
	private SinkMetricsCallback metricsCallback;

	public HttpClientBuilder socketTimeout(long socketTimeoutMs) {
		this.socketTimeoutMs = socketTimeoutMs;
		return this;
	}

	public HttpClientBuilder retryConfiguration(VictoriaSinkConfiguration.RetryConfiguration retryConfiguration) {
		this.retryConfiguration = retryConfiguration;
		return this;
	}

	public HttpClientBuilder metricsCallback(SinkMetricsCallback metricsCallback) {
		this.metricsCallback = metricsCallback;
		return this;
	}

	public CloseableHttpAsyncClient buildAndStartClient() {
		Preconditions.checkNotNull(socketTimeoutMs, "Socket timeout must not be null");
		Preconditions.checkArgument(socketTimeoutMs >= 0, "Socket timeout must be >= 0");
		Preconditions.checkNotNull(retryConfiguration, "Retry configuration must not be null");
		Preconditions.checkNotNull(metricsCallback, "MetricsCallback must not be null");

		var actualRetryConfiguration = Optional.ofNullable(retryConfiguration)
			.orElse(VictoriaSinkConfiguration.RetryConfiguration.getDefault());

		final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
			// Remote-Writes must be single-threaded to prevent out-of-order writes
			.setIoThreadCount(1)
			.setSoTimeout(Timeout.ofMilliseconds(socketTimeoutMs))
			.build();

		TlsConfig tlsConfig = TlsConfig.custom().setVersionPolicy(HttpVersionPolicy.FORCE_HTTP_1).build();
		PoolingAsyncClientConnectionManager connectionManager = PoolingAsyncClientConnectionManagerBuilder.create()
			.setDefaultTlsConfig(tlsConfig)
			.build();
		CloseableHttpAsyncClient client = HttpAsyncClients.custom()
			.setConnectionManager(connectionManager)
			.setIOReactorConfig(ioReactorConfig)
			.setIOSessionListener(new RethrowingIOSessionListener())
			.setRetryStrategy(
				new RetryStrategy(actualRetryConfiguration, this.metricsCallback))
			.build();

		client.start();
		return client;
	}
}
