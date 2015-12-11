package nars.nal.meta.op;

import nars.Premise;
import nars.Symbols;
import nars.nal.RuleMatch;
import nars.nal.meta.BeliefFunction;
import nars.nal.meta.DesireFunction;
import nars.nal.meta.PreCondition;
import nars.nal.meta.TruthOperator;
import nars.task.Task;
import nars.term.Term;

/**
 * Evaluates the truth of a premise
 */
public final class TruthEval extends PreCondition {
    public final Term beliefTerm, desireTerm;
    public final TruthOperator belief;
    public final TruthOperator desire;
    public final char puncOverride;

    private final transient String id;

    public TruthEval(Term beliefTerm, Term desireTerm, char puncOverride) {
        this.beliefTerm = beliefTerm;
        this.desireTerm = desireTerm;
        this.puncOverride = puncOverride;

        belief = BeliefFunction.get(beliefTerm);
        if (belief == null)
            throw new RuntimeException("unknown belief function " + beliefTerm);

        desire = DesireFunction.get(desireTerm);


        String beliefLabel = belief == null ? "_" :
                beliefTerm.toString();
        String desireLabel = desire == null ? "_" :
                desireTerm.toString();

        String sn = getClass().getSimpleName();
        id = puncOverride == 0 ?
                sn + ":(" + beliefLabel + ", " + desireLabel + ')' :
                sn + ":(" + beliefLabel + ", " + desireLabel + ", \"" + puncOverride + "\")";
    }

    @Override
    public String toString() {
        return id;
    }

//        final TruthOperator getTruth(char punc) {
//
//            switch (punc) {
//
//                case Symbols.JUDGMENT:
//                    return belief;
//
//                case Symbols.GOAL:
//                    return desire;
//
//            /*case Symbols.QUEST:
//            case Symbols.QUESTION:
//            */
//
//                default:
//                    return null;
//            }
//
//        }

    @Override
    public boolean test(RuleMatch m) {

        Premise premise = m.premise;


        /** calculate derived task truth value */


        Task task = premise.getTask();

        /** calculate derived task punctuation */
        char punct = puncOverride;
        if (punct == 0) {
            /** use the default policy determined by parent task */
            punct = task.getPunctuation();
        }

        m.punct.set(punct);

        if (punct == Symbols.JUDGMENT || punct == Symbols.GOAL) {
            return measureTruth(m, punct);
        }
        /*else {
            //question or quest, no truth is involved
            truth = null;
        }*/

        return true;
    }

    public boolean measureTruth(RuleMatch m, char punct) {
        TruthOperator tf = (punct == Symbols.JUDGMENT) ? belief : desire;
        if (tf == null)
            return false;

        /** filter cyclic double-premise results  */
        if (m.cyclic && !tf.allowOverlap()) {
            //                if (Global.DEBUG && Global.DEBUG_REMOVED_CYCLIC_DERIVATIONS) {
            //                    match.removeCyclic(outcome, premise, truth, punct);
            //                }
            return false;
        }

        return tf.apply(m);
    }
}
