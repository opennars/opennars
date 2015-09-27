package nars.term;

import nars.Global;
import nars.Op;
import nars.nal.nal1.Negation;
import nars.term.transform.TermVisitor;
import nars.util.data.Util;
import nars.util.utf8.Byted;
import nars.util.utf8.Utf8;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.CharBuffer;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by me on 4/25/15.
 */
public class Atom implements Term, Byted /*extends ImmutableAtom*/, Externalizable {

    private static final Map<String,Atom> atoms = Global.newHashMap(4096);
    public static final Function<String, Atom> AtomInterner = Atom::new;

    public static final Term Null = new Atom(new byte[0]) {
        @Override
        public String toString() {
            return "NULL";
        }

        @Override
        public Op op() {
            return Op.NONE;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    };

    private byte[] data;
    private int hash;


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

    @Override
    public Op op() {
        return Op.ATOM;
    }

    final static int atomOrdinal = (1 << Op.ATOM.ordinal());


    @Override
    public int structure() {
        //atomic terms should not need to store data in the upper 32 bits, like Image and other compound terms may need
        return atomOrdinal;
    }

    @Override
    public void append(final Appendable w, final boolean pretty) throws IOException {
        w.append(CharBuffer.wrap(Utf8.fromUtf8ToChars(bytes())));
    }


    /** preferably use toCharSequence if needing a CharSequence; it avoids a duplication */
    public StringBuilder toStringBuilder(final boolean pretty) {
        char[] c = chars(pretty);
        return new StringBuilder(c.length).append(c);
    }


    @Override
    public String toString() {
        return new String(chars(true));
        //return toStringBuilder(true).toString();
    }

    public char[] chars(final boolean pretty) {
        return Utf8.fromUtf8ToChars(bytes());
    }

    @Override
    public boolean equals(final Object x) {
        if (this == x) return true;
        if (!(x instanceof Atom)) return false;
        return Byted.equals(this, (Byted)x);
    }


    /**
     * @param that The Term to be compared with the current Term
     */
    @Override
    public int compareTo(final Object that) {
        if (that==this) return 0;

        // group variables by earlier sorting order than non-variables
        if (that instanceof Variable)
            return 1;

        if (that instanceof Atom) {
            return Byted.compare(this, (Atom)that);
        }
        else {
            return -1;
        }

    }

    public Atom() {
        this((byte[])null);
    }


//    /**
//     * Default constructor that build an internal Term
//     */
//    @Deprecated protected Atom() {
//    }

    /**
     * Constructor with a given name
     *
     * @param id A String as the name of the Term
     */
    protected Atom(final String id) {
        this(Utf8.toUtf8(id));
    }

    protected Atom(final byte[] id) {
        setBytes(id);
    }

    /** interns the atomic term given a name, storing it in the static symbol table */
    public final static Atom theCached(final String name) {
        return atoms.computeIfAbsent(name, AtomInterner);
    }

    public final static Atom the(final String name, boolean quoteIfNecessary) {
        if (quoteIfNecessary) {
            if (quoteNecessary(name))
                return quote(name);
        }
        return the(name);
    }

    @Override
    public int hashCode() {
        if (hash == 0)
            rehash();
        return hash;
    }

    public final static Term the(Term x) {
        return x;
    }
    public final static Atom the(Number o) {
        if (o instanceof Short) return the(o.intValue());
        if (o instanceof Integer) return the(o.intValue());
        return the(o.toString(), true);
    }

    /** gets the atomic term given a name */
    public final static Atom the(String o) {
        return new Atom(o);
    }
    public final static Atom the(byte c) {
        return new Atom(new byte[] { c });
    }
    /*
    // similar to String.intern()
    public final static Atom the(final String name) {
        if (name.length() <= 2)
            return theCached(name);
        return new Atom(name);
    }
    */

    final static Atom[] digits = new Atom[10];

    /** gets the atomic term of an integer */
    public final static Atom the(final int i) {
        //fast lookup for single digits
        if ((i >= 0) && (i <= 9)) {
            Atom a = digits[i];
            if (a == null)
                a = digits[i] = the(Integer.toString(i));
            return a;
        }
        return the(Integer.toString(i), true);
    }



    /**
     * Atoms are singular, so it is useless to clone them
     */
    @Override
    public Term clone() {
        return this;
    }



    @Override
    public Term cloneDeep() {
        return clone();
    }

    @Override
    public boolean hasVar(Op type) {
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




    @Override
    public final void recurseTerms(final TermVisitor v, final Term parent) {
        v.visit(this, parent);
    }

//    public final void recurseSubtermsContainingVariables(final TermVisitor v) {
//        recurseSubtermsContainingVariables(v, null);
//    }

//    /**
//     * Recursively check if a compound contains a term
//     *
//     * @param target The term to be searched
//     * @return Whether the two have the same content
//     */
//    @Override public final void recurseSubtermsContainingVariables(final TermVisitor v, Term parent) {
//        //TODO move to Variable subclass and leave this empty here
//        if (hasVar())
//            v.visit(this, parent);
//    }


    @Override
    public final String toString(boolean pretty) {
        return toString();
    }

    /**
     * Whether this compound term contains any variable term
     *
     * @return Whether the name contains a variable
     */
    @Override public boolean hasVar() {
        return false;
    }
    @Override public int vars() {
        return 0;
    }

    @Override public boolean hasVarIndep() {
        return false;
    }


    @Override public boolean hasVarDep() {
        return false;
    }


    @Override public boolean hasVarQuery() {
        return false;
    }


    public static String unquote(final Term s) {
        String x = s.toString();
        if (x.startsWith("\"") && x.endsWith("\"")) {
            return x.substring(1, x.length()-1);
        }
        return x;
    }

    public static Term[] the(final String... s) {
        final int l = s.length;
        final Term[] x = new Term[l];
        for (int i = 0; i < l; i++)
            x[i] = Atom.the(s[i]);
        return x;
    }

    public static Term the(final Object o) {

        if (o instanceof Term) return ((Term)o);
        else if (o instanceof String)
            return the((String)o);
        else if (o instanceof Number)
            return the((Number)o);
        return null;
    }

    @Override
    public int complexity() {
        return 1;
    }



    @Override
    public final int containedTemporalRelations() {        return 0;     }

    @Override
    public final int length() {
        throw new RuntimeException("Atomic terms have no subterms and length() should be zero");
        //return 0;
    }

    @Override public int volume() { return 1; }

    final public boolean impossibleSubTermVolume(final int otherTermVolume) {
        return true;
    }

    public void rehash() {
        this.hash = Util.ELFHashNonZero(data, 1);
    }


    @Override
    final public byte[] bytes() {
        return data;
    }

    final public byte byt(int n) {
        return data[n];
    }

    final byte byt0() {
        return data[0];
    }

    @Override
    public void setBytes(final byte[] id) {
        if (id!=this.data) {
            this.data = id;
            rehash();
        }
    }

    @Override public final boolean impossibleToMatch(int possibleSubtermStructure) {
        /*
        for atomic terms, there will be only one
        bit set in this (for the operator). if it does not equal
        the parameter, then the structure can not match.
        */
        return possibleSubtermStructure != 0 &&
            structure()!=possibleSubtermStructure;
    }

    @Override
    public final boolean containsTerm(Term target) {
        return false;
    }

    @Override
    public final boolean equalsOrContainsTermRecursively(Term target) {
        return equals(target);
    }




    @Override
    public int varIndep() {
        return 0;
    }

    @Override
    public int varDep() {
        return 0;
    }

    @Override
    public int varQuery() {
        return 0;
    }


    @Override
    public final boolean isNormalized() {
        return true;
    }

    @Override
    public final Atom normalized() {
        return this;
    }


    public static Negation notThe(String untrue) {
        return (Negation) Negation.make(Atom.the(untrue));
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        byte[] name = bytes();
        out.writeShort(name.length);
        out.write(bytes());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int nameLen = in.readShort();
        byte[] name = new byte[nameLen];

        in.read(name);

        setBytes(name);
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
