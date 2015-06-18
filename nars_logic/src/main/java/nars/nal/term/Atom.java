package nars.nal.term;

import nars.Global;
import nars.nal.NALOperator;
import nars.nal.nal7.TemporalRules;
import nars.nal.term.transform.TermVisitor;
import nars.util.data.id.Identifier;
import nars.util.data.id.LiteralUTF8Identifier;

import java.util.Map;

/**
 * Created by me on 4/25/15.
 */
public class Atom extends ImmutableAtom {

    private static final Map<String,Atom> atoms = Global.newHashMap(4096);



    /** Creates a quote-escaped term from a string. Useful for an atomic term that is meant to contain a message as its name */
    public static Atom quote(String t) {
        return the('"' + t + '"');
    }

    /** determines if the string is invalid as an unquoted term according to the characters present */
    public static boolean quoteNecessary(CharSequence t) {
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (Character.isWhitespace(c)) return true;
            if ((!Character.isDigit(c)) && (!Character.isAlphabetic(c))) return true;
        }
        return false;
    }

    public NALOperator operator() {
        return NALOperator.ATOM;
    }


    public boolean isNormalized() {
        return true;
    }

    @Override
    public int length() {
        throw new RuntimeException("Atomic terms have no subterms and length() should be zero");
        //return 0;
    }

    /**
     * @param that The Term to be compared with the current Term
     */
    @Override
    public int compareTo(final Object that) {
        if (that==this) return 0;

        // variables have earlier sorting order than non-variables
        if (that instanceof Atom) {
            if (that.getClass() == Variable.class)
                return 1;

            return compareHash((Atom)that);
        }
        else {
            return -1;
        }

    }

    @Override public int getMass() { return 1; }

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
        super(name);
    }

    protected Atom(final byte[] name) {  super(name);    }

    /** gets the atomic term given a name, storing it in the static symbol table */
    public final static Atom theCached(final String name) {
        Atom x = atoms.get(name);
        if (x != null) return x;
        atoms.put(name, x = new Atom(name));
        return x;
    }

    public final static Atom the(final String name, boolean quoteIfNecessary) {
        if (quoteIfNecessary) {
            if (quoteNecessary(name))
                return quote(name);
        }
        return the(name);
    }

    public final static Term the(Term x) {
        return x;
    }
    public final static Atom the(Number o) {
        return quote(String.valueOf((Number) o));
    }

    /** gets the atomic term given a name */
    public final static Atom the(String o) {
        return new Atom(o);
    }

    /*
    // similar to String.intern()
    public final static Atom the(final String name) {
        if (name.length() <= 2)
            return theCached(name);
        return new Atom(name);
    }
    */

    /** gets the atomic term of an integer */
    public final static Term the(final int i) {
        //fast lookup for single digits
        switch (i) {
            case 0: return the("0");
            case 1: return the("1");
            case 2: return the("2");
            case 3: return the("3");
            case 4: return the("4");
            case 5: return the("5");
            case 6: return the("6");
            case 7: return the("7");
            case 8: return the("8");
            case 9: return the("9");
        }
        return the(Integer.toString(i));
    }



    /**
     * Atoms are singular, so it is useless to clone them
     */
    @Override
    public Term clone() {
        return this;
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

    @Override
    public boolean hasVar(char type) {
        return false;
    }

//    /**
//     * Equal terms have identical name, though not necessarily the same
//     * reference.
//     *
//     * @return Whether the two Terms are equal
//     * @param that The Term to be compared with the current Term
//     */
//    @Override
//    public boolean equals(final Object that) {
//        if (this == that) return true;
//        if (!(that instanceof Atom)) return false;
//        final Atom t = (Atom)that;
//        return equalID(t);
//
////        if (equalsType(t) && equalsName(t)) {
////            t.name = name; //share
////            return true;
////        }
////        return false;
//    }




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


    @Override
    public boolean requiresNormalizing() {
        return false;
    }

    /**
     * The syntactic complexity, for constant atomic Term, is 1.
     *
     * @return The complexity of the term, an integer
     */
    public int getComplexity() {
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
    @Override public boolean hasVar() {
        return false;
    }
    @Override public int getTotalVariables() {
        return 0;
    }

    @Override public boolean hasVarIndep() {
        return false;
    }
    @Override public int varIndep() { return 0;     }

    @Override public boolean hasVarDep() {
        return false;
    }
    @Override public int varDep() { return 0;     }

    @Override public boolean hasVarQuery() {
        return false;
    }
    @Override public int varQuery() { return 0;     }

    public static String unquote(Term s) {
        String x = s.toString();
        if (x.startsWith("\"") && x.endsWith("\"")) {
            return x.substring(1, x.length()-1);
        }
        return x;
    }

    public static Term the(final Object o) {
        if (o instanceof Term) return ((Term)o);
        else if (o instanceof String) return the((String)o);
        else if (o instanceof Number) return the((Number)o);
        return null;
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
