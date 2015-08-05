package nars.process.concept;

import nars.link.TermLink;
import nars.nal.LogicStage;
import nars.process.ConceptProcess;

/** when a concept fires a tasklink that fires a termlink */
abstract public class ConceptFireTaskTerm implements LogicStage<ConceptProcess> {


    abstract public boolean apply(ConceptProcess f, TermLink termLink);

    @Override
    public final boolean test(final ConceptProcess f) {

        final TermLink ftl = f.getTermLink();

        if (ftl !=null) {
            return apply(f, ftl);
        }

        //continue by default
        return true;
    }

}

