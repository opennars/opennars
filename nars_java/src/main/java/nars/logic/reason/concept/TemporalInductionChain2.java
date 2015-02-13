package nars.logic.reason.concept;

import nars.core.Memory;
import nars.core.Parameters;
import nars.logic.entity.*;
import nars.logic.nal5.Implication;
import nars.logic.nal7.TemporalRules;
import nars.logic.reason.ConceptFire;

import java.util.Set;

/**
* Patrick's new version which 'restores the special reasoning context'
* new attempt/experiment to make nars effectively track temporal coherences
*/
public class TemporalInductionChain2 extends ConceptFireTaskTerm {

    @Override
    public boolean apply(ConceptFire f, TaskLink taskLink, TermLink termLink) {

        if (!f.nal(7)) return true;

        final Sentence belief = f.getCurrentBelief();
        if (belief == null) return true;

        final Concept concept = f.getCurrentConcept();

        Task task = f.getCurrentTask();
        Sentence taskSentence = task.sentence;
        if (!taskSentence.isEternal())
            return true;

        final Memory memory = f.memory;

        final Term beliefTerm = belief.getTerm();

        if (beliefTerm instanceof Implication &&
                (beliefTerm.getTemporalOrder() == TemporalRules.ORDER_FORWARD || beliefTerm.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT)) {

            final int chainSamples = Parameters.TEMPORAL_INDUCTION_CHAIN_SAMPLES;

            //prevent duplicate inductions
            Set<Object> alreadyInducted = Parameters.newHashSet(chainSamples);

            for (int i = 0; i < chainSamples; i++) {

                //TODO create and use a sampleNextConcept(NALOperator.Implication) method

                Concept next = memory.concepts.sampleNextConcept();
                if (next == null || next.beliefsTemporal.isEmpty() || next.equals(concept)) continue;

                Term t = next.getTerm();

                if ((t instanceof Implication) && (alreadyInducted.add(t))) {

                    Sentence otherBelief = next.getBestBelief(false);
                    if (otherBelief!=null) {
                        Sentence current, prev;

                        if(otherBelief.after(taskSentence, memory.getDuration())) {
                            current = otherBelief;
                            prev = task.sentence;
                        } else {
                            current = task.sentence;
                            prev = otherBelief;
                        }

                        TemporalRules.temporalInductionProceed(current, prev, task, f);
                    }

                }
            }
        }

        return true;

    }
}
