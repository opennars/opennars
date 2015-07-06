package nars.process.concept;

import nars.concept.Concept;
import nars.link.TermLink;
import nars.process.ConceptProcess;
import nars.task.Task;
import nars.term.Terms;

public class FilterEqualSubtermsInRespectToImageAndProduct extends ConceptFireTaskTerm {

    @Override
    public boolean apply(ConceptProcess f, TermLink termLink) {
        if(Terms.equalSubTermsInRespectToImageAndProduct(f.getTaskLink().getTerm(), termLink.getTerm()))
            return false;

        final Concept beliefConcept = f.memory.concept(termLink.target);
        if (beliefConcept!=null) {

            Task t = beliefConcept.getBeliefs().top(f.getTask(), f.time());

            if (t!=null)
                f.setCurrentBelief(t);
            else
                f.setCurrentBelief( null );

            return true;
        }

        //f.setCurrentBelief( null );

        return true;
    }
}
