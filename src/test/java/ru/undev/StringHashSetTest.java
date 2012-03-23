package ru.undev;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class StringHashSetTest {

	private static int found;
	private static int notFound;

	public static void main(String[] args) {
		int searchForEach = 10;
		int count = 1000000;

		Set<String> set = new HashSet<String>();
		List<String> searchData = new ArrayList<String>((count / searchForEach) * 2);

		System.out.print("preparing main data...");
		for (int i = 0; i < count; i++) {
			String e = UUID.randomUUID().toString();
			set.add(e);
			if (i % searchForEach == 0) {
				searchData.add(e);
				searchData.add(UUID.randomUUID().toString());
			}
		}
		System.out.println("done");

		{
			// start the searching test
			System.out.print("testing searching...");

			long start = System.currentTimeMillis();

			for (String s : searchData) {
				if (set.contains(s)) {
					found++;
				} else {
					notFound++;
				}

			}

			long time = System.currentTimeMillis() - start;
			System.out.println("done. found: " + found + ", not found: " + notFound + ", time: " + time + "ms");
		}
	}
}
