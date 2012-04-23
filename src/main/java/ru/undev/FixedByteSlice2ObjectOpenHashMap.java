package ru.undev;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;
import static it.unimi.dsi.fastutil.HashCommon.maxFill;
import it.unimi.dsi.fastutil.Hash;

import java.nio.ByteBuffer;

import util.hash.MurmurHash3;

public class FixedByteSlice2ObjectOpenHashMap<V> implements Hash {

	/** The array of keys. */
	protected transient byte key[];
	/** The array of values. */
	protected transient V value[];
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

	public FixedByteSlice2ObjectOpenHashMap(int sliceLength) {
		this(sliceLength, DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
	}

	public FixedByteSlice2ObjectOpenHashMap(int sliceLength, int expected) {
		this(sliceLength, expected, DEFAULT_LOAD_FACTOR);
	}

	public FixedByteSlice2ObjectOpenHashMap(int sliceLength, float f) {
		this(sliceLength, DEFAULT_INITIAL_SIZE, f);
	}

	@SuppressWarnings("unchecked")
	public FixedByteSlice2ObjectOpenHashMap(int sliceLength, int expected, float f) {
		if (f <= 0 || f > 1) {
			throw new IllegalArgumentException(
					"Load factor must be greater than 0 and smaller than or equal to 1");
		}
		if (expected < 0) {
			throw new IllegalArgumentException("The expected number of elements must be nonnegative");
		}

		this.sliceLength = sliceLength;
		this.f = f;
		n = arraySize(expected, f);
		mask = n - 1;
		maxFill = maxFill(n, f);
		key = new byte[n * sliceLength];
		value = (V[]) new Object[n];
		used = new boolean[n];
	}

	public void put(byte[] key, V val) {
		put(key, 0, val);
	}

	public void put(ByteBuffer src, int keyOffset, V val) {
		put(src.array(), keyOffset, val);
	}

	public void put(byte[] keySrc, int keyOffset, V val) {
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
		value[pos] = val;

		if (++size >= maxFill) rehash(arraySize(size + 1, f));
	}

	@SuppressWarnings("unchecked")
	protected void rehash(int newN) {
		int i = 0, pos;
		final boolean[] used = this.used;
		final byte[] key = this.key;
		final V[] value = this.value;
		final int newMask = newN - 1;
		final byte[] newKey = new byte[newN * sliceLength];
		final V[] newValue = (V[]) new Object[newN];
		final boolean[] newUsed = new boolean[newN];
		for (int j = size; j-- != 0;) {
			while (!used[i])
				i++;
			pos = MurmurHash3.murmurhash3_x86_32(key, i * sliceLength, sliceLength) & newMask;
			while (newUsed[pos])
				pos = (pos + 1) & newMask;
			newUsed[pos] = true;
			copyFromKey(i, newKey, pos);
			newValue[pos] = value[i];
			i++;
		}
		n = newN;
		mask = newMask;
		maxFill = maxFill(n, f);
		this.key = newKey;
		this.value = newValue;
		this.used = newUsed;
	}

	private void copyFromKey(int srcPos, byte[] dest, int destPos) {
		System.arraycopy(key, srcPos * sliceLength, dest, destPos * sliceLength, sliceLength);
	}

	private void copyToKey(byte[] keySrc, int keyOffset, int pos) {
		System.arraycopy(keySrc, keyOffset, key, pos * sliceLength, sliceLength);
	}

	private boolean isEqualToKey(byte[] keySrc, int keyOffset, int pos) {
		for (int i = pos * sliceLength, j = keyOffset; j < sliceLength; i++, j++) {
			if (key[i] != keySrc[j]) return false;
		}
		return true;
	}

	public V get(byte[] keySrc) {
		return get(keySrc, 0);
	}

	public V get(ByteBuffer src, int keyOffset) {
		return get(src.array(), keyOffset);
	}

	public V get(byte[] keySrc, int keyOffset) {
		int pos = MurmurHash3.murmurhash3_x86_32(keySrc, keyOffset, sliceLength) & mask;
		while (used[pos]) {
			if (isEqualToKey(keySrc, keyOffset, pos))
				return value[pos];
			pos = (pos + 1) & mask;
		}
		return null;
	}

}
