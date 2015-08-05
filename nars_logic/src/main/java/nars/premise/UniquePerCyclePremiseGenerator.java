package nars.premise;

import com.gs.collections.api.tuple.Pair;
import nars.Global;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.task.Sentence;
import nars.term.Term;

import java.lang.ref.SoftReference;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by me on 7/18/15.
 */
public class UniquePerCyclePremiseGenerator extends TermLinkBagPremiseGenerator {

    long prevCycle = -1;

    /** how many cycles after which to clear the history and allow a repeat */
    final int clearAfterCycles;

    /** use a soft reference so that this doesnt become attached to any term/sentence pair contents, if it becomes inactive */
    transient SoftReference<Set<Pair<Term,Sentence>>> premisesThisCycle;

    public UniquePerCyclePremiseGenerator(AtomicInteger maxSelectionAttempts) {
        this(maxSelectionAttempts, 1);
    }

    public UniquePerCyclePremiseGenerator(AtomicInteger maxSelectionAttempts, int clearAfterCycles) {
        super(maxSelectionAttempts);
        this.clearAfterCycles = clearAfterCycles;
    }

    @Override
    public boolean validTermLinkTarget(Concept concept, TaskLink c, TermLink t) {
        if (super.validTermLinkTarget(concept, c, t)) {
            long now = concept.time();


            Set<Pair<Term,Sentence>> s = null;
            if (premisesThisCycle != null)
                s = premisesThisCycle.get();

            if (s == null) {
                s = Global.newHashSet(1);
                premisesThisCycle = new SoftReference(s);
            }
            else {
                if (now - prevCycle > clearAfterCycles) {
                    s.clear();
                }
            }

            prevCycle = now;

            return s.add(PremiseGenerator.pair(c, t));
        }
        return false;
    }
}
