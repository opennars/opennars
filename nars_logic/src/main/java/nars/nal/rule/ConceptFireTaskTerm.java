package nars.nal.rule;

import nars.nal.LogicRule;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.process.ConceptProcess;

/** when a concept fires a tasklink that fires a termlink */
abstract public class ConceptFireTaskTerm extends LogicRule<ConceptProcess>  {


    abstract public boolean apply(ConceptProcess f, TaskLink taskLink, TermLink termLink);

    @Override
    public boolean accept(ConceptProcess f) {
        if (f.getCurrentTermLink()!=null) {
            if (!apply(f, f.getCurrentTaskLink(), f.getCurrentTermLink()))
                return false;
        }
        return true;
    }

}

