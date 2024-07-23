package org.example.asynchbase;

import java.time.Duration;

public record HealthyCheckerConfig(
	Duration checkInterval,
	int timeoutMillis,
	int maxTolerance,
	String targetTableName) {
}
