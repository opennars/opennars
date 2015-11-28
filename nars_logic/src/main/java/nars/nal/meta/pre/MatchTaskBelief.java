package nars.nal.meta.pre;

import nars.Op;
import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.nal.meta.TaskBeliefPair;
import nars.term.transform.FindSubst;

import java.util.List;


@Deprecated public class MatchTaskBelief extends PreCondition {

    public final TaskBeliefPair pattern;

    final FindSubst.TermPattern compiled;

    final String id;

    public MatchTaskBelief(TaskBeliefPair pattern) {

        this.pattern = pattern;
        this.compiled = new FindSubst.TermPattern(Op.VAR_PATTERN, pattern);

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

    public void addConditions(List<PreCondition> l) {
        //l.add(this);
        for (PreCondition o : compiled.code) {
            l.add(o);
        }
    }

    public final boolean test(final RuleMatch m) {

        final TaskBeliefPair tb = m.taskBelief;

//        //if (!tb.substitutesMayExistParanoid(pattern)) {
//        if (!tb.substitutesMayExistFast(pattern)) {
//            return false;
//        }
//        return m.next(pattern, tb, m.unificationPower);

        //TODO parameterize the power by budget
        return m.next(compiled, tb, m.subst.power);

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
