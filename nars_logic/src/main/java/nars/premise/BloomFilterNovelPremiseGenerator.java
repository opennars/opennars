package nars.premise;

import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.util.data.bloom.BloomFilter;

import java.util.concurrent.atomic.AtomicInteger;

import static nars.util.data.Util.int2Bytes;

/**
 * Applies a BloomFilter as a lossy short-term memory
 * to decide the novelty of termlink/tasklink pairs
 * https://code.google.com/p/guava-libraries/wiki/HashingExplained#BloomFilter
 * <p>
 * Not thread safe unless validTermLinkTarget were synch or hashBuffer is threadlocal
 */
public class BloomFilterNovelPremiseGenerator extends TermLinkBagPremiseGenerator {

    private final int clearAfterCycles;
    long lastClear;

    final BloomFilter history;

    //for statistics:
    //private int novel, nonnovel;

    final byte[] hashBuffer = new byte[2 * 4];
    private TaskLink prevTask;
    private TermLink prevTerm;

    /**
     * clear the bloom filter when it becomes too dirty (too high FPP)
     */
    protected void reset(long now) {
        //novel = nonnovel = 0;
        history.clear();
        lastClear = now;
        prevTask = null;
        prevTerm = null;
    }

    public BloomFilterNovelPremiseGenerator(AtomicInteger maxSelectionAttempts) {
        this(maxSelectionAttempts, 1, 32, 0.005);
    }

    public BloomFilterNovelPremiseGenerator(AtomicInteger maxSelectionAttempts, int clearAfterCycles, int expectedSize, double fpp) {
        super(maxSelectionAttempts);

        this.history = new BloomFilter(expectedSize, fpp);

        this.clearAfterCycles = clearAfterCycles;
    }

    protected byte[] bytes(final Concept c, final TermLink term, final TaskLink task) {
        final byte[] b = this.hashBuffer;

        int p = 0;

        //simple hash function: just use each int (32 bit) hashCode's:
        p = int2Bytes(term.hashCode(), b, p);
        p = int2Bytes(task.hashCode(), b, p);


//                .putLong(term.structuralHash()).putInt(term.hashCode())
//
//                .putLong(task.getTerm().structuralHash()).putInt(task.getTerm().hashCode())
//                .putChar(task.getPunctuation())
//                .putLong(task.getOccurrenceTime())
//                .putInt(task.getTruth() != null ? task.hashCode() : 0);
//


        return b;
    }

    @Override
    public boolean validTermLinkTarget(Concept c, TermLink termLink, TaskLink taskLink) {
        if (!super.validTermLinkTarget(c, termLink, taskLink)) return false;

        final long now = c.getMemory().time();

        if (now - lastClear >= clearAfterCycles) {
            reset(now);
        }
        else {
            /** quick eliminate of repeat */
            if ((prevTerm == termLink) && (prevTask == taskLink))
                return false;
        }

        this.prevTerm = termLink;
        this.prevTask = taskLink;


        final boolean mightContain = history.testBytes(bytes(c, termLink, taskLink));

        if (mightContain) {
            //nonnovel++;
            //print(now);

            return false;
        }


        history.add(hashBuffer);

        //novel++;
        //print(now);


        return true;


    }

//    protected void print(long now) {
//        System.out.println(now + ": " + novel + "/" + nonnovel + ": " + Arrays.toString(hashBuffer) + " <- " + history.getBitSet().toString());
//    }
}



//System.out.println(c + " " + s + " " + mightContain + " #" + novel + "/" + nonnovel + " ~"+ history.expectedFpp());;

//            double fpp = history.getEstimatedFalsePositiveProbability();
//            if (fpp > maxFPP) {
//
//                //System.out.println(this + " fpp limit, reset " + fpp + " with " + novel);
//                reset();
//            }
