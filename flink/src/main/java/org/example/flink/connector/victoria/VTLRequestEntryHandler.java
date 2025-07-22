package org.example.flink.connector.victoria;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.example.flink.connector.victoria.utils.Json;

public class VTLRequestEntryHandler implements RequestEntryHandler<VTLRequestEntry> {
	@Override
	public byte[] mergeRequests(List<VTLRequestEntry> requestEntries) {
		var streams = requestEntries.stream()
			.map(VTLRequestEntry::log)
			.filter(Objects::nonNull)
			.map(LokiStream::from)
			.toList();
		var lokiStreams = new LokiStreams(streams);
		return Json.writeValueAsBytes(lokiStreams);
	}

	@Override
	public int getSampleCount(List<VTLRequestEntry> requestEntries) {
		return Optional.ofNullable(requestEntries)
			.map(List::size)
			.orElse(0);
	}

	@Override
	public long getSizeInBytes(VTLRequestEntry requestEntity) {
		return requestEntity.sizeInBytes();
	}
}
