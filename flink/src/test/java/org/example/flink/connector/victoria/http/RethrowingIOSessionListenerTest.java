package org.example.flink.connector.victoria.http;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.example.flink.connector.victoria.VictoriaSinkException;
import org.junit.jupiter.api.Test;

public class RethrowingIOSessionListenerTest {
	@Test
	void exceptionHandlerShouldRethrowPrometheusSinkWriteException() {
		RethrowingIOSessionListener listener = new RethrowingIOSessionListener();
		VictoriaSinkException exception = new VictoriaSinkException("Test exception");

		Exception thrown = assertThrows(Exception.class, () -> listener.exception(null, exception));
		assertEquals(exception, thrown);
	}

	@Test
	void exceptionHandlerShouldNotRethrowOtherExceptions() {
		RethrowingIOSessionListener listener = new RethrowingIOSessionListener();
		Exception otherException = new Exception("Other exception");

		assertDoesNotThrow(() -> listener.exception(null, otherException));
	}
}
