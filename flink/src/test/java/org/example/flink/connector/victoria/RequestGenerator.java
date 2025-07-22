package org.example.flink.connector.victoria;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class RequestGenerator {
	private RequestGenerator() {
	}

	public static VTLog log() {
		return new VTLog(
			System.currentTimeMillis(),
			"0-PPE-4 : default EVENT MONITORUP 118369379 0 : Monitor MonServiceBinding_10.113.115.117:80_(HTTP_80_10.118.228.31_L3DSR)(dev.gidweb01.ncl_80) - State UP : 4575131 ",
			Map.of(
				"msgType", "syslog",
				"deviceId", "6242955"
			),
			Map.of(
				"collectTime", "1752736225756",
				"locationName", "Chuncheon",
				"dataType", "syslog",
				"ip", "10.117.248.27",
				"hostNm", "test1a.w20802c.x670x.krccw",
				"id", "1_f17f63b6-9c17-4381-9c20-cbabef6d7a77"
			)
		);
	}

	public static VTLRequestEntry logEntry() {
		var vtLog = log();

		return new VTLRequestEntry(vtLog, VTLElementConverter.computeSize(vtLog));
	}

	public static List<VTLRequestEntry> logEntries(int count) {
		return Stream.generate(RequestGenerator::logEntry)
			.limit(count)
			.toList();
	}

	public static Metric metric() {
		Metric metric = new Metric();
		metric.setMetric("metric");
		metric.setValue(1.0);
		metric.setTimestamp(System.currentTimeMillis());
		metric.setTags(
			Map.of(
				"k1", "v1",
				"k2", "v2"
			)
		);
		return metric;
	}

	public static VTMRequestEntry metricEntry() {
		Metric metric = metric();
		return new VTMRequestEntry(metric, VTMElementConverter.computeSize(metric));
	}

	public static List<VTMRequestEntry> metricEntries(int count) {
		return Stream.generate(RequestGenerator::metricEntry)
			.limit(count)
			.toList();
	}

}
