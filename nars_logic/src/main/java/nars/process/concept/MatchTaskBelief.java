//package nars.process.concept;
//
//import nars.Op;
//import nars.link.TermLink;
//import nars.nal.nal1.LocalRules;
//import nars.nal.nal7.TemporalRules;
//import nars.process.ConceptProcess;
//import nars.process.NAL;
//import nars.task.Task;
//
//
//public class MatchTaskBelief extends ConceptFireTaskTerm {
//
//    @Override
//    public final boolean apply(ConceptProcess f, TermLink termLink) {
//        Task currentBelief = f.getBelief();
//        if ((currentBelief != null) &&
//                (match(f.getTask(), currentBelief, f))) {
//
//            //Unification occurred, Filter this from further processing
//            return STOP;
//        }
//        return CONTINUE;
//    }
//
//
//    /* -------------------- same contents -------------------- */
//
//    /**
//     * The task and belief have the same content
//     * <p>
//     * called in RuleTables.rule
//     *
//     * @param task   The task
//     * @param belief The belief
//     */
//    public static boolean match(final Task task, final Task belief, final NAL nal) {
//
//
//        if (task.isJudgment() ) {
//            if (LocalRules.revisible(task, belief)) {
//                return LocalRules.revision(task, belief, true, nal) != null;
//            }
//        } else {
//            /* if goal question or quest */
//            if (TemporalRules.matchingOrder(task, belief)) {
//                if (nal.unify(Op.VAR_QUERY, task.getTerm(), belief.getTerm())) {
//                    //TODO see if this is correct because it will be producing
//                    //a Task which isnt used
//                    return LocalRules.trySolution(belief, task, nal) != null;
//                }
//            }
//        }
//        return false;
//    }
//}
