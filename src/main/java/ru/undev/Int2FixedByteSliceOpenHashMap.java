package ru.undev;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;
import static it.unimi.dsi.fastutil.HashCommon.maxFill;
import it.unimi.dsi.fastutil.Hash;

import java.nio.ByteBuffer;

public class Int2FixedByteSliceOpenHashMap implements Hash {

	/** The array of keys. */
	protected transient int key[];
	/** The array of values. */
	protected transient byte value[];
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

	public Int2FixedByteSliceOpenHashMap(int sliceLength) {
		this(sliceLength, DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
	}

	public Int2FixedByteSliceOpenHashMap(int sliceLength, int expected) {
		this(sliceLength, expected, DEFAULT_LOAD_FACTOR);
	}

	public Int2FixedByteSliceOpenHashMap(int sliceLength, float f) {
		this(sliceLength, DEFAULT_INITIAL_SIZE, f);
	}

	public Int2FixedByteSliceOpenHashMap(int sliceLength, int expected, float f) {
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
		key = new int[n];
		value = new byte[n * sliceLength];
		used = new boolean[n];
	}

	public void put(int k, byte[] val) {
		put(k, val, 0);
	}

	public void put(int k, ByteBuffer val, int valOffset) {
		put(k, val.array(), valOffset);
	}

	public void put(int k, byte[] valSrc, int valOffset) {
		int pos = it.unimi.dsi.fastutil.HashCommon.murmurHash3(k) & mask;

		while(used[pos]) {
			if (key[pos] == k) {
				copyToValue(valSrc, valOffset, pos);
				return;
			}
			pos = (pos + 1) & mask;
		}
		used[pos] = true;
		key[pos] = k;
		copyToValue(valSrc, valOffset, pos);

		if (++size >= maxFill) rehash(arraySize(size + 1, f));
	}

	protected void rehash(int newN) {
		int i = 0, pos;
		final boolean[] used = this.used;
		int k;
		final int[] key = this.key;
		final byte[] value = this.value;
		final int newMask = newN - 1;
		final int[] newKey = new int[newN];
		final byte[] newValue = new byte[newN * sliceLength];
		final boolean[] newUsed = new boolean[newN];
		for (int j = size; j-- != 0;) {
			while (!used[i])
				i++;
			k = key[i];
			pos = it.unimi.dsi.fastutil.HashCommon.murmurHash3(k) & newMask;
			while (newUsed[pos])
				pos = (pos + 1) & newMask;
			newUsed[pos] = true;
			newKey[pos] = k;
			copyFromValue(i, newValue, pos);
			i++;
		}
		n = newN;
		mask = newMask;
		maxFill = maxFill(n, f);
		this.key = newKey;
		this.value = newValue;
		this.used = newUsed;
	}

	private void copyFromValue(int srcPos, byte[] dest, int destPos) {
		System.arraycopy(value, srcPos * sliceLength, dest, destPos * sliceLength, sliceLength);
	}

	private void copyValue(int toPos, int fromPos) {
		System.arraycopy(value, fromPos * sliceLength, value, toPos * sliceLength, sliceLength);
	}

	private void copyToValue(byte[] valSrc, int valOffset, int pos) {
		System.arraycopy(valSrc, valOffset, value, pos * sliceLength, sliceLength);
	}

	public int getPos(int k) {
		int pos = it.unimi.dsi.fastutil.HashCommon.murmurHash3(k) & mask;
		while (used[pos]) {
			if (key[pos] == k) return pos;
			pos = (pos + 1) & mask;
		}
		return -1;
	}

	public int absoluteOffset(int entry) {
		return entry * sliceLength;
	}

	public byte[] array() {
		return value;
	}

	/**
	 * Shifts left entries with the specified hash code, starting at the
	 * specified position, and empties the resulting free entry.
	 *
	 * @param pos
	 *            a starting position.
	 * @return the position cleared by the shifting process.
	 */
	protected final int shiftKeys(int pos) {
		// Shift entries with the same hash.
		int last, slot;
		for (;;) {
			pos = ((last = pos) + 1) & mask;
			while (used[pos]) {
				slot = (it.unimi.dsi.fastutil.HashCommon
						.murmurHash3((key[pos]))) & mask;
				if (last <= pos ? last >= slot || slot > pos : last >= slot
						&& slot > pos)
					break;
				pos = (pos + 1) & mask;
			}
			if (!used[pos])
				break;
			key[last] = key[pos];
			copyValue(last, pos);
		}
		used[last] = false;
		return last;
	}

	@SuppressWarnings("unchecked")
	public void remove(final int k) {
		// The starting point.
		int pos = (it.unimi.dsi.fastutil.HashCommon.murmurHash3((k))) & mask;
		// There's always an unused entry.
		while (used[pos]) {
			if (((key[pos]) == (k))) {
				size--;
				shiftKeys(pos);
				return;
			}
			pos = (pos + 1) & mask;
		}
	}

}
