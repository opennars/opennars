package nars.logic.rule.concept;

import nars.logic.FireConcept;
import nars.logic.LogicRule;
import nars.logic.Terms;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;
import nars.logic.rule.TaskFireTerm;

public class FilterEqualSubtermsInRespectToImageAndProduct extends ConceptFireRule {

    @Override
    public boolean apply(FireConcept f, TaskLink taskLink, TermLink termLink) {
        if(Terms.equalSubTermsInRespectToImageAndProduct(taskLink.getTerm(), termLink.getTerm()))
            return false;
        return true;
    }
}
