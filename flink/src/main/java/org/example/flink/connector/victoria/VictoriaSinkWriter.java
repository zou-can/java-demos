package org.example.flink.connector.victoria;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.flink.api.connector.sink2.WriterInitContext;
import org.apache.flink.connector.base.sink.writer.AsyncSinkWriter;
import org.apache.flink.connector.base.sink.writer.BufferedRequestState;
import org.apache.flink.connector.base.sink.writer.ElementConverter;
import org.apache.flink.connector.base.sink.writer.ResultHandler;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.io.CloseMode;
import org.example.flink.connector.victoria.http.HttpClientBuilder;
import org.example.flink.connector.victoria.http.HttpRequestBuilder;
import org.example.flink.connector.victoria.metrics.SinkMetricsCallback;
import org.example.flink.connector.victoria.utils.Gzip;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VictoriaSinkWriter<InputT, RequestEntryT extends Serializable>
	extends AsyncSinkWriter<InputT, RequestEntryT> {
	private final RequestEntryHandler<RequestEntryT> requestEntryHandler;
	private final HttpRequestBuilder requestBuilder;
	private final SinkMetricsCallback metricsCallback;
	private final VictoriaSinkConfiguration.ErrorHandlingConfiguration errorHandlingConfig;

	private final CloseableHttpAsyncClient asyncHttpClient;

	public VictoriaSinkWriter(
		VictoriaSinkConfiguration sinkConfig,
		ElementConverter<InputT, RequestEntryT> elementConverter,
		RequestEntryHandler<RequestEntryT> requestEntryHandler,
		SinkMetricsCallback metricsCallback,
		WriterInitContext context,
		Collection<BufferedRequestState<RequestEntryT>> state) {

		super(elementConverter, context, sinkConfig.writerConfig().toAsyncSinkWriterConfiguration(), state);

		this.requestEntryHandler = requestEntryHandler;
		this.metricsCallback = metricsCallback;
		this.errorHandlingConfig = sinkConfig.errorHandlingConfig();

		this.requestBuilder = new HttpRequestBuilder(sinkConfig.url());

		this.asyncHttpClient = new HttpClientBuilder()
			.retryConfiguration(sinkConfig.retryConfig())
			.socketTimeout(sinkConfig.writerConfig().socketTimeout())
			.metricsCallback(metricsCallback)
			.buildAndStartClient();
	}

	@Override
	protected void submitRequestEntries(List<RequestEntryT> requestEntries,
		ResultHandler<RequestEntryT> resultHandler) {

		int sampleCount = requestEntryHandler.getSampleCount(requestEntries);

		byte[] compressed;
		try {
			byte[] requestBody = requestEntryHandler.mergeRequests(requestEntries);
			compressed = Gzip.compress(requestBody);
		} catch (IOException e) {
			throw new VictoriaSinkException("Encoding the request body failed.", e);
		}

		SimpleHttpRequest postRequest = requestBuilder.buildHttpRequest(compressed);
		String url = postRequest.getRequestUri();

		VictoriaRequestCallback<RequestEntryT> callback = new VictoriaRequestCallback<>(
			url,
			sampleCount,
			resultHandler,
			metricsCallback,
			errorHandlingConfig);

		asyncHttpClient.execute(postRequest, callback);
	}

	@Override
	public void close() {
		if (this.asyncHttpClient != null) {
			this.asyncHttpClient.close(CloseMode.GRACEFUL);
		}
		super.close();

		log.info("VictoriaSinkWriter closed.");
	}

	@Override
	protected long getSizeInBytes(RequestEntryT requestEntry) {
		return requestEntryHandler.getSizeInBytes(requestEntry);
	}
}
