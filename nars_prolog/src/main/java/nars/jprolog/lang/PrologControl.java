package nars.jprolog.lang;

import nars.jprolog.Prolog;

import java.io.Serializable;
/**
 * Prolog thread.<br>
 * The <code>PrologControl</code> class is an implementation of 
 * <em>Prolog Box Control Flow Model</em>.<br>
 * This <code>PrologControl</code> provides methods 
 * for both sequential and parallel execution.
 *
 * <pre>
 * // An example of sequential execution
 * // calls a goal <code>father(abraham, X)</code> and get all solutions.
 * PrologControl p = new PrologControl();
 * Predicate code = new PRED_father_2();
 * Term a1 = SymbolTerm.makeSymbol("abraham");
 * Term a2 = new VariableTerm();
 * Term[] args = {a1, a2};
 * p.setPredicate(code, args);
 * for (boolean r = p.call(); r; r = p.redo()) {
 *     System.out.println(a2.toString());
 * }
 * </pre>
 *
 * <pre>
 * // To get only one solution.
 * PrologControl p = new PrologControl();
 * Predicate code = new PRED_father_2();
 * Term a1 = SymbolTerm.makeSymbol("abraham");
 * Term a2 = new VariableTerm();
 * Term[] args = {a1, a2};
 * if (p.execute(code, args))
 *     System.out.println(a2.toString());
 * else 
 *     System.out.println("fail");
 * </pre>
 * 
 * <pre>
 * // An example of parallel execution
 * // calls <code>queens(4,X)</code> and <code>queens(8,Y)</code> in parallel.
 * // Usage:
 * //   % plcafe -cp queens.jar T
 * // 
 * import jp.ac.kobe_u.cs.prolog.lang.*;
 * public class T {
 *     public static void main(String args[]) {
 *     long t = System.currentTimeMillis();
 *     boolean r1 = true;
 *     boolean r2 = true;
 *     Term a1[] = {new IntegerTerm(4), new VariableTerm()};
 *     Term a2[] = {new IntegerTerm(8), new VariableTerm()};
 *
 *     PrologControl e1 = new PrologControl();
 *     PrologControl e2 = new PrologControl();
 *     Term v1 = new VariableTerm();
 *     Term v2 = new VariableTerm();
 *     e1.setPredicate(new PRED_queens_2(), a1);
 *     e2.setPredicate(new PRED_queens_2(), a2);
 *     System.out.println("Start");
 *     e1.start();
 *     e2.start();
 *     while (r1 || r2) {
 *	    try {
 *		Thread.sleep(10);
 *	    } catch (InterruptedException e) {}
 *	    if (r1 && e1.ready()) {
 *		r1 = e1.next();
 *		if (r1) {
 *		    System.out.println("Success1 = " + a1[1]);
 *		    e1.cont();
 *		} else {
 *		    System.out.println("Fail1");
 *		}
 *	    } else if (r2 && e2.ready()) {
 *		r2 = e2.next();
 *		if (r2) {
 *		    System.out.println("Success2 = " + a2[1]);
 *		    e2.cont();
 *		} else {
 *		    System.out.println("Fail2");
 *		}
 *	    } else {
 *		System.out.println("Waiting");
 *		try {
 *		    Thread.sleep(100);
 *		} catch (InterruptedException e) {}
 *	    }
 *	}
 *	System.out.println("End");
 *	long t1 = System.currentTimeMillis();
 *	long t2 = t1 - t;
 *	System.out.println("time = " + t2 + "msec.");
 *    }
 * }
 * </pre>
 * 
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.2
 */
public class PrologControl implements Runnable, Serializable {
    /** A volatile instance variable holding a thread. */
    public volatile Thread thread;

    /** Holds a Prolog engine. */
    public Prolog engine;

    /** Holds a Prolog goal to be executed. */
    public Predicate code;

    /** A flag that indicates whether the result of goal is <code>true</code> or <code>false</code>. */
    public boolean result;

    /** A flag that indicates whether the result of goal is ready or not. */
    public boolean resultReady;

    /** Constructs a new <code>PrologControl</code>. */
    public PrologControl(PrologClassLoader pcl) {
	thread = null;
	engine = new Prolog(this, pcl);
	code = null;
	result = false;
	resultReady = false;
    }

    /** Sets a goal and its arguments to this Prolog thread. 
     * An initial continuation goal (a <code>Success</code> object)
     * is set to the <code>cont</code> field of goal <code>p</code> as continuation.
     */
    public void setPredicate(Predicate p, Term[] args) {
	code = p;
	code.setArgument(args, new Success(this));
    }

    /** Sets a goal <code>call(t)</code> to this Prolog thread. 
     * An initial continuation goal (a <code>Success</code> object)
     * is set to the <code>cont</code> field of <code>call(t)</code> as continuation.
     */
    public void setPredicate(Term t)  {
	try {
	    Class clazz = engine.pcl.loadPredicateClass("jp.ac.kobe_u.cs.prolog.builtin", "call", 1, true);
	    Term[] args = {engine.copy(t)};
	    code = (Predicate)(clazz.newInstance());
	    code.setArgument(args, new Success(this));
	} catch (Exception e){
	    e.printStackTrace();
	}
    }

    /**
     * Returns <code>true</code> if the system succeeds to find a first solution
     * of the given goal, <code>false</code> otherwise.<br>
     *
     * This method is useful to find only one solution.<br>
     *
     * This method first initilizes the Prolog engine by invoking <code>engine.init()</code>,
     * allocates a new <code>Thread</code> object, and start the execution of the given goal.
     * And then it stops the thread and returns <code>true</code> 
     * if the goal succeeds, <code>false</code> otherwise.
     * @see #run
     */
    public synchronized boolean execute(Predicate p, Term[] args) {
	engine.init();
	code = p;
	code.setArgument(args, new Success(this));
	thread = new Thread(this);
	thread.start(); // execute run() in new thread.
	try {
	    wait();     // wait caller's thread.
	} catch (InterruptedException e) {}
	stop();
	return result;
    }

    /**
     * Returns <code>true</code> if the system succeeds to find a first solution
     * of the goal, <code>false</code> otherwise.<br>
     * 
     * This method first invokes the <code>start()</code> method that
     * initilizes the Prolog engine, allocates a new <code>Thread</code> object, 
     * and start the execution.
     * And then it returns the <code>boolean</code> whose value is <code>next()</code>.
     * @see #start
     * @see #next
     */
    public synchronized boolean call() {
	start();
	return next();
    }

    /**
     * Returns <code>true</code> if the system succeeds to find a next solution
     * of the goal, <code>false</code> otherwise.<br>
     * 
     * This method first invokes the <code>cont()</code> method that
     * sets the <code>resultReady</code> to <code>false</code>
     * and wakes up all threads that are waiting on this object's monitor.
     * And then it returns the <code>boolean</code> whose value is <code>next()</code>.
     * @see #cont
     * @see #next
     */
    public synchronized boolean redo() {
	cont();
	return next();
    }

    /**
     * Is invoked when the system succeeds to find a solution.<br>
     * 
     * This method is invoked from the initial continuation goal
     * (a <code>Success</code> object).<br>
     * 
     * This method first sets the <code>resultReady</code> and <code>result</code> to <code>true</code>.
     * And then it wakes up all threads that are waiting by <code>notifyAll()</code>.
     * Finally, while the <code>thread</code> is not <code>null</code> and 
     * the <code>resultReady</code> is <code>true</code>, 
     * it waits until another thread invokes the <code>notify()</code> method 
     * or the <code>notifyAll()</code> method for this object.
     * @see #resultReady
     * @see #result
     * @see #thread
     */
    protected synchronized void success() {
	resultReady = true;
	result = true;
	notifyAll();
	while (thread != null && resultReady) {
	    try {
		wait();
	    } catch (InterruptedException e) {}
	}
    }

    /**
     * Is invoked after failure of all trials.<br>
     * 
     * This method is invoked from the <code>run</code> method.<br>
     * 
     * This method first sets the <code>resultReady</code> and <code>result</code> 
     * to <code>true</code> and <code>false</code> respectively.
     * And then it wakes up all threads that are waiting by <code>notifyAll()</code>.
     * Finally, while the <code>thread</code> is not <code>null</code> and 
     * the <code>resultReady</code> is <code>true</code>, 
     * it waits until another thread invokes the <code>notify()</code> method 
     * or the <code>notifyAll()</code> method for this object.
     * @see #resultReady
     * @see #result
     * @see #thread
     */
    protected synchronized void fail() {
	resultReady = true;
	result = false;
	notifyAll();
	while (thread != null && resultReady) {
	    try {
		wait();
	    } catch (InterruptedException e) {}
	}
    }

    /** Waits for this thread to die. */
    public synchronized void join() {
	while (thread != null && ! resultReady) {
	    try {
		wait();
	    } catch (InterruptedException e) {}
	}
	stop();
    }

    /** 
     * Forces the thread to stop.<br>
     *
     * This method first sets the <code>resultReady</code> and <code>thread</code> 
     * to <code>false</code> and <code>null</code> respectively.
     * And then it wakes up all threads that are waiting by <code>notifyAll()</code>.
     * @see #resultReady
     * @see #thread
     */
    public synchronized void stop() {
	resultReady = false;
	thread = null;
	notifyAll();
    }

    /**
     * Forces the thread to start the execution.<br>
     *
     * This method initilizes the Prolog engine by invoking <code>engine.init()</code>,
     * allocates a new <code>Thread</code> object, and start the execution.
     * The Java Virtual Machine calls the <code>run</code> method of this thread.
     * @see #run
     */
    public synchronized void start() {
	resultReady = false;
	engine.init();
	thread = new Thread(this);
	thread.start();
    }

    /**
     * Forces the thread to continue the execution.<br>
     * 
     * This method sets the <code>resultReady</code> to <code>false</code>,
     * and then wakes up all threads that are waiting by <code>notifyAll()</code>.
     * @see #resultReady
     */
    public synchronized void cont() {
	resultReady = false;
	notifyAll();
    }

    /** 
     * Returns <code>true</code> if the result of goal is ready,
     * <code>false</code> otherwise.
     * @return a <code>boolean</code> whose value is <code>resultReady</code>.
     * @see #resultReady
     */
    public synchronized boolean ready() {
	return resultReady;
    }

    /** 
     * Returns <code>true</code> if the result of goal is ready and true, otherwise <code>false</code>.
     * @return a <code>boolean</code> whose value is <code>(ready() &amp;&amp; result)</code>.
     * @see #ready
     * @see #result
     */
    public synchronized boolean in_success() {
	return ready() && result;
    }

    /** 
     * Returns <code>true</code> if the result of goal is ready and false, otherwise <code>false</code>.
     * @return a <code>boolean</code> whose value is <code>(ready() &amp;&amp; !result)</code>.
     * @see #ready
     * @see #result
     */
    public synchronized boolean in_failure() {
	return ready() && ! result;
    }

    /**
     * Wait until the system finds a next solution,
     * and then returns the result as <code>boolean</code>.<br>
     *
     * This method first waits until another thread invokes the <code>notify()</code>
     * method or the <code>notifyAll()</code> method for this object, 
     * while the <code>thread</code> is not <code>null</code> and 
     * the <code>resultReady</code> is <code>false</code>.
     * And then invokes the <code>stop()</code> if the <code>result</code> is <code>false</code>.
     * Finally, returns the <code>result</code>.
     * @see #resultReady
     * @see #result
     * @see #thread
     */
    public synchronized boolean next() {
	while (thread != null && ! resultReady) {
	    try {
		wait();
	    } catch (InterruptedException e) {}
	}
	if (! result) {
	    stop();
	}
	return result;
    }

    /**
     * Executes the goal.<br>
     *
     * Every time finding a solution, the <code>success</code> method is invoked.
     * And then the <code>fail</code> method is invoked after failure of all trials.
     * Finally, the <code>stop</code> method is invoked at the end of this <code>run</code>.
     * @see #success
     * @see #fail
     * @see #stop
     */
    public void run() {
	try {
	    main_loop:while(true) {
		while (engine.exceptionRaised == 0) {
		    if (thread == null)
			break main_loop;
		    code = code.exec(engine);
		}
		switch (engine.exceptionRaised) {
		case 1:  // halt/0
		    break main_loop;
		case 2:  // freeze/2
		    throw new SystemException("freeze/2 is not supported yet");
		    // Do something here
		    // engine.exceptionRaised = 0 ;
		    // break;
		default:
		    throw new SystemException("Invalid value of exceptionRaised");
		}
	    }
	} catch (PrologException e){
	    if (engine.getPrintStackTrace().equals("on"))
		e.printStackTrace();
	    else 
		System.out.println(e.toString());
	} catch (Exception e){
	    e.printStackTrace();
	}
	stop();
    }
}
