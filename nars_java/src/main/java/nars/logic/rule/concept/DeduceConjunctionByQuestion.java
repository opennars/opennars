package nars.logic.rule.concept;

import nars.logic.CompositionalRules;
import nars.logic.FireConcept;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;

/**
* Created by me on 2/7/15.
*/
public class DeduceConjunctionByQuestion extends ConceptFireRule {

    @Override
    public boolean apply(FireConcept f, TaskLink taskLink, TermLink termLink) {
        CompositionalRules.dedConjunctionByQuestion(
                taskLink.getSentence(), f.getCurrentBelief(), f);
        return true;
    }
}
