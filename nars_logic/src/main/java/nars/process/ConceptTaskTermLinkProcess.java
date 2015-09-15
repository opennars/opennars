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
public class ConceptTaskTermLinkProcess extends ConceptProcess {

    protected final TermLink termLink;

    public ConceptTaskTermLinkProcess(NAR nar, Concept concept, TaskLink taskLink, TermLink termLink) {
        super(nar, concept, taskLink);

        this.termLink = termLink;
    }

    /**
     * @return the current termLink aka BeliefLink
     */
    @Override
    public TermLink getTermLink() {
        return termLink;
    }


    @Override
    protected void derive(Consumer<Premise> p) {

        //final Memory memory = nar().memory();

        p.accept(this);

    }

//    /**
//     * the current termlink / belieflink's concept
//     */
//    public Concept getTermLinkConcept() {
//        final TermLink tl = getTermLink();
//        if (tl != null) {
//            return concept(tl.getTerm());
//        }
//        return null;
//    }

    @Override
    public String toString() {
        return new StringBuilder().append(getClass().getSimpleName())
                .append("[").append(getTask()).append(',')
                .append(getTermLink()).append(",").append(getBelief())
                .append(']')
                .toString();
    }

}
