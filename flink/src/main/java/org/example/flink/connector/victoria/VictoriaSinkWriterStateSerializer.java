package org.example.flink.connector.victoria;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.flink.connector.base.sink.writer.BufferedRequestState;
import org.apache.flink.connector.base.sink.writer.RequestEntryWrapper;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.example.flink.connector.victoria.utils.Json;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VictoriaSinkWriterStateSerializer<
	RequestEntryT extends Serializable>
	implements SimpleVersionedSerializer<BufferedRequestState<RequestEntryT>> {

	private static final int VERSION = 1;

	private final RequestEntryHandler<RequestEntryT> requestEntryHandler;
	private final TypeReference<List<RequestEntryT>> typeRef;

	public VictoriaSinkWriterStateSerializer(RequestEntryHandler<RequestEntryT> requestEntryHandler,
		TypeReference<List<RequestEntryT>> typeRef) {
		this.requestEntryHandler = requestEntryHandler;
		this.typeRef = typeRef;
	}

	@Override
	public int getVersion() {
		return VERSION;
	}

	@Override
	public byte[] serialize(BufferedRequestState<RequestEntryT> obj) {
		List<RequestEntryT> requestEntries = obj.getBufferedRequestEntries().stream()
			.map(RequestEntryWrapper::getRequestEntry)
			.toList();
		return Json.writeValueAsBytes(requestEntries);
	}

	@Override
	public BufferedRequestState<RequestEntryT> deserialize(int version, byte[] serialized) {
		switch (version) {
			case 1:
				return deserializeV1(serialized);
			default:
				log.warn("Ignore state version - {} for VictoriaSink", version);
				return new BufferedRequestState<>(Collections.emptyList());
		}
	}

	private BufferedRequestState<RequestEntryT> deserializeV1(byte[] serialized) {
		List<RequestEntryT> requestEntries = Json.deserialize(serialized, this.typeRef);

		List<RequestEntryWrapper<RequestEntryT>> res = requestEntries
			.stream()
			.map(entry ->
				new RequestEntryWrapper<>(
					entry, requestEntryHandler.getSizeInBytes(entry)
				)
			)
			.toList();

		return new BufferedRequestState<>(res);
	}
}
