package nars.nal.rule;

import nars.nal.Concept;
import nars.nal.ConceptProcess;
import nars.nal.Sentence;
import nars.nal.Terms;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;

public class FilterEqualSubtermsInRespectToImageAndProduct extends ConceptFireTaskTerm {

    @Override
    public boolean apply(ConceptProcess f, TaskLink taskLink, TermLink termLink) {
        if(Terms.equalSubTermsInRespectToImageAndProduct(taskLink.getTerm(), termLink.getTerm()))
            return false;

        final Concept beliefConcept = f.memory.concept(termLink.target);
        Sentence belief = (beliefConcept != null) ? beliefConcept.getBelief(f, f.getCurrentTask()) : null;
        f.setCurrentBelief( belief );  // may be null

        return true;
    }
}
