package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.process.ConceptProcess;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
abstract public class AbstractMeasureTime extends PreCondition3Output {

    public AbstractMeasureTime(Term var1, Term var2, Term var3) {
        super(var1, var2, var3);
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b, Term c) {
        final ConceptProcess premise = m.premise;

        if (!premise.isTaskAndBeliefEvent())
            return false;

        return testEvents(m, a, b, c);
    }

    protected abstract boolean testEvents(RuleMatch m, Term a, Term b, Term c);
}
