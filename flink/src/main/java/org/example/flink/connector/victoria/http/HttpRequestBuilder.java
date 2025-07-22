package org.example.flink.connector.victoria.http;

import java.util.HashMap;
import java.util.Map;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;

public class HttpRequestBuilder {
	static final ContentType CONTENT_TYPE = ContentType.APPLICATION_JSON;

	static final String CONTENT_ENCODING = "gzip";

	private final String url;

	private final Map<String, String> fixedHeaders;

	public HttpRequestBuilder(String url) {
		this.url = url;
		this.fixedHeaders = new HashMap<>();
		fixedHeaders.put(HttpHeaders.CONTENT_ENCODING, CONTENT_ENCODING);
		fixedHeaders.put(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE.getMimeType());
	}

	public SimpleHttpRequest buildHttpRequest(byte[] httpRequestBody) {
		Map<String, String> headers = new HashMap<>(fixedHeaders);

		SimpleRequestBuilder builder =
			SimpleRequestBuilder.post()
				.setUri(url)
				.setBody(httpRequestBody, CONTENT_TYPE);

		for (Map.Entry<String, String> header : headers.entrySet()) {
			builder.addHeader(header.getKey(), header.getValue());
		}

		return builder.build();
	}
}
