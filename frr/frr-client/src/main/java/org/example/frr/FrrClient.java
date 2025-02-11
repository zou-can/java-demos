package org.example.frr;

import org.example.frr.grpc.FrrNorthbound;
import org.example.frr.grpc.NorthboundGrpc;
import org.example.frr.grpc.NorthboundGrpc.NorthboundBlockingStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class FrrClient {

	private final NorthboundBlockingStub blockingStub;

	public FrrClient(String host, int port) {
		ManagedChannel channel = ManagedChannelBuilder.
			forAddress(host, 50051)
			.usePlaintext()
			.build();
		this.blockingStub = NorthboundGrpc.newBlockingStub(channel);
	}

	public String getConfiguration() {
		FrrNorthbound.GetRequest request = FrrNorthbound.GetRequest
			.newBuilder()
			.addPath("/frr-interface:lib")
			.setType(FrrNorthbound.GetRequest.DataType.ALL)
			.setEncoding(FrrNorthbound.Encoding.JSON)
			.build();

		var iterator = blockingStub.get(request);

		if (iterator.hasNext()) {
			FrrNorthbound.GetResponse response = iterator.next();
			return response.getData().getData();
		}

		throw new RuntimeException("No response received");
	}

}
