package nars.nal.rule;

import nars.task.Task;
import nars.term.Terms;
import nars.concept.Concept;
import nars.nal.process.ConceptProcess;
import nars.link.TaskLink;
import nars.link.TermLink;

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
