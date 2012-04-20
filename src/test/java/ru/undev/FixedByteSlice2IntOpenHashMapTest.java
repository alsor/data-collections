package ru.undev;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.unimi.dsi.fastutil.Hash;

import org.junit.Test;


public class FixedByteSlice2IntOpenHashMapTest {

	@Test
	public void map_bytes_to_int() throws Exception {
		int sliceLength = 3;
		FixedByteSlice2IntOpenHashMap map = new FixedByteSlice2IntOpenHashMap(sliceLength);
		
		for (int i = 0; i < Hash.DEFAULT_INITIAL_SIZE * 5; i++) {
			byte[] key = new byte[] { (byte) (i + 1), (byte) (i + 2), (byte) (i + 3) };
			map.put(key, i);
		}

		for (int i = 0; i < Hash.DEFAULT_INITIAL_SIZE * 5; i++) {
			byte[] key = new byte[] { (byte) (i + 1), (byte) (i + 2), (byte) (i + 3) };
			int pos = map.getPos(key);
			assertTrue(pos != -1);
			assertTrue(i == map.getValue(pos));
		}
	}

//	@Test
//	public void report_size_and_isEmpty_correctly() throws Exception {
//		FixedByteSlice2IntOpenHashMap map = new FixedByteSlice2IntOpenHashMap(3);
//
//		assertTrue(map.size() == 0);
//		assertTrue(map.isEmpty());
//		
//		map.put(new byte[] { 1, 2, 3 }, 1);
//		assertTrue(map.size() == 1);
//		assertFalse(map.isEmpty());
//	}

	@Test
	public void throw_an_exception_when_load_factor_is_wrong() throws Exception {
		try {
			new FixedByteSlice2IntOpenHashMap(3, 3, 0);
			fail("0 as a load factor causes an exception");
		} catch (Exception e) {
		}

		try {
			new FixedByteSlice2IntOpenHashMap(3, 3, -1);
			fail("negative load factor causes an exception");
		} catch (Exception e) {
		}

		try {
			new FixedByteSlice2IntOpenHashMap(3, 3, 1.1f);
			fail("load factor > 1 causes an exception");
		} catch (Exception e) {
		}
	}

	@Test
	public void throw_an_exception_when_expected_number_is_negative() throws Exception {
		try {
			new FixedByteSlice2IntOpenHashMap(3, -1);
			fail("negative expected number causes an exception");
		} catch (Exception e) {
		}
	}

}
