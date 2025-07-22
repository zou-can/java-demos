package org.example.flink.connector.victoria;

import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.connector.base.sink.writer.ElementConverter;
import org.example.flink.connector.victoria.utils.Size;

public class VTMElementConverter implements ElementConverter<Metric, VTMRequestEntry> {

	@Override
	public VTMRequestEntry apply(Metric element, SinkWriter.Context context) {
		long sizeInBytes = computeSize(element);
		return new VTMRequestEntry(element, sizeInBytes);
	}

	static long computeSize(Metric element) {
		return element.getMetric().length()
			+ Long.BYTES
			+ Size.computeMapSizeInByte(element.getTags());
	}
}
