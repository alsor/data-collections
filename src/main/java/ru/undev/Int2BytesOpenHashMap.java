package ru.undev;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;
import static it.unimi.dsi.fastutil.HashCommon.maxFill;
import it.unimi.dsi.fastutil.Hash;

import java.nio.ByteBuffer;

import util.hash.MurmurHash3;

public class Int2BytesOpenHashMap implements Hash {

	/** The array of keys. */
	public transient byte array[];
	/** The array telling whether a position is used. */
	protected transient boolean used[];
	/** The acceptable load factor. */
	protected final float f;
	/** The current table size. */
	protected transient int n;
	/** Threshold after which we rehash. It must be the table size times {@link #f}. */
	protected transient int maxFill;
	/** The mask for wrapping a position counter. */
	protected transient int mask;
	/** Number of entries in the set. */
	protected int size;
	/** The length of a byte slices that can be stored in this collection */
	protected final int sliceLength;
	protected final int keyLength;
	protected final int valueLength;

	public Int2BytesOpenHashMap(int keyLength, int valueLength) {
		this(keyLength, valueLength, DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
	}

	public Int2BytesOpenHashMap(int keyLength, int valueLength, int expected) {
		this(keyLength, valueLength, expected, DEFAULT_LOAD_FACTOR);
	}

	public Int2BytesOpenHashMap(int keyLength, int valueLength, float f) {
		this(keyLength, valueLength, DEFAULT_INITIAL_SIZE, f);
	}

	public Int2BytesOpenHashMap(int keyLength, int valueLength, int expected, float f) {
		if (f <= 0 || f > 1) {
			throw new IllegalArgumentException(
					"Load factor must be greater than 0 and smaller than or equal to 1");
		}
		if (expected < 0) {
			throw new IllegalArgumentException("The expected number of elements must be nonnegative");
		}

		this.keyLength = keyLength;
		this.valueLength = valueLength;
		this.sliceLength = keyLength + valueLength;
		this.f = f;
		n = arraySize(expected, f);
		mask = n - 1;
		maxFill = maxFill(n, f);
		array = new byte[n * sliceLength];
		used = new boolean[n];
	}

	public int putKey(byte[] key) {
		return putKey(key, 0);
	}

	public int putKey(ByteBuffer src, int keyOffset) {
		return putKey(src.array(), keyOffset);
	}

	public int putKey(byte[] keySrc, int keyOffset) {
		if (size + 1 >= maxFill)
			rehash(arraySize(size, f));

		int pos = MurmurHash3.murmurhash3_x86_32(keySrc, keyOffset, keyLength) & mask;
		
		while(used[pos]) {
			if (isEqualToKey(keySrc, keyOffset, pos)) {
				return pos;
			}
			pos = (pos + 1) & mask;
		}
		used[pos] = true;
		copyToKey(keySrc, keyOffset, pos);
		size++;
		return pos;
	}

	protected void rehash(int newN) {
		int i = 0, pos;
		final boolean[] used = this.used;
		final byte[] array = this.array;
		final int newMask = newN - 1;
		final byte[] newArray = new byte[newN * sliceLength];
		final boolean[] newUsed = new boolean[newN];
		for (int j = size; j-- != 0;) {
			while (!used[i])
				i++;
			pos = MurmurHash3.murmurhash3_x86_32(array, i * sliceLength, keyLength) & newMask;
			while (newUsed[pos])
				pos = (pos + 1) & newMask;
			newUsed[pos] = true;
			copyFromArray(i, newArray, pos);
			i++;
		}
		n = newN;
		mask = newMask;
		maxFill = maxFill(n, f);
		this.array = newArray;
		this.used = newUsed;
	}

	private void copyFromArray(int srcPos, byte[] dest, int destPos) {
		System.arraycopy(array, srcPos * sliceLength, dest, destPos * sliceLength, sliceLength);
	}

	private void copyToKey(byte[] keySrc, int keyOffset, int pos) {
		System.arraycopy(keySrc, keyOffset, array, pos * sliceLength, keyLength);
	}

	private boolean isEqualToKey(byte[] keySrc, int keyOffset, int pos) {
		for (int i = pos * sliceLength, j = keyOffset; j < keyLength; i++, j++) {
			if (array[i] != keySrc[j])
				return false;
		}
		return true;
	}

	public int getPos(byte[] keySrc) {
		return getPos(keySrc, 0);
	}

	public int getPos(ByteBuffer src, int keyOffset) {
		return getPos(src.array(), keyOffset);
	}

	private int getPos(byte[] keySrc, int keyOffset) {
		int pos = MurmurHash3.murmurhash3_x86_32(keySrc, keyOffset, keyLength) & mask;
		while (used[pos]) {
			if (isEqualToKey(keySrc, keyOffset, pos)) return pos;
			pos = (pos + 1) & mask;
		}
		return -1;
	}

	public int absoluteOffset(int entry, int entryOffset) {
		return entry * sliceLength + keyLength + entryOffset;
	}

	public void putBytes(int entry, int entryOffset, byte[] valSrc, int valOffset, int valLength) {
		System.arraycopy(valSrc, valOffset, array, absoluteOffset(entry, entryOffset),
				valLength);
	}

	public void putInt(int entry, int entryOffset, int val) {
		byte b0 = (byte) (val >> 0);
		byte b1 = (byte) (val >> 8);
		byte b2 = (byte) (val >> 16);
		byte b3 = (byte) (val >> 24);

		int offset = absoluteOffset(entry, entryOffset);
		array[offset + 0] = b0;
		array[offset + 1] = b1;
		array[offset + 2] = b2;
		array[offset + 3] = b3;
	}

	public void putByte(int entry, int entryOffset, byte val) {
		array[absoluteOffset(entry, entryOffset)] = val;
	}

	public int getInt(int entry, int entryOffset) {
		int offset = absoluteOffset(entry, entryOffset);
		byte b0 = array[offset + 0];
		byte b1 = array[offset + 1];
		byte b2 = array[offset + 2];
		byte b3 = array[offset + 3];

		return (int) ((((b3 & 0xff) << 24) | ((b2 & 0xff) << 16) | ((b1 & 0xff) << 8) | ((b0 & 0xff) << 0)));
	}

}
