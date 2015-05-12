package nars.nal.term;

import nars.Global;
import nars.nal.NALOperator;
import nars.nal.nal7.TemporalRules;
import nars.util.data.Utf8;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by me on 4/25/15.
 */
public class Atom implements Term {

    protected byte[] name;
    transient protected final int hash;

    private static final Map<String,Atom> atoms = Global.newHashMap(8192);

    @Override
    public byte[] name() {
        return name;
    }

    /** Creates a quote-escaped term from a string. Useful for an atomic term that is meant to contain a message as its name */
    public static Atom quoted(String t) {
        return get('"' + t + '"');
    }


    public NALOperator operator() {
        return NALOperator.ATOM;
    }


    public boolean isNormalized() {
        return true;
    }

    @Override
    public int length() {
        return 1;
    }

    /**
     * @param that The Term to be compared with the current Term
     */
    @Override
    public int compareTo(final Term that) {
        if (that==this) return 0;

        // variables have earlier sorting order than non-variables
        if (that instanceof Atom) {
            if (that.getClass() == Variable.class)
                return 1;

            return compareHash(that);
        }
        else {
            return -1;
        }

    }

    public int compareHash(final Term that) {
        return Integer.compare(hashCode(), that.hashCode());
    }

//    /**
//     * Default constructor that build an internal Term
//     */
//    @Deprecated protected Atom() {
//    }

    /**
     * Constructor with a given name
     *
     * @param name A String as the name of the Term
     */
    protected Atom(final String name) {
        this(Utf8.toUtf8(name));
    }

    protected Atom(final byte[] name) {
        this.name = name;
        this.hash = Arrays.hashCode(name);
    }

    /** gets the atomic term given a name */
//    public final static Atom get(final String name) {
//        Atom x = atoms.get(name);
//        if (x != null) return x;
//        atoms.put(name, x = new Atom(name));
//        return x;
//    }

    public final static Atom get(final String name) {
        return new Atom(name);
    }

    public final static Term get(Object o) {
        if (o instanceof Term) return (Term)o;
        if (o instanceof String) {
            return get((String) o);
        }
        return null;
    }

    /** gets the atomic term of an integer */
    public final static Term get(final int i) {
        //fast lookup for single digits
        switch (i) {
            case 0: return get("0");
            case 1: return get("1");
            case 2: return get("2");
            case 3: return get("3");
            case 4: return get("4");
            case 5: return get("5");
            case 6: return get("6");
            case 7: return get("7");
            case 8: return get("8");
            case 9: return get("9");
        }
        return get(Integer.toString(i));
    }

    @Override public String toString() {
        return Utf8.fromUtf8(name());
    }


    /**
     * Make a new Term with the same name.
     *
     * @return The new Term
     */
    @Override
    public Term clone() {
        return this;
        //return new Atom(name());
    }

    /** attempts to return cloneNormalize result,
     * if it's necessary and possible.
     *  does not modify this term
     * */
    public Term normalized() {
        return this;
    }

    public Term cloneDeep() {
        return clone();
    }

    /**
     * Equal terms have identical name, though not necessarily the same
     * reference.
     *
     * @return Whether the two Terms are equal
     * @param that The Term to be compared with the current Term
     */
    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (!(that instanceof Atom)) return false;
        final Atom t = (Atom)that;
        if (equalsType(t) && equalsName(t)) {
            t.name = name; //share
            return true;
        }
        return false;
    }



    /**
     * Produce a hash code for the term
     *
     * @return An integer hash code
     */
    @Override
    public int hashCode() {
        return hash;
    }

    /**
     * Alias for 'isNormalized'
     * @return A Term is constant by default
     */
    public boolean isConstant() {
        return true;
    }

    public int getTemporalOrder() {
        return TemporalRules.ORDER_NONE;
    }

    public void recurseTerms(final TermVisitor v, Term parent) {
        v.visit(this, parent);
    }

    public void recurseSubtermsContainingVariables(final TermVisitor v) {
        recurseSubtermsContainingVariables(v, null);
    }

    public void recurseSubtermsContainingVariables(final TermVisitor v, Term parent) {
        //TODO move to Variable subclass and leave this empty here
        if (hasVar())
            v.visit(this, parent);
    }


    /**
     * The syntactic complexity, for constant atomic Term, is 1.
     *
     * @return The complexity of the term, an integer
     */
    public short getComplexity() {
        return 1;
    }







    public int containedTemporalRelations() {
        return 0;
    }

    /**
     * Recursively check if a compound contains a term
     *
     * @param target The term to be searched
     * @return Whether the two have the same content
     */
    public boolean containsTermRecursivelyOrEquals(final Term target) {
        return equals(target);
    }

    /** whether this contains a term in its components. */
    public boolean containsTerm(final Term target) {
        return equals(target);
    }




    /**
     * Whether this compound term contains any variable term
     *
     * @return Whether the name contains a variable
     */
    public boolean hasVar() {
        return false;
    }


    public boolean hasVarIndep() {
        return false;
    }

    public boolean hasVarDep() {
        return false;
    }

    public boolean hasVarQuery() {
        return false;
    }

    public static String unquote(Term s) {
        String x = s.toString();
        if (x.startsWith("\"") && x.endsWith("\"")) {
            return x.substring(1, x.length()-1);
        }
        return x;
    }


    /** performs a thorough check of the validity of a term (by cloneDeep it) to see if it's valid */
//    public static boolean valid(final Term content) {
//
//        return true;
////        try {
////            Term cloned = content.cloneDeep();
////            return cloned!=null;
////        }
////        catch (Throwable e) {
////            if (Global.DEBUG && Global.DEBUG_INVALID_SENTENCES) {
////                System.err.println("INVALID TERM: " + content);
////                e.printStackTrace();
////            }
////            return false;
////        }
////
//    }
}
