package nars.nal.rule;

import nars.nal.Terms;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.ConceptProcess;

public class FilterEqualSubtermsInRespectToImageAndProduct extends ConceptFireTaskTerm {

    @Override
    public boolean apply(ConceptProcess f, TaskLink taskLink, TermLink termLink) {
        if(Terms.equalSubTermsInRespectToImageAndProduct(taskLink.getTerm(), termLink.getTerm()))
            return false;
        return true;
    }
}
