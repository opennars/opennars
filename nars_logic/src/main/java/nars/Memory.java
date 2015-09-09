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

import nars.Events.ResetStart;
import nars.Events.Restart;
import nars.bag.Bag;
import nars.bag.impl.CacheBag;
import nars.budget.Budget;
import nars.clock.Clock;
import nars.concept.Concept;
import nars.concept.ConceptBuilder;
import nars.link.TaskLink;
import nars.meter.EmotionMeter;
import nars.meter.LogicMeter;
import nars.nal.PremiseProcessor;
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
import nars.premise.Premise;
import nars.process.ConceptProcess;
import nars.process.CycleProcess;
import nars.process.TaskProcess;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.term.*;
import nars.util.event.EventEmitter;
import nars.util.event.Observed;
import nars.util.meter.ResourceMeter;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
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
public class Memory implements Serializable, AbstractMemory {

    private Atom self;

    transient public final Random random;

    transient private CycleProcess cycle;

    @Deprecated transient public final EventEmitter<Class,Object[]> event;
    transient public final Observed<ConceptProcess> eventBeliefReason = new Observed.DefaultObserved();
    transient public final Observed<Task> eventTaskRemoved = new Observed.DefaultObserved();
    transient public final Observed<ConceptProcess> eventConceptProcessed = new Observed.DefaultObserved();

    transient public final Observed<Concept> eventConceptActive = new Observed.DefaultObserved();
    transient public final Observed<Concept> eventConceptForget = new Observed.DefaultObserved();

    transient public final Observed<Memory>
            /** fired at the beginning of each memory cycle */
            eventCycleStart = new Observed.DefaultObserved(),
            /** fired at the end of each memory cycle */
            eventCycleEnd = new Observed.DefaultObserved();

    transient public final Observed<TaskProcess> eventTaskProcess = new Observed.DefaultObserved<>();


    transient public final EventEmitter<Term,Operation> exe;


    transient public final EmotionMeter emotion;
    transient public final LogicMeter logic;
    transient public final ResourceMeter resource;

    public final Param param;

    transient private final Deque<Runnable> nextTasks = new ConcurrentLinkedDeque();

    transient private final Set<Concept> questionConcepts = Global.newHashSet(16);
    transient private final Set<Concept> goalConcepts = Global.newHashSet(16);

    transient private final Set<Concept> pendingDeletions = Global.newHashSet(16);

    transient final ConceptBuilder conceptBuilder;

    transient public PremiseProcessor rules;

    public final Clock clock;
    transient ExecutorService laterTasks = null;

    public final CacheBag<Term, Concept> concepts;


    transient private int level;
    private long currentStampSerial = 1;
    /**
     * The remaining number of steps to be carried out (stepLater mode)
     */
    transient private long inputPausedUntil = -1;
    transient private boolean inCycle = false;
    private long nextRandomSeed = 1;



    /**
     * Create a new memory
     *
     * @param narParam reasoner paramerters
     * @param policy logic parameters
     */
    public Memory(Random rng, int nalLevel, Param narParam, ConceptBuilder conceptBuilder, PremiseProcessor policy, CacheBag<Term,Concept> concepts) {

        this.random = rng;
        this.level = nalLevel;

        this.clock = narParam.getClock();

        this.concepts = concepts;

        /*final Consumer<Concept> deleteOnConceptRemove = c -> delete(c);
        concepts.setOnRemoval(deleteOnConceptRemove);*/

        this.param = narParam;
        this.rules = policy;

        this.self = Global.DEFAULT_SELF; //default value

        this.event = new EventEmitter.DefaultEventEmitter();
        //this.event = new EventEmitter.FastDefaultEventEmitter();
        this.exe = new EventEmitter.DefaultEventEmitter();
        //this.exe = new EventEmitter.FastDefaultEventEmitter();


        this.conceptBuilder = conceptBuilder;

        //optional:
        this.resource = null; //new ResourceMeter();
        this.logic = new LogicMeter(this);
        this.emotion = new EmotionMeter(this);


    }

    @Override
    public Param getParam() {
        return param;
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
            return term(compound.op(), components);
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
    public static Term term(final Op op, final Term... a) {


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



    public Atom self() {
        return self;
    }

    public void setSelf(Atom t) {
        this.self = t;
    }

    /**
     * Entry point for all potentially executable tasks.
     * Enters a task and determine if there is a decision to execute.
     * @return number of invoked handlers
     */
    public int execute(final Task goal) {
        Term term = goal.getTerm();
        if (term instanceof Operation) {
            Operation o = (Operation)term;
            o.setTask(goal);
            if (!o.setMemory(this)) {
                throw new RuntimeException("operation " + o + " already executing from different memory");
            }
            int n = exe.emit(o.getOperator(), o);
            o.setMemory(null /* signals finished */);
            return n;
        }
        /*else {
            System.err.println("Unexecutable: " + goal);
        }*/
        return 0;
    }




    /* ---------- Constructor ---------- */



    public Concept newConcept(final Term term, final Budget budget) {

        Concept concept = conceptBuilder.newConcept(term, budget, this);
        if (concept!=null)
            concepts.put(concept);

        return concept;
    }

    public Atom the(final String s) {
        return Atom.the(s);
    }


    /** sets the random seed which will be used in the next reset(), then reset() */
    public void reset(long randomSeed) {
        nextRandomSeed = randomSeed;
        reset();
    }

    /** called when a Concept's lifecycle has changed */
    public void updateConceptState(Concept c) {
        boolean hasQuestions = c.hasQuestions();
        boolean hasGoals = !c.getGoals().isEmpty();

        if (isActive(c)) {
            //index an incoming concept with existing questions or goals
            if (hasQuestions) updateConceptQuestions(c);
            //if (hasGoals) updateConceptGoals(c);
        }
        else  {
            //unindex an outgoing concept with questions or goals
            if (hasQuestions) questionConcepts.remove(c);
            //..
        }

    }

    /** handles maintenance of concept question/goal indices when concepts change according to reports by certain events
        called by a Concept when its questions state changes (becomes empty or becomes un-empty) */
    public void updateConceptQuestions(Concept c) {
        if (!c.hasQuestions() && !c.hasQuests()) {
            if (!questionConcepts.remove(c))
                throw new RuntimeException("Concept " + c + " never registered any questions");
        }
        else {
            if (!questionConcepts.add(c)) {
                throw new RuntimeException("Concept " + c + " aready registered existing questions");
            }

            //this test was cycle.size() previously:
            if (questionConcepts.size() > getCycleProcess().size()) {
                throw new RuntimeException("more questionConcepts " +questionConcepts.size() + " than concepts " + getCycleProcess().size());
            }
        }
    }

    public void updateConceptGoals(Concept c) {
        //TODO
    }

    public void delete() {
        reset();

        event.delete();
    }

    public void reset(CycleProcess control) {
        this.cycle = control;
        reset();
    }

    public synchronized void reset() {

        cycle.reset(this);


        nextTasks.clear();

        if (laterTasks!=null) {
            laterTasks.shutdown();
            laterTasks = null;
        }


        event.emit(ResetStart.class);

        clock.reset();


        //NOTE: allow stamp serial to continue increasing after reset.
        //currentStampSerial = ;

        inputPausedUntil = -1;

        questionConcepts.clear();

        concepts.clear();

        goalConcepts.clear();

        emotion.clear();

        event.emit(Restart.class);

        //unless explicitly changed, nextRandomSeed will remain unchanged
        random.setSeed(nextRandomSeed);
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
    @Override
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
    @Override
    public Concept conceptualize(Termed termed, final Budget budget) {

        if (termed == null)
            return null;

        //validation here is to avoid checking a term if we know it is already normalized
        final boolean needsValidation;

        needsValidation = (termed instanceof Term);
//        if (termed instanceof Term) {
//            needsValidation = true;
//        }
//        else if (termed instanceof Task) {
//            //in a task should mean it's already valid
//            needsValidation = false;
//        }
//        else if (termed instanceof TaskLink) {
//            needsValidation = false;
//        }
//        else if (termed instanceof TermLinkTemplate) {
//            needsValidation = false;
//        }
//        else if (termed instanceof TermLinkKey) {
//            needsValidation = false;
//        }
//        else {
//            throw new RuntimeException("unknown validation requirement: " + termed + " " + termed.getClass());
//        }

        Term term = termed.getTerm();

        if (needsValidation) {
            if (!validConceptTerm(term))
                return null;

            if ((term = term.normalized()) == null)
                return null;
        }

        return getCycleProcess().conceptualize(term, budget, true);
    }

    private boolean validConceptTerm(Term term) {
        return !((term instanceof Variable) || (term instanceof AbstractInterval));
    }

    /**
     * Get the current activation level of a concept.
     *
     * @param t The Term naming a concept
     *          @param v the value returned in case of a non-existing concept
     * @return the priority value of the concept
     */
    public float conceptPriority(final Term t, float valueForMissing) {
        final Concept c = concept(t);
        return (c == null) ? valueForMissing : c.getPriority();
    }


//    public void add(final Iterable<Task> source) {
//        for (final Task t : source)
//            add((Task) t);
//    }


    @Override public int duration() {
        return param.duration.get();
    }


    /** safety checks which should not ordinarily be ncessary but maybe for debugging */
    static void ensureValidTask(final Task t) {

        switch (t.getPunctuation()) {
            case Symbols.JUDGMENT:
            case Symbols.QUESTION:
            case Symbols.QUEST:
            case Symbols.GOAL:
            case Symbols.COMMAND:
                break;
            default:
                throw new RuntimeException("Invalid sentence punctuation");
        }

        if (t.isJudgmentOrGoal() && (t.getTruth() == null)) {
            throw new RuntimeException("Judgment and Goal sentences require non-null truth value");
        }

        if ((t.getParentTaskRef() != null && t.getParentTask() == null))
            throw new RuntimeException("parentTask must be null itself, or reference a non-null Task");

        /*
        if (t.equals( t.getParentTask()) ) {
            throw new RuntimeException(t + " has parentTask equal to itself");
        }
        */

        if (t.getEvidence()==null)
            throw new RuntimeException(t + " from premise " + t.getParentTask() + "," + t.getParentBelief()
                    + " yet no evidence provided");


        if (Global.DEBUG) {
            if (Sentence.invalidSentenceTerm(t.getTerm())) {
                throw new RuntimeException("Invalid sentence content term: " + t.getTerm());
            }
        }

    }

    final public void input(final Task[] t) {
        for (final Task x : t) input(x);
    }

    /**
     * exposes the memory to an input, derived, or immediate task.
     * the memory then delegates it to its controller
     *
     * return true if the task was processed
     * if the task was a command, it will return false even if executed
     */
    public boolean input(final Task t) {

        if (t == null || !t.init(this)) {
            return false;
        }

        if (t.isCommand()) {
            int n = execute(t);
            if (n == 0) {
                emit(Events.ERR.class, "Unknown command: " + t);
            }
            return false;
        }

        if (Global.DEBUG) {
            ensureValidTask(t);
        }

        if (!Terms.levelValid(t, nal())) {
            removed(t, "Insufficient NAL level");
            return false;
        }

        /* delegate the fate of this task to controller */
        if (getCycleProcess().accept(t)) {


            emit(t.isInput() ? Events.IN.class : Events.OUT.class, t);


            //NOTE: if duplicate outputs happen, the budget wil have changed
            //but they wont be displayed.  to display them,
            //we need to buffer unique TaskAdd ("OUT") tasks until the end
            //of the cycle


            logic.TASK_ADD_NEW.hit();
            return true;
        }
        else {
            removed(t, "Ignored");
        }

        return false;
    }



    /* ---------- new task entries ---------- */

    /** called anytime a task has been removed, deleted, discarded, ignored, etc. */
    public void removed(final Task task, final String removalReason) {

        if (removalReason!=null)
            task.log(removalReason);

        if (Global.DEBUG_DERIVATION_STACKTRACES && Global.DEBUG_TASK_LOG)
            task.log(Premise.getStack());

        //System.err.println("REMOVED: " + task.getExplanation());

        eventTaskRemoved.emit(task);
        task.delete();
    }

    final public void removed(final Task task) {
        removed(task, null);
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
    final public void taskNext(Runnable t) {
        nextTasks.addLast(t);
    }

    /** runs all the tasks in the 'Next' queue */
    protected final void runNextTasks() {
        int originalSize = nextTasks.size();
        if (originalSize == 0) return;

        CycleProcess.run(nextTasks, originalSize);
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

    /** whether the NAR is currently accepting new inputs */
    public boolean isInputting() {
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



    public void forEachTask(boolean includeTaskLinks, Consumer<Task> each) {
        getCycleProcess().forEachConcept(c -> {
            if (c.getTaskLinks() != null)
                c.getTaskLinks().forEach(tl -> {
                    each.accept(tl.getTask());
                });
        });
    }

    /**
     * get all tasks in the system by iterating all newTasks, novelTasks; does not change or remove any
     * Concept TaskLinks
     */
    public void getTasks(boolean includeTaskLinks, boolean includeNewTasks, boolean includeNovelTasks, Set<Task> target) {


        if (includeTaskLinks) {
            getCycleProcess().forEachConcept(c -> {
                Bag<Sentence, TaskLink> tl = c.getTaskLinks();
                if (tl != null)
                    tl.forEach(t ->  target.add(t.targetTask) );
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

    public <T extends Compound> TaskSeed newTask(T t) {
        return TaskSeed.make(this, t);
    }




    /**
     * samples a next active concept for processing;
     * may return null if no concept is available depending on the control system
     */
    public Concept nextConcept() {
        return getCycleProcess().nextConcept();
    }

    /** scan for a next concept matching the predicate */
    public Concept nextConcept(Predicate<Concept> pred, float v) {
        return getCycleProcess().nextConcept(pred, v);
    }

    @Override
    public Clock getClock() {
        return clock;
    }

    /** returns the concept index */
    public CacheBag<Term, Concept> getConcepts() {
        return cycle;
    }

//

    public boolean isActive(Concept c) {
        return cycle.concept(c.getTerm())!=null;
    }

    public int numConcepts(boolean active, boolean inactive) {
        int total = 0;
        if (active && !inactive) return getCycleProcess().size();
        else if (!active && inactive) return concepts.size() - getCycleProcess().size();
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

        //event.emit(Events.CycleStart.class);
        eventCycleStart.emit(this);

        getCycleProcess().cycle();

        //event.emit(Events.CycleEnd.class);
        eventCycleEnd.emit(this);

        inCycle = false;

        deletePendingConcepts();

        emotion.commit();

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

        Concept removedFromActive = getCycleProcess().remove(c);

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

    /** sums the priorities of items in different active areas of the memory */
    public double getActivePrioritySum(final boolean concept, final boolean tasklink, final boolean termlink) {
        final double[] total = {0};
        getCycleProcess().forEachConcept(c-> {
            if (concept)
                total[0] += c.getPriority();
            if (tasklink)
                total[0] += c.getTaskLinks().getPrioritySum();
            if (termlink)
                total[0] += c.getTermLinks().getPrioritySum();
        });
        return total[0];
    }

    public CycleProcess<Memory> getCycleProcess() {
        return cycle;
    }


    public double getActivePriorityPerConcept(final boolean concept, final boolean tasklink, final boolean termlink) {
        int c = numConcepts(true, false);
        if (c == 0) return 0;
        return getActivePrioritySum(concept, tasklink, termlink)/c;
    }

    public static boolean equals(Memory a, Memory b) {

        //TODO
        //for now, just accept that they include the same set of terms

        Set<Term> aTerm = new LinkedHashSet();
        Set<Term> bTerm = new LinkedHashSet();
        a.concepts.forEach(ac -> aTerm.add(ac.getTerm()));
        b.concepts.forEach(bc -> bTerm.add(bc.getTerm()));
        if (!aTerm.equals(bTerm)) {
            System.out.println(aTerm.size() + " " + aTerm);
            System.out.println(bTerm.size() + " " + bTerm);
            return false;
        }


        /*if (!a.concepts.equals(b.concepts)) {

        }*/
        return true;
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


    //public Iterator<Concept> getConcepts(boolean active, boolean inactive) {
//        if (active && !inactive)
//            return getControl().iterator();
//        else if (!active && inactive)
//            return Iterators.filter(concepts.iterator(), c -> isActive(c));
//        else if (active && inactive)
//            return concepts.iterator(); //'concepts' should contain all concepts
//        else
//            return Iterators.emptyIterator();
//    }
}

