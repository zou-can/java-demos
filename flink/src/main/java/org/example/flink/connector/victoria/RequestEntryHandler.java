package org.example.flink.connector.victoria;

import java.io.Serializable;
import java.util.List;

public interface RequestEntryHandler<RequestEntryT extends Serializable> extends Serializable {

	byte[] mergeRequests(List<RequestEntryT> requestEntries);

	int getSampleCount(List<RequestEntryT> requestEntries);

	long getSizeInBytes(RequestEntryT requestEntity);
}
