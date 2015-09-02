package nars.concept.experimental;

import nars.Events;
import nars.Global;
import nars.Memory;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.concept.BeliefTable;
import nars.concept.DefaultConcept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.premise.Premise;
import nars.premise.PremiseGenerator;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Term;

import java.util.List;

/**
 * Created by me on 8/19/15.
 */
public class AxiomatizableConcept extends DefaultConcept {

    public AxiomatizableConcept(Term term, Budget b, Bag<Sentence, TaskLink> taskLinks, Bag<TermLinkKey, TermLink> termLinks, BeliefTable.RankBuilder rb, PremiseGenerator ps, Memory memory) {
        super(term, b, taskLinks, termLinks, rb, ps, memory);
    }

    @Override
    public boolean processBelief(final Premise nal, Task belief) {

        {
            /** contradicting a constant concept's only belief */
            if (isConstant() && hasBeliefs()) {
                Task topBelief = getBeliefs().top();
                if (!topBelief.getTruth().equals(belief.getTruth())) {

                    //TODO allow reversing constant status if confidence is == 0 */
                    getMemory().emit(Events.OUT.class, belief + " contradicts constant concept belief: " + topBelief);
                    belief.getBudget().setPriority(0);
                }

                return false;
            }

            /** 100% confidence for a blank concept, set to constant */
            if (!isConstant() && belief.getConfidence() >= 1f) {
                if (hasBeliefs()) {
                    getMemory().emit(Events.OUT.class, belief + " axiom replacing existing beliefs: " + getBeliefs());
                    getBeliefs().clear();
                    List<TaskLink> toRemove = Global.newArrayList();
                    getTaskLinks().forEach(t -> {
                        if (t.getTarget().equals(getTerm()))
                            toRemove.add(t);
                    });
                    toRemove.forEach(t -> getTaskLinks().remove(t.targetTask));
                }
                setConstant(true);
            }
        }

        return super.processBelief(nal, belief);
    }

}
