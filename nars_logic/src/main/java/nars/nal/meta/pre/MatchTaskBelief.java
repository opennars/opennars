package nars.nal.meta.pre;

import com.gs.collections.api.tuple.Twin;
import nars.Op;
import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.nal.meta.TaskBeliefPair;
import nars.nal.meta.TermPattern;
import nars.term.variable.Variable;

import java.util.Collections;
import java.util.List;
import java.util.Set;


@Deprecated public class MatchTaskBelief extends PreCondition {

    //public final TaskBeliefPair pattern;

    final TermPattern compiled;

    final String id;

    public MatchTaskBelief(TaskBeliefPair pattern, Set<Twin<Variable>> notEqual) {

        //this.pattern = pattern;
        compiled = new TermPattern(Op.VAR_PATTERN, pattern, notEqual);

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

    @Override
    public void addConditions(List<PreCondition> l) {
        Collections.addAll(l, compiled.code);
    }

    @Override
    public final boolean test(RuleMatch m) {
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
