package nars.process.concept;

import nars.nal.LogicRule;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.process.ConceptProcess;

/** when a concept fires a tasklink that fires a termlink */
abstract public class ConceptFireTaskTerm implements LogicRule<ConceptProcess>  {


    abstract public boolean apply(ConceptProcess f, TaskLink taskLink, TermLink termLink);

    @Override
    public boolean test(final ConceptProcess f) {

        final TermLink ftl = f.getCurrentTermLink();

        if (ftl !=null) {
            return apply(f, f.getCurrentTaskLink(), ftl);
        }

        //continue by default
        return true;
    }

}

