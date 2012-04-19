package ru.undev;

import it.unimi.dsi.fastutil.Hash;

import org.junit.Test;


public class FixedByteSlice2IntOpenHashMapTest {

	@Test
	public void map_bytes_to_int() throws Exception {
		int sliceLength;
		FixedByteSlice2IntOpenHashMap map = new FixedByteSlice2IntOpenHashMap(sliceLength);
		
		for (int i = 0; i < Hash.DEFAULT_INITIAL_SIZE * 5; i++) {
			byte[] key = new byte[] { (byte) (i + 1), (byte) (i + 2), (byte) (i + 3) };
			map.put(key, i);
		}

		for (int i = 0; i < Hash.DEFAULT_INITIAL_SIZE * 5; i++) {
			byte[] key = new byte[] { (byte) (i + 1), (byte) (i + 2), (byte) (i + 3) };
			assertTrue(map.containsKey(key));
			assertTrue(map.containsValue(i));
			assertTrue(i == map.get(key));
		}
	}

}
