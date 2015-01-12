package nars.jprolog.lang;

/**
 * Closure.<br>
 * The class <code>ClosureTerm</code> represents a closure structure.<br>
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class ClosureTerm extends Term {
    /** Holds a <code>Predicate</code> object that represents a Prolog goal. */
    protected Predicate code;

    /** Constructs a new closure structure for the given Prolog goal. */
    public ClosureTerm(Predicate _code) { code = _code; }

    /** Returns the Prolog goal of this <code>ClosureTerm</code>. */
    public Predicate getCode() { return code; }

    /* Term */
    public boolean unify(Term t, Trail trail) {
	//	t = t.dereference();
	if (t.isVariable())
	    return ((VariableTerm)t).unify(this, trail);
	if (! t.isClosure())
	    return false;
	return code.equals(((ClosureTerm)t).code);
    }

    public String toQuotedString() { return toString(); }

    /* Object */
    /**
     * Checks <em>term equality</em> of two terms.
     * The result is <code>true</code> if and only if the argument is an instance of
     * <code>ClosureTerm</code>, and 
     * the pairs of goals in the two closures are <em>equal</em>.
     * @param obj the object to compare with. This must be dereferenced.
     * @return <code>true</code> if the given object represents a closure
     * equivalent to this <code>ClosureTerm</code>, false otherwise.
     * @see #compareTo
     */
    public boolean equals(Object obj) { // obj must be dereferenced
	if (! (obj instanceof ClosureTerm))
	    return false;
	return code.equals(((ClosureTerm)obj).code);
    }

    public int hashCode() {
	return code.hashCode();
    }

    /** Returns a string representation of this <code>ClosureTerm</code>. */
    public String toString() {
	return "closure[" + code.toString() + "]";
    }

    /* Comparable */
    /** 
     * Compares two terms in <em>Prolog standard order of terms</em>.<br>
     * It is noted that <code>t1.compareTo(t2) == 0</code> has the same
     * <code>boolean</code> value as <code>t1.equals(t2)</code>.
     * @param anotherTerm the term to compared with. It must be dereferenced.
     * @return the value <code>0</code> if two terms are identical; 
     * a value less than <code>0</code> if this term is <em>before</em> the <code>anotherTerm</code>;
     * and a value greater than <code>0</code> if this term is <em>after</em> the <code>anotherTerm</code>.
     */
    public int compareTo(Term anotherTerm) { // anotherTerm must be dereferenced
	if (! anotherTerm.isClosure())
	    return AFTER;
	if (code.equals(((ClosureTerm) anotherTerm).code))
	    return EQUAL;
	return code.hashCode() - ((ClosureTerm) anotherTerm).code.hashCode(); //???
    }
}
