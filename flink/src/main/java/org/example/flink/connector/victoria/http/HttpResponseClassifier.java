package org.example.flink.connector.victoria.http;

import org.apache.hc.core5.http.HttpResponse;

import static org.example.flink.connector.victoria.http.HttpResponseType.*;

public class HttpResponseClassifier {
	public static HttpResponseType classify(HttpResponse response) {
		int statusCode = response.getCode();
		if (statusCode >= 200 && statusCode < 300) {
			// 2xx: success
			return SUCCESS;
		} else if (statusCode == 429) {
			// 429, Too Many Requests: throttling
			return RETRYABLE_ERROR;
		} else if (statusCode == 403 || statusCode == 404) {
			// 403, Forbidden: authentication error
			// 404, Not Found: wrong endpoint URL path
			return FATAL_ERROR;
		} else if (statusCode >= 400 && statusCode < 500) {
			// 4xx (except 403, 404, 429): wrong request/bad data
			return NON_RETRYABLE_ERROR;
		} else if (statusCode >= 500) {
			// 5xx: internal errors, recoverable
			return RETRYABLE_ERROR;
		} else {
			// Other status code are unhandled
			return UNHANDLED;
		}
	}
}
