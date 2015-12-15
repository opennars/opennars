package nars.process;

import nars.NAR;
import nars.Premise;
import nars.bag.BagBudget;
import nars.concept.Concept;
import nars.task.Task;
import nars.term.Termed;

/**
 * Created by me on 8/5/15.
 */
public class ConceptTaskTermLinkProcess extends ConceptProcess {

    protected final BagBudget<Termed> termLink;

    public ConceptTaskTermLinkProcess(NAR nar, Concept concept, BagBudget<Task> taskLink, BagBudget<Termed> termLink) {
        this(nar, concept, taskLink, termLink, null);
    }

    public ConceptTaskTermLinkProcess(NAR nar, Concept concept, BagBudget<Task> taskLink, BagBudget<Termed> termLink, Task belief) {
        super(nar, concept, taskLink);

        this.termLink = termLink;

        Task task = taskLink.get();

        if (belief == null) {
            Concept beliefConcept = nar.concept(termLink.get());
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
    public final BagBudget<Termed> getTermLink() {
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
