package org.example.flink.connector.victoria.utils;

import java.io.IOException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.example.flink.connector.victoria.VictoriaSinkException;

public final class Json {
	private Json() {
	}

	private static final ObjectMapper objectMapper;

	static {
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static <T> T deserialize(byte[] data, Class<T> clazz) throws IOException {
		try {
			return objectMapper.readValue(data, clazz);
		} catch (JsonProcessingException e) {
			throw new VictoriaSinkException("Deserialize failed, data: " + new String(data), e);
		}
	}

	public static <T> T deserialize(byte[] data, TypeReference<T> typeReference) {
		try {
			return objectMapper.readValue(data, typeReference);
		} catch (IOException e) {
			throw new VictoriaSinkException("Deserialize failed, data: " + new String(data), e);
		}
	}

	public static <T> T deserialize(String json, Class<T> clazz) {
		try {
			return objectMapper.readValue(json, clazz);
		} catch (JsonProcessingException e) {
			throw new VictoriaSinkException("Deserialize failed, data: " + json, e);
		}
	}

	public static String objToJson(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new VictoriaSinkException("Serialize to string failed, obj: " + obj, e);
		}
	}

	public static byte[] writeValueAsBytes(Object obj) {
		try {
			return objectMapper.writeValueAsBytes(obj);
		} catch (JsonProcessingException e) {
			throw new VictoriaSinkException("Serialize to bytes failed, obj: " + obj, e);
		}
	}

}
