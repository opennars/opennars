package nars.nal.nal8;

import nars.Op;
import nars.term.Atom;
import nars.term.Atomic;
import nars.term.Compound;
import nars.term.Term;
import nars.term.transform.Substitution;

import java.io.IOException;

/**
 * Wraps a term to represent an operator that can be used as the predicate
 * of an Operation
 * TODO inherit AbstractAtomic
 */
public final class Operator<T extends Term> extends Atomic { //implements Term {


    //final static byte[] opPrefix = new byte[] { (byte)'^' };

    private final T term;

    public Operator(T the) {
        super();

        this.term = the;
    }

    @Override
    public final Op op() {
        return Op.OPERATOR;
    }

//defined in abstractatomic
//    @Override
//    public final int volume() {
//        return 1;
//    }

    @Override
    public final int complexity() {
        return 1;
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
    public final int structure() {
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
        String tString = term.toString();
        StringBuilder sb = new StringBuilder(tString.length()+1);
        return sb.append('^').append(tString);
    }

    @Override
    public String toString() {
        return '^' + term.toString();
    }

    @Override
    public final Term substituted(Substitution s) {
        return this;
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
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        Term t = (Term)obj;
        return (t.op() == Op.OPERATOR) && term.equals(((Operator)t).term);
    }

    public final Term identifier() {
        return term;
    }

    @Override
    public final int compareTo(Object o) {
        if (o == this) return 0;
        if (o instanceof Operator) {
            return term.compareTo( ((Operator)o).term );
        }
        return 1;
    }
}
