package nars.process.concept;

import nars.link.TaskLink;
import nars.nal.LogicStage;
import nars.premise.Premise;
import nars.process.ConceptProcess;

/**
 * when a concept fires a tasklink but before a termlink is selected
 */
abstract public class ConceptFireTask implements LogicStage<Premise> {

    abstract public boolean apply(Premise f, TaskLink taskLink);

    @Override
    public final boolean test(final Premise f) {
        if (f.getTermLink()==null) {
            return apply(f, f.getTaskLink());
        }
        return true;
    }

}

