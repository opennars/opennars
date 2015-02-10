package nars.logic.reason.filter;

import nars.logic.NAL;
import nars.logic.entity.Sentence;
import nars.logic.entity.Stamp;
import nars.logic.entity.Task;
import nars.logic.entity.Term;
import nars.logic.nal1.Negation;

/** applies updates to stamp, and rejects derivations determined to be cyclic */
public class FilterCyclic implements NAL.DerivationFilter {
    @Override public String reject(NAL nal, Task task, boolean revised, boolean single, Task parent, Sentence occurence2, Sentence derivedCurrentBelief, Task derivedCurrentTask) {

        final Sentence occurence = parent != null ? parent.sentence : null;
        final Stamp stamp = task.sentence.stamp;
        Sentence currentBelief = nal.getCurrentBelief();

        if (occurence != null && !occurence.isEternal()) {
            stamp.setOccurrenceTime(occurence.getOccurenceTime());
        }
        if (occurence2 != null && !occurence2.isEternal()) {
            stamp.setOccurrenceTime(occurence2.getOccurenceTime());
        }


        final Term currentTaskContent = derivedCurrentTask.getTerm();
        if (derivedCurrentBelief != null && derivedCurrentBelief.isJudgment()) {
            final Term currentBeliefContent = derivedCurrentBelief.term;
            stamp.chainReplace(currentBeliefContent, currentBeliefContent);
        }
        //workaround for single premise task issue:
        if (currentBelief == null && single && task != null && task.sentence.isJudgment()) {
            stamp.chainReplace(currentTaskContent, currentTaskContent);
        }
        //end workaround
        if (task != null && !single && task.sentence.isJudgment()) {
            stamp.chainReplace(currentTaskContent,currentTaskContent);
        }

        //its a logic reason, so we have to do the derivation chain check to hamper cycles
        if (!revised) {
            Term taskTerm = task.getTerm();

            if (task.sentence.isJudgment()) {

                if (stamp.getChain().contains(taskTerm)) {
                    Term parentTaskTerm = task.getParentTask() != null ? task.getParentTask().getTerm() : null;
                    if ((parentTaskTerm == null) || (!Negation.areMutuallyInverse(taskTerm, parentTaskTerm))) {
                        return "Cyclic Reasoning";
                    }
                }
            }

        }

//        else {
//            //its revision, of course its cyclic, apply evidental base policy
//            final int stampLength = stamp.baseLength;
//            for (int i = 0; i < stampLength; i++) {
//                final long baseI = stamp.evidentialBase[i];
//                for (int j = 0; j < stampLength; j++) {
//                    if ((i != j) && (baseI == stamp.evidentialBase[j])) {
//                        throw new RuntimeException("Overlapping Revision Evidence: Should have been discovered earlier: " + Arrays.toString(stamp.evidentialBase));
//
//                        //memory.removeTask(task, "Overlapping Revision Evidence");
//                        //"(i=" + i + ",j=" + j +')' /* + " in " + stamp.toString()*/
//                        //return false;
//                    }
//                }
//            }
//        }

        return null;
    }
}
