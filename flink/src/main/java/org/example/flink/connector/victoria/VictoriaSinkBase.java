package org.example.flink.connector.victoria;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.connector.sink2.StatefulSinkWriter;
import org.apache.flink.api.connector.sink2.SupportsWriterState;
import org.apache.flink.api.connector.sink2.WriterInitContext;
import org.apache.flink.connector.base.sink.writer.BufferedRequestState;
import org.apache.flink.connector.base.sink.writer.ElementConverter;
import org.apache.flink.metrics.MetricGroup;
import org.example.flink.connector.victoria.metrics.SinkMetrics;
import org.example.flink.connector.victoria.metrics.SinkMetricsCallback;


public abstract class VictoriaSinkBase<InputT, RequestEntryT extends Serializable>
	implements Sink<InputT>, SupportsWriterState<InputT, BufferedRequestState<RequestEntryT>> {

	private final VictoriaSinkConfiguration sinkConfig;

	final ElementConverter<InputT, RequestEntryT> elementConverter;

	final RequestEntryHandler<RequestEntryT> requestEntryHandler;

	public VictoriaSinkBase(
		VictoriaSinkConfiguration sinkConfig,
		ElementConverter<InputT, RequestEntryT> elementConverter,
		RequestEntryHandler<RequestEntryT> requestEntryHandler) {
		this.sinkConfig = sinkConfig;
		this.elementConverter = elementConverter;
		this.requestEntryHandler = requestEntryHandler;
	}

	@Override
	public VictoriaSinkWriter<InputT, RequestEntryT> createWriter(InitContext context) {
		throw new UnsupportedOperationException(
			"Deprecated, please use restoreWriter(WriterInitContext, Collection<WriterStateT>)");
	}

	@Override
	public SinkWriter<InputT> createWriter(WriterInitContext context) {
		SinkMetricsCallback metricsCallback = registerSinkMetrics(context);

		return new VictoriaSinkWriter<>(
			sinkConfig,
			elementConverter,
			requestEntryHandler,
			metricsCallback,
			context,
			Collections.emptyList());
	}

	@Override
	public StatefulSinkWriter<InputT, BufferedRequestState<RequestEntryT>> restoreWriter(
		WriterInitContext context,
		Collection<BufferedRequestState<RequestEntryT>> recoveredState) {
		SinkMetricsCallback metricsCallback = registerSinkMetrics(context);

		return new VictoriaSinkWriter<>(
			sinkConfig,
			elementConverter,
			requestEntryHandler,
			metricsCallback,
			context,
			recoveredState);
	}

	private SinkMetricsCallback registerSinkMetrics(WriterInitContext context) {
		MetricGroup metricGroup = context
			.metricGroup()
			.addGroup(sinkConfig.metricGroup());

		SinkMetrics sinkMetrics = SinkMetrics.registerSinkMetrics(metricGroup);
		return new SinkMetricsCallback(sinkMetrics);
	}


}
