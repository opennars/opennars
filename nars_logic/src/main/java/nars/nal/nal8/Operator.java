package nars.nal.nal8;

import nars.Op;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.term.transform.Substitution;
import nars.term.transform.TermVisitor;
import nars.util.data.Util;
import nars.util.utf8.Utf8;

import java.io.IOException;
import java.util.Map;

/**
 * Wraps a term to represent an operator that can be used as the predicate
 * of an Operation
 */
public class Operator<T extends Term> implements Term {


    private final T term;

    public Operator(T the) {
        super();

        this.term = the;
    }

    @Override
    public final Op op() {
        return Op.OPERATOR;
    }

    @Override
    public final int volume() {
        return 1;
    }

    @Override
    public final int complexity() {
        return 1;
    }

    @Override public final int size() {
        //copied from Atomic.java
        throw new RuntimeException("Atomic terms have no subterms and length() should be zero");
    }


    @Override
    public boolean impossibleSubTermVolume(int otherTermVolume) {
        return true;
    }

    @Override
    public void recurseTerms(TermVisitor v, Term parent) {
        v.visit(this, parent);
    }

    @Override
    public String toString() {
        return toString(false);
    }

    @Override
    public <T extends Term> T normalized() {
        return (T)this;
    }

    @Override
    public boolean containsTerm(Term target) {
        return false;
    }

    @Override
    public boolean containsTermRecursively(Term target) {
        return false;
    }


    @Override
    public final Term clone() {
        return this;
    }

    @Override
    public Term cloneDeep() {
        return this;
    }

    @Override
    public boolean hasVar() {
        return false;
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
    public int vars() {
        return 0;
    }


    @Override
    public final byte[] bytes() {
        return Compound.newCompound1Key(op(), term);
    }

    @Override
    public int getByteLen() {
        return 1 + term.getByteLen();
    }

    @Override
    public int structure() {
        return (1 << Op.OPERATOR.ordinal());
    }

    @Override
    public final void append(Appendable p, boolean pretty) throws IOException {
        p.append(op().ch);
        term.append(p, pretty);
    }

    @Override
    public StringBuilder toStringBuilder(boolean pretty) {
        //copied from Atomic.java:

        StringBuilder sb = new StringBuilder();
        Utf8.fromUtf8ToStringBuilder(bytes(), sb);
        return sb;
    }

    @Override
    public String toString(boolean pretty) {
        return toStringBuilder(pretty).toString();
    }

    @Override
    public Term substituted(Map<Term, Term> subs) {
        return this;
    }
    @Override
    public final Term substituted(Substitution s) {
        return this;
    }

    @Override
    public final void rehash() {
        //nothing
        term.rehash();
    }

//
//    @Override
//    protected final void init(T... term) {
//        super.init(term);
//        this.structureHash = operatorOrdinal;
//        this.volume = 1;
//        this.complexity = 1;
//    }

    public static Operator the(final String name) {
        return the(Atom.the(name));
    }
    public static Operator the(final Term x) {
        return new Operator(x);
    }

    @Override
    public final int compareTo(Object o) {
        if (this == o) return 0;

        Term t = (Term)o;
        int diff = op().compareTo(t.op());
        if (diff!=0) return diff;

        return term.compareTo(((Operator)t).term);
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        Term t = (Term)obj;
        return t.op() == Op.OPERATOR &&
                term.equals(((Operator)t).term);
    }

    @Override
    public final int hashCode() {
        return term.hashCode() ^ Util.PRIME2; //TODO determine if this is good
    }

    public final Term identifier() {
        return term;
    }

    @Override
    public final boolean equalsAll(Term[] cls) {
        return false;
    }
}
