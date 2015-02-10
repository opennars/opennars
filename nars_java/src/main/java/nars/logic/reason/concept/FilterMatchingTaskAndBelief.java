package nars.logic.reason.concept;

import nars.logic.reason.ConceptFire;
import nars.logic.entity.Sentence;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;
import nars.logic.nal1.LocalRules;


public class FilterMatchingTaskAndBelief extends ConceptFireTaskTerm {

    @Override public boolean apply(ConceptFire f, TaskLink taskLink, TermLink termLink) {
        Sentence currentBelief = f.getCurrentBelief();
        if ((currentBelief!=null) && (LocalRules.match(taskLink.targetTask, currentBelief, f))) {
            //Filter this from further processing
            return false;
        }
        return true;
    }

}
