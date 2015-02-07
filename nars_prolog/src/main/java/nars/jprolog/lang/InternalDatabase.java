package nars.jprolog.lang;
import java.io.Serializable;
import java.util.LinkedList;
/**
 * Internal database for dynamic predicates.<br>
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.1
 */
public class InternalDatabase implements Serializable {
    /** Maximum size of enties. Initial size is <code>100000</code>. */
    protected int maxContents = 100000;

    /** An array of <code>Term</code> entries. */
    protected Term[] buffer;

    /* For GC */
    /** A list of reusable entry indices. */
    protected LinkedList<Integer> reusableIndices = new LinkedList<>();

    /** the top index of this <code>InternalDatabase</code>. */
    protected int top;

    /** Constructs a new internal dababase. */
    public InternalDatabase() {
	buffer = new Term[maxContents];
	top = -1;
    }

    /** Constructs a new internal dababase with the given size. */
    public InternalDatabase(int n) {
	maxContents = n;
	buffer = new Term[maxContents];
	top = -1;
    }

    /** Discards all entries. */
    public void init() { eraseAll(); }

    /** Inserts an entry to this <code>InternalDatabase</code>. */
    public int insert(Term t) {
	try {
	    if (reusableIndices.isEmpty()) {
		buffer[++top] = t;
		return top;
	    } else {
		int i = reusableIndices.remove();
		//		System.out.println("Reuse " + i);
		buffer[i] = t;
		return i;
	    }
	} catch (ArrayIndexOutOfBoundsException e) {
	    System.out.println("{expanding internal database...}");
	    int len = buffer.length;
	    Term[] new_buffer = new Term[len+10000];
		System.arraycopy(buffer, 0, new_buffer, 0, len);
	    buffer = new_buffer;
	    buffer[top] = t;
	    maxContents = len+20000;
	    return top;
	}
    }

    /** Returns an entry with the given index from this <code>InternalDatabase</code>. */
    public Term get(int i) {
	return buffer[i];
    }

    /** Erases an entry with the given index from this <code>InternalDatabase</code>. */
    public Term erase(int i) {
	Term t = buffer[i];
	buffer[i] = null;
	//	System.out.println("add Reuse index" + i);
	reusableIndices.add(i);
	return t;
    }

    /** Discards all entries. */
    protected void eraseAll() {
	while (! empty()) {
	    buffer[top--] = null;
	}	
    }

    /** Tests if this has no entry. */
    public boolean empty() {
	return top == -1;
    }

    /** Returns the value of <code>top</code>. 
     * @see #top
     */
    public int top() { return top; }

    /** Shows the contents of this <code>InternalDatabase</code>. */
    public void show() {
	if (empty())
	    System.out.println("{internal database is empty!}");
	System.out.println("{reusable indices: " + reusableIndices.toString() + '}');
	for (int i=0; i<=top; i++) {
	    System.out.print("internal database[" + i + "]: ");
	    System.out.println(buffer[i]);
	}
    }
}
