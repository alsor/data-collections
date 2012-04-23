package ru.undev;

import static org.junit.Assert.*;
import static ru.undev.testutils.Utils.contentEqual;
import it.unimi.dsi.fastutil.Hash;

import org.junit.Test;

public class Int2FixedByteSliceOpenHashMapTest {
	@Test
	public void map_int_to_bytes() throws Exception {
		Int2FixedByteSliceOpenHashMap map = new Int2FixedByteSliceOpenHashMap(3);

		for (int i = 0; i < Hash.DEFAULT_INITIAL_SIZE * 5; i++) {
			byte[] key = new byte[] { (byte) (i + 1), (byte) (i + 2), (byte) (i + 3) };
			map.put(i, key);
		}

		for (int i = 0; i < Hash.DEFAULT_INITIAL_SIZE * 5; i++) {
			byte[] key = new byte[] { (byte) (i + 1), (byte) (i + 2), (byte) (i + 3) };
			int pos = map.getPos(i);
			assertTrue(pos != -1);
			assertTrue(contentEqual(map.array(), map.absoluteOffset(pos), key, 0, 3));
		}

	}
}
