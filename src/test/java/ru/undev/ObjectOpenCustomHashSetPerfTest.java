package ru.undev;

import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.io.IOException;

public class ObjectOpenCustomHashSetPerfTest extends HashSetPerfTestBase {

	@SuppressWarnings("unchecked")
	private ObjectSet<byte[]> set = new ObjectOpenCustomHashSet<byte[]>(ByteArrays.HASH_STRATEGY);


	@Override
	protected boolean searchElement(byte[] bs) {
		return set.contains(bs);
	}

	@Override
	protected void addElement(byte[] bs) {
		set.add(bs);
	}

	public static void main(String[] args) throws IOException {
		new ObjectOpenCustomHashSetPerfTest().test();
	}
}
