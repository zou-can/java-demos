package org.example.flink.connector.victoria;

import java.io.Serializable;

public record VTMRequestEntry(Metric data, long sizeInBytes) implements Serializable {
}
