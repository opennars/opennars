package nars.process.concept;

import nars.Symbols;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.nal.nal1.LocalRules;
import nars.nal.nal7.TemporalRules;
import nars.process.ConceptProcess;
import nars.process.NAL;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Term;
import nars.term.Variables;


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


        if (task.isJudgment()) {
            if (LocalRules.revisible(task, belief)) {
                return LocalRules.revision(task, belief, true, nal)!=null;
            }
        } else {
            if (TemporalRules.matchingOrder(task, belief)) {
                Term[] u = new Term[] {task.getTerm(), belief.getTerm()};
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
