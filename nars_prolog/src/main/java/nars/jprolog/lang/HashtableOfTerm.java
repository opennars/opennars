package nars.jprolog.lang;
import java.util.Hashtable;
/**
 * <code>Hashtable&lt;Term,Term&gt;</code>.<br>
 * <font color="red">This document is under construction.</font>
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class HashtableOfTerm extends Hashtable<Term,Term> {
    public HashtableOfTerm() { 
	super(); 
    }
    public HashtableOfTerm(int initialCapacity) { 
	super(initialCapacity);
    }
    public HashtableOfTerm(int initialCapacity, float loadFactor) {
	super(initialCapacity, loadFactor);
    }
}