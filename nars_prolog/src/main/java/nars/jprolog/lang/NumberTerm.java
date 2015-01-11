package nars.jprolog.lang;
/**
 * The superclass of classes for integers and floating point numbers.
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public abstract class NumberTerm extends Term {
    /** Returns the numeric value represented by this object after conversion to type <code>int</code>. */
    abstract public int intValue();
    /** Returns the numeric value represented by this object after conversion to type <code>long</code>. */
    abstract public long longValue();
    /** Returns the numeric value represented by this object after conversion to type <code>double</code>. */
    abstract public double doubleValue();

    /** 
     * Compares two <code>NumberTerm</code> objects numerically.
     * @param t the <code>NumberTerm</code> to compare with.
     * @return the value <code>0</code>
     * if this object is numerically equal to the argument <code>NumberTerm</code>;
     * a value less than <code>0</code>
     * if this object is numerically less than the argument <code>NumberTerm</code>; and
     * a value greater than <code>0</code>
     * if this object is numerically greater than the argument <code>NumberTerm</code>.
     */
    abstract public int arithCompareTo(NumberTerm t);

    /** Returns a <code>NumberTerm</code> whose value is <code>abs(this)</code>. */
    abstract public NumberTerm abs();
    /** Returns a <code>NumberTerm</code> whose value is <code>acos(this)</code>. */
    abstract public NumberTerm acos();
    /** Returns a <code>NumberTerm</code> whose value is <code>(this + t)</code>. */
    abstract public NumberTerm add(NumberTerm t);
    /** Returns a <code>NumberTerm</code> whose value is <code>(this &amp; t)</code>. */
    abstract public NumberTerm and(NumberTerm t);
    /** Returns a <code>NumberTerm</code> whose value is <code>asin(this)</code>. */
    abstract public NumberTerm asin();
    /** Returns a <code>NumberTerm</code> whose value is <code>tan(this)</code>. */
    abstract public NumberTerm atan();
    /** Returns a <code>NumberTerm</code> whose value is <code>ceil(this)</code>. */
    abstract public NumberTerm ceil();
    /** Returns a <code>NumberTerm</code> whose value is <code>cos(this)</code>. */
    abstract public NumberTerm cos();
    /** Returns a <code>NumberTerm</code> whose value is <code>(this / t)</code>. */
    abstract public NumberTerm divide(NumberTerm t);
    /** Returns a <code>NumberTerm</code> whose value is <code>exp(this)</code>. */
    abstract public NumberTerm exp();
    /** Returns a <code>NumberTerm</code> whose value is the float-integer-part of <code>this</code>. */
    abstract public NumberTerm floatIntPart();
    /** Returns a <code>NumberTerm</code> whose value is the float-fractional-part of <code>this</code>. */
    abstract public NumberTerm floatFractPart();
    /** Returns a <code>NumberTerm</code> whose value is <code>floor(this)</code>. */
    abstract public NumberTerm floor();
    /** Returns a <code>NumberTerm</code> whose value is <code>(int)(this / t)</code>. */
    abstract public NumberTerm intDivide(NumberTerm t);
    /** Returns a <code>NumberTerm</code> whose value is <code>log(this)</code>. */
    abstract public NumberTerm log();
    /** Returns a <code>NumberTerm</code> whose value is <code>max(this, t)</code>. */
    abstract public NumberTerm max(NumberTerm t);
    /** Returns a <code>NumberTerm</code> whose value is <code>min(this, t)</code>. */
    abstract public NumberTerm min(NumberTerm t);
    /** Returns a <code>NumberTerm</code> whose value is <code>(this mod t)</code>. */
    abstract public NumberTerm mod(NumberTerm t);
    /** Returns a <code>NumberTerm</code> whose value is <code>(this * t)</code>. */
    abstract public NumberTerm multiply(NumberTerm t);
    /** Returns a <code>NumberTerm</code> whose value is <code>(- this)</code>. */
    abstract public NumberTerm negate();
    /** Returns a <code>NumberTerm</code> whose value is <code>(~ this)</code>. */
    abstract public NumberTerm not();
    /** Returns a <code>NumberTerm</code> whose value is <code>(this | t)</code>. */
    abstract public NumberTerm or(NumberTerm t);
    /** Returns a <code>NumberTerm</code> whose value is <code>(this<sup>t</sup>)</code>. */
    abstract public NumberTerm pow(NumberTerm t);
    /** Returns a <code>NumberTerm</code> whose value is <code>rint(this)</code>. */
    abstract public NumberTerm rint();
    /** Returns a <code>NumberTerm</code> whose value is <code>round(this)</code>. */
    abstract public NumberTerm round();
    /** Returns a <code>NumberTerm</code> whose value is <code>(this &lt;&lt; t)</code>. */
    abstract public NumberTerm shiftLeft(NumberTerm t);
    /** Returns a <code>NumberTerm</code> whose value is <code>(this &gt;&gt; t)</code>. */
    abstract public NumberTerm shiftRight(NumberTerm t);
    /** Returns a <code>NumberTerm</code> whose value is <code>signum(this)</code>. */
    abstract public NumberTerm signum();
    /** Returns a <code>NumberTerm</code> whose value is <code>sin(this)</code>. */
    abstract public NumberTerm sin();
    /** Returns a <code>NumberTerm</code> whose value is <code>sqrt(this)</code>. */
    abstract public NumberTerm sqrt();
    /** Returns a <code>NumberTerm</code> whose value is <code>(this - t)</code>. */
    abstract public NumberTerm subtract(NumberTerm t);
    /** Returns a <code>NumberTerm</code> whose value is <code>tan(this)</code>. */
    abstract public NumberTerm tan();
    /** Returns a <code>NumberTerm</code> whose value is <code>toDegrees(this)</code>. */
    abstract public NumberTerm toDegrees();
    /** Returns a <code>NumberTerm</code> whose value is <code>(double)(this)</code>. */
    abstract public NumberTerm toFloat();
    /** Returns a <code>NumberTerm</code> whose value is <code>toRadians(this)</code>. */
    abstract public NumberTerm toRadians();
    /** Returns a <code>NumberTerm</code> whose value is the truncate of <code>this</code>. */
    abstract public NumberTerm truncate();
    /** Returns a <code>NumberTerm</code> whose value is <code>(this ^ t)</code>. */
    abstract public NumberTerm xor(NumberTerm t);
}
