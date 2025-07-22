package org.example.flink.connector.victoria;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;


@Data
public class Metric implements Serializable {
	private String metric;
	private Long timestamp;
	private Double value;
	private Map<String, String> tags;

	public Metric addTag(String key, String value) {
		if (tags == null) {
			tags = new HashMap<>();
		}
		tags.put(key, value);
		return this;
	}
}