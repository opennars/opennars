package nars.nal.nal8;

import nars.Op;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Compound1;
import nars.term.Term;

import java.io.IOException;
import java.io.Writer;

/**
 * Wraps a term to represent an operator that can be used as the predicate
 * of an Operation
 */
public class Operator<T extends Term> extends Compound1<T> {

    public Operator(T the) {
        super(the);

        init(the);
    }

    @Override
    public Op operator() {
        return Op.OPERATOR;
    }

    @Override
    public Term clone() {
        return new Operator(getTerm());
    }

    @Override
    public Term clone(Term[] replaced) {
        if (replaced.length != 1)
            return null;
        return new Operator(replaced[0]);
    }

    @Override
    public byte[] init() {
        return Compound.newCompound1Key(operator(), the());
    }

    @Override
    public void append(Writer p, boolean pretty) throws IOException {
        p.append(operator().ch);
        the().append(p, pretty);
    }

    final static int operatorOrdinal = (1 << Op.OPERATOR.ordinal());


    @Override
    public int structure() {
        return operatorOrdinal;
    }

    @Override
    final public int complexity() {
        return 1;
    }

    @Override
    final public int volume() {  return 1;    }

    public static Operator the(final String name) {
        return the(Atom.the(name));
    }
    public static Operator the(final Term x) {
        return new Operator(x);
    }

}
