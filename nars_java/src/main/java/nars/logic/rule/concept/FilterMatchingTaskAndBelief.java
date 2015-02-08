package nars.logic.rule.concept;

import nars.logic.FireConcept;
import nars.logic.LogicRule;
import nars.logic.entity.Sentence;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;
import nars.logic.nal1.LocalRules;
import nars.logic.rule.TaskFireTerm;


public class FilterMatchingTaskAndBelief extends ConceptFireRule {

    @Override public boolean apply(FireConcept f, TaskLink taskLink, TermLink termLink) {
        Sentence currentBelief = f.getCurrentBelief();
        if ((currentBelief!=null) && (LocalRules.match(taskLink.targetTask, currentBelief, f))) {
            //Filter this from further processing
            return false;
        }
        return true;
    }

}
