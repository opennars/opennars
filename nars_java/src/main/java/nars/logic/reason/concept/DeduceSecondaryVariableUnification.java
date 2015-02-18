package nars.logic.reason.concept;

import nars.logic.CompositionalRules;
import nars.logic.entity.Sentence;
import nars.logic.entity.Task;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;
import nars.logic.reason.ConceptFire;

/**
* Created by me on 2/7/15.
*/
public class DeduceSecondaryVariableUnification extends ConceptFireTaskTerm {

    //TODO decide if f.currentBelief needs to be checked for null like it was originally

    @Override
    public boolean apply(ConceptFire f, TaskLink taskLink, TermLink termLink) {
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
