package org.example.flink.connector.victoria;

import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.connector.base.sink.writer.ElementConverter;
import org.example.flink.connector.victoria.utils.Size;

public class VTLElementConverter implements ElementConverter<VTLog, VTLRequestEntry> {

	@Override
	public VTLRequestEntry apply(VTLog element, SinkWriter.Context context) {
		long sizeInBytes = computeSize(element);
		return new VTLRequestEntry(element, sizeInBytes);
	}

	static long computeSize(VTLog element) {
		return 13
			+ element.msg().length()
			+ Size.computeMapSizeInByte(element.streamFields())
			+ Size.computeMapSizeInByte(element.otherFields());
	}

}
