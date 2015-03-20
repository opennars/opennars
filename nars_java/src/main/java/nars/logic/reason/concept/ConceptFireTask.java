package nars.logic.reason.concept;

import nars.logic.LogicRule;
import nars.logic.entity.TaskLink;
import nars.logic.reason.ConceptProcess;

/**
 * when a concept fires a tasklink but before a termlink is selected
 */
abstract public class ConceptFireTask extends LogicRule<ConceptProcess> {



    abstract public boolean apply(ConceptProcess f, TaskLink taskLink);

    @Override
    public boolean accept(ConceptProcess f) {
        if (f.getCurrentTermLink()==null) {
            boolean result = apply(f, f.getCurrentTaskLink());
            if (!result) {
                return false;
            }
        }
        return true;
    }

}

