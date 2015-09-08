package nars.meta.pre;

import nars.Op;
import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.premise.Premise;
import nars.task.Task;
import nars.term.Term;

import java.util.EnumMap;


public class BeliefTermType extends PreCondition {

    final Op op;
    transient private final String id;

    public final static EnumMap<Op,BeliefTermType> the = new EnumMap(Op.class);

    public static BeliefTermType the(final Op o) {
        return the.computeIfAbsent(o, BeliefTermType::new);
    }

    BeliefTermType(Op o) {
        this.op = o;
        this.id = getClass().getSimpleName() + "[" + op.toString() + "]";
    }

    @Override public boolean test(final RuleMatch ruleMatch) {
        final Task b = getTask(ruleMatch.premise);
        if (b == null) return false;
        final Term t = b.getTerm();

        //if (t == null && op == null) return true;
        return (t.op() == op);
    }


    protected Task getTask(final Premise p) {
        return p.getBelief();
    }


    @Override
    public String toString() {
        return id;
    }


}
