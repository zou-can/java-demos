package org.example.flink.connector.victoria.utils;

import java.util.Map;

import org.apache.commons.collections.MapUtils;

public final class Size {

	private Size() {
	}

	public static long computeMapSizeInByte(Map<String, String> map) {
		if (MapUtils.isEmpty(map)) {
			return 0;
		}

		long sizeInBytes = 0;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			sizeInBytes += entry.getKey().length() + entry.getValue().length();
		}

		return sizeInBytes;
	}
}
