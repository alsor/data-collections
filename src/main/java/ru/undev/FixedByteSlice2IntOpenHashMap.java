package ru.undev;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;
import static it.unimi.dsi.fastutil.HashCommon.maxFill;
import it.unimi.dsi.fastutil.Hash;
import util.hash.MurmurHash3;

public class FixedByteSlice2IntOpenHashMap implements Hash {

	/** The array of keys. */
	protected transient byte key[];
	/** The array of values. */
	protected transient int value[];
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

	public FixedByteSlice2IntOpenHashMap(int sliceLength) {
		this(sliceLength, DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
	}

	public FixedByteSlice2IntOpenHashMap(int sliceLength, int expected) {
		this(sliceLength, expected, DEFAULT_LOAD_FACTOR);
	}

	public FixedByteSlice2IntOpenHashMap(int sliceLength, float f) {
		this(sliceLength, DEFAULT_INITIAL_SIZE, f);
	}

	public FixedByteSlice2IntOpenHashMap(int sliceLength, int expected, float f) {
		this.sliceLength = sliceLength;
		this.f = f;
		n = arraySize(expected, f);
		mask = n - 1;
		maxFill = maxFill(n, f);
		key = new byte[n * sliceLength];
		value = new int[n];
		used = new boolean[n];
	}

	public void put(byte[] key, int val) {
		put(key, 0, val);
	}

	public void put(byte[] keySrc, int keyOffset, int val) {
		int pos = MurmurHash3.murmurhash3_x86_32(keySrc, keyOffset, sliceLength) & mask;
		
		while(used[pos]) {
			if (isEqualToKey(keySrc, keyOffset, pos)) {
				value[pos] = val;
				return;
			}
			pos = (pos + 1) & mask;
		}
		used[pos] = true;
		copyToKey(keySrc, keyOffset, pos);
		value[pos] = v;

		if (++size >= maxFill) rehash(arraySize(size + 1, f));
	}

	private boolean isEqualToKey(byte[] keySrc, int keyOffset, int pos) {
		for (int i = pos * sliceLength, j = 0; j < sliceLength; i++, j++) {
			if (key[i] != bs[j]) return false;
		}
		return true;
	}

}
