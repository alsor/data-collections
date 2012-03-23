package ru.undev;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public abstract class PerfTestCase {

	private volatile boolean stopped = false;

	abstract public void prepareTest();

	abstract public void perSecond();

	abstract public void runTest(PerfTestContext context);

	public void test() throws IOException {
		System.out.print("Preparing test...");


		final PerfTestContext context = new PerfTestContext() {

			public boolean stopped() {
				return stopped;
			}

		};

		prepareTest();

		System.gc();

		System.out.println("done.");
		System.out.println("Press Enter to start test...");
		System.in.read();

		new Timer(true).schedule(new TimerTask() {

			@Override
			public void run() {
				perSecond();
			}
		}, 1000, 1000);

		new Thread(new Runnable() {

			public void run() {
				runTest(context);
			}
		}).start();

		System.out.println("Press Enter to exit...");
		System.in.read();
		stopped = true;
	}
}
