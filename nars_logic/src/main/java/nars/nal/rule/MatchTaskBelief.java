package nars.nal.rule;

import nars.Symbols;
import nars.nal.*;
import nars.nal.process.ConceptProcess;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.nal1.LocalRules;
import nars.nal.nal7.TemporalRules;
import nars.nal.term.Term;


public class MatchTaskBelief extends ConceptFireTaskTerm {

    @Override public boolean apply(ConceptProcess f, TaskLink taskLink, TermLink termLink) {
        Task currentBelief = f.getCurrentBeliefTask();
        if (currentBelief!=null){
            if (match(taskLink.targetTask, currentBelief, f)) {
                //System.err.println("MatchTaskBelief: false: " + taskLink.targetTask + " : " + currentBelief);
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
     * called in RuleTables.rule
     *
     * @param task The task
     * @param belief The belief
     */
    public static boolean match(final Task task, final Task belief, final NAL nal) {
        final Sentence taskSentence = task.sentence;

        if (taskSentence.isJudgment()) {
            if (LocalRules.revisible(taskSentence, belief)) {
                return LocalRules.revision(task, belief, true, nal)!=null;
            }
        } else {
            if (TemporalRules.matchingOrder(taskSentence, belief)) {
                Term[] u = new Term[] {taskSentence.getTerm(), belief.getTerm()};
                if (Variables.unify(Symbols.VAR_QUERY, u, nal.memory.random)) {
                    //TODO see if this is correct because it will be producing
                    //a Task which isnt used
                    return LocalRules.trySolution(belief, task, nal)!=null;
                }
            }
        }
        return false;
    }
}
