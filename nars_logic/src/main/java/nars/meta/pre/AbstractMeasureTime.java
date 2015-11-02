package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.premise.Premise;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
abstract public class AbstractMeasureTime extends PreCondition3Output {


    public AbstractMeasureTime(Term var1, Term var2, Term target) {
        super(var1, var2, target);
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b, Term target) {
        final Premise premise = m.premise;

        if (!premise.isTaskAndBeliefEvent())
            return false;

        return testEvents(m, a, b, target);
    }

    protected abstract boolean testEvents(RuleMatch m, Term a, Term b, Term target);
}
