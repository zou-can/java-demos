package org.example.algorithms.collections;

public class BitMap {
	private final byte[] bits;

	public BitMap(int size) {
		this.bits = new byte[(size + 7) / 8];
	}

	/**
	 * x | 1
	 */
	public void set(int pos) {
		int index = pos / 8;
		int offset = pos % 8;
		bits[index] |= (byte)(1 << offset);
	}

	/**
	 * x & 0
	 */
	public void clear(int pos) {
		int index = pos / 8;
		int offset = pos % 8;
		bits[index] &= (byte)~(1 << offset);
	}

	/**
	 * x & 1
	 */
	public boolean get(int pos) {
		int index = pos / 8;
		int offset = pos % 8;
		return (bits[index] & (1 << offset)) != 0;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < bits.length; i++) {
			String binaryString = String.format("%8s", Integer.toBinaryString(bits[i] & 0xFF)).replace(' ', '0');
			builder.append(binaryString);

			if (i < bits.length - 1) {
				builder.append(" ");
			}
		}

		return builder.toString();
	}
}
