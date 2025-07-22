package org.example.flink.connector.victoria.http;

public enum HttpResponseType {
	/** The Write-Request was successfully accepted. */
	SUCCESS,
	/** Write-Request temporarily rejected. The request can be retried. */
	RETRYABLE_ERROR,
	/**
	 * Write-Request permanently rejected. It cannot be retried. The error condition is recoverable,
	 * after discarding the offending request.
	 */
	NON_RETRYABLE_ERROR,
	/** Unrecoverable error condition. */
	FATAL_ERROR,
	/** Unhandled status code. */
	UNHANDLED;
}

