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


import com.gs.collections.api.tuple.Twin;
import nars.bag.impl.CacheBag;
import nars.clock.Clock;
import nars.concept.Concept;
import nars.meter.EmotionMeter;
import nars.meter.LogicMeter;
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
import nars.nal.nal7.TemporalRules;
import nars.nal.nal8.ExecutionResult;
import nars.nal.nal8.Operation;
import nars.premise.Premise;
import nars.process.ConceptProcess;
import nars.process.TaskProcess;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.util.event.DefaultTopic;
import nars.util.event.EventEmitter;
import nars.util.event.Topic;
import nars.util.meter.ResourceMeter;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

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
public class Memory extends Param implements Serializable {

    private Atom self;

    public final Random random;

    @Deprecated transient public final EventEmitter<Class,Object[]> event;

    transient public final Topic<Task<?>> eventTaskRemoved = new DefaultTopic();
    transient public final Topic<ConceptProcess> eventConceptProcessed = new DefaultTopic();

    transient public final Topic<Memory> eventReset = new DefaultTopic();

    transient public final Topic<Concept> eventConceptActivated = new DefaultTopic();
    transient public final Topic<Concept> eventConceptForget = new DefaultTopic();

    //transient public final Topic<NAR> eventFrameStart = new DefaultTopic();
    transient public final Topic<NAR> eventFrameEnd = new DefaultTopic();

    transient public final Topic<Memory>
            /** fired at the beginning of each memory cycle */
            eventCycleStart = new DefaultTopic(),
            /** fired at the end of each memory cycle */
            /* @Deprecated  */ eventCycleEnd = new DefaultTopic(); //eventCycleStart; //new DefaultObserved();

    transient public final Topic<TaskProcess> eventTaskProcess = new DefaultTopic<>();

    transient public final Topic<ExecutionResult> eventExecute = new DefaultTopic<>();

    transient public final EventEmitter<Term,Operation> exe;


    transient public final EmotionMeter emotion;
    transient public final LogicMeter logic;
    transient public final ResourceMeter resource;


    /*transient private final Set<Concept> questionConcepts = Global.newHashSet(16);
    transient private final Set<Concept> goalConcepts = Global.newHashSet(16);

    transient private final Set<Concept> pendingDeletions = Global.newHashSet(16);*/

    //transient final ConceptBuilder conceptBuilder;



    public final Clock clock;

    public final CacheBag<Term, Concept> concepts;


    private int level;

    private long currentStampSerial = 1;
    private boolean inCycle = false;


    public transient final Topic<Task> eventInput = new DefaultTopic<>();
    public transient final Topic<Object> eventError = new DefaultTopic<>();
    public transient final Topic<Task> eventDerived = new DefaultTopic();
    public transient final Topic<Twin<Task>> eventAnswer = new DefaultTopic();


    /**
     * Create a new memory
     *
     * @param narParam reasoner paramerters
     * @param policy logic parameters
     */
    public Memory(Clock clock, Random rng, CacheBag<Term,Concept> concepts) {

        this.random = rng;

        this.level = 8;

        this.clock = clock;

        this.concepts = concepts;

        /*final Consumer<Concept> deleteOnConceptRemove = c -> delete(c);
        concepts.setOnRemoval(deleteOnConceptRemove);*/

        this.self = Global.DEFAULT_SELF; //default value

        this.event = new EventEmitter.DefaultEventEmitter();
        //this.event = new EventEmitter.FastDefaultEventEmitter();
        this.exe = new EventEmitter.DefaultEventEmitter();
        //this.exe = new EventEmitter.FastDefaultEventEmitter();


        //optional:
        this.resource = null; //new ResourceMeter();
        this.logic = new LogicMeter(this);
        this.emotion = new EmotionMeter(this);
        emotion.commit();


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

//    /** provides fast iteration to concepts with questions */
//    public Set<Concept> getQuestionConcepts() {
//        return questionConcepts;
//    }
//
//    /** provides fast iteration to concepts with goals */
//    public Set<Concept> getGoalConcepts() {
//        throw new RuntimeException("disabled until it is useful");
//        //return goalConcepts;
//    }



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




    public void setRandomSeed(long l) { random.setSeed(l); }



    public void delete() {
        clear();

        event.delete();
    }



    public synchronized void clear() {



        eventReset.emit(this);

        clock.clear();

        //NOTE: allow stamp serial to continue increasing after reset.
        //currentStampSerial = ;

        //questionConcepts.clear();

        concepts.clear();

        //goalConcepts.clear();

        emotion.clear();



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


    public int duration() {
        return this.duration.get();
    }






    /* ---------- new task entries ---------- */

    /** called anytime a task has been removed, deleted, discarded, ignored, etc. */
    public void remove(final Task task, String removalReason) {

        if (task.isDeleted()) {
            throw new RuntimeException(task + " already deleted");
        }

        if (removalReason==null)
            removalReason = "Unknown";

        task.log(removalReason);

        if (Global.DEBUG_DERIVATION_STACKTRACES && Global.DEBUG_TASK_LOG)
            task.log(Premise.getStack());

        //System.err.println("REMOVED: " + task.getExplanation());

        eventTaskRemoved.emit(task);
        task.delete();
    }

    final public void remove(final Task task) {
        remove(task, null);
    }

    /** sends an event signal to listeners subscribed to channel 'c' */
    final public void emit(final Class c, final Object... signal) {
        event.emit(c, signal);
    }

    /** sends an event signal to listeners subscribed to channel 'c' */
    final public void emit(final Class c) {
        event.emit(c);
    }





    /** produces a new stamp serial #, used to uniquely identify inputs */
    public long newStampSerial() {
        return currentStampSerial++;
    }

//    /** whether the NAR is currently accepting new inputs */
//    public boolean isInputting() {
//        if (inputPausedUntil == -1) return true;
//        return time() >= inputPausedUntil;
//    }



//    /**
//     * samples a next active concept for processing;
//     * may return null if no concept is available depending on the control system
//     */
//    public Concept nextConcept() {
//        return getCycleProcess().nextConcept();
//    }
//
//    /** scan for a next concept matching the predicate */
//    public Concept nextConcept(Predicate<Concept> pred, float v) {
//        return getCycleProcess().nextConcept(pred, v);
//    }

    public Clock getClock() {
        return clock;
    }

    /** TODO return value */
    public void delete(Term term) {
        Concept c = concept(term);
        if (c == null) return;

        delete(c);
    }

//    /** queues the deletion of a concept until after the current cycle ends.
//     */
//    public synchronized void delete(Concept c) {
//        if (!inCycle()) {
//            //immediately delete
//            _delete(c);
//        }
//        else {
//            pendingDeletions.add(c);
//        }
//
//    }
//
//    /** called by Memory at end of each cycle to flush deleted concepts */
//    protected void deletePendingConcepts() {
//        if (!pendingDeletions.isEmpty()) {
//
//            for (Concept c : pendingDeletions)
//                _delete(c);
//
//            pendingDeletions.clear();
//        }
//    }

    /** actually delete procedure for a concept; removes from indexes
     * TODO return value
     * */
    protected void delete(Concept c) {

//        Concept removedFromActive = getCycleProcess().remove(c);
//
//        if (c!=removedFromActive) {
//            throw new RuntimeException("another instances of active concept " + c + " detected on removal: " + removedFromActive);
//        }

        Concept removedFromIndex = concepts.remove(c.getTerm());
        if (removedFromIndex == null) {
            throw new RuntimeException("concept " + c + " was not removed from memory");
        }
        /*if (c!=removedFromIndex) {
            throw new RuntimeException("another instances of concept " + c + " detected on removal: " + removedFromActive);
        }*/

        c.delete();
    }


    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Memory)) return false;
        return Memory.equals(this, (Memory)obj);
    }

    public static boolean equals(final Memory a, final Memory b) {

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

    public final long time() {
        return getClock().time();
    }

    public void put(final Concept c) {
        concepts.put(c);
    }

    public final CacheBag<Term, Concept> getConcepts() {
        return concepts;
    }

    public synchronized void cycle(int num) {

        for ( ; num > 0; num--) {

            inCycle = true;

            clock.preCycle();

            eventCycleStart.emit(this);

            eventCycleEnd.emit(this);

            inCycle = false;

            //deletePendingConcepts();

            //randomUpdate();
        }

    }



//    /** called when a Concept's lifecycle has changed */
//    public void updateConceptState(Concept c) {
//        boolean hasQuestions = c.hasQuestions();
//        boolean hasGoals = !c.getGoals().isEmpty();
//
//        if (isActive(c)) {
//            //index an incoming concept with existing questions or goals
//            if (hasQuestions) updateConceptQuestions(c);
//            //if (hasGoals) updateConceptGoals(c);
//        }
//        else  {
//            //unindex an outgoing concept with questions or goals
//            if (hasQuestions) questionConcepts.remove(c);
//            //..
//        }
//
//    }
//
//    /** handles maintenance of concept question/goal indices when concepts change according to reports by certain events
//        called by a Concept when its questions state changes (becomes empty or becomes un-empty) */
//    public void updateConceptQuestions(Concept c) {
//        if (!c.hasQuestions() && !c.hasQuests()) {
//            if (!questionConcepts.remove(c))
//                throw new RuntimeException("Concept " + c + " never registered any questions");
//        }
//        else {
//            if (!questionConcepts.add(c)) {
//                throw new RuntimeException("Concept " + c + " aready registered existing questions");
//            }
//
//            //this test was cycle.size() previously:
//            if (questionConcepts.size() > getCycleProcess().size()) {
//                throw new RuntimeException("more questionConcepts " +questionConcepts.size() + " than concepts " + getCycleProcess().size());
//            }
//        }
//    }
//
//    public void updateConceptGoals(Concept c) {
//        //TODO
//    }


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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[@" + time() + ",C=" + size() + "]";
    }

    public int size() {
        return concepts.size();
    }

    /** identifies the type of memory as a string */
    public String toTypeString() {
        return getClass().getSimpleName();
    }


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

