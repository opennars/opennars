package nars.jprolog.lang;

import nars.jprolog.Prolog;

import java.io.Serializable;
/**
 * Trail stack.<br>
 * The class <code>Trail</code> represents a trail stack.<br>
 * Entries pushed to this trail stack must implement
 * the <code>Undoable</code> interface.
 * @see Undoable
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class Trail implements Serializable {
    /** Maximum size of enties. Initial size is <code>20000</code>. */
    protected int maxContents = 20000;

    /** An array of <code>Undoable</code> entries. */
    protected Undoable[] buffer;

    /** the top index of this <code>Trail</code>. */
    protected int top;

    /** Holds the Prolog engine that this <code>Trail</code> belongs to. */
    protected Prolog engine;
	
    /** Constructs a new trail stack. */
    public Trail(Prolog _engine) {
	engine = _engine;
	buffer = new Undoable[maxContents];
	top = -1;
    }

    /** Constructs a new trail stack with the given size. */
    public Trail(Prolog _engine, int n) {
	engine = _engine;
	maxContents = n;
	buffer = new Undoable[maxContents];
	top = -1;
    }

    /** Discards all entries. */
    public void init() { deleteAll(); }

    /** Pushs an entry to this <code>Trail</code>. */
    public void push(Undoable t) {
	try {
	    buffer[++top] = t;
	} catch (ArrayIndexOutOfBoundsException e) {
	    System.out.println("{expanding trail...}");
	    int len = buffer.length;
	    Undoable[] new_buffer = new Undoable[len+20000];
	    for(int i=0; i<len; i++){
		new_buffer[i] = buffer[i];
	    }
	    buffer = new_buffer;
	    buffer[top] = t;
	    maxContents = len+20000;
	}
    }

    /** Pops an entry from this <code>Trail</code>. */
    public Undoable pop() {
	Undoable t = buffer[top];
	buffer[top--] = null;
	return t;
    }

    /** Discards all entries. */
    protected void deleteAll() {
	while (! empty()) {
	    buffer[top--] = null;
	}	
    }

    /** Tests if this stack has no entry. */
    public boolean empty() {
	return top == -1;
    }

    /** Returns the value of <code>maxContents</code>. 
     * @see #maxContents
     */
    public int max() { return maxContents; }

    /** Returns the value of <code>top</code>. 
     * @see #top
     */
    public int top() { return top; }

    /** Unwinds all entries after the value of <code>i</code>. */
    public void unwind(int i) {
	Undoable t;
	while (top > i) {
	    t = pop();
	    t.undo();
	}
    }

    /** Shows the contents of this <code>Trail</code>. */
    public void show() {
	if (empty()) {
	    System.out.println("{trail stack is empty!}");
	    return;
	}
	for (int i=0; i<=top; i++) {
	    System.out.print("trail[" + i + "]: ");
	    System.out.println(buffer[i]);
	}
    }
}

