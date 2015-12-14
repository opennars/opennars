package nars.process;

import nars.NAR;
import nars.Premise;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.task.Task;

/**
 * Created by me on 8/5/15.
 */
public class ConceptTaskTermLinkProcess extends ConceptProcess {

    protected final TermLink termLink;

    public ConceptTaskTermLinkProcess(NAR nar, Concept concept, TaskLink taskLink, TermLink termLink) {
        this(nar, concept, taskLink, termLink, null);
    }

    public ConceptTaskTermLinkProcess(NAR nar, Concept concept, TaskLink taskLink, TermLink termLink, Task belief) {
        super(nar, concept, taskLink);

        this.termLink = termLink;

        Task task = taskLink.getTask();

        if (belief == null) {
            Concept beliefConcept = nar.concept(termLink.target);
            if (beliefConcept != null) {

                belief = beliefConcept.getBeliefs().top(task, nar.time());

                if (belief != null)
                    belief = Premise.match(task, belief, nar);
            }
        }

        //belief can be null:
        if (belief!=null)
            updateBelief(belief);

    }


    /**
     * @return the current termLink aka BeliefLink
     */
    @Override
    public final TermLink getTermLink() {
        return termLink;
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
        return new StringBuilder().append(
                getClass().getSimpleName())
                .append('[').append(getConcept()).append(',')
                            .append(getTaskLink()).append(',')
                            .append(getTermLink()).append(',')
                            .append(getBelief())
                .append(']')
                .toString();
    }

}
