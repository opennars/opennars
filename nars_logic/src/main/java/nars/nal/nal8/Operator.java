package nars.nal.nal8;

import com.google.common.primitives.Bytes;
import nars.Op;
import nars.term.AbstractAtomic;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.term.transform.Substitution;
import nars.util.utf8.Utf8;

import java.io.IOException;

/**
 * Wraps a term to represent an operator that can be used as the predicate
 * of an Operation
 * TODO inherit AbstractAtomic
 */
public class Operator<T extends Term> extends AbstractAtomic { //implements Term {


    final static byte[] opPrefix = new byte[] { (byte)'^' };

    private final T term;
    private final int hash;

    private transient final byte[] bytes;

    public Operator(T the) {
        super();



        this.term = the;
        this.bytes = Bytes.concat(opPrefix, the.bytes());
        this.hash = Atom.hash(term.bytes(), Op.OPERATOR.ordinal());
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
    public boolean hasVarIndep() {
        return false;
    }

    @Override
    public boolean hasVarDep() {
        return false;
    }

    @Override
    public boolean hasVarQuery() {
        return false;
    }


    @Override
    public final byte[] bytes() {
        return Compound.newCompound1Key(op(), term);
    }

    @Override
    public void setBytes(byte[] b) {

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

        StringBuilder sb = new StringBuilder();
        Utf8.fromUtf8ToStringBuilder(bytes(), sb);
        return sb;
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

    @Override
    public final int hashCode() {
        return hash;
    }

    public final Term identifier() {
        return term;
    }

}
