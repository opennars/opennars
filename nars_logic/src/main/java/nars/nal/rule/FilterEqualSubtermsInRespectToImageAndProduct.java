package nars.nal.rule;

import nars.nal.Task;
import nars.nal.Terms;
import nars.nal.concept.Concept;
import nars.nal.process.ConceptProcess;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;

public class FilterEqualSubtermsInRespectToImageAndProduct extends ConceptFireTaskTerm {

    @Override
    public boolean apply(ConceptProcess f, TaskLink taskLink, TermLink termLink) {
        if(Terms.equalSubTermsInRespectToImageAndProduct(taskLink.getTerm(), termLink.getTerm()))
            return false;

        final Concept beliefConcept = f.memory.concept(termLink.target);
        if (beliefConcept!=null) {

            Task t = beliefConcept.getBelief(f, f.getCurrentTask());
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
