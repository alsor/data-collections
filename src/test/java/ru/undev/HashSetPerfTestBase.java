package ru.undev;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class HashSetPerfTestBase extends PerfTestCase {

	private static AtomicInteger count = new AtomicInteger();

	protected abstract boolean searchElement(byte[] bs);

	protected abstract void addElement(byte[] bs);

	private AtomicInteger found = new AtomicInteger();
	private AtomicInteger notFound = new AtomicInteger();
	private Random rnd = new Random();
	private List<byte[]> search = new ArrayList<byte[]>();

	public Set<byte[]> memholders = new HashSet<byte[]>();
	
	public HashSetPerfTestBase() {
		super();
	}

	public void prepareTest() {
		int total = 500000;
		int toSearch = 10000;
		float foundRatio = 0.6f;
		int memholesEach = 100;
		int memholesSize = 100;
	
		for (int i = 0; i < total; i++) {
			byte[] bs = UUID.randomUUID().toString().getBytes();
			addElement(bs);
	
			if (i % (total / toSearch) == 0) {
				if (rnd.nextFloat() < foundRatio) {
					search.add(bs);
				} else {
					search.add(UUID.randomUUID().toString().getBytes());
				}
			}

			if (i % memholesEach == 0) {
				for (int j = 0; j < memholesSize; j++) {
					memholders.add(UUID.randomUUID().toString().getBytes());
				}
			}
		}
	}

	public void perSecond() {
		System.out.println("found: " + found.getAndSet(0) + ",\tnot found: " + notFound.getAndSet(0) + ",\t"
				+ count.getAndSet(0) + " per second");
	}

	public void runTest(PerfTestContext context) {
		int i = 0;
		while (!context.stopped()) {
			if (searchElement(search.get(i))) {
				found.incrementAndGet();
			} else {
				notFound.incrementAndGet();
			}
			count.incrementAndGet();
	
			i++;
			if (i >= search.size()) {
				i = 0;
			}
		}
	}

}