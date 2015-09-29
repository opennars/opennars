//package nars.nar;
//
//import com.google.common.util.concurrent.AtomicDouble;
//import nars.Global;
//import nars.LocalMemory;
//import nars.Memory;
//import nars.NAR;
//import nars.bag.Bag;
//import nars.bag.impl.CurveBag;
//import nars.budget.Budget;
//import nars.budget.ItemAccumulator;
//import nars.clock.CycleClock;
//import nars.concept.AtomConcept;
//import nars.concept.Concept;
//import nars.concept.ConceptActivator;
//import nars.concept.DefaultConcept;
//import nars.event.CycleReaction;
//import nars.link.TaskLink;
//import nars.link.TermLink;
//import nars.link.TermLinkKey;
//import nars.nal.SimpleDeriver;
//import nars.nal.nal8.OperatorReaction;
//import nars.nal.nal8.operator.NullOperator;
//import nars.op.app.STMTemporalLinkage;
//import nars.process.ConceptProcess;
//import nars.process.TaskProcess;
//import nars.task.Sentence;
//import nars.task.Task;
//import nars.term.Atom;
//import nars.term.Term;
//import nars.util.data.MutableInteger;
//import nars.util.event.On;
//
//import java.util.ArrayDeque;
//import java.util.Deque;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//
//
///**
// * Default set of NAR parameters which have been classically used for development.
// * <p>
// * WARNING this Seed is not immutable yet because it extends Param,
// * which is supposed to be per-instance/mutable. So do not attempt
// * to create multiple NAR with the same Default seed model
// */
//public class SingleStepNAR extends Default {
//
//    public static final OperatorReaction[] exampleOperators = new OperatorReaction[]{
//            //new Wait(),
//            new NullOperator("break"),
//            new NullOperator("drop"),
//            new NullOperator("goto"),
//            new NullOperator("open"),
//            new NullOperator("pick"),
//            new NullOperator("strike"),
//            new NullOperator("throw"),
//            new NullOperator("activate"),
//            new NullOperator("deactivate")
//    };
//
//    public final DefaultCycle core;
//    public int cyclesPerFrame = 1;
//
//    /**
//     * Size of TaskLinkBag
//     */
//    int taskLinkBagSize;
//    /**
//     * Size of TermLinkBag
//     */
//    int termLinkBagSize;
//
//
//    /**
//     * Default DEFAULTS
//     */
//    public SingleStepNAR() {
//        this(768, 1, 2, 3);
//    }
//
//    public SingleStepNAR(int maxConcepts, int conceptsFirePerCycle, int termLinksPerCycle, int taskLinksPerCycle) {
//        this(new LocalMemory(new CycleClock()), maxConcepts, conceptsFirePerCycle, termLinksPerCycle, taskLinksPerCycle);
//    }
//
//    public SingleStepNAR(Memory m, int maxConcepts, int conceptsFirePerCycle, int termLinksPerCycle, int taskLinksPerCycle) {
//        super(m,maxConcepts,conceptsFirePerCycle,termLinksPerCycle,taskLinksPerCycle);
//
//
//        setTaskLinkBagSize(8);
//        setTermLinkBagSize(16);
//
//
//        m.duration.set(5);
//        m.shortTermMemoryHistory.set(5);
//        m.conceptActivationFactor.set(1.0);
//        m.conceptFireThreshold.set(0.0);
//
//        m.conceptForgetDurations.set(1.0);
//        m.taskLinkForgetDurations.set(2.0);
//        m.termLinkForgetDurations.set(3.0);
//
//        m.conceptBeliefsMax.set(11);
//        m.conceptGoalsMax.set(5);
//        m.conceptQuestionsMax.set(4);
//        m.activeConceptThreshold.set(0.0);
//        m.questionFromGoalThreshold.set(0.35);
//        m.taskProcessThreshold.set(Global.BUDGET_EPSILON*2);
//        m.termLinkThreshold.set(0); //Global.BUDGET_EPSILON);
//        m.taskLinkThreshold.set(0); //Global.BUDGET_EPSILON);
//        m.executionThreshold.set(0.5);
//
//        //m.reliance.set(Global.DEFAULT_JUDGMENT_CONFIDENCE);
//        m.conceptCreationExpectation.set(0);//.66);
//
//        setCyclesPerFrame(cyclesPerFrame);
//
//        //core loop
//        {
//            DefaultCycle c = this.core = new DefaultCycle(
//                    m.the("defaultCore", this),
//                    m.the("logic", getDeriver()),
//                    new ConceptBagActivator(this),
//                    m.the("inputBuffer", new ItemAccumulator(Budget.max)),
//                    newConceptBag()
//            );
//            m.the("core", c);
//
//            c.conceptsFired = conceptsFirePerCycle;
//            c.termlinks = termLinksPerCycle; //TODO make mutable int
//            c.tasklinks = taskLinksPerCycle; //TODO make mutable int
//            c.capacity.set(maxConcepts);
//            c.inputsMaxPerCycle.set(conceptsFirePerCycle);
//            c.conceptsFiredPerCycle.set(conceptsFirePerCycle);
//
//        }
//
//        if (nal() >= 7) {
//
//            //scope: control
//            m.the(new STMTemporalLinkage(this, core.deriver ) );
//
//            if (nal() >= 8) {
//
//                for (OperatorReaction o : defaultOperators)
//                    on(o);
//                /*for (OperatorReaction o : exampleOperators)
//                    on(o);*/
//
//                //n.on(Anticipate.class);      // expect an event
//
////                if (internalExperience == Minimal) {
////                    new InternalExperience(this);
////                    new Abbreviation(this);
////                } else if (internalExperience == Full) {
////                    on(FullInternalExperience.class);
////                    on(Counting.class);
////                }
//            }
//        }
//        //n.on(new RuntimeNARSettings());
//
//    }
//
//
////    static String readFile(String path, Charset encoding)
////            throws IOException {
////        byte[] encoded = Files.readAllBytes(Paths.get(path));
////        return new String(encoded, encoding);
////    }
//
////    protected DerivationFilter[] getDerivationFilters() {
////        return new DerivationFilter[]{
////                new FilterBelowConfidence(0.01),
////                new FilterDuplicateExistingBelief()
////                //param.getDefaultDerivationFilters().add(new BeRational());
////        };
////    }
//
//
//
//    public Concept newConcept(final Term t, final Budget b) {
//
//        Bag<Sentence, TaskLink> taskLinks =
//                new CurveBag<>(rng, /*sentenceNodes,*/ getConceptTaskLinks());
//        taskLinks.mergePlus();
//
//        Bag<TermLinkKey, TermLink> termLinks =
//                new CurveBag<>(rng, /*termlinkKeyNodes,*/ getConceptTermLinks());
//        termLinks.mergePlus();
//
//        return newConcept(t, b, taskLinks, termLinks, memory());
//    }
//
//    public class ConceptAttentionEnhancer {
//
//        /**
//         * called by concept before it fires to update any pending changes
//         */
//        public void updateLinks(Concept c) {
//
//
//            final Memory memory = memory();
//
//            if (Global.TERMLINK_FORGETTING_EXTRA_DEPTH > 0)
//                c.getTermLinks().forgetNext(
//                        memory.termLinkForgetDurations,
//                        Global.TERMLINK_FORGETTING_EXTRA_DEPTH,
//                        memory);
//
//
//
//            if (Global.TASKLINK_FORGETTING_EXTRA_DEPTH > 0)
//                c.getTaskLinks().forgetNext(
//                        memory.taskLinkForgetDurations,
//                        Global.TASKLINK_FORGETTING_EXTRA_DEPTH,
//                        memory);
//
//
//            //linkTerms(null, true);
//
//        }
//    }
//
//
//    public Concept newConcept(Term t, Budget b, Bag<Sentence, TaskLink> taskLinks, Bag<TermLinkKey, TermLink> termLinks, Memory m) {
//
//        if (t instanceof Atom) {
//            return new AtomConcept(t, b, termLinks, taskLinks, m
//            );
//        }
//        else {
//            return new DefaultConcept(t, b,
//                    taskLinks, termLinks,
//                    m
//            );
//        }
//
//    }
//
//
//
//    public Bag<Term, Concept> newConceptBag() {
//        CurveBag<Term, Concept> b = new CurveBag(rng, 1);
//        b.mergePlus();
//        return b;
//    }
//
//    public int getConceptTaskLinks() {
//        return taskLinkBagSize;
//    }
//
//    public SingleStepNAR setTaskLinkBagSize(int taskLinkBagSize) {
//        this.taskLinkBagSize = taskLinkBagSize;
//        return this;
//    }
//
//    public int getConceptTermLinks() {
//        return termLinkBagSize;
//    }
//
//    public SingleStepNAR setTermLinkBagSize(int termLinkBagSize) {
//        this.termLinkBagSize = termLinkBagSize;
//        return this;
//    }
//
//
//
//    protected SimpleDeriver getDeriver() {
//        return SimpleDeriver.standardDeriver;
//    }
//
//    protected boolean process(Task t) {
//        return true;
//    }
//
//    /**
//     * The original deterministic memory cycle implementation that is currently used as a standard
//     * for development and testing.
//     */
//    public static class DefaultCycle extends CycleReaction  /*extends SequentialCycle*/ {
//
//        final Deque<Task> percepts = new ArrayDeque();
//
//        /**
//         * How many concepts to fire each cycle; measures degree of parallelism in each cycle
//         */
//        public final AtomicInteger conceptsFiredPerCycle;
//
//        //public final MutableInt termLinksPerConcept = new MutableInt();
//
//        /**
//         * max # of inputs to perceive per cycle; -1 means unlimited (attempts to drains input to empty each cycle)
//         */
//        public final AtomicInteger inputsMaxPerCycle;
//        private final SimpleDeriver deriver;
//        private final Function<ConceptProcess,Stream<Task>> premiseProcessor;
//        public int conceptsFired;
//
////        final Function<Task, Task> derivationPostProcess = d -> {
////            return LimitDerivationPriority.limitDerivation(d);
////        };
//
//
//        /** samples an active concept */
//        public Concept next() {
//            return active.peekNext();
//        }
//
//        /**
//         * New tasks with novel composed terms, for delayed and selective processing
//         */
//        private final ItemAccumulator<Task> newTasks;
//
//        /** concepts active in this cycle */
//        private final Bag<Term, Concept> active;
//
//        private final On onInput;
//
//        private final NAR nar;
//
//        public final MutableInteger capacity = new MutableInteger();
//
//        private final ConceptBagActivator ca;
//
//        private final AtomicDouble conceptForget;
//
//        int tasklinks = 2;
//        int termlinks = 3;
//
//        /* ---------- Short-term workspace for a single cycle ------- */
//
//        public DefaultCycle(NAR nar, SimpleDeriver deriver, ConceptBagActivator ca, ItemAccumulator<Task> newTasks, Bag<Term, Concept> concepts) {
//            super(nar);
//
//            nar.memory.eventReset.on((m) -> {
//                reset();
//            });
//
//            this.nar = nar;
//            this.ca = ca;
//
//            this.deriver = deriver;
//
//            this.premiseProcessor = (premise) -> {
//
//                //used to estimate the fraction this batch should be scaled but this is not accurate
//                //final int numPremises = termlinks*tasklinks;
//
//                return Task.normalize(
//                        premise.derive(deriver).collect(Collectors.toList()),
//                        premise.getMeanSummary() /*/numPremises*/
//                ).stream();
//
//                //OPTION 1: re-input to input buffers
//                //t.input(nar, deriver, derivationPostProcess);
//
//                //OPTION 2: immediate process
//                /*t.apply(deriver).forEach(r -> {
//                    run(r);
//                });*/
//
//            };
//
//            this.conceptForget = nar.memory().conceptForgetDurations;
//
//            this.newTasks = newTasks;
//            this.inputsMaxPerCycle = new AtomicInteger(1);
//            this.conceptsFiredPerCycle = new AtomicInteger(1);
//            this.active = concepts;
//
//            onInput = nar.memory().eventInput.on(t-> {
//                if (t.isInput())
//                    percepts.add(t);
//                else {
//                    //newTasks.add(t);
//                }
//            });
//        }
//
//            public void reset() {
//
//                percepts.clear();
//
//                newTasks.clear();
//
//            }
//
//        /**
//         * An atomic working cycle of the system:
//         * 0) optionally process inputs
//         * 1) optionally process new task(s)
//         * 2) optionally process novel task(s)
//         * 2) optionally fire a concept
//         **/
//        @Override public void onCycle() {
//            enhanceAttention();
//            runInputTasks(inputsMaxPerCycle.get());
//            runNewTasks(/*inputsMaxPerCycle.get()*/);
//            fireConcepts(conceptsFiredPerCycle.get());
//        }
//
//        protected void fireConcepts(int max) {
//
//            active.setCapacity(capacity.intValue());
//
//            //1 concept if (memory.newTasks.isEmpty())*/
//            final int conceptsToFire = newTasks.isEmpty() ? max : 0;
//            if (conceptsToFire == 0) return;
//
//            final float conceptForgetDurations = nar.memory().conceptForgetDurations.floatValue();
//
//            final long now = nar.time();
//
//            for (int i = 0; i < conceptsToFire; i++) {
//
//                Concept[] buffer = new Concept[]{active.forgetNext(conceptForgetDurations, nar.memory())};
//
//                for (final Concept c : buffer) {
//                    if (c == null) break;
//                    fireConcept(conceptForgetDurations, now, c);
//                }
//            }
//        }
//
//
//
//        private void fireConcept(float conceptForgetDurations, long now, Concept c) {
//
//            nar.input( ConceptProcess.nextPremiseSquare(nar, c,
//                    conceptForgetDurations,
//                    premiseProcessor,
//                    termlinks, tasklinks ) );
//        }
//
//
//        //TODO move this to separate enhance plugin
//        protected void enhanceAttention() {
//            active.forgetNext(
//                    conceptForget,
//                    Global.CONCEPT_FORGETTING_EXTRA_DEPTH,
//                    nar.memory());
//        }
//
//        protected void runNewTasks() {
//            runNewTasks(newTasks.size()); //all
//        }
//
//        protected void runNewTasks(int max) {
//
//            int numNewTasks = Math.min(max, newTasks.size());
//            if (numNewTasks == 0) return;
//
//            //queueNewTasks();
//
//            for (int n = newTasks.size() - 1; n >= 0; n--) {
//                Task highest = newTasks.removeHighest();
//                if (highest == null) break;
//
//                run(highest);
//            }
//            //commitNewTasks();
//        }
//
//        protected void runInputTasks(int max) {
//
//            int m = Math.min(percepts.size(),
//                        max);
//
//            for (int n = m; n > 0; n--) {
//                run(percepts.removeFirst());
//            }
//        }
//
//        protected boolean run(Task task) {
//            return TaskProcess.run(nar, task) != null;
//        }
//
//        public final long time() { return nar.time(); }
//
//        public Concept update(Term term, Budget b, boolean b1, float v, Bag<Term, Concept> active) {
//            active.setCapacity(capacity.intValue());
//            return ca.update(term.getTerm(), b, true, time(), 1f, active);
//        }
//
//        public Iterable<?> concepts() {
//            return active;
//        }
//    }
//
////    @Deprecated
////    public static class CommandLineNARBuilder extends Default {
////
////        List<String> filesToLoad = new ArrayList();
////
////        public CommandLineNARBuilder(String[] args) {
////            super();
////
////            for (int i = 0; i < args.length; i++) {
////                String arg = args[i];
////                if ("--silence".equals(arg)) {
////                    arg = args[++i];
////                    int sl = Integer.parseInt(arg);
////                    //outputVolume.set(100 - sl);
////                } else if ("--noise".equals(arg)) {
////                    arg = args[++i];
////                    int sl = Integer.parseInt(arg);
////                    //outputVolume.set(sl);
////                } else {
////                    filesToLoad.add(arg);
////                }
////            }
////
////            for (String x : filesToLoad) {
////                taskNext(() -> {
////                    try {
////                        input(new File(x));
////                    } catch (FileNotFoundException fex) {
////                        System.err.println(getClass() + ": " + fex.toString());
////                    } catch (Exception ex) {
////                        ex.printStackTrace();
////                    }
////                });
////
////                //n.run(1);
////            }
////        }
////
////        /**
////         * Decode the silence level
////         *
////         * @param param Given argument
////         * @return Whether the argument is not the silence level
////         */
////        public static boolean isReallyFile(String param) {
////            return !"--silence".equals(param);
////        }
////    }
//
//    private class ConceptBagActivator extends ConceptActivator {
//
//
//        public ConceptBagActivator(NAR n) {
//            super(n);
//        }
//
//        @Override
//        @Deprecated public Concept newConcept(Term t, Budget b, @Deprecated Memory m) {
//            return SingleStepNAR.this.newConcept(t, b);
//        }
//
//        @Override public Concept newItem() {
//            //default behavior overriden; a new item will be maanually inserted into the bag under certain conditons to be determined by this class
//            Concept c = SingleStepNAR.this.newConcept(getKey(),getBudget());
//            memory().put(c);
//
//            return c;
//        }
//
//        @Override
//        protected void on(Concept c) {
//
//        }
//
//        @Override
//        protected void off(Concept c) {
//
//        }
//
////        public Concept update(Bag<Term,Concept> bag) {
////            return bag.update(this);
////        }
//    }
//}
