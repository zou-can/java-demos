package org.example.flink.connector.victoria.http;

import org.apache.flink.annotation.Internal;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.IOSessionListener;
import org.example.flink.connector.victoria.VictoriaSinkException;

/**
 * Selectively rethrow PrometheusSinkWriteException, causing the httpclient to fail. Otherwise, the
 * exception would be swallowed by the IOReactor.
 */
@Internal
public class RethrowingIOSessionListener implements IOSessionListener {
	@Override
	public void exception(IOSession ioSession, Exception e) {
		if (e instanceof VictoriaSinkException) {
			// Rethrow the exception
			throw (VictoriaSinkException) e;
		}
	}

	@Override
	public void connected(IOSession ioSession) {
		// Nothing to do
	}

	@Override
	public void startTls(IOSession ioSession) {
		// Nothing to do
	}

	@Override
	public void inputReady(IOSession ioSession) {
		// Nothing to do
	}

	@Override
	public void outputReady(IOSession ioSession) {
		// Nothing to do
	}

	@Override
	public void timeout(IOSession ioSession) {
		// Nothing to do
	}

	@Override
	public void disconnected(IOSession ioSession) {
		// Nothing to do
	}
}
