package nars.premise;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import com.gs.collections.api.tuple.Pair;
import com.gs.collections.impl.tuple.Tuples;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Term;

/**
 * Applies a BloomFilter as a lossy short-term memory
 * to decide the novelty of termlink/tasklink pairs
 * https://code.google.com/p/guava-libraries/wiki/HashingExplained#BloomFilter
 */
public class BloomPremiseSelector extends DirectPremiseSelector implements Funnel<Pair<Term, Sentence>> {

    BloomFilter<Pair<Term,Sentence>> history;

    final double maxFPP = 0.15;
    final int attempts = 3;

    public BloomPremiseSelector() {
        super();
        reset();
    }

    @Override
    public TermLink nextTermLink(Concept c, TaskLink taskLink) {

        int a = attempts;
        while (a-- > 0) {
            TermLink t = super.nextTermLink(c, taskLink);
            Pair<Term, Sentence> s = Tuples.pair(t.getTerm(), taskLink.getTask());

            final boolean mightContain = history.mightContain(s);



            if (mightContain) {
                //System.out.println(c + " " + s + " " + mightContain);
                continue;
            }
            else {
                double fpp = history.expectedFpp();
                if (fpp > maxFPP) {
                    reset();
                }



                history.put(s);
                return t;
            }
        }
        return null;
    }


    /** clear the bloom filter when it becomes too dirty (too high FPP) */
    protected void reset() {
        history = BloomFilter.create(this, 64, 0.005);
    }

    @Override
    public void funnel(Pair<Term, Sentence> from, PrimitiveSink into) {

        //into.putBytes(from.getOne().bytes()).put
        final Term term = from.getOne();
        final Sentence task = from.getTwo();

        into
            .putLong(term.structuralHash()).putInt(term.hashCode())

            .putLong(task.getTerm().structuralHash()).putInt(task.getTerm().hashCode())
            .putChar(task.getPunctuation())
            .putLong(task.getOccurrenceTime())
            .putInt(task.getTruth() != null ? task.hashCode() : 0);

        for (long l : task.getEvidentialSet())
            into.putLong(l);


    }
}
