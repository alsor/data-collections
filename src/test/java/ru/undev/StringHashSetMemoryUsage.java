package ru.undev;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StringHashSetMemoryUsage {
	private static Set<String> set;

	public static void main(String[] args) throws IOException {
		int count = 2000000;

		set = new HashSet<String>();

		System.out.print("filling up the collection...");
		for (int i = 0; i < count; i++) {
			set.add(UUID.randomUUID().toString());
		}
		System.out.println("done");

		// pause the test so we can use any external profiler to view heap
		System.out.print("Press Enter to exit...");
		System.in.read();
	}
}
