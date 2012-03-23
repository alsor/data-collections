package ru.undev;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;
import static it.unimi.dsi.fastutil.HashCommon.maxFill;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.booleans.BooleanArrays;
import util.hash.MurmurHash3;

/**
 * Implementation is heavily based on <a href="http://fastutil.dsi.unimi.it/">fastutil's</a> OpenHashSet collections.
 * 
 * The length of a slice should be reasonably small. Primarily this collection intended to store something like
 * Mongodb's style ids as bytes (not references to Strings).
 * 
 * @author alsor
 * 
 */
public class FixedByteSliceOpenHashSet implements Hash {

	/** The array of keys. */
	protected transient byte key[];
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

	public FixedByteSliceOpenHashSet(int sliceLength) {
		this(sliceLength, DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
	}

	public FixedByteSliceOpenHashSet(int sliceLength, int expected) {
		this(sliceLength, expected, DEFAULT_LOAD_FACTOR);
	}

	public FixedByteSliceOpenHashSet(int sliceLength, float f) {
		this(sliceLength, DEFAULT_INITIAL_SIZE, f);
	}

	/**
	 * Creates a new hash set.
	 * 
	 * <p>
	 * The actual number of entries in the table will be the least power of two greater than <code>expected</code>/
	 * <code>f</code>.
	 * 
	 * @param expected
	 *            the expected number of elements in the hash set.
	 * @param f
	 *            the load factor.
	 */
	public FixedByteSliceOpenHashSet(int sliceLength, int expected, float f) {
		if (f <= 0 || f > 1) {
			throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than or equal to 1");
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
		used = new boolean[n];
	}

	public boolean add(byte[] bs) {
		if (bs.length != sliceLength) {
			throw new IllegalArgumentException("Byte slice has wrong length: " + bs.length
					+ ", collections is accepting: " + sliceLength);
		}

		int pos = MurmurHash3.murmurhash3_x86_32(bs) & mask;

		while (used[pos]) {
			if (isEqualToKey(bs, pos)) return false;
			pos = (pos + 1) & mask;
		}
		used[pos] = true;
		copyToKey(bs, pos);

		if (++size >= maxFill) rehash(arraySize(size + 1, f));
		return true;
	}

	public boolean remove(byte[] bs) {
		int pos = MurmurHash3.murmurhash3_x86_32(bs) & mask;
		while (used[pos]) {
			if (isEqualToKey(bs, pos)) {
				size--;
				shiftKeys(pos);
				return true;
			}
			pos = (pos + 1) & mask;
		}
		return false;
	}

	private int shiftKeys(int pos) {
		// Shift entries with the same hash.
		int last, slot;
		for (;;) {
			pos = ((last = pos) + 1) & mask;
			while (used[pos]) {
				slot = MurmurHash3.murmurhash3_x86_32(key, pos * sliceLength, sliceLength) & mask;
				if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) break;
				pos = (pos + 1) & mask;
			}
			if (!used[pos]) break;
			copyKeys(pos, last);
		}
		used[last] = false;
		return last;
	}

	private void copyKeys(int fromPos, int toPos) {
		System.arraycopy(key, fromPos * sliceLength, key, toPos * sliceLength, sliceLength);
	}

	public boolean contains(byte[] bs) {
		int pos = MurmurHash3.murmurhash3_x86_32(bs) & mask;
		while (used[pos]) {
			if (isEqualToKey(bs, pos)) return true;
			pos = (pos + 1) & mask;
		}
		return false;
	}

	public void clear() {
		if (size == 0) return;
		size = 0;
		BooleanArrays.fill(used, false);
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	protected void rehash(final int newN) {
		int i = 0, pos;
		final boolean[] used = this.used;
		final byte[] key = this.key;
		final int newMask = newN - 1;
		final byte[] newKey = new byte[newN * sliceLength];
		final boolean[] newUsed = new boolean[newN];
		for (int j = size; j-- != 0;) {
			while (!used[i])
				i++;
			pos = MurmurHash3.murmurhash3_x86_32(key, i * sliceLength, sliceLength) & newMask;
			while (newUsed[pos])
				pos = (pos + 1) & newMask;
			newUsed[pos] = true;
			copyFromKey(i, newKey, pos);
			i++;
		}
		n = newN;
		mask = newMask;
		maxFill = maxFill(n, f);
		this.key = newKey;
		this.used = newUsed;
	}

	private void copyToKey(byte[] bs, int pos) {
		System.arraycopy(bs, 0, key, pos * sliceLength, sliceLength);
	}

	private boolean isEqualToKey(byte[] bs, int pos) {
		for (int i = pos * sliceLength, j = 0; j < sliceLength; i++, j++) {
			if (key[i] != bs[j]) return false;
		}
		return true;
	}

	private void copyFromKey(int srcPos, byte[] dest, int destPos) {
		System.arraycopy(key, srcPos * sliceLength, dest, destPos * sliceLength, sliceLength);
	}

}
