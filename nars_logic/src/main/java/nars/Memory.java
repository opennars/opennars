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
package nars;

import com.google.common.collect.Iterators;
import nars.Events.ResetStart;
import nars.Events.Restart;
import nars.Events.TaskRemove;
import nars.bag.impl.CacheBag;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.budget.Itemized;
import nars.clock.Clock;
import nars.meter.EmotionMeter;
import nars.meter.LogicMetrics;
import nars.nal.*;
import nars.concept.Concept;
import nars.concept.ConceptBuilder;
import nars.nal.nal1.Inheritance;
import nars.nal.nal1.Negation;
import nars.nal.nal2.Instance;
import nars.nal.nal2.InstanceProperty;
import nars.nal.nal2.Property;
import nars.nal.nal2.Similarity;
import nars.nal.nal3.*;
import nars.nal.nal4.Image;
import nars.nal.nal4.ImageExt;
import nars.nal.nal4.ImageInt;
import nars.nal.nal4.Product;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Disjunction;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.nal.nal7.AbstractInterval;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal8.Operation;
import nars.nal.process.NAL;
import nars.task.stamp.AbstractStamper;
import nars.task.stamp.Stamp;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.term.*;
import nars.util.data.buffer.Perception;
import nars.util.event.EventEmitter;
import nars.util.meter.ResourceMeter;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

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
public class Memory implements Serializable, AbstractStamper {



    private Atom self;

    public final Random random;
    public final Perception<Task> perception;
    public final ControlCycle cycle;
    public final EventEmitter<Class> event;
    public final EventEmitter<Term> exe;
    public final EmotionMeter emotion = new EmotionMeter();
    public final LogicMetrics logic;
    public final ResourceMeter resource;
    public final Param param;
    final List<ConceptBuilder> conceptBuilders;
    private final Deque<Runnable> nextTasks = new ConcurrentLinkedDeque();
    private final Set<Concept> questionConcepts = Global.newHashSet(16);
    private final Set<Concept> goalConcepts = Global.newHashSet(16);

    private final Set<Concept> pendingDeletions = Global.newHashSet(16);

    public LogicPolicy rules;

    public final Clock clock;
    ExecutorService laterTasks = null;

    public final CacheBag<Term, Concept> concepts;


    private int level;
    private long currentStampSerial = 1;
    /**
     * The remaining number of steps to be carried out (stepLater mode)
     */
    private long inputPausedUntil = -1;
    private boolean inCycle = false;

    /**
     * Create a new memory
     *
     * @param narParam reasoner paramerters
     * @param policy logic parameters
     */
    public Memory(Random rng, int nalLevel, Param narParam, LogicPolicy policy, CacheBag<Term,Concept> concepts, ControlCycle cycle) {

        this.random = rng;
        this.level = nalLevel;

        this.clock = narParam.getClock();

        this.concepts = concepts;
        concepts.setOnRemoval(c -> delete(c));

        this.param = narParam;
        this.rules = policy;

        this.perception = new Perception();


        this.cycle = cycle;

        this.self = Symbols.DEFAULT_SELF; //default value

        this.event = new EventEmitter.DefaultEventEmitter();
        //this.event = new EventEmitter.FastDefaultEventEmitter();
        this.exe = new EventEmitter.DefaultEventEmitter();
        //this.exe = new EventEmitter.FastDefaultEventEmitter();


        this.conceptBuilders = new ArrayList(1);

        //optional:
        this.resource = null; //new ResourceMeter();
        this.logic = new LogicMetrics(this);

        //after this line begins actual logic, now that the essential data strucures are allocated
        //------------------------------------
        reset(false, false);


    }

    /**
     * Try to make a compound term from a template and a list of term
     *
     * @param compound The template
     * @param components The term
     * @return A compound term or null
     */
    public static Term term(final Compound compound, final Term[] components) {
        if (compound instanceof ImageExt) {
            return new ImageExt(components, ((Image) compound).relationIndex);
        } else if (compound instanceof ImageInt) {
            return new ImageInt(components, ((Image) compound).relationIndex);
        } else {
            return term(compound.operator(), components);
        }
    }

    public static Term term(final Compound compound, Collection<Term> components) {
        Term[] c = components.toArray(new Term[components.size()]);
        return term(compound, c);
    }

    private static boolean ensureTermLength(int num, Term[] a) {
        return (a.length==num);
        /*if (a.length!=num)
            throw new CompoundTerm.InvalidTermConstruction("Expected " + num + " args to create Term from " + Arrays.toString(a));*/
    }

    /**
     * Try to make a compound term from an operate and a list of term
     * <p>
     * Called from StringParser
     *
     * @param op Term operate
     * @return A term or null
     */
    public static Term term(final NALOperator op, final Term... a) {


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
            case PRODUCT:
                return Product.make(a);
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

            case OPERATION:
                throw new RuntimeException("Can not use this static method to instantiate an Operation, because a Memory instance is required to provide its Operator");

            //STATEMENTS --------------------------
            case PROPERTY:
                if (ensureTermLength(2, a)) return Property.make(a[0], a[1]); break;
            case INSTANCE:
                if (ensureTermLength(2, a)) return Instance.make(a[0], a[1]); break;
            case INSTANCE_PROPERTY:
                if (ensureTermLength(2, a)) return InstanceProperty.make(a[0], a[1]); break;

            case INHERITANCE:
                if (ensureTermLength(2, a)) return Inheritance.makeTerm(a[0], a[1]); break;

            case SIMILARITY:
                if (ensureTermLength(2, a)) return Similarity.makeTerm(a[0], a[1]); break;

            case IMPLICATION:
                if (ensureTermLength(2, a)) return Implication.makeTerm(a[0], a[1]); break;
            case IMPLICATION_AFTER:
                if (ensureTermLength(2, a)) return Implication.make(a[0], a[1], TemporalRules.ORDER_FORWARD); break;
            case IMPLICATION_BEFORE:
                if (ensureTermLength(2, a)) return Implication.make(a[0], a[1], TemporalRules.ORDER_BACKWARD); break;
            case IMPLICATION_WHEN:
                if (ensureTermLength(2, a)) return Implication.make(a[0], a[1], TemporalRules.ORDER_CONCURRENT); break;

            case EQUIVALENCE:
                if (ensureTermLength(2, a)) return Equivalence.makeTerm(a[0], a[1]); break;
            case EQUIVALENCE_WHEN:
                if (ensureTermLength(2, a)) return Equivalence.make(a[0], a[1], TemporalRules.ORDER_CONCURRENT); break;
            case EQUIVALENCE_AFTER:
                if (ensureTermLength(2, a)) return Equivalence.make(a[0], a[1], TemporalRules.ORDER_FORWARD); break;

            default:
                throw new RuntimeException("Unknown op: " + op + " (" + op.name() + ')');
        }

        return null;
    }

    /**
     * this will not remove a concept. it is not good to use directly because it
     * can disrupt the bag's priority order. it should only be used after it has
     * been removed then before inserted
     *
     * TODO move to BudgetFunctions
     */
    static public void forget(long now, final Itemized x, final float forgetCycles, final float relativeThreshold) {
        /*switch (param.forgetting) {
            case Iterative:
                BudgetFunctions.forgetIterative(x.budget, forgetCycles, relativeThreshold);
                break;
            case Periodic:*/
                BudgetFunctions.forgetPeriodic(x.getBudget(), forgetCycles, relativeThreshold, now);
                //break;
        //}
    }

    public void setLevel(int nalLevel) {
        if ((nalLevel < 1) || (nalLevel > 8))
            throw new RuntimeException("NAL level must be between 1 and 8 (inclusive)");
        this.level = nalLevel;
    }

    public int nal() {
        return level;
    }

    public boolean nal(int isEqualToOrGreater) {
        return nal() >= isEqualToOrGreater;
    }

    public Concept concept(final String t) {
        return concept(Atom.the(t));
    }

    /** provides fast iteration to concepts with questions */
    public Set<Concept> getQuestionConcepts() {
        return questionConcepts;
    }

    /** provides fast iteration to concepts with goals */
    public Set<Concept> getGoalConcepts() {
        throw new RuntimeException("disabled until it is useful");
        //return goalConcepts;
    }

    public void taskAdd(final Iterable<Task> source) {
        for (final Task t : source)
            taskAdd((Task)t);
    }

    public Atom self() {
        return self;
    }

    public void setSelf(Atom t) {
        this.self = t;
    }

    /**
     * Entry point for all potentially executable tasks.
     * Enters a task and determine if there is a decision to execute.
     * Returns true if the Task has a Term which can be executed
     */
    public void execute(Concept c, Task t) {
        Term term = t.getTerm();
        if (term instanceof Operation) {
            Operation o = (Operation)term;
            o.setTask(t);
            Term e = o.getOperator();
            exe.emit(e, o, c, this);
        }
        /*else {
            System.err.println("Unexecutable: " + t);
        }*/
    }

    /** attempt to answer a question task with an Operator execution */
    public boolean answer(Concept c, Task t) {
        if (t.sentence.isQuestion()) {
            Term term = t.getTerm();
            if (term instanceof Operation) {
                Operation o = (Operation) term;
                o.setTask(t);
                Term e = o.getOperator();
                exe.emit(e, o, c, this);
            }
        }
        return false;
    }

    /** prepend a conceptbuilder to the conceptbuilder handler chain */
    public void on(ConceptBuilder c) {
        conceptBuilders.add(0, c);
    }

    /** remove a conceptbuilder which has been added; return true if successfully removed or false if it wasnt present */
    public boolean off(ConceptBuilder c) {
        return conceptBuilders.remove(c);
    }

    /** conceptbuilder handler chain */
    public List<ConceptBuilder> getConceptBuilders() {
        return conceptBuilders;
    }

    /** gets the final concept builder in the chain */
    public ConceptBuilder getConceptBuilderDefault() {
        List<ConceptBuilder> cb = getConceptBuilders();
        return cb.get(cb.size() - 1);
    }

    /* ---------- Constructor ---------- */

    public Concept newConcept(final Budget budget, final Term term) {

        Concept concept = null;

        /** use the concept created by the first conceptbuilder to return non-null */
        List<ConceptBuilder> cb = getConceptBuilders();
        int cbn = cb.size();

        for (int i = 0; i < cbn; i++) {
            ConceptBuilder c  =  cb.get(i);
            concept = c.newConcept(term, budget, this);
            if (concept != null) break;
        }

        concepts.put(concept);

        return concept;
    }

    public Atom the(final String s) {
        return Atom.the(s);
    }

    /** applies default settings, with a new serial # as its evidentialBase, for a new input sentence */
    @Override public void applyToStamp(Stamp t) {
//
//    public Stamp(final long[] evidentialBase, final long creationTime, final long occurenceTime, final int duration) {
//        super();
//        this.creationTime = creationTime;
//        this.occurrenceTime = occurenceTime;
//        this.duration = duration;
//        this.evidentialBase = evidentialBase;
//    }
//
//    protected Stamp(final long serial, final long creationTime, final long occurenceTime, final int duration) {
//        this(new long[]{serial}, creationTime, occurenceTime, duration);
//    }
        t.setCreationTime(time());
        t.setDuration(duration());
        t.setEvidentialSet(new long[]{newStampSerial()});
    }

    public void randomSeed(long randomSeed) {
        random.setSeed(randomSeed);
    }

    /** called when a Concept's lifecycle has changed */
    public void updateConceptState(Concept c) {
        boolean hasQuestions = c.hasQuestions();
        boolean hasGoals = !c.getGoals().isEmpty();

        if (c.isActive()) {
            //index an incoming concept with existing questions or goals
            if (hasQuestions) updateConceptQuestions(c);
            //if (hasGoals) updateConceptGoals(c);
        }
        else if (c.isForgotten() || c.isDeleted()) {
            //unindex an outgoing concept with questions or goals
            if (hasQuestions) questionConcepts.remove(c);
            //..
        }

    }

    /** handles maintenance of concept question/goal indices when concepts change according to reports by certain events
        called by a Concept when its questions state changes (becomes empty or becomes un-empty) */
    public void updateConceptQuestions(Concept c) {
        if (!c.hasQuestions()) {
            if (!questionConcepts.remove(c))
                throw new RuntimeException("Concept " + c + " never registered any questions");
        }
        else {
            if (!questionConcepts.add(c)) {
                throw new RuntimeException("Concept " + c + " aready registered existing questions");
            }

            //this test was cycle.size() previously:
            if (questionConcepts.size() > concepts.size()) {
                throw new RuntimeException("more questionConcepts " +questionConcepts.size() + " than concepts " + cycle.size());
            }
        }
    }

    public void updateConceptGoals(Concept c) {
        //TODO
    }

    public void delete() {
        reset(true, true);

        event.delete();
    }

    public void reset(boolean resetInputs, boolean delete) {

        nextTasks.clear();

        if (laterTasks!=null) {
            laterTasks.shutdown();
            laterTasks = null;
        }

        if (resetInputs)
            perception.reset();

        event.emit(ResetStart.class);

        clock.reset();

        inputPausedUntil = -1;

        questionConcepts.clear();

        concepts.clear();
        cycle.reset(this, delete);


        goalConcepts.clear();

        emotion.set(0.5f, 0.5f);

        event.emit(Restart.class);


    }

    public long time() {
        return clock.time();
    }

    public int duration() {
        return param.duration.get();
    }




    /**
     * difference in time since last cycle
     */
    public long timeSinceLastCycle() {
        return clock.timeSinceLastCycle();
    }



    /**
     * Get an existing (active OR forgotten) Concept identified
     * by the provided Term
     */
    public Concept concept(Term t) {
        if (!t.isNormalized()) {
            t = ((Compound)t).normalized();
            if (t == null) return null;
        }
        return concepts.get(t);
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
    public Concept conceptualize(final Budget budget, Term term) {

        if (!validConceptTerm(term))
            return null;


        if ((term = term.normalized()) == null)
            return null;

        Concept c = cycle.conceptualize(budget, term, true);
        if (c == null)
            return null;

        return c;
    }

    private boolean validConceptTerm(Term term) {
        return !((term instanceof Variable) || (term instanceof AbstractInterval));
    }

    /**
     * Get the current activation level of a concept.
     *
     * @param t The Term naming a concept
     * @return the priority value of the concept
     */
    public float conceptPriority(final Term t) {
        final Concept c = concept(t);
        return (c == null) ? 0.0f : c.getPriority();
    }

    public boolean taskAdd(final Task t) {

        /* process ImmediateOperation and Operations of ImmediateOperators */
        if (t.executeIfImmediate(this)) {
            return false;
        }


        if (!Terms.levelValid(t.sentence, nal())) {
            removed(t, "Insufficient NAL level");
            return false;
        }

        float inputPriorityFactor = param.inputActivationFactor.floatValue();
        if (inputPriorityFactor!=1.0f)
            t.mulPriority( inputPriorityFactor );



        if (cycle.addTask(t)) {

            //NOTE: if duplicate outputs happen, the budget wil have changed
            //but they wont be displayed.  to display them,
            //we need to buffer unique TaskAdd ("OUT") tasks until the end
            //of the cycle

            if (!t.isInput())
                emit(Events.OUT.class, t);

            logic.TASK_ADD_NEW.hit();
            return true;
        }
        return false;
    }

    /**
     * Input task processing. Invoked by the outside or inside environment.
     * Outside: StringParser (addInput); Inside: InnateOperator (feedback).
     * Input tasks with low priority are ignored, and the others are put into
     * task buffer.
     *
     * @param task The addInput task
     * @return how many tasks were queued to newTasks
     */
    public int input(final Task task) {

        if (task.perceivable(this)) {
            if (taskAdd(task)) {
                emit(Events.IN.class, task);
                return 1;
            }
        }

        return 0;
    }


    /* ---------- new task entries ---------- */

    /** called anytime a task has been removed, deleted, discarded, ignored, etc. */
    public void removed(final Task task, final String removalReason) {
        task.addHistory(removalReason);
        if (Global.DEBUG_TASK_HISTORY && Global.DEBUG_DERIVATION_STACKTRACES)
            task.addHistory(NAL.getNALStack());
        emit(TaskRemove.class, task, removalReason);
        task.delete();
    }

    /* There are several types of new tasks, all added into the
     newTasks list, to be processed in the next cycleMemory.
     Some of them are reported and/or logged. */

    public void removed(final TaskSeed task, final String removalReason) {
        //nothing yet
    }

    /** sends an event signal to listeners subscribed to channel 'c' */
    final public void emit(final Class c, final Object... signal) {
        event.emit(c, signal);
    }
    /** sends an event signal to listeners subscribed to channel 'c' */
    final public void emit(final Class c) {
        event.emit(c);
    }

    /** tells whether a given channel has any listeners that might react to something emitted to it */
    final public boolean emitting(final Class channel) {
        return event.isActive(channel);
    }

    /** attempts to perceive the next input from perception, and
     *  handle it by immediately acting on it, or
     *  adding it to the new tasks queue for future reasoning.
     * @return how many tasks were generated as a result of perceiving (which can be zero), or -1 if no percept is available */
    public int perceiveNext() {
        if (!perceiving()) return -1;

        Task t = perception.get();
        if (t != null)
            return input(t);

        return -1;
    }

    /** attempts to perceive at most N perceptual tasks.
     *  this allows Attention to regulate input relative to other kinds of mental activity
     *  if N == -1, continue perceives until perception buffer is emptied
     *  @return how many tasks perceived
     */
    public int perceiveNext(int maxPercepts) {
        if (!perceiving()) return 0;

        boolean inputEverything;

        if (maxPercepts == -1) { inputEverything = true; maxPercepts = 1; }
        else inputEverything = false;

        int perceived = 0;
        while (perceived < maxPercepts) {
            int p = perceiveNext();
            if (p == -1) break;
            else if (!inputEverything) perceived += p;
        }
        return perceived;
    }



    /** queues a task to (hopefully) be executed at an unknown time in the future,
     *  in its own thread in a thread pool */
    public void taskLater(Runnable t) {
        if (laterTasks==null) {
            laterTasks = Executors.newFixedThreadPool(1);
        }

        laterTasks.submit(t);
        laterTasks.execute(t);
    }



//
//    /** applies entropy to the random number genrator;
//     * if called at the end of each cycle, the entire sequence remains
//     * algorithmically deterministic and repeatable.
//     * this helps overcome low entropy inherent in fast / efficient random
//     * number genreators, we involve NARS as part of the RNG process.
//     * */
//    protected void randomUpdate() {
//        random.setSeed( random.nextLong() * time() );
//    }

    /** adds a task to the queue of task which will be executed in batch
     *  at the end of the current cycle.     */
    public void taskNext(Runnable t) {
        nextTasks.addLast(t);
    }

    /** runs all the tasks in the 'Next' queue */
    public void runNextTasks() {
        int originalSize = nextTasks.size();
        if (originalSize == 0) return;

        ControlCycle.run(nextTasks, originalSize);
    }

    /** signals an error through one or more event notification systems */
    protected void error(Throwable ex) {
        emit(Events.ERR.class, ex);

        ex.printStackTrace();

        if (Global.DEBUG && Global.EXIT_ON_EXCEPTION) {
            //throw the exception to the next lower stack catcher, or cause program exit if none exists
            throw new RuntimeException(ex);
        }

    }

    /** produces a new stamp serial #, used to uniquely identify inputs */
    public long newStampSerial() {
        return currentStampSerial++;
    }

    public boolean perceiving() {
        if (inputPausedUntil == -1) return true;
        return time() >= inputPausedUntil;
    }

    /**
     * Queue additional cycle()'s to the logic process during which no new input will
     * be perceived.  Analogous to closing one's eyes to focus internally for a brief
     * or extended amount of time - but not necessarily sleeping.
     *
     * @param cycles The number of logic steps to think for, will end thinking at time() + cycles unless more thinking is queued
     */
    public void think(final long cycles) {
        inputPausedUntil = (time() + cycles);
    }

    /**
     * get all tasks in the system by iterating all newTasks, novelTasks; does not change or remove any
     * Concept TaskLinks
     */
    public void getTasks(boolean includeTaskLinks, boolean includeNewTasks, boolean includeNovelTasks, Set<Task> target) {


        if (includeTaskLinks) {
            cycle.forEach( c -> {
                if (c.getTaskLinks() !=null)
                    c.getTaskLinks().forEach(tl -> {
                        target.add(tl.targetTask);
                    });
            });
        }

        /*
        if (includeNewTasks) {
            t.addAll(newTasks);
        }

        if (includeNovelTasks) {
            for (Task n : novelTasks) {
                t.add(n);
            }
        }
        */

    }

    public <T extends Compound> TaskSeed<T> newTask(T t) {
        return new TaskSeed(this, t);
    }



    /**
     * samples a next active concept for processing;
     * may return null if no concept is available depending on the control system
     */
    public Concept nextConcept() {

        Concept c = cycle.nextConcept();

        if (Global.DEBUG) {
            if (!c.isActive())
                throw new RuntimeException("returned nextConcept is not active");
        }

        return c;

    }

    /** scan for a next concept matching the predicate */
    public Concept nextConcept(Predicate<Concept> pred, float v) {
        return cycle.nextConcept(pred, v);
    }

    public Clock getClock() {
        return clock;
    }

    /** returns the concept index */
    public CacheBag<Term, Concept> getConcepts() {
        return concepts;
    }

    public Iterator<Concept> getConcepts(boolean active, boolean inactive) {
        if (active && !inactive)
            return cycle.iterator();
        else if (!active && inactive)
            return Iterators.filter(concepts.iterator(), c -> !c.isActive());
        else if (active && inactive)
            return concepts.iterator(); //'concepts' should contain all concepts
        else
            return Iterators.emptyIterator();
    }

    public int numConcepts(boolean active, boolean inactive) {
        int total = 0;
        if (active && !inactive) return cycle.size();
        else if (!active && inactive) return concepts.size() - cycle.size();
        else if (active && inactive)
            return concepts.size();
        else
            return 0;
    }

    public boolean inCycle() {
        return inCycle;
    }

    public synchronized void cycle() {

        inCycle = true;

        clock.preCycle();

        event.emit(Events.CycleStart.class);

        cycle.cycle();

        event.emit(Events.CycleEnd.class);

        inCycle = false;

        deletePendingConcepts();

        //randomUpdate();

    }

    public void delete(Term term) {
        Concept c = concept(term);
        if (c == null) return;

        delete(c);
    }

    /** queues the deletion of a concept until after the current cycle ends.
     */
    public synchronized void delete(Concept c) {
        if (c.isDeleted())
            throw new RuntimeException(c + " is already deleted");

        if (!inCycle()) {
            //immediately delete
            _delete(c);
        }
        else {
            pendingDeletions.add(c);
        }

    }

    /** called by Memory at end of each cycle to flush deleted concepts */
    protected void deletePendingConcepts() {
        if (!pendingDeletions.isEmpty()) {

            for (Concept c : pendingDeletions)
                _delete(c);

            pendingDeletions.clear();
        }
    }

    /** actually delete procedure for a concept; removes from indexes */
    protected void _delete(Concept c) {
        Concept removedFromActive = cycle.remove(c);
        if (c!=removedFromActive) {
            throw new RuntimeException("another instances of active concept " + c + " detected on removal: " + removedFromActive);
        }

        Concept removedFromIndex = concepts.remove(c.getTerm());
        if (removedFromIndex == null) {
            throw new RuntimeException("concept " + c + " was not removed from memory");
        }
        if (c!=removedFromIndex) {
            throw new RuntimeException("another instances of concept " + c + " detected on removal: " + removedFromActive);
        }

        c.delete();
    }



//    private String toStringLongIfNotNull(Bag<?, ?> item, String title) {
//        return item == null ? "" : "\n " + title + ":\n"
//                + item.toString();
//    }
//
//    private String toStringLongIfNotNull(Item item, String title) {
//        return item == null ? "" : "\n " + title + ":\n"
//                + item.toStringLong();
//    }
//
//    private String toStringIfNotNull(Object item, String title) {
//        return item == null ? "" : "\n " + title + ":\n"
//                + item.toString();
//    }
}
