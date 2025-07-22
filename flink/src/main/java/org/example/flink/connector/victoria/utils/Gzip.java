package org.example.flink.connector.victoria.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public final class Gzip {
	private Gzip() {
	}

	public static byte[] compress(byte[] data) throws IOException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
			 GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
			gzip.write(data);
			gzip.close();
			return bos.toByteArray();
		}
	}
}
