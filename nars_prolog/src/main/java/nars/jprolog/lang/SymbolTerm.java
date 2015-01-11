package nars.jprolog.lang;
import java.util.Hashtable;
/**
 * Atom.<br>
 * The <code>SymbolTerm</code> class represents a Prolog atom.<br>
 *
 * <pre>
 *   Term t = SymbolTerm.makeSymbol("kobe");
 *   String name = ((SymbolTerm)t).name();
 * </pre>
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class SymbolTerm extends Term {

    /** Symbol table. */
    protected static Hashtable<String,SymbolTerm> SYMBOL_TABLE = new Hashtable<String,SymbolTerm>();

    /** Holds a string representation of this <code>SymbolTerm</code>. */
    protected String name;

    /** Holds the arity of this <code>SymbolTerm</code>. */
    protected int arity;

    /** Returns a Prolog atom for the given name. */
    public static SymbolTerm makeSymbol(String _name) {
	return makeSymbol(_name, 0);
    }

    /** Returns a Prolog functor for the given name and arity. */
    public static SymbolTerm makeSymbol(String _name, int _arity) {
	String key = _name + "/" + _arity;
	SymbolTerm sym;

	synchronized (SYMBOL_TABLE) {
	    sym = SYMBOL_TABLE.get(key);
	    if (sym == null) {
		sym = new SymbolTerm(_name, _arity);
		SYMBOL_TABLE.put(key, sym);
	    }
	}
	return sym;
    }

    /** Constructs a new Prolog atom (or functor) with the given symbol name and arity. */
    protected SymbolTerm(String _name, int _arity) { 
	name  = _name; 
	arity = _arity;
    }

    /** Returns the arity of this <code>SymbolTerm</code>.
     * @return the value of <code>arity</code>.
     * @see #arity
     */
    public int arity() { return arity; }

    /** Returns the string representation of this <code>SymbolTerm</code>.
     * @return the value of <code>name</code>.
     * @see #name
     */
    public String name() { return name; }

    /* Term */
    public boolean unify(Term t, Trail trail) {
	t = t.dereference();
	if (t.isVariable()) {
	    ((VariableTerm) t).bind(this, trail);
	    return true;
	}
	return this == t;
	//	return name.equals(((SymbolTerm)t).name());
    }

    /** 
     * @return the <code>boolean</code> whose value is
     * <code>convertible(String.class, type)</code>.
     * @see Term#convertible(Class, Class)
     */
    public boolean convertible(Class type) { return convertible(String.class, type); }

    /** 
     * Returns a <code>java.lang.String</code> corresponds to this <code>SymbolTerm</code>
     * according to <em>Prolog Cafe interoperability with Java</em>.
     * @return a <code>java.lang.String</code> object equivalent to
     * this <code>SymbolTerm</code>.
     */
    public Object toJava() { return name; }

    public String toQuotedString() { return Token.toQuotedString(name); }

    /** Returns a string representation of this <code>SymbolTerm</code>. */
    public String toString() { return name; }

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
	if (anotherTerm.isVariable() || anotherTerm.isNumber())
	    return AFTER;
	if (! anotherTerm.isSymbol())
	    return BEFORE;
	if (this == anotherTerm) 
	    return EQUAL;
	int x = name.compareTo(((SymbolTerm)anotherTerm).name());
	if (x != 0)
	    return x;
	int y = this.arity - ((SymbolTerm)anotherTerm).arity();
	if (y != 0)
	    return y;
	throw new InternalException("SymbolTerm is not unique");
    }
}
