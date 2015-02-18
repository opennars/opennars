package nars.logic.reason.concept;

import nars.logic.Terms;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;
import nars.logic.reason.ConceptFire;

public class FilterEqualSubtermsInRespectToImageAndProduct extends ConceptFireTaskTerm {

    @Override
    public boolean apply(ConceptFire f, TaskLink taskLink, TermLink termLink) {
        if(Terms.equalSubTermsInRespectToImageAndProduct(taskLink.getTerm(), termLink.getTerm()))
            return false;
        return true;
    }
}
