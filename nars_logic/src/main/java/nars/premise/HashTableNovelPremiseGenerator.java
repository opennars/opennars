package nars.premise;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.task.Sentence;
import nars.term.Term;

import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicInteger;


public class HashTableNovelPremiseGenerator extends TermLinkBagPremiseGenerator {

    long prevCycle = -1;

    /** how many cycles after which to clear the history and allow a repeat */
    final int clearAfterCycles;

    /** use a soft reference so that this doesnt become attached to any term/sentence pair contents, if it becomes inactive */
    //transient SoftReference<Set<Pair<Term,Sentence>>> premisesThisCycle;
    transient SoftReference<HashMultimap<Sentence,Term>> premisesThisCycle;
    //use Sentence as key because it is more specific

    public HashTableNovelPremiseGenerator(AtomicInteger maxSelectionAttempts) {
        this(maxSelectionAttempts, 1);
    }

    public HashTableNovelPremiseGenerator(AtomicInteger maxSelectionAttempts, int clearAfterCycles) {
        super(maxSelectionAttempts);
        this.clearAfterCycles = clearAfterCycles;
    }

    @Override
    public boolean validTermLinkTarget(TermLink term, TaskLink task) {
        if (super.validTermLinkTarget(term, task)) {
            final long now = time();


            SetMultimap<Sentence, Term> s = null;
            if (premisesThisCycle != null)
                s = premisesThisCycle.get();

            if (s == null) {
                s = MultimapBuilder.hashKeys().hashSetValues(1).build();
                premisesThisCycle = new SoftReference(s);
            }
            else {
                if (now - prevCycle > clearAfterCycles) {
                    s.clear();
                }
            }

            prevCycle = now;

            return s.put(task.getTask(), term.getTerm());
        }
        return false;
    }
}
