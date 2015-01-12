package nars.jprolog.lang;

import nars.jprolog.Prolog;

import java.util.Vector;
/**
 * List.<br>
 * The class <code>ListTerm</code> represents a list structure.<br>
 * 
 * <pre>
 *  % [1,2]
 *  Term Nil = SymbolTerm.makeSymbol("[]");
 *  Term  n1 = IntegerTerm(1);
 *  Term  n2 = IntegerTerm(2);
 *  Term   t = new ListTerm(n1, new ListTerm(n2, Nil));
 *  
 *  Term car = ((ListTerm)t).car();
 *  Term cdr = ((ListTerm)t).cdr();
 * </pre>
 *
 * Here is sample program for creating a list from <code>1</code> to <code>n</code>.
 * <pre>
 * public static Term makeList(int n) {
 *   Term t = SymbolTerm.makeSymbol("[]");
 *   for (int i=n; i>0; i--) {
 *     t = new ListTerm(new IntegerTerm(i), t);
 *   }
 *   return t;
 * }
 * </pre>
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class ListTerm extends Term {
    /** A functor <code>'.' /2</code>. */
    protected static SymbolTerm SYM_DOT = SymbolTerm.makeSymbol(".", 2);

    /** Holds the first element of this <code>ListTerm</code>. */
    protected Term car;

    /**
     * Holds the list consisting of all the rest of the elements of 
     * this <code>ListTerm</code> but the first one.
     */
    protected Term cdr;

    /**
     * Constructs a new Prolog list structure
     * such that <code>_car</code> is the first element of this list, and 
     * <code>_cdr</code> is the list consisting of all the rest of the 
     * elements of this list but the first one.
     */
    public ListTerm(Term _car, Term _cdr) { 
	car = _car;
	cdr = _cdr; 
    }

    /** Returns the value of <code>car</code>.
     * @see #car
     */
    public Term car() { return car; }

    /** Returns the value of <code>cdr</code>.
     * @see #cdr
     */
    public Term cdr() { return cdr; }

    /** Sets the value to <code>car</code>.
     * @see #car
     */
    public void setCar(Term t) { car = t; }

    /** Sets the value to <code>cdr</code>.
     * @see #cdr
     */
    public void setCdr(Term t) { cdr = t; }

    /* Term */
    public boolean unify(Term t, Trail trail) {
	t = t.dereference();
	if (t.isVariable()) {
	    ((VariableTerm) t).bind(this, trail);
	    return true;
	}
	if (! t.isList())
	    return false;
	return car.unify(((ListTerm)t).car(), trail) 
	    && cdr.unify(((ListTerm)t).cdr(), trail);
    }

    /** 
     * @return the <code>boolean</code> whose value is
     * <code>convertible(Vector.class, type)</code>.
     * @see Term#convertible(Class, Class)
     */
    public boolean convertible(Class type) { 
	return convertible(Vector.class, type); 
    }

    public Term copy(Prolog engine) { 
	return new ListTerm(car.copy(engine), cdr.copy(engine)); 
    }

    public boolean isGround() {
	if (! car.isGround())
	    return false;
	if (! cdr.isGround())
	    return false;
	return true;
    }

    /** Returns the length of this <code>ListTerm</code>. */
    public int length() {
	int count = 0;
	Term t = this;
	while(t.isList()) {
	    count++;
	    t = ((ListTerm)t).cdr().dereference();
	}
	return count;
    }

    /** 
     * Returns a <code>java.other.Vector</code> corresponds to this <code>ListTerm</code>
     * according to <em>Prolog Cafe interoperability with Java</em>.
     * @return a <code>java.other.Vector</code> object equivalent to
     * this <code>IntegerTerm</code>.
     */
    public Object toJava() { 
	Vector<Object> vec = new Vector<Object>();
	Term t = this;
	while(t.isList()) {
	    vec.addElement(((ListTerm)t).car().dereference().toJava());
	    t = ((ListTerm)t).cdr().dereference();  
	}
	return vec;	
    }

    public String toQuotedString() {
	Term x = this;
	String s = "[";
	for (;;) {
	    s += ((ListTerm)x).car.dereference().toQuotedString();
	    x  = ((ListTerm)x).cdr.dereference();
	    if (! x.isList())
		break;
	    s += ",";
	}
	if (! x.isNil())
	    s += "|" + x.toQuotedString();
	s += "]";
	return s;
    }

    /* Object */
    /**
     * Checks <em>term equality</em> of two terms.
     * The result is <code>true</code> if and only if the argument is an instance of
     * <code>ListTerm</code>, and 
     * all corresponding pairs of elements in the two lists are <em>term-equal</em>.
     * @param obj the object to compare with. This must be dereferenced.
     * @return <code>true</code> if the given object represents a Prolog list
     * equivalent to this <code>ListTerm</code>, false otherwise.
     * @see #compareTo
     */
    public boolean equals(Object obj) {
    	if (! (obj instanceof ListTerm))
    	    return false;
	return car.equals(((ListTerm)obj).car().dereference()) 
	    && cdr.equals(((ListTerm)obj).cdr().dereference());
    }

    public int hashCode() {
	int h = 1;
	h = 31*h + SYM_DOT.hashCode();
	h = 31*h + car.dereference().hashCode();
	h = 31*h + cdr.dereference().hashCode();
	return h;
    }

    /** Returns a string representation of this <code>ListTerm</code>. */
    public String toString() {
	Term x = this;	
	String s = "[";
	for (;;) {
	    s += ((ListTerm)x).car.dereference().toString();
	    x  = ((ListTerm)x).cdr.dereference();
	    if (! x.isList())
		break;
	    s += ",";
	}
	if (! x.isNil())
	    s += "|" + x.toString();
	s += "]";
	return s;
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
    public int compareTo(Term anotherTerm) { // anotherTerm must be dereferenced.
	if (anotherTerm.isVariable() || anotherTerm.isNumber() || anotherTerm.isSymbol())
	    return AFTER;
	if (anotherTerm.isStructure()) {
	    int arity = ((StructureTerm)anotherTerm).arity();
	    if (2 != arity)
		return (2 - arity);
	    SymbolTerm functor = ((StructureTerm)anotherTerm).functor();
	    if (! SYM_DOT.equals(functor))
		return SYM_DOT.compareTo(functor);
	}
	Term[] args = new Term[2];
	if (anotherTerm.isList()) {
	    args[0] = ((ListTerm)anotherTerm).car();
	    args[1] = ((ListTerm)anotherTerm).cdr();
	} else if (anotherTerm.isStructure()) {
	    args = ((StructureTerm)anotherTerm).args();
	} else {
	    return BEFORE;
	}
	Term tmp = car;
	int rc;
	for (int i=0; i<2; i++) {
	    rc = tmp.compareTo(args[i].dereference());
	    if (rc != EQUAL) 
		return rc;
	    tmp = cdr;
	}
	return EQUAL;
    }
}
