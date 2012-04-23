package ru.undev;

import java.io.IOException;
import java.util.UUID;

public class FixedByteSliceHashSetTestPerf extends HashSetPerfTestBase {

	private FixedByteSliceOpenHashSet set = new FixedByteSliceOpenHashSet(
			UUID.randomUUID().toString().getBytes().length);
	
	@Override
	protected boolean searchElement(byte[] bs) {
		return set.contains(bs);
	}

	@Override
	protected void addElement(byte[] bs) {
		set.add(bs);
	}

	public static void main(String[] args) throws IOException {
		new FixedByteSliceHashSetTestPerf().test();
	}
}
