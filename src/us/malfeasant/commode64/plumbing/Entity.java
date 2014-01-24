package us.malfeasant.commode64.plumbing;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Entity {
	private final Executor localExec = Executors.newSingleThreadExecutor();
	
	protected void enqueue(Runnable job) {
		localExec.execute(job);
	}
}
