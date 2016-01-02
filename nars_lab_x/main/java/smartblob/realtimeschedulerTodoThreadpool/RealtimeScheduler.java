/** Ben F Rayfield offers this software under GNU GPL 2+ open source license(s) */
package smartblob.realtimeschedulerTodoThreadpool;


//import jselfmodify.JSelfModify;
//import jsoundcard.JSoundCard;

import static smartblob.commonfuncs.CommonFuncs.log;

/** TODO Use higher priority threads and sleep more often? Will system permissions allow it? */
public class RealtimeScheduler{
	private RealtimeScheduler(){}
	
	public static final double MAX_INTERVAL = 100.*365.25*24*60*60; //100 years
	
	/** relative to 1.0, considering how many digits double has */
	public static final double epsilon = 1e-40;
	
	protected static final double MAX_INTERVAL_PLUS_EPSILONS = MAX_INTERVAL*(1+epsilon);
	
	//private static final List<Task> tasks = new ArrayList<Task>();
	
	//TODO? private static final List<TaskThread> threads = new ArrayList<TaskThread>();
	
	//TODO tasks should say when they want to run again
	

	private static Map<Task,TaskThread> taskToThread = new TreeMap<Task,TaskThread>(new Comparator<Task>(){
		public int compare(Task x, Task y){
			return x.toString().compareTo(y.toString());
		}
	});


	//Collections.synchronizedMap(new HashMap<Task,TaskThread>());
	
	/** uses Task.preferredInterval() */
	public static synchronized void start(Task t){
		start(t, t.getTargetFPS());
	}
	
	/** TODO If many tasks, use a thread pool */
	public static synchronized void start(Task t, double fps ){
		double secondsSleep = 1.0 / fps;
		log("START RealtimeScheduler.start secondsSleep="+secondsSleep+" task="+t);
		TaskThread th = taskToThread.get(t);
		if(th == null){
			th = new TaskThread(t, secondsSleep);
			taskToThread.put(t, th);
			//setPriority(th, Thread.NORM_PRIORITY);
			//setPriority(th, Thread.MAX_PRIORITY);
			//increaseToMaxPriority(th);
			th.start();
		}else{
			th.secondsSleep = secondsSleep; //has no effect on current sleep. Starts next sleep.
		}
		log("END RealtimeScheduler.start secondsSleep="+secondsSleep+" task="+t);
	}
	
	public static synchronized void scheduleStop(Task t){
		stop(t, false);
	}
	
	/** May need some synchronized here...
	TODO? TaskThread and maybe each Task's loops should check Thread.interrupted()
	and use InterruptedException to cancel in the middle of any long calculations
	as described at http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
	--Ben F Rayfield (sign your comments if you change things)
	*/
	public static synchronized void stop(Task t, boolean waitForStop){
		log("Stopping task "+t);
		if(waitForStop) throw new RuntimeException("TODO this thread system needs redesign because if waitForStop is true it has been deadlocking");
		log("START RealtimeScheduler.stop waitForStop="+waitForStop+" task="+t);
		TaskThread th = taskToThread.get(t);
		if(th != null){
			log("TaskThread found for "+t+" Setting keepRunning to false");
			th.keepRunning = false;
			//increaseToMaxPriority(th); //so it can see its supposed to stop quickly
			//th.task = null;
			//th.interrupt(); //TODO is this line needed?
		}
		//will onTaskEnd when it finishes its last run
		//TODO JSelfModify.logToUser("RealtimeScheduler.stop: TODO is there something I can do to allow ending tasks faster? Thread.interrupted from inside the task more often?");
		if(waitForStop){
			//TODO Will this ever create deadlock?
			while(taskIsRunning(t)){
				try{
					Thread.sleep(1L);
				}catch(InterruptedException e){}
			}
		}
		log("END RealtimeScheduler.stop waitForStop="+waitForStop+" task="+t);
	}
	
	public static synchronized boolean taskIsRunning(Task t){
		return taskToThread.containsKey(t);
	}
	
//	public static synchronized Set<Task> tasks(){
//		//return tasks.toArray(new Task[0]);
//		return Collections.unmodifiableSet(new HashSet<Task>(taskToThread.keySet()));
//	}
	public static Iterable<Task> tasksSorted(){
		//return tasks.toArray(new Task[0]);
		return taskToThread.keySet();
	}


	
	static synchronized void onTaskEnd(Task t){
		log("\r\n\r\n\r\n\r\nRealtimeScheduler removing "+t+"\r\n\r\n\r\n\r\n");
		taskToThread.remove(t);
	}
	
	/** Uses Thread.sleep(milliseconds,nanoseconds) for extra accuracy,
	but don't count on Java running threads often enough to use the extra accuracy on all computers.
	--Ben F Rayfield (sign your comments if you change things)
	*/
	public static void threadSleep(double seconds) throws InterruptedException{
		if(seconds <= 0) return;
		double millis = seconds*1e3;
		long millisL = (long)millis;
		millis -= millisL;
		double nanos = millis*1e6;
		int nanosI = (int)Math.round(nanos);
		Thread.sleep(millisL, nanosI);
	}

	/** TODO Reuse these threads for multiple Tasks
	--Ben F Rayfield (sign your comments if you change things)
	*/
	protected static class TaskThread extends Thread{
		
		protected boolean keepRunning = true;
		
		public final Task task;
		protected long lastNanotime = System.nanoTime();
		
		/** After task runs, sleeps this long, and repeats this cycle until the task stops.
		This var can be changed but will have no effect on the current sleep. It starts next sleep.
		--Ben F Rayfield (sign your comments if you change things)
		*/
		protected double secondsSleep;
		
		public TaskThread(Task task, double secondsSleep){
			if(secondsSleep <= 0 || MAX_INTERVAL_PLUS_EPSILONS < secondsSleep) throw new IllegalArgumentException(
				"secondsSleep="+secondsSleep);
			this.task = task;
			this.secondsSleep = secondsSleep;
		}
		
		public void run(){
			while(true){
				if(!keepRunning){
					RealtimeScheduler.onTaskEnd(task);
					return;
				}
				//log("TaskThread.run for "+task+" keepRunning="+keepRunning);
				long nowNanotime = System.nanoTime();
				double seconds = (nowNanotime-lastNanotime)*.000000001;
				lastNanotime = nowNanotime;
				task.event();
				secondsSleep = task.getTargetFPS();
				if(secondsSleep == Double.MAX_VALUE){
					RealtimeScheduler.onTaskEnd(task);
					return;
				}
				/*if(!keepRunning || !task.nextState(seconds)){
					RealtimeScheduler.onTaskEnd(task);
					return;
				}*/
				try{
					threadSleep(secondsSleep);
				}catch(InterruptedException e){
					System.out.println("TODO What to do about InterruptedExceptions? See comment in stop(Task) function. e="+e);
				}
			}
		}
		
	}
	
	public static void increaseToMaxPriority(Thread t){
		int thisPri = t.getPriority();
		int maxPri = t.getThreadGroup().getMaxPriority();
		if(thisPri < maxPri){
			try{
				t.setPriority(maxPri);
				log("Increased priority of "+t+" from "+thisPri+" to "+maxPri);
			}catch(Exception e){
				throw new RuntimeException("Could not increase priority of "+t, e);
			}
		}else{
			log("Thread "+t+" was already at its max priority "+maxPri);
		}
	}
	
	public static void setPriority(Thread t, int priority){
		int thisPri = t.getPriority();
		int maxPri = t.getThreadGroup().getMaxPriority();
		int nextPriority = Math.min(priority, maxPri);
		if(nextPriority != thisPri){
			try{
				t.setPriority(maxPri);
				log("Increased priority of "+t+" from "+thisPri+" to "+maxPri);
			}catch(Exception e){
				throw new RuntimeException("Could not increase priority of "+t, e);
			}
		}
	}
	
	/*
	public static void setPriority(Thread t, int priority){
		try{
			t.setPriority(priority);
			log("Set priority of "+t+" to "+priority);
		}catch(Exception e){
			throw new RuntimeException("Could not set priority of "+t+" to "+priority, e);
		}
	}*/
	
	public static void main(String args[]){
		List<Task> testTasks = new ArrayList<Task>();
		increaseToMaxPriority(Thread.currentThread());
		for(int i=0; i<200; i++){
		//for(int i=0; i<20; i++){
		//for(int i=0; i<5; i++){
		//for(int i=0; i<1; i++){
			final int I = i;
			testTasks.add(new Task(){
				public final int taskNum = I;
				/*public boolean nextState(double secondsSinceLastCall){
					log(this+" nextState "+secondsSinceLastCall);
					return true;
				}*/
				public void event(){
					/*if(o instanceof TimedEvent){
						log(this+" nextState "+((TimedEvent)o).time);
					}*/
					log("\r\n\r\n"+this+" ");
				}
				public double getTargetFPS(){ return .5; }
				public String toString(){
					if(taskNum < 10) return "Task0"+taskNum;
					return "Task"+taskNum;
				}
			});
		}
		for(Task t : testTasks){
			start(t);
		}
		boolean tasksExist = true;
		int i = 0;
		//Nanotimer timer = new Nanotimer();
		while(tasksExist){
			//try{
			//	JSoundCard.sleepSeconds(.1);
			//}catch(InterruptedException e){
			//	System.out.println("Interrupted");
			//}
			Iterable<Task> tasksArray = tasksSorted();
			String s = "Tasks running:";
			for(Task t : tasksArray) s += " "+t;
			log(s);
			if(/*timer.secondsSinceStart() > 2 &&*/ i < testTasks.size()){
				//log("About to stop and wait on it stopping "+testTasks.get(i));
				//stop(testTasks.get(i), true);
				scheduleStop(testTasks.get(i));
				i++;
			}
			/*try{
				threadSleep(.0001);
			}catch(InterruptedException e){
				throw new RuntimeException(e);
			}*/
			Thread.yield();
		}
	}

}