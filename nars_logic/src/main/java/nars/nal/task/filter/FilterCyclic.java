//package nars.logic.reason.filter;
//
//import nars.logic.NAL;
//import nars.logic.entity.Sentence;
//import nars.logic.entity.stamp.Stamp;
//import nars.logic.entity.Task;
//import nars.logic.entity.Term;
//import nars.logic.nal1.Negation;
//
///** applies updates to stamp, and rejects derivations determined to be cyclic */
//public class FilterCyclic implements NAL.DerivationFilter {
//    @Override public String reject(NAL nal, Task task, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {
//
//        final Stamp<Sentence> stamp = task.sentence.stamp;
//
//        final Term currentTaskContent = currentTask.getTerm();
//        if (currentBelief != null && currentBelief.isJudgment()) {
//            //final Term currentBeliefContent = currentBelief.term;
//            stamp.chainReplace(currentBelief, currentBelief);
//        }
//        //workaround for single premise task issue:
//        if (
//                (currentBelief == null && single && task != null && task.sentence.isJudgment())
//                         ||
//                (task != null && !single && task.sentence.isJudgment())
//           )        {
//            stamp.chainReplace(currentTask.sentence, currentTask.sentence);
//        }
//
//        //its a logic rule, so we have to do the derivation chain check to hamper cycles
//        if (!revised) {
//            Term taskTerm = task.getTerm();
//
//            if (task.sentence.isJudgment()) {
//
//                if (stamp.getChain().contains(task.sentence)) {
//                    Term parentTaskTerm = task.getParentTask() != null ? task.getParentTask().getTerm() : null;
//                    if ((parentTaskTerm == null) || (!Negation.areMutuallyInverse(taskTerm, parentTaskTerm))) {
//                        return "Cyclic Reasoning";
//                    }
//                }
//            }
//
//        }
//
//        return null;
//    }
//}
//
////        else {
////            //its revision, of course its cyclic, apply evidental base policy
////            final int stampLength = stamp.baseLength;
////            for (int i = 0; i < stampLength; i++) {
////                final long baseI = stamp.evidentialBase[i];
////                for (int j = 0; j < stampLength; j++) {
////                    if ((i != j) && (baseI == stamp.evidentialBase[j])) {
////                        throw new RuntimeException("Overlapping Revision Evidence: Should have been discovered earlier: " + Arrays.toString(stamp.evidentialBase));
////
////                        //memory.removeTask(task, "Overlapping Revision Evidence");
////                        //"(i=" + i + ",j=" + j +')' /* + " in " + stamp.toString()*/
////                        //return false;
////                    }
////                }
////            }
////        }