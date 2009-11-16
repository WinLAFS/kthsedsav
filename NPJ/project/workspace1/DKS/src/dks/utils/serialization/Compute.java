package dks.utils.serialization;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ComputeProcess extends ComputeLoop implements Runnable {
	final Compute computeObj;

	public ComputeProcess(Compute computeObjIn) {
		computeObj = computeObjIn;
	}

	public void run() {
    	while (true) {
    		compute(computeObj.iterations);
    		try {
				Thread.sleep(computeObj.sleep);
			} catch (InterruptedException e) {
				;
			}
			// System.err.print(".");
    		computeObj.waitIdle();
    	}
    }
}

public class Compute {
	public Args args;
	public long iterations; // per thread;
	public long sleep;      // per thread;
	private boolean waiting = true;
	private final Lock computeLock = new ReentrantLock();
	private final Condition resume = computeLock.newCondition();
	ExecutorService computeThreadPool = null;
	ComputeProcess processes[];

	public Compute(Args argsIn, ExecutorService thrPool) {
		args = argsIn;
		computeThreadPool = thrPool;

		if (Math.abs(args.load) > 0.01) {
			if (args.cycles == 0) {
				System.err.println("WARNING: cannot simulate computation without the \"-cycles\" argument");
				args.threads = 0;
				args.load = 0.0;
			} else {
				iterations = (long) (((double) args.cycles) * args.load / (double) args.threads);
				sleep = (long) (((double) 1000) * Math.abs(1.0 - (args.load / (double) args.threads)));
				// System.err.println("iterations=" + iterations + ", sleep=" + sleep);

				// start in "suspended" state;
				processes = new ComputeProcess[args.threads];
				for (int i = 0; i < args.threads; i++) {
					processes[i] = new ComputeProcess(this);
					computeThreadPool.execute(processes[i]);
				}
			}
		}
	}

	public void waitIdle() {
		computeLock.lock();
		while (waiting) {
			resume.awaitUninterruptibly();
		}
		computeLock.unlock();
	}

	public void suspend() {
		computeLock.lock();
		waiting = true;
		computeLock.unlock();
	}
	
	public void resume() {
		computeLock.lock();
		// System.err.println("resuming!");
		waiting = false;
		resume.signalAll();
		computeLock.unlock();
	}
};
