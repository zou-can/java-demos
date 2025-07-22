package org.example.flink.connector.victoria;

import java.util.ArrayList;
import java.util.List;

import org.example.flink.connector.victoria.utils.Json;

public class VTMRequestEntryHandler implements RequestEntryHandler<VTMRequestEntry> {

	/**
	 * <a href="https://docs.victoriametrics.com/victoriametrics/integrations/opentsdb/">OpenTSDB HTTP API</a>
	 */
	@Override
	public byte[] mergeRequests(List<VTMRequestEntry> requestEntries) {
		List<Metric> metrics = new ArrayList<>();
		for (VTMRequestEntry requestEntry : requestEntries) {
			Metric cur = requestEntry.data();
			metrics.add(cur);
		}

		return Json.writeValueAsBytes(metrics);
	}

	@Override
	public int getSampleCount(List<VTMRequestEntry> requestEntries) {
		return requestEntries.size();
	}

	@Override
	public long getSizeInBytes(VTMRequestEntry requestEntity) {
		return requestEntity.sizeInBytes();
	}

}
