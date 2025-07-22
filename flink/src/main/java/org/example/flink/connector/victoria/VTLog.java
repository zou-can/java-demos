package org.example.flink.connector.victoria;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nonnull;

public record VTLog(
	long ts,
	@Nonnull String msg,
	Map<String, String> streamFields,
	Map<String, String> otherFields) implements Serializable {
}
