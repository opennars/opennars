package nars.process;

import nars.NAR;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.task.Task;

import java.util.function.Function;
import java.util.stream.Stream;

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
                belief = Unification.match(task, belief, this);

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
    public TermLink getTermLink() {
        return termLink;
    }


    @Override
    public final Stream<Task> derive(final Function<ConceptProcess,Stream<Task>> p) {
        nar.memory.eventConceptProcess.emit(this);
        return p.apply(this);
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
