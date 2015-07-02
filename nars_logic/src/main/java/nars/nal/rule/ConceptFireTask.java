package nars.nal.rule;

import nars.nal.LogicRule;
import nars.link.TaskLink;
import nars.nal.process.ConceptProcess;

/**
 * when a concept fires a tasklink but before a termlink is selected
 */
abstract public class ConceptFireTask implements LogicRule<ConceptProcess> {



    abstract public boolean apply(ConceptProcess f, TaskLink taskLink);

    @Override
    public boolean accept(final ConceptProcess f) {
        if (f.getCurrentTermLink()==null) {
            return apply(f, f.getCurrentTaskLink());
        }
        return true;
    }

}

