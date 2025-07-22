package org.example.flink.connector.victoria;

import org.apache.flink.connector.base.sink.writer.ResultHandler;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.example.flink.connector.victoria.http.HttpResponseType;
import org.example.flink.connector.victoria.metrics.SinkMetricsCallback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.example.flink.connector.victoria.http.HttpResponseClassifier.*;
import static org.example.flink.connector.victoria.VictoriaSinkConfiguration.OnErrorBehavior.*;

@Slf4j
@RequiredArgsConstructor
public class VictoriaRequestCallback<RequestEntryT> implements FutureCallback<SimpleHttpResponse> {

	private final String url;
	private final int sampleCount;
	private final ResultHandler<RequestEntryT> resultHandler;
	private final SinkMetricsCallback metricsCallback;
	private final VictoriaSinkConfiguration.ErrorHandlingConfiguration errorHandlingConfig;

	@Override
	public void completed(SimpleHttpResponse response) {
		try {
			onCompleted(response);
			resultHandler.complete();
		} catch (Exception e) {
			resultHandler.completeExceptionally(e);
		}
	}

	private void onCompleted(SimpleHttpResponse response) {
		HttpResponseType responseType = classify(response);

		switch (responseType) {
			case SUCCESS:
				// Increment successful writes counts
				metricsCallback.onSuccessfulWriteRequest(sampleCount);
				log.debug(
					"{},{} - successfully posted {} samples to {}",
					response.getCode(),
					response.getReasonPhrase(),
					sampleCount,
					url);
				break;
			case FATAL_ERROR:
				throw new VictoriaSinkException(
					"Fatal error response from " + url,
					response.getCode(),
					response.getReasonPhrase(),
					sampleCount,
					response.getBodyText());

			case NON_RETRYABLE_ERROR: // Response is a non-retryable error.
				// If behavior is FAIL, throw an exception
				if (errorHandlingConfig.onOtherError() == FAIL) {
					throw new VictoriaSinkException(
						"Non-retryable error response from " + url,
						response.getCode(),
						response.getReasonPhrase(),
						sampleCount,
						response.getBodyText());
				}

				// Otherwise, increment discarded data counts & log ERROR
				metricsCallback.onFailedWriteRequestForNonRetryableError(sampleCount);
				log.error(
					"{},{} {} (discarded request to {}, containing {} samples)",
					response.getCode(),
					response.getReasonPhrase(),
					response.getBodyText(),
					url,
					sampleCount);
				break;
			case RETRYABLE_ERROR: // Retry limit exceeded on retryable error
				// If behavior is FAIL, throw an exception
				if (errorHandlingConfig.onMaxRetryExceeded() == FAIL) {
					throw new VictoriaSinkException(
						"Max retry limit exceeded on retryable error from " + url,
						response.getCode(),
						response.getReasonPhrase(),
						sampleCount,
						response.getBodyText());
				}

				// Otherwise, increment discarded data counts & log ERROR
				metricsCallback.onFailedWriteRequestForRetryLimitExceeded(sampleCount);
				log.error(
					"{},{} {} (after retry limit reached, discarded request to {}, containing {} samples)",
					response.getCode(),
					response.getReasonPhrase(),
					response.getBodyText(),
					url,
					sampleCount);
				break;

			default: // Unexpected/unhandled response outcome
				throw new VictoriaSinkException(
					"Unexpected status code returned from " + url,
					response.getCode(),
					response.getReasonPhrase(),
					sampleCount,
					response.getBodyText());
		}
	}

	/**
	 * Exception reported by the http client (e.g. I/O failure). Always throw an exception.
	 *
	 * @param ex exception reported by the http client
	 */
	@Override
	public void failed(Exception ex) {
		resultHandler.completeExceptionally(
			new VictoriaSinkException("Error condition detected by the http response callback (on failed), url: " + url,
				ex));
	}

	/**
	 * The async http client was cancelled. Always throw an exception.
	 */
	@Override
	public void cancelled() {
		// When the async http client is cancelled, the sink should always throw an exception
		resultHandler.completeExceptionally(
			new VictoriaSinkException("The async http client was cancelled, url: " + url));
	}

}
