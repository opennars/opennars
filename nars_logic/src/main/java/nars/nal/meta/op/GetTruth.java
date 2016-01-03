package nars.nal.meta.op;

import nars.Premise;
import nars.Symbols;
import nars.nal.RuleMatch;
import nars.nal.meta.BeliefFunction;
import nars.nal.meta.DesireFunction;
import nars.nal.meta.PreCondition;
import nars.nal.meta.TruthFunction;
import nars.task.Task;
import nars.truth.ProjectedTruth;
import nars.truth.Truth;
import nars.truth.TruthFunctions;

/**
 * Created by me on 11/28/15.
 */
public class GetTruth extends PreCondition {
    public final BeliefFunction belief;
    public final DesireFunction desire;
    public final char puncOverride;

    transient private final String id;

    public GetTruth(BeliefFunction belief, DesireFunction desire, char puncOverride) {
        this.belief = belief;
        this.desire = desire;
        this.puncOverride = puncOverride;

        this.id = (puncOverride == 0) ?
                (getClass().getSimpleName() + "[" + belief + "," + desire + "]")  :
                (getClass().getSimpleName() + "[" + belief + "," + desire + "," + puncOverride + "]");

    }

    @Override
    public String toString() {
        return id;
    }

    TruthFunction getTruth(char punc) {

        switch (punc) {

            case Symbols.JUDGMENT:
                return belief;

            case Symbols.GOAL:
                return desire;

            /*case Symbols.QUEST:
            case Symbols.QUESTION:
            */

            default:
                return null;
        }

    }

    @Override
    public boolean test(RuleMatch match) {

        Premise premise = match.premise;

        final Task task = premise.getTask();

        /** calculate derived task truth value */


        Task belief = premise.getBelief();
        ProjectedTruth projtruth = null;
        if(belief != null) {
            projtruth = belief.projection(task.getOccurrenceTime(), match.premise.memory().time());
        }


        final Truth T = task.getTruth();
        Truth B = belief == null ? null : belief.getTruth();

        //we always project the belief truth to the task truth except in case where measure_time is used
        if(/*match.rule.project_eternalize &&*/ belief!=null) {
            B = projtruth;
        }

        /** calculate derived task punctuation */
        char punct = puncOverride;
        if (punct == 0) {
            /** use the default policy determined by parent task */
            punct = task.getPunctuation();
        }


        final Truth truth;
        TruthFunction tf;

        if (punct == Symbols.JUDGMENT || punct == Symbols.GOAL) {
            tf = getTruth(punct);
            if (tf == null)
                return false;

            truth = tf.get(T, B);

            if (truth == null) {
                //no truth value function was applicable but it was necessary, abort
                return false;
            }
        } else {
            //question or quest, no truth is involved
            truth = null;
            tf = null;
        }

        /** eliminate cyclic double-premise results
         *  TODO move this earlier to precondition check, or change to altogether new policy
         */
        final boolean single = (belief == null);
        if ((!single) && (RuleMatch.cyclic(tf, premise))) {
//                if (Global.DEBUG && Global.DEBUG_REMOVED_CYCLIC_DERIVATIONS) {
//                    match.removeCyclic(outcome, premise, truth, punct);
//                }
            return false;
        }

        RuleMatch.PostMods post = match.post;
        post.truth = truth;
        post.punct = punct;

        return true;
    }
}
