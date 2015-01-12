package nars.jprolog.lang;

import nars.jprolog.Prolog;

import java.io.Serializable;
/**
 * Choice point frame.<br>
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
class CPFEntry implements Serializable {
    public long timeStamp;
    public Term[] args;     // argument register
    public Predicate cont;  // continuation goal
    public Predicate bp;    // next cluase
    public int tr;          // trail pointer
    public int b0;          // cut point

    protected CPFEntry(Term[] args, Predicate cont){
	this.args = args;
	this.cont = cont;
    }

    public String toString() {
	String t = " time:" + timeStamp + "\n" ;
	t = t + "args:";
	for (int i=0; i<args.length; i++) {
	    t = t + args[i] + " ";
	}
	t = t + "\n";
	t = t + " cont:" + cont + "\n";
	t = t + " bp:" + bp + "\n";
	t = t + " tr:" + tr + "\n";
	t = t + " b0:" + b0 + "\n";
	return t;
    }
}

/**
 * Choice point frame stack.<br>
 * The <code>CPFStack</code> class represents a stack
 * of choice point frames.<br>
 * Each choice point frame has the following fields:
 * <ul>
 * <li><em>arguments</em>
 * <li><em>continuation goal</em>
 * <li><em>next clause</em>
 * <li><em>trail pointer</em>
 * <li><em>cut point</em>
 * <li><em>time stamp</em>
 * </ul>
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class CPFStack implements Serializable {
    /** Maximum size of enties. Initial size is <code>20000</code>. */
    protected int maxContents = 20000;

    /** An array of choice point frames. */
    protected CPFEntry[] buffer;

    /** the top index of this <code>CPFStack</code>. */
    protected int top;

    /** Holds the Prolog engine that this <code>CPFStack</code> belongs to. */
    protected Prolog engine;
	
    /** Constructs a new choice point frame stack. */
    public CPFStack(Prolog _engine) {
	engine = _engine;
	buffer = new CPFEntry[maxContents];
	top = -1;
    }

    /** Constructs a new choice point frame stack with the given size. */
    public CPFStack(Prolog _engine, int n) {
	engine = _engine;
	maxContents = n;
	buffer = new CPFEntry[maxContents];
	top = -1;
    }

    /** Create a new choice point frame.
     * @param args <em>arguments</em>
     * @param p a <em>continuation goal</em>
     */
    public void create(Term[] args, Predicate p){
	try {
	    buffer[++top] = new CPFEntry(args, p);
	} catch (ArrayIndexOutOfBoundsException e) {
	    System.out.println("{expanding choice point stack...}");
	    int len = buffer.length;
	    CPFEntry[] new_buffer = new CPFEntry[len+10000];
        System.arraycopy(buffer, 0, new_buffer, 0, len);
	    buffer = new_buffer;
	    buffer[top] = new CPFEntry(args, p);
	    maxContents = len+10000;
	}
    }

    /** Discards all choice points. */
    public void deleteAll() { 
	while (! empty()) {
	    buffer[top--] = null;
	}
    }

    /** Discards all choice points after the value of <code>i</code>. */
    public void cut(int i) {
	while (top > i) {
	    buffer[top--] = null;
	}
    }

    /** Discards the top of choice points. */
    public void delete() { buffer[top--] = null; }

    /** Discards all choice points. */
    public void init() { deleteAll(); }

    /** Tests if this stack has no entry. */
    public boolean empty() { return top == -1; }

    /** Returns the value of <code>top</code>. 
     * @see #top
     */
    public int top() { return top; }

    /** Returns the value of <code>maxContents</code>. 
     * @see #maxContents
     */
    public int max() { return maxContents; }

    /** Returns the <em>arguments</em> of current choice point frame. */
    public Term[] getArgs() { return buffer[top].args; }

    /** Returns the <em>continuation goal</em> of current choice point frame. */
    public Predicate getCont() { return buffer[top].cont; }

    /** Returns the <em>time stamp</em> of current choice point frame. */
    public long getTimeStamp() { return buffer[top].timeStamp; }
    /** Sets the <em>time stamp</em> of current choice point frame. */
    public void setTimeStamp(long t) { buffer[top].timeStamp = t; }
    
    /** Returns the <em>next clause</em> of current choice point frame. */
    public Predicate getBP() { return buffer[top].bp; }
    /** Sets the <em>next clause</em> of current choice point frame. */
    public void setBP(Predicate p) { buffer[top].bp = p; }

    /** Returns the <em>trail pointer</em> of current choice point frame. */
    public int getTR() { return buffer[top].tr; }
    /** Sets the <em>trail pointer</em> of current choice point frame. */
    public void setTR(int i) { buffer[top].tr = i; }

    /** Returns the <em>cut point</em> of current choice point frame. */
    public int getB0() { return buffer[top].b0; }
    /** Sets the <em>cut point</em> of current choice point frame. */
    public void setB0(int i) { buffer[top].b0 = i; }

    /** Shows the contents of this <code>CPFStack</code>. */
    public void show() {
	if (empty()) {
	    System.out.println("{choice point stack is empty!}");
	    return;
	}
	for (int i=0; i<=top; i++) {
	    System.out.print("stack[" + i + "]: ");
	    System.out.println(buffer[i]);
	}
    }
}
