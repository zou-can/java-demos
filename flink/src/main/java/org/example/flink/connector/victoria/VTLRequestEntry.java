package org.example.flink.connector.victoria;

import java.io.Serializable;

public record VTLRequestEntry(VTLog log, long sizeInBytes) implements Serializable {
}
