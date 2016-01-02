package nars.nal.meta.pre;

import com.google.common.collect.ListMultimap;
import nars.nal.PremiseMatch;
import nars.nal.meta.AtomicBooleanCondition;
import nars.nal.meta.TaskBeliefPair;
import nars.nal.meta.TermPattern;
import nars.term.Term;
import nars.term.constraint.MatchConstraint;

import java.util.Collection;
import java.util.Collections;
import java.util.List;


@Deprecated public class MatchTaskBelief extends AtomicBooleanCondition<PremiseMatch> {

    //public final TaskBeliefPair pattern;

    final TermPattern compiled;

    final String id;

    public MatchTaskBelief(TaskBeliefPair pattern, ListMultimap<Term, MatchConstraint> constraints) {

        //this.pattern = pattern;
        compiled = new TermPattern(pattern, constraints);

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

        id = getClass().getSimpleName() + '[' + pattern.toStringCompact() + ']';

    }

    public void addPreConditions(Collection<Term> l) {
        Collections.addAll(l, compiled.pre);
    }

    @Override
    public void addConditions(List<Term> l) {
        Collections.addAll(l, compiled.code);
    }

    @Override
    public boolean booleanValueOf(PremiseMatch m) {
        throw new RuntimeException("this should not be called");
    }

    @Override
    public final String toString() {
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
