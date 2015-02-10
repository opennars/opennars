package nars.logic.reason.concept;

import nars.core.Memory;
import nars.core.Parameters;
import nars.logic.reason.ConceptFire;
import nars.logic.entity.*;
import nars.logic.nal5.Implication;
import nars.logic.nal7.TemporalRules;

import java.util.Set;

/**
* Created by me on 2/7/15.
*/
public class TemporalInductionChain extends ConceptFireTaskTerm {


    @Override
    public boolean apply(ConceptFire f, TaskLink taskLink, TermLink termLink) {

        if (!f.nal(7)) return true;
        final Sentence belief = f.getCurrentBelief();
        if (belief == null) return true;

        final Memory memory = f.memory;

        final Term beliefTerm = belief.getTerm();
        Set<Object> alreadyInducted = null;

        //this is a new attempt/experiment to make nars effectively track temporal coherences
        if (beliefTerm instanceof Implication &&
                (beliefTerm.getTemporalOrder() == TemporalRules.ORDER_FORWARD || beliefTerm.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT)) {

            final int chainSamples = Parameters.TEMPORAL_INDUCTION_CHAIN_SAMPLES;

            //prevent duplicate inductions
            if (alreadyInducted == null)
                alreadyInducted = Parameters.newHashSet(chainSamples);
            else
                alreadyInducted.clear();

            for (int i = 0; i < chainSamples; i++) {

                Concept next = memory.concepts.sampleNextConcept();
                if (next == null) continue;

                Term t = next.getTerm();

                if ((t instanceof Implication) && (alreadyInducted.add(t))) {

                    Implication implication = (Implication) t;

                    if (!next.beliefs.isEmpty() && (implication.isForward() || implication.isConcurrent())) {

                        Sentence s = next.beliefs.get(0);

                        TemporalRules.temporalInductionChain(s, belief, f);
                        TemporalRules.temporalInductionChain(belief, s, f);

                    }
                }
            }
        }

        return true;

    }
}
