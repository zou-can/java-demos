package org.example.flink.connector.victoria;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.flink.api.connector.sink2.SinkWriter;
import org.junit.Test;

public class VTLElementConverterTest {

	private final SinkWriter.Context dummyContext = new SinkWriter.Context() {
		@Override
		public long currentWatermark() {
			return 0L;
		}

		@Override
		public Long timestamp() {
			return null;
		}
	};

	@Test
	public void test() {
		VTLElementConverter converter = new VTLElementConverter();
		VTLog input = RequestGenerator.log();

		VTLRequestEntry res = converter.apply(input, dummyContext);
		VTLog log = res.log();
		assertEquals(input, log);
	}
}
