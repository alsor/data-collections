package ru.undev;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.unimi.dsi.fastutil.Hash;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class FixedByteSliceOpenHashSetTest {

	// this is actually a perf test
	public static void main(String[] args) {
		int searchForEach = 10;
		int warmup = 100000;
		int count = 1000000;
		int length = UUID.randomUUID().toString().getBytes().length;
		System.out.println("length: " + length);

		// prepare warmup data
		System.out.print("preparing warmup data...");
		List<byte[]> warmupData = new ArrayList<byte[]>(warmup);
		for (int i = 0; i < warmup; i++) {
			warmupData.add(UUID.randomUUID().toString().getBytes());
		}
		System.out.println("done");
		
		// prepare main data
		System.out.print("preparing main data...");
		List<byte[]> srcData = new ArrayList<byte[]>(count);
		List<byte[]> searchData = new ArrayList<byte[]>((count / searchForEach) * 2);
		for (int i = warmup; i < warmup + count; i++) {
			byte[] e = UUID.randomUUID().toString().getBytes();
			srcData.add(e);
			if (i % searchForEach == 0) {
				searchData.add(e);
				searchData.add(UUID.randomUUID().toString().getBytes());
			}
		}
		System.out.println("done");

		FixedByteSliceOpenHashSet set = new FixedByteSliceOpenHashSet(length);

		System.gc();

		// warmup
		System.out.print("warming up...");
		for (byte[] bs : warmupData) {
			set.add(bs);
		}
		System.out.println("done");
		
		{
			// start the addition test
			System.out.print("testing addition...");

			long start = System.currentTimeMillis();

			for (byte[] bs : srcData) {
				set.add(bs);
			}

			long time = System.currentTimeMillis() - start;
			System.out.println("done. time: " + time + "ms");
		}

		{
			// start the searching test
			System.out.print("testing searching...");

			int found = 0;
			int notFound = 0;
			long start = System.currentTimeMillis();

			for (byte[] bs : searchData) {
				if (set.contains(bs)) {
					found++;
				} else {
					notFound++;
				}
			}

			long time = System.currentTimeMillis() - start;
			System.out.println("done. found: " + found + ", not found: " + notFound + ", time: " + time + "ms");
		}
	}

	@Test
	public void store_bytes() throws Exception {
		FixedByteSliceOpenHashSet set = new FixedByteSliceOpenHashSet(3);

		// we need this to REALLY test addition
		// particularly growing of the collection and resolving collisions

		// fill the collection for sure
		for (int i = 0; i < Hash.DEFAULT_INITIAL_SIZE * 5; i++) {
			set.add(new byte[] { (byte) (i + 1), (byte) (i + 2), (byte) (i + 3) });
		}

		// test that all entries are here
		for (int i = 0; i < Hash.DEFAULT_INITIAL_SIZE * 5; i++) {
			assertTrue("contains entry for " + i,
					set.contains(new byte[] { (byte) (i + 1), (byte) (i + 2), (byte) (i + 3) }));
		}
	}

	@Test
	public void remove_bytes() throws Exception {
		FixedByteSliceOpenHashSet set = new FixedByteSliceOpenHashSet(3);
	
		// we need this to REALLY test correct shifting after removal
	
		// fill the collection for sure
		for (int i = 0; i < Hash.DEFAULT_INITIAL_SIZE * 5; i++) {
			set.add(new byte[] { (byte) (i + 1), (byte) (i + 2), (byte) (i + 3) });
		}
	
		// remove one entry
		set.remove(new byte[] { 1, 2, 3 });
	
		// check that all the rest entries are still in the collection
		for (int i = 1; i < Hash.DEFAULT_INITIAL_SIZE * 5; i++) {
			assertTrue("contains entry for " + i,
					set.contains(new byte[] { (byte) (i + 1), (byte) (i + 2), (byte) (i + 3) }));
		}
	}

	@Test
	public void report_size_and_isEmpty_correctly() throws Exception {
		FixedByteSliceOpenHashSet set = new FixedByteSliceOpenHashSet(3);

		assertTrue(set.size() == 0);
		assertTrue(set.isEmpty());

		assertTrue(set.add(new byte[] { 1, 2, 3 }));
		assertTrue(set.size() == 1);
		assertFalse(set.isEmpty());
		assertTrue(set.contains(new byte[] { 1, 2, 3 }));

		assertFalse(set.contains(new byte[] { 3, 2, 1 }));

		assertTrue(set.add(new byte[] { 4, 5, 6 }));
		assertTrue(set.size() == 2);
		assertTrue(set.contains(new byte[] { 1, 2, 3 }));
		assertTrue(set.contains(new byte[] { 4, 5, 6 }));

		assertTrue(set.remove(new byte[] { 1, 2, 3 }));
		assertTrue(set.size() == 1);
		assertFalse(set.contains(new byte[] { 1, 2, 3 }));
		assertTrue(set.contains(new byte[] { 4, 5, 6 }));

		assertFalse(set.remove(new byte[] { 3, 2, 1 }));

		assertTrue(set.remove(new byte[] { 4, 5, 6 }));
		assertFalse(set.contains(new byte[] { 4, 5, 6 }));
		assertTrue(set.size() == 0);
		assertTrue(set.isEmpty());
	}

	@Test
	public void throw_an_exception_if_a_slice_is_of_wrong_length() throws Exception {
		try {
			new FixedByteSliceOpenHashSet(3).add(new byte[] { 1, 2 });
			fail("too short slice causes an exception");
		} catch (Exception e) {
		}

		try {
			new FixedByteSliceOpenHashSet(3).add(new byte[] { 1, 2, 3, 4 });
			fail("too long slice causes an exception");
		} catch (Exception e) {
		}
	}

	@Test
	public void throw_an_exception_when_load_factor_is_wrong() throws Exception {
		try {
			new FixedByteSliceOpenHashSet(3, 3, 0);
			fail("0 as a load factor causes an exception");
		} catch (Exception e) {
		}

		try {
			new FixedByteSliceOpenHashSet(3, 3, -1);
			fail("negative load factor causes an exception");
		} catch (Exception e) {
		}

		try {
			new FixedByteSliceOpenHashSet(3, 3, 1.1f);
			fail("load factor > 1 causes an exception");
		} catch (Exception e) {
		}
	}

	@Test
	public void throw_an_exception_when_expected_number_is_negative() throws Exception {
		try {
			new FixedByteSliceOpenHashSet(3, -1);
			fail("negative expected number causes an exception");
		} catch (Exception e) {
		}
	}

}
