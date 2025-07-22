package org.example.flink.connector.victoria;

import org.apache.flink.util.FlinkRuntimeException;

public class VictoriaSinkException extends FlinkRuntimeException {

	public VictoriaSinkException(String reason) {
		super("Reason: " + reason);
	}

	public VictoriaSinkException(String reason, Exception cause) {
		super("Reason: " + reason, cause);
	}

	public VictoriaSinkException(
		String reason,
		int httpStatusCode,
		String httpReasonPhrase,
		long sampleCount,
		String httpResponseBody) {
		super(
			String.format(
				"Reason: %s. Http response: %d,%s (%s) .The offending write-request contains %d samples",
				reason,
				httpStatusCode,
				httpReasonPhrase,
				httpResponseBody,
				sampleCount));
	}
}
