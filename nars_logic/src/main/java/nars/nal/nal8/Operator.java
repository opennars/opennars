package nars.nal.nal8;

import nars.Op;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Compound1;
import nars.term.Term;

import java.io.IOException;

/**
 * Wraps a term to represent an operator that can be used as the predicate
 * of an Operation
 */
public class Operator<T extends Term> extends Compound1<T> {

    public Operator(T the) {
        super();

        init(the);
    }

    @Override
    public final Op op() {
        return Op.OPERATOR;
    }

    @Override public final boolean isCommutative() {
        return false;
    }

    @Override
    public final Term clone() {
        return new Operator(getTerm());
    }

    @Override
    public Term clone(Term[] replaced) {
        if (replaced.length != 1)
            return null;
        return new Operator(replaced[0]);
    }

    @Override
    public final byte[] bytes() {
        return Compound.newCompound1Key(op(), the());
    }

    @Override
    public final void append(Appendable p, boolean pretty) throws IOException {
        p.append(op().ch);
        the().append(p, pretty);
    }

    final static int operatorOrdinal = (1 << Op.OPERATOR.ordinal());


    @Override
    protected final void init(T... term) {
        super.init(term);
        this.structureHash = operatorOrdinal;
        this.volume = 1;
        this.complexity = 1;
    }

    public static Operator the(final String name) {
        return the(Atom.the(name));
    }
    public static Operator the(final Term x) {
        return new Operator(x);
    }

}
