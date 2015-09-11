package nars.process;

import nars.NAR;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;

/**
 * Created by me on 8/5/15.
 */
public class ConceptTaskLinkProcess extends ConceptProcess {

    public ConceptTaskLinkProcess(NAR nar, Concept concept, TaskLink taskLink) {
        super(nar, concept, taskLink);
    }

    @Override
    protected void derive() {
        nar().mem().getDeriver().fire(this);
    }

    @Override
    public TermLink getTermLink() {
        return null;
    }
}
