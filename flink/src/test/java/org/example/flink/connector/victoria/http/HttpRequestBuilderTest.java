package org.example.flink.connector.victoria.http;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.core5.http.HttpHeaders;
import org.junit.Test;

public class HttpRequestBuilderTest {

	private static final String ENDPOINT = "/dummy";
	private static final byte[] REQUEST_BODY = {(byte)0x01};

	@Test
	public void testBuildRequest() throws Exception {
		var builder = new HttpRequestBuilder(ENDPOINT);

		SimpleHttpRequest request = builder.buildHttpRequest(REQUEST_BODY);

		// Content-Type
		assertEquals(
			HttpRequestBuilder.CONTENT_TYPE.getMimeType(),
			request.getHeader(HttpHeaders.CONTENT_TYPE).getValue()
		);

		// Content-Encoding
		assertEquals(
			HttpRequestBuilder.CONTENT_ENCODING,
			request.getHeader(HttpHeaders.CONTENT_ENCODING).getValue()
		);

		// Body
		assertEquals(REQUEST_BODY, request.getBody().getBodyBytes());
	}
}
