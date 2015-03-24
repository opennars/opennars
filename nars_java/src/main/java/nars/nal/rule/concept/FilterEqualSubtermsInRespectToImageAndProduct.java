package nars.nal.rule.concept;

import nars.nal.Terms;
import nars.nal.entity.TaskLink;
import nars.nal.entity.TermLink;
import nars.nal.rule.ConceptProcess;

public class FilterEqualSubtermsInRespectToImageAndProduct extends ConceptFireTaskTerm {

    @Override
    public boolean apply(ConceptProcess f, TaskLink taskLink, TermLink termLink) {
        if(Terms.equalSubTermsInRespectToImageAndProduct(taskLink.getTerm(), termLink.getTerm()))
            return false;
        return true;
    }
}
