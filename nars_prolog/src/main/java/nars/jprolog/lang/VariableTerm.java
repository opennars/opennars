package nars.jprolog.lang;

import nars.jprolog.Prolog;

/**
 * Variable.<br>
 * The <code>VariableTerm</code> class represents a logical variable.<br>
 * For example,
 * <pre>
 *   Term t = new VariableTerm();
 * </pre>
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class VariableTerm extends Term implements Undoable {
    /** Holds a term to which this variable is bound. Initial value is <code>this</code> (self-reference). */
    protected Term val;
    /** A CPF time stamp when this object is newly constructed. */
    protected long timeStamp;

    /** Constructs a new logical variable so that
     * the <code>timeStamp</code> field is set to <code>Long.MIN_VALUE</code>.
     */
    public VariableTerm() {
	val = this;
    	timeStamp = Long.MIN_VALUE;
    }

    /** Constructs a new logical variable so that
     * the <code>timeStamp</code> field is set to the current value of
     * <code>CPFTimeStamp</code> of the specified Prolog engine.
     * @param engine Current Prolog engine.
     * @see Prolog#getCPFTimeStamp
     */
    public VariableTerm(Prolog engine) {
	val = this;
	timeStamp = engine.getCPFTimeStamp();
    }

    /** 
     * Returns the value of <code>timeStamp</code>.
     * @see #timeStamp
     */
    public long timeStamp() { return timeStamp; }

    /** Returns a string representation of this object.*/
    protected String name() { return "_" + Integer.toHexString(hashCode()).toUpperCase(); }

    /* Term */
    /** 
     * Checks whether the argument term is unified with this one.
     * If this is an unbound variable, the <code>unify</code> method binds this to 
     * the dereferenced value of argument term: <code>bind(t.dereference(), trail)</code>,
     * and returns <code>true</code>.
     * Otherwise, it returns a <code>boolean</code> whose value is <code>val.unify(t, trail)</code>.
     * @param t the term to be unified with.
     * @param trail Trail Stack.
     * @return <code>true</code> if succeeds, otherwise <code>false</code>.
     * @see #val
     * @see #bind(Term,Trail)
     * @see Trail
     */
    public boolean unify(Term t, Trail trail) {
	if (val != this)
	    return val.unify(t, trail);
	t = t.dereference();
	if (this != t)
	    bind(t, trail);
	return true;
    }

    /** 
     * Binds this variable to a given term. 
     * And pushs this variable to trail stack if necessary. 
     * @param t a term to be bound.
     * @param trail Trail Stack
     * @see Trail
     */
    public void bind(Term t, Trail trail) {
	if (t.isVariable() && ((VariableTerm)t).timeStamp() >= this.timeStamp) {
	    ((VariableTerm)t).val = this;
	    if (((VariableTerm)t).timeStamp() < trail.engine.stack.getTimeStamp())
		trail.push((VariableTerm)t);
	} else {
	    this.val = t;
	    if (this.timeStamp() < trail.engine.stack.getTimeStamp())
		trail.push(this);
	}
    }

    /** 
     * Checks whether this object is convertible with the given Java class type 
     * if this variable is unbound.
     * Otherwise, returns the value of <code>val.convertible(type)</code>.
     * @param type the Java class type to compare with.
     * @return <code>true</code> if this (or dereferenced term) is 
     * convertible with <code>type</code>. Otherwise <code>false</code>.
     * @see #val
     */
    public boolean convertible(Class type) {
	if (val != this)
	    return val.convertible(type);
	return convertible(this.getClass(), type);
    }

    /** 
     * Returns a copy of this object if unbound variable.
     * Otherwise, returns the value of <code>val.copy(engine)</code>.
     * @see #val
     */
    public Term copy(Prolog engine) {
	VariableTerm co;
	if (val != this)
	    return val.copy(engine);
	co = engine.copyHash.get(this);
	if (co == null) {
	    //	    co = new VariableTerm(engine);
	    co = new VariableTerm();
	    engine.copyHash.put(this, co);
	}
	return co;
    }

    public Term dereference() {
	if (val == this)
	    return this;
	return val.dereference();
    }

    public boolean isGround() {
	if (val != this)
	    return val.isGround();
	return false;
    }

    /** 
     * Returns <code>this</code> if this variable is unbound.
     * Otherwise, returns a Java object that corresponds to the dereferenced term:
     * <code>val.toJava()</code>.
     * @return a Java object defined in <em>Prolog Cafe interoperability with Java</em>.
     * @see #val
     */
    public Object toJava() { 
	if (val != this)
	    return val.toJava();
	return this;
    }

    /**
     * Returns a quoted string representation of this term if unbound.
     * Otherwise, returns the value of dereferenced term:
     * <code>val.toQuotedString()</code>
     * @see #val
     */
    public String toQuotedString() {
	if (val != this)
	    return val.toQuotedString();
	return name();
    }

    /* Object */
    /**
     * Checks <em>term equality</em> of two terms.
     * This method returns a <code>boolean</code> whose value is
     * (<code>this == obj</code>) if this variable is unbound.
     * Otherwise, it returns the value of <code>val.equals(obj)</code>.
     * @param obj the object to compare with. This must be dereferenced.
     * @return <code>true</code> if this (or dereferenced term) is the same as the argument;
     * <code>false</code> otherwise.
     * @see #val
     * @see #compareTo
    */
    public boolean equals(Object obj) {
	if(val != this)
	    return val.equals(obj);
	if (! (obj instanceof VariableTerm)) // ???
	    return false; //???
	return this == obj;
    }

    /**
     * Returns a string representation of this term if unbound.
     * Otherwise, returns the value of dereferenced term:
     * <code>val.toString()</code>
     * @see #val
     */
    public String toString() {
	if (val != this)
	    return val.toString();
	return name();
    }

    /* Undoable */
    public void undo() { val = this; }

    /* Comparable */
    /** 
     * Compares two terms in <em>Prolog standard order of terms</em>.<br>
     * It is noted that <code>t1.compareTo(t2) == 0</code> has the same
     * <code>boolean</code> value as <code>t1.equals(t2)</code>.
     * @param anotherTerm the term to compare with. It must be dereferenced.
     * @return the value <code>0</code> if two terms are identical; 
     * a value less than <code>0</code> if this term is <em>before</em> the <code>anotherTerm</code>;
     * and a value greater than <code>0</code> if this term is <em>after</em> the <code>anotherTerm</code>.
     */
    public int compareTo(Term anotherTerm) { // anotherTerm must be dereferenced.
	if(val != this)
	    return val.compareTo(anotherTerm);
	if (! anotherTerm.isVariable())
	    return BEFORE;
	if (this == anotherTerm) 
	    return EQUAL;
	int x = this.hashCode() - anotherTerm.hashCode();
	if (x != 0)
	    return x;
	throw new InternalException("VariableTerm is not unique");
    }
}
