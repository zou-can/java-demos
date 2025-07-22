package org.example.flink.connector.victoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

/// { "stream": { "foo": "bar2" }, "values": [ ["1570818238000000000", "fizzbuzz",  {"trace_id": "0242ac120002", "user_id": "superUser123"}] ] }
public record LokiStream(Map<String, String> stream, List<List<Object>> values) {

	public LokiStream {
		if (stream == null) {
			stream = new HashMap<>();
		}
		if (values == null) {
			values = new ArrayList<>();
		}
	}

	public static LokiStream from(VTLog vtLog) {
		Map<String, String> otherFields = vtLog.otherFields();

		List<Object> value = new ArrayList<>(3);
		value.add(Long.toString(vtLog.ts()));
		value.add(vtLog.msg());

		if (MapUtils.isNotEmpty(otherFields)) {
			value.add(otherFields);
		}

		// Note, if you need to modify values, please use a mutable list instead.
		return new LokiStream(vtLog.streamFields(), Collections.singletonList(value));
	}
}
