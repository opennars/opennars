package nars.nal.rule;

import nars.Global;
import nars.Memory;
import nars.nal.*;
import nars.nal.concept.Concept;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.nal.term.Term;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;

import java.util.Set;

import static nars.nal.Terms.equalSubTermsInRespectToImageAndProduct;

/**
* Patrick's new version which 'restores the special reasoning context'
* new attempt/experiment to make nars effectively track temporal coherences
*/
public class TemporalInductionChain2 extends ConceptFireTaskTerm {

    @Override
    public boolean apply(ConceptProcess f, TaskLink taskLink, TermLink termLink) {

        if (!f.nal(7)) return true;

        final Sentence belief = f.getCurrentBelief();
        if (belief == null) return true;

        if (!belief.isJudgment()) return true;

        final Concept concept = f.getCurrentConcept();

        Task task = f.getCurrentTask();
        Sentence taskSentence = task.sentence;
        if (taskSentence.isEternal())
            return true;

        final Memory memory = f.memory;

        final Term beliefTerm = belief.getTerm();

        if (beliefTerm instanceof Implication &&
                (beliefTerm.getTemporalOrder() == TemporalRules.ORDER_FORWARD || beliefTerm.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT)) {

            final int chainSamples = Global.TEMPORAL_INDUCTION_CHAIN_SAMPLES;

            //prevent duplicate inductions
            Set<Object> alreadyInducted = Global.newHashSet(chainSamples);

            for (int i = 0; i < chainSamples; i++) {

                //TODO create and use a sampleNextConcept(NALOperator.Implication) method

                Concept next = memory.concepts.nextConcept();
                if (next == null || next.equals(concept)) continue;

                Term t = next.getTerm();

                if ((t instanceof Implication) && (alreadyInducted.add(t))) {

                    Sentence temporalBelief = next.getBestBelief(true, true);

                    //TODO: make this work if it is needed, but i think it just implements a restore point that is not needed due to the refactor
//                    ///SPECIAL REASONING CONTEXT FOR TEMPORAL INDUCTION
//                    Stamp SVSTamp=nal.getNewStamp();
//                    Sentence SVBelief=nal.getCurrentBelief();
//                    NAL.StampBuilder SVstampBuilder=nal.newStampBuilder;

                    //now set the current context:
//                    f.setCurrentBelief(temporalBelief);

                    if (temporalBelief!=null) {
                        if(!taskSentence.isEternal() && !temporalBelief.isEternal()) {
                            induct(f, task, taskSentence, memory, temporalBelief);
                        }
                    }

                }
            }
        }

        return true;

    }

    /** should only take non-eternalized beliefs? */
    private void induct(ConceptProcess f, Task task, Sentence taskSentence, Memory memory, Sentence otherBelief) {
        Sentence current, prev;

        if(otherBelief.after(taskSentence, memory.duration())) {
            current = otherBelief;
            prev = task.sentence;
        } else {
            current = task.sentence;
            prev = otherBelief;
        }

        temporalInductionProceed(current, prev, task, f);
    }

    public static boolean temporalInductionProceed(final Sentence currentBelief, final Sentence prevBelief, Task controllerTask, NAL nal) {
        if(!controllerTask.isParticipatingInTemporalInduction()) { //todo refine, add directbool in task
            return false;
        }

        if (currentBelief.isEternal() || !TemporalRules.isInputOrTriggeredOperation(controllerTask, nal.memory)) {
            return false;
        }

        //temporal inductions for judgments only. this should always be the case
        /*if(!currentBelief.isJudgment() || !prevBelief.isJudgment())
            return false;*/

        if (equalSubTermsInRespectToImageAndProduct(currentBelief.term, prevBelief.term)) {
            return false;
        }

        //if(newEvent.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY)
        TemporalRules.temporalInduction(currentBelief, prevBelief,
                nal.newStamp(currentBelief, prevBelief),
                nal, controllerTask);
        return false;
    }


}
