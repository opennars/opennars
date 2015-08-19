package nars.meta.pre;

import nars.Op;
import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.premise.Premise;
import nars.task.Task;
import nars.term.Term;


public class BeliefTermType extends PreCondition {

    final Op op;

    public BeliefTermType(Op o) {
        this.op = o;
    }

    @Override public boolean test(final RuleMatch ruleMatch) {
        final Task b = getTask(ruleMatch.premise);
        if (b == null) return false;
        final Term t = b.getTerm();

        //if (t == null && op == null) return true;
        return (t.operator() == op);
    }


    protected Task getTask(final Premise p) {
        return p.getBelief();
    }


    @Override
    public String toString() {
        //return getClass().getSimpleName() + "[" + op.toString() + "]";
        return getClass().getSimpleName() + "[" + op.toString() + "]";
    }


}
