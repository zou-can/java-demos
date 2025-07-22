package org.example.flink.connector.victoria;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.type.TypeReference;

public class VTMSink extends VictoriaSinkBase<Metric, VTMRequestEntry> {

	public VTMSink(VictoriaSinkConfiguration sinkConfig) {
		super(sinkConfig, new VTMElementConverter(), new VTMRequestEntryHandler());
	}

	@Override
	public VictoriaSinkWriterStateSerializer<VTMRequestEntry> getWriterStateSerializer() {
		return new VictoriaSinkWriterStateSerializer<>(
			requestEntryHandler, new TypeReference<>() {
		});
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		public static final String DEFAULT_METRIC_GROUP = "VTMSink";

		private String url;
		private String metricGroup = DEFAULT_METRIC_GROUP;

		private VictoriaSinkConfiguration.WriterConfiguration writerConfig =
			VictoriaSinkConfiguration.WriterConfiguration.getDefault();

		private VictoriaSinkConfiguration.ErrorHandlingConfiguration errorHandlingConfig =
			VictoriaSinkConfiguration.ErrorHandlingConfiguration.getDefault();

		private VictoriaSinkConfiguration.RetryConfiguration retryConfig =
			VictoriaSinkConfiguration.RetryConfiguration.getDefault();

		public Builder url(String url) {
			this.url = url;
			return this;
		}

		public Builder metricGroup(String metricGroup) {
			this.metricGroup = metricGroup;
			return this;
		}

		public Builder writerConfig(VictoriaSinkConfiguration.WriterConfiguration writerConfig) {
			this.writerConfig = writerConfig;
			return this;
		}

		public Builder errorHandlingConfig(VictoriaSinkConfiguration.ErrorHandlingConfiguration errorHandlingConfig) {
			this.errorHandlingConfig = errorHandlingConfig;
			return this;
		}

		public Builder retryConfig(VictoriaSinkConfiguration.RetryConfiguration retryConfig) {
			this.retryConfig = retryConfig;
			return this;
		}

		public VTMSink build() {
			var sinkConfig = VictoriaSinkConfiguration.builder()
				.url(url)
				.metricGroup(metricGroup)
				.writerConfig(writerConfig)
				.errorHandlingConfig(errorHandlingConfig)
				.retryConfig(retryConfig)
				.build();

			return new VTMSink(sinkConfig);
		}

	}
}
