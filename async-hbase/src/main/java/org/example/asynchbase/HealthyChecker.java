package org.example.asynchbase;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.hbase.async.HBaseClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HealthyChecker implements AutoCloseable {

	private final HealthyCheckerConfig config;

	private final HBaseClient hbaseClient;

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private final AtomicLong unhealthyCount = new AtomicLong(0);

	public HealthyChecker(HealthyCheckerConfig config, HBaseClient hbaseClient) {
		this.config = config;
		this.hbaseClient = hbaseClient;
	}

	public void start() {
		executor.scheduleWithFixedDelay(
			this::check,
			0,
			config.checkInterval().toMillis(),
			TimeUnit.MILLISECONDS);
	}

	void check() {
		try {
			hbaseClient.ensureTableExists(config.targetTableName())
				.join(config.timeoutMillis());
		} catch (IllegalArgumentException e) {
			// Never reach here.
			throw e;
		} catch (InterruptedException e) {
			log.warn("Interrupted while waiting for healthy check.", e);
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			long currentCount = unhealthyCount.incrementAndGet();
			log.error("Checked hbase is unhealthy. Current unhealthy count is {}, max tolerance is {}",
				currentCount, config.maxTolerance(), e);
		}

		// reset unhealthy count
		unhealthyCount.set(0);
	}

	public boolean isHBaseAvailable() {
		return unhealthyCount.get() <= config.maxTolerance();
	}

	public long getUnhealthyCount() {
		return unhealthyCount.get();
	}

	@Override
	public void close() {
		executor.shutdown();
	}
}
