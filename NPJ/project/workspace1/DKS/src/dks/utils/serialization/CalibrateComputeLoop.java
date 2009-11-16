package dks.utils.serialization;

import java.lang.management.*;

public class CalibrateComputeLoop {
	public CalibrateComputeLoop() {}

	// returns the number of "compute" loop iterations needed to consume 1 sec
	static public long test() {
		final long startIterations = 100000L;
		final long maxIterations = 1000000000000L;
		final long one_sec = 1000000000L; // nanoseconds;
		final double maxerror = 0.05;   // 5% oughta be enough?
		final ThreadMXBean bean = ManagementFactory.getThreadMXBean( );

		long iterations = startIterations;

		if (!bean.isCurrentThreadCpuTimeSupported()) {
			System.err.println("Cannot calibrate the \"compute\" loop");
			return(-1);
		}

		while (true) { // until we succeed
			long start, end;
			double ratio;

			start = bean.getCurrentThreadCpuTime();
			ComputeLoop.compute(iterations);
			end = bean.getCurrentThreadCpuTime();

			if (end - start == 0) {
				// clock is not precise enough?
				iterations = iterations*10;
				if (iterations > maxIterations) {
					System.err.println("Cannot calibrate the \"compute\" loop");
					return (-1);
				}
			} else {
				ratio = ((double) one_sec) / ((double) (end - start));
				if (Math.abs(1.0 - ratio) < maxerror) {
					return (iterations);
				} else {
					// otherwise loop with the new estimation in order to verify it;
					iterations = (long) (((double) iterations) * ratio);	
				}
			}
		}
	}

	public static void main(String[] args) {
		System.out.println("1 sec is consumed by " + test() + " iterations");
	}
};
