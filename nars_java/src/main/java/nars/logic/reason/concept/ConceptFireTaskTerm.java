package nars.logic.reason.concept;

import nars.logic.LogicRule;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;
import nars.logic.reason.ConceptProcess;

/** when a concept fires a tasklink that fires a termlink */
abstract public class ConceptFireTaskTerm extends LogicRule<ConceptProcess>  {


    abstract public boolean apply(ConceptProcess f, TaskLink taskLink, TermLink termLink);

    @Override
    public boolean accept(ConceptProcess f) {
        if (f!=null && f.getCurrentTermLink()!=null) {
            if (!apply(f, f.getCurrentTaskLink(), f.getCurrentTermLink()))
                return false;
        }
        return true;
    }

}

