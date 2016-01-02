package nars.nal.meta;

import nars.Op;
import nars.term.Term;
import nars.term.TermVector;
import nars.term.compound.GenericCompound;

import java.util.Collection;
import java.util.List;

/**
 * Created by me on 12/31/15.
 */
public final class AndCondition<C> extends GenericCompound<BooleanCondition<C>> implements BooleanCondition<C> {

    public AndCondition(BooleanCondition<C>[] p) {
        super(Op.CONJUNCTION, new TermVector(p));
    }
    public AndCondition(Collection<BooleanCondition<C>> p) {
        super(Op.CONJUNCTION, new TermVector(p, BooleanCondition.class));
    }

    @Override
    public boolean booleanValueOf(C m) {
        for (BooleanCondition<C> x : terms()) {
            if (!x.booleanValueOf(m))
                return false;
        }
        return true;
    }

    public void appendJavaCondition(StringBuilder s) {
//        Joiner.on(" && ").appendTo(s, Stream.of(terms()).map(
//                b -> ('(' + b.toJavaConditionString() + ')'))
//                .iterator()
//        );
    }

    @Override
    public void addConditions(List<Term> l) {
        l.add(this);
    }
}
