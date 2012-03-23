package ru.undev;

import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.io.IOException;
import java.util.UUID;

public class ObjectOpenHashSetMemoryUsage {
	private static ObjectSet<byte[]> set;

	public static void main(String[] args) throws InterruptedException, IOException {
		int count = 2000000;

		set = new ObjectOpenCustomHashSet<byte[]>(ByteArrays.HASH_STRATEGY);

		System.out.print("filling up the collection...");
		for (int i = 0; i < count; i++) {
			set.add(UUID.randomUUID().toString().getBytes());
		}
		System.out.println("done");

		// pause the test so we can use any external profiler to view heap
		System.out.print("Press Enter to exit...");
		System.in.read();
	}

}
