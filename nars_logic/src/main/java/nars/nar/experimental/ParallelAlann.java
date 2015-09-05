package nars.nar.experimental;

import com.google.common.collect.Iterators;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.bag.Bag;
import nars.bag.impl.MapCacheBag;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.io.Texts;
import nars.io.out.TextOutput;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.meter.NARTrace;
import nars.op.app.Commander;
import nars.process.TaskProcess;
import nars.task.Sentence;
import nars.term.Term;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.infinispan.commons.equivalence.AnyEquivalence;
import org.infinispan.util.concurrent.BoundedConcurrentHashMap;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Parallel ALANN implementation
 */
public class ParallelAlann extends AbstractAlann {

    private final int deriversPerThread;
    final int threads;

    final static int maxConcepts = 128*1024;


    float maxCyclesPerSecond = -1;


    ExecutorService exe;


    public static void main(String[] args) {
        //temporary testing shell

        Global.DEBUG = true;
        Global.EXIT_ON_EXCEPTION = true;

        NAR n = new NAR(new ParallelAlann(4, 2));
        TextOutput.out(n);
        NARTrace.out(n);

        n.input("<x --> y>.\n" +
                "<y --> z>.\n" +
                "<x --> z>?\n");

        n.frame(8);

    }


    @Override
    public Concept newConcept(final Term t, final Budget b, final Memory m) {

        Bag<Sentence, TaskLink> taskLinks =
                new SynchronizedCurveBag<>(rng, /*sentenceNodes,*/ param.getConceptTaskLinks());
        taskLinks.mergeAverage();

        Bag<TermLinkKey, TermLink> termLinks =
                new SynchronizedCurveBag<>(rng, /*termlinkKeyNodes,*/ param.getConceptTermLinks());
        termLinks.mergeAverage();

        return newConcept(t, b, taskLinks, termLinks, m);
    }

    public class DeriveletsThread extends DeriveletContext implements Runnable {

        final static long ifNoConceptsWaitMS = 1;

        int defaultTTL = memory.duration() * 8;

        public Derivelet[] d;
        private long now;

        public DeriveletsThread(Collection<Derivelet> d, Supplier<Concept> conceptSupply) {
            super(memory, newRandom(), conceptSupply);
            this.d = d.toArray(new Derivelet[d.size()]);
        }

        @Override
        public String toString() {
            return "DeriveletsThread:" + '/' + Thread.currentThread().getId() + '*' +
                    d.length;
                    //Arrays.toString(d);
        }

        protected void cycle() {
            Derivelet current = null;

            try {

                this.now = time();

                boolean active = false;

                for (final Derivelet x : d) {
                    active |= cycleDerivelet(current = x);
                }

                if (!active) {
                    try {
                        Thread.sleep(ifNoConceptsWaitMS);
                    } catch (InterruptedException e) { }
                }

            } catch (Exception e) {
                e.printStackTrace();
                kill(current);
            }
        }

        final boolean cycleDerivelet(final Derivelet d) {
            //System.out.println(d + " cycle");

            if (!d.cycle(now)) {

                //recycle this derivelet
                Concept next = conceptSupply.get();
                if (next != null) {
                    d.start(next, defaultTTL, this);
                } else {
                    return false;
                }
            }

            return true;
        }

        /** kill a derivelet if it was interrupted or exception,
         * freeing any hostage concept it may have (which will be locked)  */
        protected void kill(final Derivelet d) {
            final Concept currentConcept = d.concept;
            if (currentConcept!=null) {
                d.concept = null;
            }
        }

        public void run() {

            //System.out.println(this + " start");

            while (true /*running*/) {
                cycle();
            }
        }


        public final float nextFloat() {
            return this.rng.nextFloat();
        }

        public final Concept concept(final Term term) {
            return memory.concept(term);
        }
    }

    public class InstrumentedDeriveletsThread extends DeriveletsThread {

        final Mean m = new Mean();
        final int reportPeriod = 50;
        private long prevCyc;

        public InstrumentedDeriveletsThread(Collection<Derivelet> d, Supplier<Concept> concepts) {
            super(d, concepts);
        }

        @Override
        protected void cycle() {

            long cyc = memory.time();
            if (cyc == prevCyc) {
                sleepWait();
                return;
            }
            this.prevCyc = cyc;

            long start = System.nanoTime();
            super.cycle();
            long end = System.nanoTime();

            double timeMS = (end-start)/1.0e6;
            m.increment(timeMS);

            if (cyc % reportPeriod == 0) {
                System.out.println(this + " @ " + cyc + " (" + Texts.n4(timeMS) + "ms avg),  " + concepts.size() + " concepts" );

                m.clear();

            }
        }

        private void sleepWait() {
            try {
                Thread.sleep(ifNoConceptsWaitMS);
            } catch (InterruptedException e) {            }
        }
    }

    public ParallelAlann(int deriversPerThread, int threads) {
        super(new MapCacheBag(
                //new ConcurrentHashMap(maxConcepts)


                new BoundedConcurrentHashMap(
                        /* capacity */ maxConcepts,
                        /* concurrency */ threads,
                        /* key equivalence */
                        AnyEquivalence.getInstance(Term.class),
                        AnyEquivalence.getInstance(Concept.class))

                //new NonBlockingHashMap<>(maxConcepts)

                //Global.newHashMap()
        ));

        this.deriversPerThread = deriversPerThread;
        this.threads = threads;


    }



    @Override protected void processConcepts() {
        float maxCPS = maxCyclesPerSecond;
        if (maxCPS > 0) {
            long ms = (long) (1000f / maxCPS);
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {

            }
        }

        //Thread.yield();

    }



    //    @Override
//    public Concept conceptualize(final Term term, final Budget budget, final boolean createIfMissing) {
//        Concept existing = get(term);
//        if (existing == null) {
//            put(existing = newConcept(term, budget, memory));
//        }
//        else {
//            budgetMerge.value(existing.getBudget(), budget);
//        }
//        return existing;
//    }


    /** this will be invoked in the DeriveletsThread.
     *  Concept.runQueued will thus be called in-between
     *  derivelet cycles.
     * */
    public final Supplier<Concept> newConceptSupply() {
        final Iterator<Concept> indexIterator = Iterators.cycle(concepts);
        return () -> {

            if (!indexIterator.hasNext())
                return null;

            return indexIterator.next();
        };
    }


    @Override
    public void init(NAR nar) {
        super.init(nar);


        initDerivelets();
    }

    private void initDerivelets() {

        exe = Executors.newFixedThreadPool(threads);

        //shared by all threads

        for (int j = 0; j < threads; j++) {

            final List<Derivelet> d = Global.newArrayList();

            for (int i = 0; i < deriversPerThread; i++) {
                d.add( new Derivelet() );
            }

            /** each thread gets its own iterator */
            final Supplier<Concept> cs = newConceptSupply();

            DeriveletsThread dt
                    //= new DeriveletsThread(d);
                    = new InstrumentedDeriveletsThread(d, cs);

            exe.execute(dt);

        }


        /** set this thread priority to zero to allow derivelet threads more priority */
        //Thread.currentThread().setPriority(1);
    }

}
