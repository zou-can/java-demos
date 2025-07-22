package org.example.flink.connector.victoria;

import java.util.List;

public record LokiStreams(List<LokiStream> streams) {
}
