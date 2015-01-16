/*
 * Memory.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.core;

import nars.core.Core.AttentionAware;
import nars.core.Events.ResetEnd;
import nars.core.Events.ResetStart;
import nars.core.Events.TaskRemove;
import nars.io.Output.ERR;
import nars.io.Output.IN;
import nars.io.Output.OUT;
import nars.io.Symbols;
import nars.io.Symbols.NativeOperator;
import nars.io.meter.EmotionMeter;
import nars.io.meter.LogicMeter;
import nars.io.meter.ResourceMeter;
import nars.logic.BudgetFunctions;
import nars.logic.ImmediateProcess;
import nars.logic.Terms;
import nars.logic.entity.*;
import nars.logic.nal1.Inheritance;
import nars.logic.nal1.Negation;
import nars.logic.nal3.*;
import nars.logic.nal4.Image;
import nars.logic.nal4.ImageExt;
import nars.logic.nal4.ImageInt;
import nars.logic.nal4.Product;
import nars.logic.nal5.Conjunction;
import nars.logic.nal5.Disjunction;
import nars.logic.nal5.Equivalence;
import nars.logic.nal5.Implication;
import nars.logic.nal7.TemporalRules;
import nars.logic.nal7.Tense;
import nars.logic.nal8.Operation;
import nars.logic.nal8.Operator;
import nars.operator.app.plan.MultipleExecutionManager;
import nars.operator.io.Echo;
import nars.operator.io.PauseInput;
import nars.operator.io.Reset;
import nars.operator.io.SetVolume;
import nars.util.XORShiftRandom;
import nars.util.bag.Bag;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Memory consists of the run-time state of a NAR, including: * term and concept
 * memory * clock * reasoner state * etc.
 *
 * Excluding input/output channels which are managed by a NAR.
 *
 * A memory is controlled by zero or one NAR's at a given time.
 *
 * Memory is serializable so it can be persisted and transported.
 */
public class Memory implements Serializable {

    @Deprecated
    public final MultipleExecutionManager executive; //used for implication graph and for planner plugin, todo 
    //get it out to plugin somehow

    private boolean enabled = true;

    private long timeRealStart;
    private long timeRealNow;
    private long timePreviousCycle;
    private long timeSimulation;
    private int level;
    private TaskSource inputs;

    public void setLevel(int nalLevel) {
        if ((nalLevel < 1) || (nalLevel > 8))
            throw new RuntimeException("NAL level must be between 1 and 8 (inclusive)");
        this.level = nalLevel;
    }

    public int nal() {
        return level;
    }


    public static enum Forgetting {
        Iterative, Periodic
    }

    public enum Timing {

        /**
         * internal, subjective time (logic steps)
         */
        Iterative,
        /**
         * hard real-time, uses system clock
         */
        Real,
        /**
         * soft real-time, uses controlled simulation time
         */
        Simulation
    }

    Timing timing;

    public static interface TaskSource {

        public AbstractTask nextTask();

        public int numBuffered();
    }

    private static long defaultRandomSeed = 1;
    public static final Random randomNumber = new XORShiftRandom(defaultRandomSeed);
    //new Random(defaultRandomSeed);

    public static void resetStatic(long randomSeed) {
        randomNumber.setSeed(randomSeed);
    }


    private final List<Runnable> otherTasks = new ArrayList();

    public final Core concepts;

    public final EventEmitter event;

    /* InnateOperator registry. Containing all registered operators of the system */
    public final HashMap<CharSequence, Operator> operators;

    private long currentStampSerial = 0;

    /**
     * New tasks with novel composed terms, for delayed and selective processing
     */
    public final Bag<Task<Term>, Sentence<Term>> novelTasks;

    /* ---------- Short-term workspace for a single cycle ---	------- */
    /**
     * List of new tasks accumulated in one cycle, to be processed in the next
     * cycle
     */
    public final Deque<Task> newTasks;


    //public final Term self;
    public final EmotionMeter emotion = new EmotionMeter();
    public final LogicMeter logic;
    public final ResourceMeter resource;

    /**
     * The remaining number of steps to be carried out (stepLater mode)
     */
    private int inputPausedUntil = -1;

    /**
     * System clock, relatively defined to guarantee the repeatability of
     * behaviors
     */
    private long cycle;

    public final Param param;


    /* ---------- Constructor ---------- */
    /**
     * Create a new memory
     *
     * @param initialOperators - initial set of available operators; more may be
     * added during runtime
     */
    public Memory(int nalLevel, Param param, Core concepts, Bag<Task<Term>, Sentence<Term>> novelTasks) {

        this.level = nalLevel;

        this.param = param;


        this.concepts = concepts;
        this.concepts.init(this);

        this.novelTasks = novelTasks;
        if (novelTasks instanceof AttentionAware) {
            ((AttentionAware) novelTasks).setAttention(concepts);
        }

        this.newTasks = (Parameters.THREADS > 1)
                ? new ConcurrentLinkedDeque<>() : new ArrayDeque<>();

        this.operators = new HashMap<>();

        this.resource = new ResourceMeter();
        this.logic = new LogicMeter();

        this.event = new EventEmitter();


        this.executive = new MultipleExecutionManager(this);

        //after this line begins actual logic, now that the essential data strucures are allocated
        //------------------------------------ 
        reset();

    }

    public void reset() {
        event.emit(ResetStart.class);

        concepts.reset();
        novelTasks.clear();
        newTasks.clear();

        timing = param.getTiming();
        cycle = 0;
        timeRealStart = timeRealNow = System.currentTimeMillis();
        timePreviousCycle = time();

        inputPausedUntil = -1;

        emotion.set(0.5f, 0.5f);

        //TODO end all plugins

        event.emit(ResetEnd.class);

        //TODO re-enable if they were enabled

    }

    public long time() {
        switch (timing) {
            case Iterative:
                return getCycleTime();
            case Real:
                return getRealTime();
            case Simulation:
                return getSimulationTime();
        }
        return 0;
    }

    public int getDuration() {
        return param.duration.get();
    }

    /**
     * internal, subjective time (logic steps)
     */
    public long getCycleTime() {
        return cycle;
    }

    /**
     * hard real-time, uses system clock
     */
    public long getRealTime() {
        return timeRealNow - timeRealStart;
    }

    /**
     * soft real-time, uses controlled simulation time
     */
    public long getSimulationTime() {
        return timeSimulation;
    }

    public void addSimulationTime(long dt) {
        timeSimulation += dt;
    }

    /**
     * difference in time since last cycle
     */
    public long getTimeDelta() {
        return time() - timePreviousCycle;
    }

    public Deque<Task> getNewTasks() {
        return newTasks;
    }


    /* ---------- conversion utilities ---------- */
    /**
     * Get an existing Concept for a given name
     * <p>
     * called from Term and ConceptWindow.
     *
     * @param t the name of a concept
     * @return a Concept or null
     */
    public Concept concept(final Term t) {
        return concepts.concept(t);
    }

    //TODO decide if this is necessary
    public void temporalRuleOutputToGraph(Sentence s, Task t) {
        if (t.sentence.term instanceof Implication && t.sentence.term.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {

            executive.graph.implication.add(s, (CompoundTerm) s.term, t);
        }

    }

    /**
     * Get the Concept associated to a Term, or create it.
     *
     * Existing concept: apply tasklink activation (remove from bag, adjust
     * budget, reinsert) New concept: set initial activation, insert Subconcept:
     * extract from cache, apply activation, insert
     *
     * If failed to insert as a result of null bag, returns null
     *
     * A displaced Concept resulting from insert is forgotten (but may be stored
     * in optional subconcept memory
     *
     * @param term indicating the concept
     * @return an existing Concept, or a new one, or null
     */
    public Concept conceptualize(final BudgetValue budget, final Term term) {
        boolean createIfMissing = true;

        /*Concept c = concept(term);
         if (c!=null)
         System.out.print(c.budget + "   ");
         System.out.println(term + " conceptualize: " + budget);*/
        return concepts.conceptualize(budget, term, createIfMissing);
    }

    /**
     * Get the current activation level of a concept.
     *
     * @param t The Term naming a concept
     * @return the priority value of the concept
     */
    public float conceptActivation(final Term t) {
        final Concept c = concept(t);
        return (c == null) ? 0f : c.getPriority();
    }


    /* static methods making new compounds, which may return null */
    /**
     * Try to make a compound term from a template and a list of term
     *
     * @param compound The template
     * @param components The term
     * @param memory Reference to the memory
     * @return A compound term or null
     */
    public Term term(final CompoundTerm compound, final Term[] components) {
        if (compound instanceof ImageExt) {
            return new ImageExt(components, ((Image) compound).relationIndex);
        } else if (compound instanceof ImageInt) {
            return ImageInt.make(components, ((Image) compound).relationIndex);
        } else {
            return term(compound.operator(), components);
        }
    }

    public Term term(final CompoundTerm compound, Collection<Term> components) {
        Term[] c = components.toArray(new Term[components.size()]);
        return term(compound, c);
    }

    /**
     * Try to make a compound term from an operator and a list of term
     * <p>
     * Called from StringParser
     *
     * @param op Term operator
     * @param arg Component list
     * @return A term or null
     */
    public Term term(final NativeOperator op, final Term[] a) {

        switch (op) {

            case SET_EXT_OPENER:
                return SetExt.make(a);
            case SET_INT_OPENER:
                return SetInt.make(a);
            case INTERSECTION_EXT:
                return IntersectionExt.make(a);
            case INTERSECTION_INT:
                return IntersectionInt.make(a);
            case DIFFERENCE_EXT:
                return DifferenceExt.make(a);
            case DIFFERENCE_INT:
                return DifferenceInt.make(a);
            case INHERITANCE:
                return Inheritance.make(a[0], a[1]);
            case PRODUCT:
                return new Product(a);
            case IMAGE_EXT:
                return ImageExt.make(a);
            case IMAGE_INT:
                return ImageInt.make(a);
            case NEGATION:
                return Negation.make(a);
            case DISJUNCTION:
                return Disjunction.make(a);
            case CONJUNCTION:
                return Conjunction.make(a);
            case SEQUENCE:
                return Conjunction.make(a, TemporalRules.ORDER_FORWARD);
            case PARALLEL:
                return Conjunction.make(a, TemporalRules.ORDER_CONCURRENT);
            case IMPLICATION:
                return Implication.make(a[0], a[1]);
            case IMPLICATION_AFTER:
                return Implication.make(a[0], a[1], TemporalRules.ORDER_FORWARD);
            case IMPLICATION_BEFORE:
                return Implication.make(a[0], a[1], TemporalRules.ORDER_BACKWARD);
            case IMPLICATION_WHEN:
                return Implication.make(a[0], a[1], TemporalRules.ORDER_CONCURRENT);
            case EQUIVALENCE:
                return Equivalence.make(a[0], a[1]);
            case EQUIVALENCE_WHEN:
                return Equivalence.make(a[0], a[1], TemporalRules.ORDER_CONCURRENT);
            case EQUIVALENCE_AFTER:
                return Equivalence.make(a[0], a[1], TemporalRules.ORDER_FORWARD);
        }
        throw new RuntimeException("Unknown Term operator: " + op + " (" + op.name() + ")");
    }

    /**
     * this will not remove a concept. it is not good to use directly because it
     * can disrupt the bag's priority order. it should only be used after it has
     * been removed then before inserted
     */
    public void forget(final Item x, final float forgetCycles, final float relativeThreshold) {
        switch (param.forgetting) {
            case Iterative:
                BudgetFunctions.forgetIterative(x.budget, forgetCycles, relativeThreshold);
                break;
            case Periodic:
                BudgetFunctions.forgetPeriodic(x.budget, forgetCycles, relativeThreshold, time());
                break;
        }
    }

    /* ---------- new task entries ---------- */
    /**
     * add new task that waits to be processed in the next cycleMemory
     */
    public boolean addNewTask(final Task t, final String reason) {
        /*if (!Term.valid(t.getContent()))
         throw new RuntimeException("Invalid term: " + t);*/

        if (!Terms.levelValid(t.sentence, nal())) {
            if (param.exceptionOnExceedingNALLevel.get())
                throw new RuntimeException("Exceeds NAL level " + nal() + ": " + t + " (reason: " + reason + ")");
            else {
                removeTask(t, "NAL level too low");
                return false;
            }

        }

        newTasks.add(t);

        logic.TASK_ADD_NEW.hit();

        emit(Events.TaskAdd.class, t, reason);

        output(t);

        return true;
    }

    /* There are several types of new tasks, all added into the
     newTasks list, to be processed in the next cycleMemory.
     Some of them are reported and/or logged. */
    /**
     * Input task processing. Invoked by the outside or inside environment.
     * Outside: StringParser (addInput); Inside: InnateOperator (feedback).
     * Input tasks with low priority are ignored, and the others are put into
     * task buffer.
     *
     * @param t The addInput task
     * @return how many tasks were queued to newTasks
     */
    public int inputTask(final AbstractTask t) {

        if (t instanceof Task) {
            Task task = (Task) t;
            Stamp s = task.sentence.stamp;
            if (s.getCreationTime() == -1) {
                s.setCreationTime(time(), param.duration.get());
            }

            emit(IN.class, task);

            if (task.budget.aboveThreshold()) {

                temporalRuleOutputToGraph(task.sentence, task);

                if (addNewTask(task, "Perceived"))
                    return 1;

            } else {
                removeTask(task, "Neglected");
            }
        } else if (t instanceof PauseInput) {
            stepLater(((PauseInput) t).cycles);
            emit(IN.class, t);
        } else if (t instanceof Reset) {
            reset();
            emit(IN.class, t);
        } else if (t instanceof Echo) {
            Echo e = (Echo) t;
            emit(e.channel, e.signal);
        } else if (t instanceof SetVolume) {
            param.noiseLevel.set(((SetVolume) t).volume);
            emit(IN.class, t);
        } else {
            emit(IN.class, "Unrecognized Input Task: " + t);
        }
        return 0;
    }

    public void removeTask(final Task task, final String reason) {
        emit(TaskRemove.class, task, reason);
        task.end();
    }

    /**
     * ExecutedTask called in Operator.call
     *
     * @param operation The operation just executed
     */
    public void executedTask(final Operation operation, TruthValue truth) {
        Task opTask = operation.getTask();
        logic.TASK_EXECUTED.hit(); //(opTask.budget.getPriority());

        Stamp stamp = new Stamp(this, Tense.Present);
        Sentence sentence = new Sentence(operation, Symbols.JUDGMENT_MARK, truth, stamp);

        Task task = new Task(sentence, opTask.budget, operation.getTask());
        task.setCause(operation);

        addNewTask(task, "Executed");
    }

    public void output(final Task t) {

        final float budget = t.budget.summary();
        final float noiseLevel = 1.0f - (param.noiseLevel.get() / 100.0f);

        if (budget >= noiseLevel) {  // only report significant derived Tasks
            emit(OUT.class, t);
        }
    }

    final public void emit(final Class c, final Object... signal) {
        event.emit(c, signal);
    }

    final public boolean emitting(final Class channel) {
        return event.isActive(channel);
    }

    /**
     * enable/disable all I/O and memory processing. CycleStart and CycleStop
     * events will continue to be generated, allowing the memory to be used as a
     * clock tick while disabled.
     */
    public void setEnabled(boolean e) {
        this.enabled = e;
    }

    public boolean isEnabled() {
        return enabled;
    }


    /** whether perceptual tasks are available to perceive */
    public boolean hasPercepts() {
        return isProcessingInput() && inputs.numBuffered() > 0;
    }

    /** attempts to perceive the next input from perception, and
     *  handle it by immediately acting on it, or
     *  adding it to the new tasks queue for future reasoning.
     * @return how many tasks were generated as a result of perceiving, or -1 if no percept was available */
    public int nextPercept() {
        if (!isProcessingInput()) return -1;

        AbstractTask t = inputs.nextTask();
        if (t != null) {
            return inputTask(t);
        }
        return -1;
    }
    /** attempts to perceive at most N perceptual tasks.
     *  this allows Attention to regulate input relative to other kinds of mental activity
     *  if N == -1, continue perceives until perception buffer is emptied
     *  @return how many tasks perceived
     */
    public int nextPercept(int maxPercepts) {
        if (!isProcessingInput()) return 0;

        int perceived = 0;
        boolean inputEverything;

        if (maxPercepts == -1) { inputEverything = true; maxPercepts = 1; }
        else inputEverything = false;

        while (perceived < maxPercepts) {
            int p = nextPercept();
            if (p == -1) break;
            else if (!inputEverything) perceived += p;
        }
        return perceived;
    }

    public synchronized void cycle(final TaskSource inputs) {

        this.inputs = inputs;
        
        if (!isEnabled()) {
            return;
        }

        logic.TASK_INPUT.set((double) inputs.numBuffered());

        event.emit(Events.CycleStart.class);

        concepts.cycle();

        event.emit(Events.CycleEnd.class);

        updateTime();

    }

    /**
     * automatically called each cycle
     */
    protected void updateTime() {
        timePreviousCycle = time();
        cycle++;
        if (getTiming()==Timing.Real)
            timeRealNow = System.currentTimeMillis();
    }


    public void addOtherTask(Runnable t) {
        synchronized (otherTasks) {
            otherTasks.add(t);
        }
    }
    
    public synchronized int dequeueOtherTasks(Collection<Runnable> target) {
        int num;
        synchronized (otherTasks) {            
            num = otherTasks.size();
            if (num > 0) {
                target.addAll(otherTasks);
                otherTasks.clear();
            }
        }
        return num;
    }

    /**
     * Select a novel task to process.
     */
    public Runnable nextNovelTask() {
        if (novelTasks.isEmpty()) return null;

        // select a task from novelTasks
        final Task task = novelTasks.takeNext();

        if (task == null)
            throw new RuntimeException("novelTasks bag output null item");

        return new ImmediateProcess(this, task);
    }

    public Runnable nextNewTask() {

        if (newTasks.isEmpty()) return null;

        final Task task = newTasks.removeFirst();

        emotion.adjustBusy(task.getPriority(), task.getDurability());

        if (task.isInput() || !task.sentence.isJudgment() || concept(task.sentence.term) != null) { //it is a question/goal/quest or a concept which exists
            // ok so lets fire it
            return new ImmediateProcess(this, task);
        } else {
            final Sentence s = task.sentence;

            if (s.isJudgment() || s.isGoal()) {

                //TODO: extract to NovelProcess class
                return new Runnable() {

                    @Override
                    public void run() {

                        final double exp = s.truth.getExpectation();

                        if (exp > Parameters.DEFAULT_CREATION_EXPECTATION) {
                            //i dont see yet how frequency could play a role here - patrick
                            //just imagine a board game where you are confident about all the board rules
                            //but the implications reach all the frequency spectrum in certain situations
                            //but every concept can also be represented with (--,) so i guess its ok


                            // new concept formation
                            Task displacedNovelTask = novelTasks.putIn(task);
                            logic.TASK_ADD_NOVEL.hit();

                            if (displacedNovelTask != null) {
                                if (displacedNovelTask == task) {
                                    removeTask(task, "Ignored");
                                } else {
                                    removeTask(displacedNovelTask, "Displaced novel task");
                                }
                            }

                        } else {
                            removeTask(task, "Neglected");
                        }
                    }

                };
            }
        }
        throw new RuntimeException("Unrecognized NewTask: " + task);
    }



    protected void error(Throwable ex) {
        emit(ERR.class, ex);

        if (Parameters.DEBUG) {
            ex.printStackTrace();
        }

    }



    public Operator getOperator(final String op) {
        return operators.get(op);
    }

    public Operator addOperator(final Operator op) {
        operators.put(op.name(), op);
        return op;
    }

    public Operator removeOperator(final Operator op) {
        return operators.remove(op.name());
    }

    @Override
    public String toString() {
        //final StringBuilder sb = new StringBuilder(1024);
        //sb.append(toStringLongIfNotNull(novelTasks, "novelTasks"))
        //      .append(toStringIfNotNull(newTasks, "newTasks"));
        //.append(toStringLongIfNotNull(getCurrentTask(), "currentTask"))
        //.append(toStringLongIfNotNull(getCurrentBeliefLink(), "currentBeliefLink"))
        //.append(toStringIfNotNull(getCurrentBelief(), "currentBelief"));
        //return sb.toString();
        return super.toString();
    }

    private String toStringLongIfNotNull(Bag<?, ?> item, String title) {
        return item == null ? "" : "\n " + title + ":\n"
                + item.toString();
    }

    private String toStringLongIfNotNull(Item item, String title) {
        return item == null ? "" : "\n " + title + ":\n"
                + item.toStringLong();
    }

    private String toStringIfNotNull(Object item, String title) {
        return item == null ? "" : "\n " + title + ":\n"
                + item.toString();
    }

    public long newStampSerial() {
        return currentStampSerial++;
    }

    public boolean isProcessingInput() {
        return time() >= inputPausedUntil;
    }

    /**
     * Queue additional cycle()'s to the logic process.
     *
     * @param cycles The number of logic steps
     */
    public void stepLater(final long cycles) {
        inputPausedUntil = (int) (time() + cycles);
    }

    /**
     * get all tasks in the system by iterating all newTasks, novelTasks; does not change or remove any
     * Concept TaskLinks
     */
    public Set<Task> getTasks(boolean includeTaskLinks, boolean includeNewTasks, boolean includeNovelTasks) {

        Set<Task> t = new HashSet();

        if (includeTaskLinks) {
            for (Concept c : concepts) {
                for (TaskLink tl : c.taskLinks) {
                    t.add(tl.targetTask);
                }
            }
        }

        if (includeNewTasks) {
            t.addAll(newTasks);
        }

        if (includeNovelTasks) {
            for (Task n : novelTasks) {
                t.add(n);
            }
        }

        return t;
    }

    public Task newTask(Term content, char sentenceType, float freq, float conf, float priority, float durability) {
        return newTask(content, sentenceType, freq, conf, priority, durability, (Task) null);
    }

    public Task newTask(Term content, char sentenceType, float freq, float conf, float priority, float durability, final Task parentTask) {
        return newTask(content, sentenceType, freq, conf, priority, durability, parentTask, Tense.Present);
    }

    public Task newTask(Term content, char sentenceType, float freq, float conf, float priority, float durability, Tense tense) {
        return newTask(content, sentenceType, freq, conf, priority, durability, null, tense);
    }

    public Task newTask(Term content, char sentenceType, float freq, float conf, float priority, float durability, Task parentTask, Tense tense) {
        return newTaskAt(content, sentenceType, freq, conf, priority, durability, parentTask, tense, time());
    }

    public Task newTaskAt(Term content, char sentenceType, float freq, float conf, float priority, float durability, Task parentTask, Tense tense, long ocurrenceTime) {

        TruthValue truth = new TruthValue(freq, conf);
        Sentence sentence = new Sentence(
                content,
                sentenceType,
                truth,
                new Stamp(this, tense, ocurrenceTime));
        BudgetValue budget = new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, truth);
        Task task = new Task(sentence, budget, parentTask);
        return task;
    }

    /**
     * gets a next concept for processing
     */
    public Concept sampleNextConcept() {
        return concepts.sampleNextConcept();
    }

    public Timing getTiming() {
        return timing;
    }


}
