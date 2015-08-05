package nars.premise;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import com.gs.collections.api.tuple.Pair;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.task.Sentence;
import nars.term.Term;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Applies a BloomFilter as a lossy short-term memory
 * to decide the novelty of termlink/tasklink pairs
 * https://code.google.com/p/guava-libraries/wiki/HashingExplained#BloomFilter
 */
public class BloomPremiseGenerator extends TermLinkBagPremiseGenerator implements Funnel<Pair<Term, Sentence>> {

    BloomFilter<Pair<Term,Sentence>> history;

    final double maxFPP = 0.1;
    static final int attempts = 12;

    final int expectedMemorySize = 24;
    final double desiredFPP = 0.02;

    //final float allowedRepeatRate = 0; //TODO allow a premise which is suspected of being non-novel to be applied anyway

    private int novel;
    private int nonnovel;

    /** clear the bloom filter when it becomes too dirty (too high FPP) */
    protected void reset() {
        history = BloomFilter.create(this, expectedMemorySize, desiredFPP);
        novel = nonnovel = 0;
    }

    public BloomPremiseGenerator() {
        super(new AtomicInteger(attempts));
        reset();
    }

    @Override
    public boolean validTermLinkTarget(Concept c, TaskLink taskLink, TermLink t) {
        if (!super.validTermLinkTarget(c, taskLink, t)) return false;

        final Pair<Term, Sentence> s = PremiseGenerator.pair(taskLink, t);

        final boolean mightContain = history.mightContain(s);

        if (mightContain) {
            nonnovel++;
            return false;
        }
        else {

            //System.out.println(c + " " + s + " " + mightContain + " #" + novel + "/" + nonnovel + " ~"+ history.expectedFpp());;

            double fpp = history.expectedFpp();
            if (fpp > maxFPP) {

                //System.out.println(this + " fpp limit, reset " + fpp + " with " + novel);
                reset();
            }
            history.put(s);
            novel++;
            return true;
        }
    }



    @Override
    public void funnel(Pair<Term, Sentence> from, PrimitiveSink into) {
        funnelSimple(from, into);
    }

    public static void funnelSimple(Pair<Term, Sentence> from, PrimitiveSink into) {
        into.putInt(from.getOne().hashCode()).putInt(from.getTwo().hashCode());
    }

    public static void funnelDetailed(Pair<Term, Sentence> from, PrimitiveSink into) {
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
