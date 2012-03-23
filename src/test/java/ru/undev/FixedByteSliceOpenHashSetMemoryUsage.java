package ru.undev;

import java.io.IOException;
import java.util.UUID;

public class FixedByteSliceOpenHashSetMemoryUsage {
	private static FixedByteSliceOpenHashSet set;

	public static void main(String[] args) throws InterruptedException, IOException {
		int count = 2000000;
		int length = UUID.randomUUID().toString().getBytes().length;
		System.out.println("length: " + length);

		set = new FixedByteSliceOpenHashSet(length, 0.9f);

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
