package nars.jprolog.lang;

import nars.jprolog.Prolog;

/**
 * Compound term. <br>
 * The <code>StructureTerm</code> class represents a compound term but list.<br>
 *
 * <pre>
 *  % father(abraham, X)
 *  Term a1 = SymbolTerm.makeSymbol("abraham");
 *  Term a2 = new VariableTerm();
 *  Term[] a3 = {a1, a2};
 *  Term a4 = SymbolTerm.makeSymbol("father", 2);
 *  Term  t = new StructureTerm(a4, a3);
 *  
 *  Term functor = ((StructureTerm)t).functor();
 *  Term[]  args = ((StructureTerm)t).args();
 *  int    arity = ((StructureTerm)t).arity();
 * </pre>
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class StructureTerm extends Term {
    /** Holds the functor symbol of this <code>StructureTerm</code>. */
    protected SymbolTerm functor;

    /** Holds the argument terms of this <code>StructureTerm</code>. */
    protected Term[] args;

    /** Holds the arity of this <code>StructureTerm</code>. */
    protected int arity;

    /**
     * Constructs a new Prolog compound term
     * such that <code>_functor</code> is the functor symbol, and 
     * <code>_args</code> is the argument terms respectively.
     */
    public StructureTerm(SymbolTerm _functor, Term[] _args){
	functor = _functor;
	arity = functor.arity();
	args = _args;
	if (arity != args.length)
	    throw new InternalException("Invalid argument length in StructureTerm");
    }

    /** Returns the functor symbol of this <code>StructureTerm</code>.
     * @return the value of <code>functor</code>.
     * @see #functor
     */
    public SymbolTerm functor(){ return functor; }

    /** Returns the arity of this <code>StructureTerm</code>.
     * @return the value of <code>arity</code>.
     * @see #arity
     */
    public int arity(){ return arity; }

    /** Returns the argument terms of this <code>StructureTerm</code>.
     * @return the value of <code>args</code>.
     * @see #args
     */
    public Term[] args(){ return args; }

    /** Returns the string representation of functor symbol of this <code>StructureTerm</code>.
     * @return a <code>String</code> whose value is <code>functor.name()</code>.
     * @see #functor
     * @see SymbolTerm#name
     */
    public String name(){ return functor.name(); }

    /* Term
    public boolean unify(Term t, Trail trail) {
	if (t.isVariable())
	    return t.unify(this, trail);
	if (! t.isStructure())
	    return false;
	if (! functor.equals(((StructureTerm)t).functor()))
	    return false;
	for (int i=0; i<arity; i++) {
	    if (! args[i].unify(((StructureTerm)t).args[i], trail))
		return false;
	}
	return true;
    } */


    public boolean unify(Term t, Trail trail) {
	t = t.dereference();
	if (t.isVariable()) {
	    ((VariableTerm) t).bind(this, trail);
	    return true;
	}
	if (! t.isStructure())
	    return false;
	if (! functor.equals(((StructureTerm)t).functor()))
	    return false;
	for (int i=0; i<arity; i++) {
	    if (! args[i].unify(((StructureTerm)t).args[i], trail))
		return false;
	}
	return true;
    }

    //    public boolean unify(Term t, Trail trail) {
    //	return trail.engine.unify(this, t);
    //    }


    public Term copy(Prolog engine) {
	Term[] a = new Term[arity];
	for (int i=0; i<arity; i++)
	    a[i] = args[i].copy(engine);
	return new StructureTerm(functor, a);
    }

    public boolean isGround() {
	for (int i=0; i<arity; i++) {
	    if (! args[i].isGround())
		return false;
	}
	return true;
    }

    public String toQuotedString() {
	String delim = "";
	String s = functor.toQuotedString() + "(";
	for (int i=0; i<arity; i++) {
	    s += delim + args[i].toQuotedString();
	    delim = ",";
	}
	s += ")";
	return s;
    }

    /* Object */
    /**
     * Checks <em>term equality</em> of two terms.
     * The result is <code>true</code> if and only if the argument is an instance of
     * <code>StructureTerm</code>, has the same functor symbol and arity, and
     * all corresponding pairs of arguments in the two compound terms are <em>term-equal</em>.
     * @param obj the object to compare with. This must be dereferenced.
     * @return <code>true</code> if the given object represents a Prolog compound term
     * equivalent to this <code>StructureTerm</code>, false otherwise.
     * @see #compareTo
     */
    public boolean equals(Object obj) {
	if (! (obj instanceof StructureTerm))
	    return false;
	if (! functor.equals(((StructureTerm)obj).functor()))
	    return false;
	for (int i=0; i<arity; i++) {
	    if (! args[i].equals(((StructureTerm)obj).args[i].dereference()))
		return false;
	}
	return true;
    }

    public int hashCode() {
	int h = 1;
	h = 31*h + functor.hashCode();
	for(int i=0; i<arity; i++)
	    h = 31*h + args[i].dereference().hashCode();
	return h;
    }

    /** Returns a string representation of this <code>StructureTerm</code>. */
    public String toString() {
	String delim = "";
	String s = functor.toString() + "(";
	for (int i=0; i<arity; i++) {
	    s += delim + args[i].toString();
	    delim = ",";
	}
	s += ")";
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
	SymbolTerm functor2;
	Term[] args2;
	int arity2, rc;

	if (anotherTerm.isVariable() || anotherTerm.isNumber() || anotherTerm.isSymbol())
	    return AFTER;
	if (anotherTerm.isList()) {
	    functor2 = ListTerm.SYM_DOT;
	    args2    = new Term[2];
	    args2[0] = ((ListTerm)anotherTerm).car();
	    args2[1] = ((ListTerm)anotherTerm).cdr();
	    arity2   = 2;
	} else if (anotherTerm.isStructure()) {
	    functor2 = ((StructureTerm)anotherTerm).functor();
	    args2    = ((StructureTerm)anotherTerm).args();
	    arity2   = ((StructureTerm)anotherTerm).arity();
	} else {
	    return BEFORE;
	}
	if (arity != arity2)
	    return (arity - arity2);
	if (! functor.equals(functor2))
	    return functor.compareTo(functor2);
	for (int i=0; i<arity; i++) {
	    rc = args[i].compareTo(args2[i].dereference());
	    if (rc != EQUAL) 
		return rc;
	}
	return EQUAL;
    }
}
