package nars.jprolog.lang;
import java.io.Serializable;
/**
 * Push down List.<br>
 * The class <code>PushDownList</code> represents a push down list.<br>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.1
 */
public class PushDownList implements Serializable {
    /** Maximum size of enties. Initial size is <code>10000</code>. */
    protected int maxContents = 1000;

    /** An array of <code>Term</code> entries. */
    protected Term[] buffer;

    /** the top index of this <code>PushDownList</code>. */
    protected int top;

    /** Constructs a new pdl. */
    public PushDownList() {
	buffer = new Term[maxContents];
	top = -1;
    }

    /** Constructs a new pdl with the given size. */
    public PushDownList(int n) {
	maxContents = n;
	buffer = new Term[maxContents];
	top = -1;
    }

    /** Discards all entries. */
    public void init() { deleteAll(); }

    /** Pushs an entry to this <code>PushDownList</code>. */
    public void push(Term t) {
	try {
	    buffer[++top] = t;
	} catch (ArrayIndexOutOfBoundsException e) {
	    System.out.println("{expanding pdl...}");
	    int len = buffer.length;
	    Term[] new_buffer = new Term[len+10000];
	    for(int i=0; i<len; i++){
		new_buffer[i] = buffer[i];
	    }
	    buffer = new_buffer;
	    buffer[top] = t;
	    maxContents = len+10000;
	}
    }

    /** Pops an entry from this <code>PushDownList</code>. */
    public Term pop() {
	Term t = buffer[top];
	buffer[top--] = null;
	return t;
    }

    /** Discards all entries. */
    protected void deleteAll() {
	while (! empty()) {
	    buffer[top--] = null;
	}	
    }

    /** Tests if this pdl has no entry. */
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

    /** Shows the contents of this <code>PushDownList</code>. */
    public void show() {
	if (empty()) {
	    System.out.println("{pdl is empty!}");
	    return;
	}
	for (int i=0; i<=top; i++) {
	    System.out.print("pdl[" + i + "]: ");
	    System.out.println(buffer[i]);
	}
    }
}

