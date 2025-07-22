package org.example.flink.connector.victoria;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.apache.flink.api.connector.sink2.SinkWriter;
import org.junit.Test;

public class VTMElementConverterTest {

	private final SinkWriter.Context dummyContext =
		new SinkWriter.Context() {
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
		VTMElementConverter converter = new VTMElementConverter();
		Metric input = new Metric();
		input.setMetric("metric");
		input.setValue(1.0);
		input.setTimestamp(System.currentTimeMillis());
		input.setTags(
			Map.of(
				"k1", "v1",
				"k2", "v2"
			)
		);

		VTMRequestEntry res = converter.apply(input, dummyContext);
		assertEquals(input, res.data());
	}
}
