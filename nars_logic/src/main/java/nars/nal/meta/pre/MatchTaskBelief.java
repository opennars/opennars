package nars.nal.meta.pre;

import nars.Global;
import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.nal.meta.TaskBeliefPair;


public class MatchTaskBelief extends PreCondition {

    public final TaskBeliefPair pattern;

    final String id;

    public MatchTaskBelief(TaskBeliefPair pattern) {

        this.pattern = pattern;
        //Term beliefPattern = pattern.term(1);

        //if (Global.DEBUG) {
//            if (beliefPattern.structure() == 0) {

                // if nothing else in the rule involves this term
                // which will be a singular VAR_PATTERN variable
                // then allow null
//                if (beliefPattern.op() != Op.VAR_PATTERN)
//                    throw new RuntimeException("not what was expected");

//            }
        //}

        /*System.out.println( Long.toBinaryString(
                        pStructure) + " " + pattern
        );*/

        this.id = getClass().getSimpleName() + '[' + pattern.toStringCompact() + ']';

    }


    public final boolean test(final RuleMatch m) {

        final TaskBeliefPair tb = m.taskBelief;

        final TaskBeliefPair pattern = this.pattern;

        //if (!tb.substitutesMayExistParanoid(pattern)) {
        if (!tb.substitutesMayExistFast(pattern)) {
            return false;
        }

        //TODO parameterize the power by budget
        return m.next(pattern, tb, Global.UNIFICATION_POWER);
    }

    @Override
    final public String toString() {
        return id;
    }


}
//    @Override public final boolean test(final RuleMatch m) {
//
//        boolean sameAsPrevPattern =
//                (m.prevRule!=null) && (m.prevRule.pattern.equals(m.rule.pattern);
//
//        if (!m.prevXY.isEmpty()) {
//            //re-use previous rule's result
//            m.xy.putAll(m.prevXY);
//            m.yx.putAll(m.prevYX);
//            return true;
//        }
//        else {
//            boolean b = _test(m);
//            if (b) {
//
//            }
//            else {
//                //put a placeholder to signal that this does not succeed
//            }
//
//        }
//
//
//        if
//                this.prevXY.putAll(xy);
//                this.prevYX.putAll(yx);
//            }
//            else {
//                this.prevXY.clear(); this.prevYX.clear();
//            }
//        }
//    }
