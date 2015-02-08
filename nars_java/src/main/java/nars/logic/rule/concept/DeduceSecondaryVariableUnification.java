package nars.logic.rule.concept;

import nars.logic.CompositionalRules;
import nars.logic.FireConcept;
import nars.logic.entity.Sentence;
import nars.logic.entity.Task;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;

/**
* Created by me on 2/7/15.
*/
public class DeduceSecondaryVariableUnification extends ConceptFireRule {

    @Override
    public boolean apply(FireConcept f, TaskLink taskLink, TermLink termLink) {
        Task task = taskLink.getTarget();
        Sentence taskSentence = taskLink.getSentence();

        // to be invoked by the corresponding links
        if (CompositionalRules.dedSecondLayerVariableUnification(task, f)) {
            //unification ocurred, done reasoning in this cycle if it's judgment
            if (taskSentence.isJudgment())
                return false;
        }
        return true;
    }
}
