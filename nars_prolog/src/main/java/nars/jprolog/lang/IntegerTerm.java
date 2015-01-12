package nars.jprolog.lang;
/**
 * Integer.<br>
 * The class <code>IntegerTerm</code> wraps a value of primitive type 
 * <code>int</code>. 
 * <pre>
 *   Term t = new IntegerTerm(100);
 *   int i = ((IntegerTerm)t).intValue();
 * </pre>
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class IntegerTerm extends NumberTerm {
    /** Holds an <code>int</code> value that this <code>IntegerTerm</code> represents. */
    protected int val;

    /** Constructs a new Prolog integer that represents the specified <code>int</code> value. */
    public IntegerTerm(int i) {	val = i; }

    /**
     * Constructs a new Prolog integer that represents integer value 
     * of specified <code>String</code> parameter.
     * @exception NumberFormatException
     * if the <code>String</code> does not contain a parsable integer.
     */
    public IntegerTerm(String i) {
	try {
	    val = Integer.parseInt(i);
	} catch (NumberFormatException e) {
	    throw e;
	}
    }

    /**
     * Returns the value of <code>val</code>.
     * @see #val
     */
    public int value() { return val; }

    /* Term */
    public boolean unify(Term t, Trail trail) {
	if (t.isVariable())
	    return t.unify(this, trail);
	if (! t.isInteger())
	    return false;
	else 
	    return this.val == ((IntegerTerm)t).value();
    }

    /** 
     * @return the <code>boolean</code> whose value is
     * <code>convertible(Integer.class, type)</code>.
     * @see Term#convertible(Class, Class)
     */
    public boolean convertible(Class type) { return convertible(Integer.class, type); }

    //    protected Term copy(Prolog engine) { return new IntegerTerm(val); }

    /** 
     * Returns a <code>java.lang.Integer</code> corresponds to this <code>IntegerTerm</code>
     * according to <em>Prolog Cafe interoperability with Java</em>.
     * @return a <code>java.lang.Integer</code> object equivalent to
     * this <code>IntegerTerm</code>.
     */
    public Object toJava() { return this.val; }

    /* Object */
    /** Returns a string representation of this <code>IntegerTerm</code>. */
    public String toString() { return Integer.toString(this.val); }

    /**
     * Checks <em>term equality</em> of two terms.
     * The result is <code>true</code> if and only if the argument is an instance of
     * <code>IntegerTerm</code> and has the same <code>int</code> value as this object.
     * @param obj the object to compare with. This must be dereferenced.
     * @return <code>true</code> if the given object represents a Prolog integer 
     * equivalent to this <code>IntegerTerm</code>, false otherwise.
     * @see #compareTo
    */
    public boolean equals(Object obj) {
	if (! (obj instanceof IntegerTerm))
	    return false;
	return this.val == ((IntegerTerm)obj).value();
    }

    public int hashCode() { return this.val; }

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
	if (anotherTerm.isVariable() || anotherTerm.isDouble())
	    return AFTER;
	if (! anotherTerm.isInteger())
	    return BEFORE;
	int v = ((IntegerTerm)anotherTerm).value();
	if (this.val == v)
	    return EQUAL;
	if (this.val > v)
	    return AFTER;
	return BEFORE;
    }

    /* NumberTerm */
    public int intValue() { return this.val; }

    public long longValue() { return (long)(this.val); }

    public double doubleValue() { return (double)(this.val); }

    public int arithCompareTo(NumberTerm t) {
	if (t.isDouble())
	    return - (t.arithCompareTo(this));
	int v = t.intValue();
	if (this.val == v)
	    return EQUAL;
	if (this.val > v)
	    return AFTER;
	return BEFORE;
    }

    public NumberTerm abs() { return new IntegerTerm(Math.abs(this.val)); }

    public NumberTerm acos() { return new DoubleTerm(Math.acos(this.doubleValue())); }

    public NumberTerm add(NumberTerm t) {
	if (t.isDouble())
	    return t.add(this);
	return new IntegerTerm(this.val + t.intValue());
    }

    /** 
     * @exception IllegalTypeException if the given argument
     * <code>NumberTerm</code> is a floating point number.
     */
    public NumberTerm and(NumberTerm t) {
	if (t.isDouble())
	    throw new IllegalTypeException("integer", t);
	return new IntegerTerm(this.val & t.intValue());
    }

    public NumberTerm asin() { return new DoubleTerm(Math.asin(this.doubleValue())); }

    public NumberTerm atan() { return new DoubleTerm(Math.atan(this.doubleValue())); }

    public NumberTerm ceil() { return this; }

    public NumberTerm cos() { return new DoubleTerm(Math.cos(this.doubleValue())); }

    /** 
     * @exception EvaluationException if the given argument
     * <code>NumberTerm</code> represents <coe>0</code>.
     */
    public NumberTerm divide(NumberTerm t) { 
	if (t.doubleValue() == 0)
	    throw new EvaluationException("zero_divisor");
	return new DoubleTerm(this.doubleValue() / t.doubleValue()); 
    }

    public NumberTerm exp() { return new DoubleTerm(Math.exp(this.doubleValue())); }

    public NumberTerm floatIntPart() { throw new IllegalTypeException("float", this); }

    public NumberTerm floatFractPart() { throw new IllegalTypeException("float", this); }

    public NumberTerm floor() { return this; }

    /** 
     * @exception IllegalTypeException if the given argument
     * <code>NumberTerm</code> is a floating point number.
     * @exception EvaluationException if the given argument
     * <code>NumberTerm</code> represents <coe>0</code>.
     */
    public NumberTerm intDivide(NumberTerm t) {
	if (t.isDouble())
	    throw new IllegalTypeException("integer", t);
	if (t.intValue() == 0)
	    throw new EvaluationException("zero_divisor");
	return new IntegerTerm(this.val / t.intValue());
    }

    /** 
     * @exception EvaluationException if this object represents <coe>0</code>.
     */
    public NumberTerm log() { 
	if (this.val == 0)
	    throw new EvaluationException("undefined");
	return new DoubleTerm(Math.log(this.doubleValue())); 
    }

    public NumberTerm max(NumberTerm t) {
	if (t.isDouble()) 
	    return t.max(this);
	return new IntegerTerm(Math.max(this.val, t.intValue()));
    }

    public NumberTerm min(NumberTerm t) {
	if (t.isDouble()) 
	    return t.min(this);
	return new IntegerTerm(Math.min(this.val, t.intValue()));
    }

    /** 
     * @exception IllegalTypeException if the given argument
     * <code>NumberTerm</code> is a floating point number.
     * @exception EvaluationException if the given argument
     * <code>NumberTerm</code> represents <coe>0</code>.
     */
    public NumberTerm mod(NumberTerm t) {
	if (t.isDouble())
	    throw new IllegalTypeException("integer", t);
	if (t.intValue() == 0)
	    throw new EvaluationException("zero_divisor");
	return new IntegerTerm(this.val % t.intValue());
    }

    public NumberTerm multiply(NumberTerm t) {
	if (t.isDouble())
	    return t.multiply(this);
	return new IntegerTerm(this.val * t.intValue());
    }

    public NumberTerm negate() { return new IntegerTerm(- this.val); }

    public NumberTerm not() { return new IntegerTerm(~ this.val); }

    /** 
     * @exception IllegalTypeException if the given argument
     * <code>NumberTerm</code> is a floating point number.
     */
    public NumberTerm or(NumberTerm t) {
	if (t.isDouble())
	    throw new IllegalTypeException("integer", t);
	return new IntegerTerm(this.val | t.intValue());
    }

    public NumberTerm pow(NumberTerm t) { return new DoubleTerm(Math.pow(this.doubleValue(), t.doubleValue())); }

    public NumberTerm rint() { return new DoubleTerm(this.doubleValue()); }

    public NumberTerm round() { return this; }

    /** 
     * @exception IllegalTypeException if the given argument
     * <code>NumberTerm</code> is a floating point number.
     */
    public NumberTerm shiftLeft(NumberTerm t) {
	if (t.isDouble())
	    throw new IllegalTypeException("integer", t);
	return new IntegerTerm(this.val << t.intValue());
    }

    /** 
     * @exception IllegalTypeException if the given argument
     * <code>NumberTerm</code> is a floating point number.
     */
    public NumberTerm shiftRight(NumberTerm t) {
	if (t.isDouble())
	    throw new IllegalTypeException("integer", t);
	return new IntegerTerm(this.val >> t.intValue());
    }

    public NumberTerm signum() {return new IntegerTerm((int) Math.signum((double) this.val)); }

    public NumberTerm sin() { return new DoubleTerm(Math.sin(this.doubleValue())); }

    /** 
     * @exception EvaluationException if this object represents
     * an integer less than <coe>0</code>.
     */
    public NumberTerm sqrt() { 
	if (this.val < 0)
	    throw new EvaluationException("undefined");
	return new DoubleTerm(Math.sqrt(this.doubleValue())); 
    }

    public NumberTerm subtract(NumberTerm t) {
	if (t.isDouble())
	    return new DoubleTerm(this.doubleValue() - t.doubleValue());
	return new IntegerTerm(this.val - t.intValue());
    }

    public NumberTerm tan() { return new DoubleTerm(Math.tan(this.doubleValue())); }

    public NumberTerm toDegrees() { return new DoubleTerm(Math.toDegrees(this.doubleValue())); }

    public NumberTerm toFloat() { return new DoubleTerm((double) this.val); }

    public NumberTerm toRadians() { return new DoubleTerm(Math.toRadians(this.doubleValue())); }

    public NumberTerm truncate() { return this; }

    /** 
     * @exception IllegalTypeException if the given argument
     * <code>NumberTerm</code> is a floating point number.
     */
    public NumberTerm xor(NumberTerm t) {
	if (t.isDouble())
	    throw new IllegalTypeException("integer", t);
	return new IntegerTerm(this.val ^ t.intValue());
    }
}
