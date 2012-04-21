package ru.undev;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FixedByteSlice2FixedByteSliceMapTest {

	@Test
	public void store_bytes() throws Exception {
		int keyLength = 3;
		int valueLength = 4 + 4 + 1;
		Bytes2BytesOpenHashMap map = new Bytes2BytesOpenHashMap(keyLength, valueLength);

		byte[] keySrc = new byte[] { 1, 2, 3 };
		int keyOffset = 0;

		int entry = map.putKey(keySrc, keyOffset);

		{
			byte[] valSrc = new byte[] { 4, 5, 6, 7 };
			int entryOffset = 0;
			int valOffset = 0;
			int valLength = 4;
			map.putBytes(entry, entryOffset, valSrc, valOffset, valLength);
		}

		{
			int entryOffset = 4;
			int val = 123;
			map.putInt(entry, entryOffset, val);
		}

		{
			int entryOffset = 4 + 4;
			byte val = 5;
			map.putByte(entry, entryOffset, val);
		}

		int pos = map.getPos(keySrc);
		assertTrue(pos != -1);
		{
			int absoluteOffset = map.absoluteOffset(pos, 0);
			assertTrue(contentEqual(map.array, absoluteOffset, new byte[] { 4, 5, 6, 7 }, 0, 4));
		}
		{
			int entryOffset = 4;
			int val = map.getInt(pos, entryOffset);
			assertTrue(val == 123);
		}
		{
			int entryOffset = 4 + 4;
			byte val = map.array[map.absoluteOffset(entry, entryOffset)];
			assertTrue(val == 5);
		}
		
	}

	private boolean contentEqual(byte[] a1, int o1, byte[] a2, int o2, int length) {
		for (int i1 = o1, i2 = o2; i1 < length; i1++, i2++) {
			if (a1[i1] != a2[i2]) return false;
		}
		return true;
	}

}
