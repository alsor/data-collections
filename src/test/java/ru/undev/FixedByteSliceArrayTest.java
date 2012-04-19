package ru.undev;

import org.junit.Test;

public class FixedByteSliceArrayTest {

	@Test
	public void store_bytes() throws Exception {
		FixedByteSliceArray array = new FixedByteSliceArray(sliceLength, arraySize);
		array.put(entry, src, srcOffset, srcLength);

		int offset = array.getEntryOffset(entry);
	}

}
