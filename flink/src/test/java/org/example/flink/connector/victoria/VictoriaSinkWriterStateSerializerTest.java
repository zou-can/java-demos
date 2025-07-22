package org.example.flink.connector.victoria;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.flink.api.common.serialization.SerializerConfigImpl;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.common.typeutils.base.ListSerializer;
import org.apache.flink.api.common.typeutils.base.MapSerializer;
import org.apache.flink.api.common.typeutils.base.StringSerializer;
import org.apache.flink.api.java.tuple.Tuple10;
import org.apache.flink.api.java.typeutils.runtime.TupleSerializer;
import org.apache.flink.connector.base.sink.writer.BufferedRequestState;
import org.apache.flink.connector.base.sink.writer.RequestEntryWrapper;
import org.apache.flink.core.memory.DataOutputViewStreamWrapper;
import org.example.flink.connector.victoria.utils.Json;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class VictoriaSinkWriterStateSerializerTest {

	@Test
	public void testVTL() {
		var serializer = VTLSink.builder()
			.url("http://localhost:8080")
			.build()
			.getWriterStateSerializer();

		var requestEntries = RequestGenerator.logEntries(100);

		var requestEntryWrappers = requestEntries
			.stream()
			.map(entry -> new RequestEntryWrapper<>(entry, entry.sizeInBytes()))
			.toList();

		var states = new BufferedRequestState<>(requestEntryWrappers);

		byte[] serialized = serializer.serialize(states);

		var deserialized = serializer.deserialize(1, serialized)
			.getBufferedRequestEntries()
			.stream()
			.map(RequestEntryWrapper::getRequestEntry)
			.toList();

		assertEquals(requestEntries, deserialized);
	}

	@Test
	public void testVTM() {
		var serializer = VTMSink.builder()
			.url("http://localhost:8080")
			.build()
			.getWriterStateSerializer();

		var requestEntries = RequestGenerator.metricEntries(100);

		var requestEntryWrappers = requestEntries
			.stream()
			.map(entry -> new RequestEntryWrapper<>(entry, entry.sizeInBytes()))
			.toList();

		var states = new BufferedRequestState<>(requestEntryWrappers);

		byte[] serialized = serializer.serialize(states);

		var deserialized = serializer.deserialize(1, serialized)
			.getBufferedRequestEntries()
			.stream()
			.map(RequestEntryWrapper::getRequestEntry)
			.toList();

		assertEquals(requestEntries, deserialized);
	}

	@Disabled
	@Test
	public void compareSerializationMethods() throws IOException {
		List<VTLRequestEntry> requestEntries = RequestGenerator.logEntries(100);

		// Json
		int json = Json.writeValueAsBytes(requestEntries).length;

		// Flink
		TypeSerializer<VTLRequestEntry> pojoSerializer = TypeInformation.of(
			new TypeHint<VTLRequestEntry>() {
			})
			.createSerializer(new SerializerConfigImpl());
		ListSerializer<VTLRequestEntry> listSerializer = new ListSerializer<>(pojoSerializer);

		int flink;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); DataOutputViewStreamWrapper out = new DataOutputViewStreamWrapper(
			baos)) {
			listSerializer.serialize(requestEntries, out);
			flink = baos.toByteArray().length;
		}

		log.info("Json serialization size: {}", json); // 47501
		log.info("Flink serialization size: {}", flink); // 47404
	}

	@Disabled
	@Test
	public void compareDifferentSerializer() throws IOException {
		long sampleCount = 10000000;
		var maps = Stream.generate(this::createMap)
			.limit(sampleCount)
			.toList();

		var tuples = Stream.generate(this::createTuple)
			.limit(sampleCount)
			.toList();

		var pojos = Stream.generate(this::createPojo)
			.limit(sampleCount)
			.toList();

		MapSerializer<String, String> mapSerializer = new MapSerializer<>(new StringSerializer(),
			new StringSerializer());

		var tupleSerializer = TypeInformation.of(
			new TypeHint<Tuple10<String, String, String, String, String, String, String, String, String, String>>() {
			})
			.createSerializer(new SerializerConfigImpl());

		assertTrue(
			tupleSerializer instanceof TupleSerializer<Tuple10<String, String, String, String, String, String, String, String, String, String>>);

		var pojoSerializer = TypeInformation.of(MyPOJO.class)
			.createSerializer(new SerializerConfigImpl());

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); DataOutputViewStreamWrapper out = new DataOutputViewStreamWrapper(
			baos)) {
			long start = System.currentTimeMillis();
			for (Map<String, String> map : maps) {
				mapSerializer.serialize(map, out);
			}
			long time = System.currentTimeMillis() - start;
			log.info("MapSerializer time cost {}, size: {}", time, out.size());
		}

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); DataOutputViewStreamWrapper out = new DataOutputViewStreamWrapper(
			baos)) {
			long start = System.currentTimeMillis();
			for (var tuple : tuples) {
				tupleSerializer.serialize(tuple, out);
			}
			long time = System.currentTimeMillis() - start;
			log.info("TupleSerializer time cost {}, size: {}", time, out.size());
		}

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); DataOutputViewStreamWrapper out = new DataOutputViewStreamWrapper(
			baos)) {
			long start = System.currentTimeMillis();
			for (var pojo : pojos) {
				pojoSerializer.serialize(pojo, out);
			}
			long time = System.currentTimeMillis() - start;
			log.info("PojoSerializer time cost {}, size: {}", time, out.size());
		}
	}

	public Map<String, String> createMap() {
		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		map.put("key3", "value3");
		map.put("key4", "value4");
		map.put("key5", "value5");
		return map;
	}

	public Tuple10<String, String, String, String, String, String, String, String, String, String> createTuple() {
		return Tuple10.of(
			"key1", "value1",
			"key2", "value2",
			"key3", "value3",
			"key4", "value4",
			"key5", "value5"
		);
	}

	public MyPOJO createPojo() {
		return new MyPOJO("key1", "key2", "key3", "key4", "key5",
			"value1", "value2", "value3", "value4", "value5");
	}

	public record MyPOJO(   String k1, String k2, String k3, String k4, String k5,
							String v1, String v2, String v3, String v4, String v5) {
	}

}
