package nars.process;

import nars.NAR;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.premise.Premise;

import java.util.function.Consumer;

/**
 * Created by me on 8/5/15.
 */
@Deprecated public class ConceptTaskLinkProcess extends ConceptProcess {

    public ConceptTaskLinkProcess(NAR nar, Concept concept, TaskLink taskLink) {
        super(nar, concept, taskLink);
    }

    @Override
    protected void derive(Consumer<Premise> processor) {
        processor.accept(this);
    }

    @Override
    public TermLink getTermLink() {
        return null;
    }
}
