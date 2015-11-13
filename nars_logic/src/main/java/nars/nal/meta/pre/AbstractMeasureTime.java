package nars.nal.meta.pre;

import nars.Premise;
import nars.nal.RuleMatch;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
abstract public class AbstractMeasureTime extends PreCondition1Output {


    public AbstractMeasureTime(Term target) {
        super(target);
    }

    @Override
    public boolean test(RuleMatch m, Term target) {
        final Premise premise = m.premise;

        if (!premise.isEvent())
            return false;

        return testEvents(m, target);
    }

    protected abstract boolean testEvents(RuleMatch m, Term target);
}
