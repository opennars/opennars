package nars.nar;

import com.gs.collections.impl.bag.mutable.HashBag;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.bag.Bag;
import nars.bag.BagBudget;
import nars.bag.impl.CurveBag;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.data.Range;
import nars.nal.Deriver;
import nars.nal.PremiseMatch;
import nars.nar.experimental.Derivelet;
import nars.process.ConceptProcess;
import nars.task.Task;
import nars.task.flow.SetTaskPerception;
import nars.task.flow.TaskPerception;
import nars.term.Termed;
import nars.term.compile.TermIndex;
import nars.time.FrameClock;
import nars.util.data.MutableInteger;
import nars.util.data.list.FasterList;
import nars.util.event.Active;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * Various extensions enabled
 */
public class Default extends AbstractNAR {

    public final AbstractCycle core;
    public final TaskPerception input;


    @Deprecated
    public Default() {
        this(1024, 1, 1, 3);
    }

    public Default(int numConcepts,
                   int conceptsFirePerCycle,
                   int tasklinkFirePerConcept,
                   int termlinkFirePerConcept) {
        this(new Memory(new FrameClock(),
                //TermIndex.memoryWeak(numConcepts * 2)
                TermIndex.memory(numConcepts * 2)

        ), numConcepts, conceptsFirePerCycle, termlinkFirePerConcept, tasklinkFirePerConcept);
    }

    public Default(Memory mem, int activeConcepts, int conceptsFirePerCycle, int termLinksPerConcept, int taskLinksPerConcept) {
        super(mem);

        the("input", input = initInput());

        the("core", core = initCore(
                activeConcepts,
                conceptsFirePerCycle,
                termLinksPerConcept, taskLinksPerConcept
        ));

        if (core!=null) {
            beforeNextFrame(this::initHigherNAL);
        }


        //new QueryVariableExhaustiveResults(this.memory());

        /*
        the("memory_sharpen", new BagForgettingEnhancer(memory, core.active));
        */

    }

//    public TaskPerception _initInput() {
//        return new FIFOTaskPerception(this, null, this::process);
//    }

    public TaskPerception initInput() {

        return new SetTaskPerception(
                memory, this::process, Budget.plus);

        /* {
            @Override
            protected void onOverflow(Task t) {
                memory.eventError.emit("Overflow: " + t + " " + getStatistics());
            }
        };*/
        //input.inputsMaxPerCycle.set(conceptsFirePerCycle);;
    }

    protected AbstractCycle initCore(int activeConcepts, int conceptsFirePerCycle, int termLinksPerConcept, int taskLinksPerConcept) {

        AbstractCycle c = new DefaultCycle(this, newDeriver(), newConceptBag(activeConcepts));

        //TODO move these to a PremiseGenerator which supplies
        // batches of Premises
        c.termlinksFiredPerFiredConcept.set(termLinksPerConcept);
        c.tasklinksFiredPerFiredConcept.set(taskLinksPerConcept);

        c.conceptsFiredPerCycle.set(conceptsFirePerCycle);

        c.capacity.set(activeConcepts);

        return c;
    }


    public Bag<Concept> newConceptBag(int initialCapacity) {
        return new CurveBag<Concept>(initialCapacity, rng) {

            @Override public BagBudget<Concept> put(Object v) {
                BagBudget<Concept> b = get(v);
                Concept c = (Concept) v;
                if (b==null)
                    b = new BagBudget(c, 0,1,1);

                c.getTaskLinks().commit();
                c.getTermLinks().commit();

                float p =
                        //Math.max(
                        //c.getTaskLinks().getSummarySum()/taskLinkBagSize
                        //(
                        //c.getTaskLinks().getSummaryMean()
                        //+c.getTermLinks().getSummaryMean()) * 0.5f
                        c.getTaskLinks().getPriorityMax()

                        // c.getTermLinks().getPriorityMax()

                        //)
                        ;

                b.set(p, 1f, 1f);

                return put(c, b);
            }

        }.mergeNull();
    }


    @Override
    public Concept conceptualize(Termed termed) {
        Concept c = super.conceptualize(termed);
        if (c!=null) {
            core.activate(c);
            return c;
        }
        return null;
    }



    @Override
    public NAR forEachConcept(Consumer<Concept> recip) {
        core.active.forEach(recip);
        return this;
    }


    public static final Predicate<BagBudget> simpleForgetDecay = (b) -> {
        float p = b.getPriority() * 0.95f;
        if (p > b.getQuality()*0.1f)
            b.setPriority(p);
        return true;
    };

    /**
     * The original deterministic memory cycle implementation that is currently used as a standard
     * for development and testing.
     */
    public abstract static class AbstractCycle implements Consumer<BagBudget<Concept>> {

        final Active handlers;

        public final Deriver  der;

        /**
         * How many concepts to fire each cycle; measures degree of parallelism in each cycle
         */
        @Range(min=0,max=64,unit="Concept")
        public final MutableInteger conceptsFiredPerCycle;

        @Range(min=0,max=16,unit="TaskLink") //TODO use float percentage
        public final MutableInteger tasklinksFiredPerFiredConcept = new MutableInteger(1);

        @Range(min=0,max=16,unit="TermLink")
        public final MutableInteger termlinksFiredPerFiredConcept = new MutableInteger(1);

        @Range(min=0.01f,max=8,unit="Duration")
        public final MutableFloat linkRemembering;

        //public final MutableFloat activationFactor = new MutableFloat(1.0f);

//        final Function<Task, Task> derivationPostProcess = d -> {
//            return LimitDerivationPriority.limitDerivation(d);
//        };



        /**
         * concepts active in this cycle
         */
        public final Bag<Concept> active;


        @Deprecated
        public final transient NAR nar;

        @Range(min=0,max=8192,unit="Concept")
        public final MutableInteger capacity = new MutableInteger();

        /** activated concepts pending (re-)insert to bag */
        public final LinkedHashSet<Concept> activated = new LinkedHashSet();

        final Derivelet deriver = new Derivelet();

        @Range(min=0, max=1f,unit="Perfection")
        public final MutableFloat perfection;

        final List<BagBudget<Concept>> firing = Global.newArrayList(1);

        //cached
        transient private int termlnksToFire, tasklinksToFire;

//        @Deprecated
//        int tasklinks = 2; //TODO use MutableInteger for this
//        @Deprecated
//        int termlinks = 3; //TODO use MutableInteger for this

        /* ---------- Short-term workspace for a single cycle ------- */

        protected AbstractCycle(NAR nar, Deriver deriver, Bag<Concept> concepts) {

            this.nar = nar;

            this.der = deriver;


            Memory m = nar.memory;

            this.linkRemembering = m.linkForgetDurations;
            this.perfection = m.perfection;

            conceptsFiredPerCycle = new MutableInteger(1);
            active = concepts;

            this.handlers = new Active(
                m.eventCycleEnd.on(this::onCycle),
                m.eventReset.on((mem) -> onReset())
            );

            alannForget = (budget) -> {
                // priority * e^(-lambda*t)
                //     lambda is (1 - durabilty) / forgetPeriod
                //     dt is the delta
                final long currentTime = nar.time(); //TODO cache

                long dt = budget.setLastForgetTime(currentTime);
                if (dt == 0) return true; //too soon to update

                float currentPriority = budget.getPriorityIfNaNThenZero();

                final float forgetPeriod = linkRemembering.floatValue() * m.duration(); //TODO cache

                float relativeThreshold = perfection.floatValue();

                float expDecayed = currentPriority * (float) Math.exp(
                        -((1.0f - budget.getDurability()) / forgetPeriod) * dt
                );
                float threshold = budget.getQuality() * relativeThreshold;

                float nextPriority = expDecayed;
                if (nextPriority < threshold) nextPriority = threshold;

                budget.setPriority(nextPriority);

                return true;
            };
        }

        private void onCycle(Memory memory) {
            fireConcepts(conceptsFiredPerCycle.intValue(), this::process);
            updateActivated();
        }

        private void updateActivated() {
            active.commit();

            //active.printAll();

            LinkedHashSet<Concept> a = this.activated;
            if (!a.isEmpty()) {
                a.forEach(active::put);
                a.clear();
            }
        }

        /** processes derivation result */
        abstract protected void process(ConceptProcess cp);

        /**
         * samples an active concept
         */
        public final Concept next() {
            return active.sample().get();
        }


        private void onReset() {
            active.clear();
            activated.clear();
        }



        public final Predicate<BagBudget> alannForget;

        protected final void fireConcepts(int conceptsToFire, Consumer<ConceptProcess> processor) {

            Bag<Concept> b = this.active;

            b.setCapacity(capacity.intValue()); //TODO share the MutableInteger so that this doesnt need to be called ever
            if (conceptsToFire == 0 || b.isEmpty()) return;

            List<BagBudget<Concept>> f = this.firing;
            b.sample(conceptsToFire, f);

            tasklinksToFire = tasklinksFiredPerFiredConcept.intValue();
            termlnksToFire = termlinksFiredPerFiredConcept.intValue();

            f.forEach(this);
            f.clear();

        }

        public final void activate(Concept c) {
            activated.add(c);
            //core.active.put(c);
        }

        /** fires a concept selected by the bag */
        @Override public final void accept(BagBudget<Concept> cb) {
            Concept c = cb.get();

            //c.getTermLinks().up(simpleForgetDecay);
            //c.getTaskLinks().update(simpleForgetDecay);

            deriver.firePremiseSquare(nar, this::process, c,
                tasklinksToFire,
                termlnksToFire,
                //simpleForgetDecay
                alannForget
            );

            activate(c);
        }


        //try to implement some other way, this is here because of serializability

    }

//    @Deprecated
//    public static class CommandLineNARBuilder extends Default {
//
//        List<String> filesToLoad = new ArrayList();
//
//        public CommandLineNARBuilder(String[] args) {
//            super();
//
//            for (int i = 0; i < args.length; i++) {
//                String arg = args[i];
//                if ("--silence".equals(arg)) {
//                    arg = args[++i];
//                    int sl = Integer.parseInt(arg);
//                    //outputVolume.set(100 - sl);
//                } else if ("--noise".equals(arg)) {
//                    arg = args[++i];
//                    int sl = Integer.parseInt(arg);
//                    //outputVolume.set(sl);
//                } else {
//                    filesToLoad.add(arg);
//                }
//            }
//
//            for (String x : filesToLoad) {
//                taskNext(() -> {
//                    try {
//                        input(new File(x));
//                    } catch (FileNotFoundException fex) {
//                        System.err.println(getClass() + ": " + fex.toString());
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                });
//
//                //n.run(1);
//            }
//        }
//
//        /**
//         * Decode the silence level
//         *
//         * @param param Given argument
//         * @return Whether the argument is not the silence level
//         */
//        public static boolean isReallyFile(String param) {
//            return !"--silence".equals(param);
//        }
//    }



    /**
     * groups each derivation's tasks as a group before inputting into
     * the main perception buffer, allowing post-processing such as budget normalization.
     * <p>
     * ex: this can ensure that a premise which produces many derived tasks
     * will not consume budget unfairly relative to another premise
     * with less tasks but equal budget.
     */
    public static class DefaultCycle extends AbstractCycle {

        /**
         * re-used, not to be used outside of this
         */
        private final PremiseMatch matcher;

        /**
         * holds the resulting tasks of one derivation so they can
         * be normalized or some other filter or aggregation
         * applied collectively.
         */
        final Collection<Task> derivedTasksBuffer;


        public DefaultCycle(NAR nar, Deriver deriver, Bag<Concept> concepts) {
            super(nar, deriver, concepts);

            matcher = new PremiseMatch(nar.memory.random);
            /* if detecting duplicates, use a list. otherwise use a set to deduplicate anyway */
            derivedTasksBuffer =
                    Global.DEBUG_DETECT_DUPLICATE_DERIVATIONS ?
                            new FasterList() : Global.newHashSet(1);

        }


//        @Override
//        protected void fireConcept(Concept c) {
//
//            Collection<Task> buffer = derivedTasksBuffer;
//            Consumer<Task> narInput = nar::input;
//            Deriver deriver = this.deriver;
//
//            BagBudget<Termed> term = c.getTermLinks().peekNext();
//            if (term!=null) {
//                BagBudget<Task> task = c.getTaskLinks().peekNext();
//                if (task!=null) {
//                    deriver.run(
//                            new ConceptTaskTermLinkProcess(nar, c , task, term),
//                            matcher,
//                            nar::input
//                    );
//                }
//            }
//


//        fireConceptSquare(c, p -> {
//
//
//            });


        @Override
        public void process(ConceptProcess p) {
            Collection<Task> buffer = derivedTasksBuffer;
            Consumer<Task> narInput = nar::input;

            Deriver deriver = this.der;
            deriver.run(p, matcher, buffer::add);

            if (Global.DEBUG_DETECT_DUPLICATE_DERIVATIONS) {
                HashBag<Task> b = detectDuplicates(buffer);
                buffer.clear();
                b.addAll(buffer);
            }

            if (!buffer.isEmpty()) {

                Task.normalize(
                        buffer,
                        //p.getMeanPriority()
                        //p.getTask().getPriority()
                        p.getTask().getPriority()/buffer.size()
                        //p.getTaskLink().getPriority()
                        //p.getTaskLink().getPriority()/buffer.size()
                );

                buffer.forEach(narInput);

                buffer.clear();
            }

        }

        static HashBag<Task> detectDuplicates(Collection<Task> buffer) {
            HashBag<Task> taskCount = new HashBag<>();
            taskCount.addAll(buffer);
            taskCount.forEachWithOccurrences((t, i) -> {
                if (i == 1) return;

                System.err.println("DUPLICATE TASK(" + i + "): " + t);
                List<Task> equiv = buffer.stream().filter(u -> u.equals(t)).collect(toList());
                HashBag<String> rules = new HashBag();
                equiv.forEach(u -> {
                    String rule = u.getLogLast().toString();
                    rules.add(rule);

//                    System.err.println("\t" + u );
//                    System.err.println("\t\t" + rule );
//                    System.err.println();
                });
                rules.forEachWithOccurrences((String r, int c) -> System.err.println("\t" + c + '\t' + r));
                System.err.println("--");

            });
            return taskCount;
        }

    }


}
