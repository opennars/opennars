package nars.logic.reason.concept;

import nars.io.Symbols;
import nars.logic.NAL;
import nars.logic.Variables;
import nars.logic.entity.*;
import nars.logic.nal1.LocalRules;
import nars.logic.nal7.TemporalRules;
import nars.logic.reason.ConceptProcess;


public class MatchTaskBelief extends ConceptFireTaskTerm {

    @Override public boolean apply(ConceptProcess f, TaskLink taskLink, TermLink termLink) {
        Sentence currentBelief = f.getCurrentBelief();
        if (currentBelief!=null){
            if (match(taskLink.targetTask, currentBelief, f)) {
                //Filter this from further processing
                return false;
            }
        }
        return true;
    }


    /* -------------------- same contents -------------------- */
    /**
     * The task and belief have the same content
     * <p>
     * called in RuleTables.reason
     *
     * @param task The task
     * @param belief The belief
     */
    public static boolean match(final Task task, final Sentence belief, final NAL nal) {
        final Sentence taskSentence = task.sentence;

        if (taskSentence.isJudgment()) {
            if (LocalRules.revisible(taskSentence, belief)) {
                return LocalRules.revision(taskSentence, belief, true, nal);
            }
        } else {
            if (TemporalRules.matchingOrder(taskSentence, belief)) {
                Term[] u = new Term[] { taskSentence.term, belief.term };
                if (Variables.unify(Symbols.VAR_QUERY, u)) {
                    return LocalRules.trySolution(belief, task, nal);
                }
            }
        }
        return false;
    }
}
