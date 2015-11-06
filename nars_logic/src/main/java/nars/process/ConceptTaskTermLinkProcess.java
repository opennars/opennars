package nars.process;

import nars.NAR;
import nars.Premise;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.nal.Deriver;
import nars.task.Task;

import java.util.function.Consumer;

/**
 * Created by me on 8/5/15.
 */
public class ConceptTaskTermLinkProcess extends ConceptProcess {

    protected final TermLink termLink;

    public ConceptTaskTermLinkProcess(NAR nar, Concept concept, TaskLink taskLink, TermLink termLink) {
        super(nar, concept, taskLink);

        this.termLink = termLink;

        final Concept beliefConcept = nar.concept(termLink.target);

        final Task task = taskLink.getTask();


        if (beliefConcept != null) {
            //belief can be null:
            Task belief = beliefConcept.getBeliefs().top(task, nar.time());

            if (belief!=null)
                belief = Premise.match(task, belief, this);

            updateBelief(belief);
        }


    }

    public ConceptTaskTermLinkProcess(NAR nar, Concept concept, TaskLink taskLink, TermLink termLink, Task belief) {
        this(nar, concept, taskLink, termLink);
        updateBelief(belief);
    }

    /**
     * @return the current termLink aka BeliefLink
     */
    @Override
    public final TermLink getTermLink() {
        return termLink;
    }


    @Override
    public final void derive(final Deriver p, Consumer<Task> t) {
        p.run(this, t);
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
                .append('[').append(getTask()).append(',')
                .append(getTermLink()).append(',').append(getBelief())
                .append(']')
                .toString();
    }

}
