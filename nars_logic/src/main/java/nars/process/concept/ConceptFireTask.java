package nars.process.concept;

import nars.link.TaskLink;
import nars.nal.LogicStage;
import nars.premise.Premise;

/**
 * when a concept fires a tasklink but before a termlink is selected
 */
@Deprecated abstract public class ConceptFireTask<P extends Premise> implements LogicStage<P> {

    abstract public boolean apply(P f, TaskLink taskLink);

    @Override
    public final boolean test(final P f) {
        throw new RuntimeException("deprecated");
//        if (f.getTermLink()==null) {
//            return apply(f, f.getTaskLink());
//        }
//          return true;
    }

}

