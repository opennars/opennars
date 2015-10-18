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

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

/**
 * Memory consists of the run-time state of a NAR, including: * term and concept
 * memory * clock * reasoner state * etc.
 * <p>
 * Excluding input/output channels which are managed by a NAR.
 * <p>
 * A memory is controlled by zero or one NAR's at a given time.
 * <p>
 * Memory is serializable so it can be persisted and transported.
 */
public class Memory extends Param {

    protected Atom self;

    public final Random random;

    @Deprecated
    transient public final EventEmitter<Class, Object[]> event;

    transient public final Topic<Task<?>> eventTaskRemoved = new DefaultTopic<>();
    transient public final Topic<ConceptProcess> eventConceptProcess = new DefaultTopic<>();

    transient public final Topic<Memory> eventReset = new DefaultTopic<>();

    transient public final Topic<Concept> eventConceptActivated = new DefaultTopic<>();

    transient public final Topic<NAR> eventFrameStart = new DefaultTopic<>();

    /**
     * fired at the end of each memory cycle
     */
    transient public final Topic<Memory> eventCycleEnd = new DefaultTopic<>(); //eventCycleStart; //new DefaultObserved();

    transient public final Topic<TaskProcess> eventTaskProcess = new DefaultTopic<>();


    /**
     * used for reporting or informing outside. consists of additional notes
     * or data which could annotate a log or summary of system activity
     */
    transient public final Topic<Serializable> eventSpeak = new DefaultTopic<>();

    transient public final Topic<ExecutionResult> eventExecute = new DefaultTopic<>();

    public transient final Topic<Task> eventInput = new DefaultTopic<>();
    public transient final Topic<Serializable> eventError = new DefaultTopic<>();
    public transient final Topic<Task> eventDerived = new DefaultTopic<>();

    public transient final Topic<Twin<Task>> eventAnswer = new DefaultTopic<>();
    public transient final Topic<Concept> eventConceptChange = new DefaultTopic();

    /** executables (incl. operators) */
    transient public final EventEmitter<Term, Task<Operation>> exe;


    //TODO move these to separate components, not part of Memory:
    transient public final EmotionMeter emotion;
    transient public final LogicMeter logic;


    public final Clock clock;

    public final CacheBag<Term, Concept> concepts;


    /** maximum NAL level currently supported by this memory, for restricting it to activity below NAL8 */
    int level;

    /** for creating new stamps
     * TODO move this to and make this the repsonsibility of Clock implementations
     * */
    volatile long currentStampSerial = 1;

    /**
     * Create a new memory
     */
    public Memory(Clock clock, Random rng, CacheBag<Term, Concept> concepts) {

        this.random = rng;

        this.level = 8;

        this.clock = clock;

        this.concepts = concepts;


        this.self = Global.DEFAULT_SELF; //default value

        this.event = new EventEmitter.DefaultEventEmitter();
        this.exe = new EventEmitter.DefaultEventEmitter();


        //temporary
        this.logic = new LogicMeter(this);
        this.emotion = new EmotionMeter(this);


    }



    @Override
    public final int nal() {
        return level;
    }

    public final void nal(int newLevel) {
        this.level = newLevel;
    }


//    public Concept concept(final String t) {
//        return concept(Atom.the(t));
//    }

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


    public final Atom self() {
        return self;
    }

    public void setSelf(Atom t) {
        this.self = t;
    }

    /**
     * Entry point for all potentially executable tasks.
     * Enters a task and determine if there is a decision to execute.
     *
     * @return number of invoked handlers
     */
    public final int execute(final Task goal) {
        Term term = goal.getTerm();

        if (term instanceof Operation) {
            final Operation o = (Operation) term;
            return exe.emit(o.getOperatorTerm(), goal);
        }
        /*else {
            System.err.println("Unexecutable: " + goal);
        }*/

        return 0;
    }


//    public void delete() {
//        clear();
//
//        event.delete();
//    }


    @Override
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
     * Get an existing (active OR forgotten) Concept identified
     * by the provided Term
     */
    public final Concept concept(Term t) {
        if (!t.isNormalized()) {
            t = ((Compound) t).normalized();
            if (t == null) return null;
        }
        return concepts.get(t);
    }


    /**
     * Get the current activation level of a concept.
     *
     * @param t The Term naming a concept
     * @return the priority value of the concept
     */
    public final float conceptPriority(final Term t, float valueIfMissing) {
        final Concept c = concept(t);
        return (c == null) ? valueIfMissing : c.getPriority();
    }


//    public void add(final Iterable<Task> source) {
//        for (final Task t : source)
//            add((Task) t);
//    }


    public final int duration() {
        return this.duration.get();
    }






    /* ---------- new task entries ---------- */

    /**
     * called anytime a task has been removed, deleted, discarded, ignored, etc.
     */
    public final void remove(final Task task, final String removalReason) {

        task.log(removalReason);

        if (task.delete()) {

            if (Global.DEBUG_DERIVATION_STACKTRACES && Global.DEBUG_TASK_LOG)
                task.log(Premise.getStack());

            eventTaskRemoved.emit(task);
        }

    }

//    /**
//     * sends an event signal to listeners subscribed to channel 'c'
//     */
//    final public void emit(final Class c, final Object... signal) {
//        event.emit(c, signal);
//    }

//    /**
//     * sends an event signal to listeners subscribed to channel 'c'
//     */
//    final public void emit(final Class c) {
//        event.emit(c);
//    }


    /**
     * produces a new stamp serial #, used to uniquely identify inputs
     */
    public final long newStampSerial() {
        //TODO does this need to be AtomicLong ?
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

    public final Clock getClock() {
        return clock;
    }

//    /**
//     * TODO return value
//     */
//    public void delete(Term term) {
//        Concept c = concept(term);
//        if (c == null) return;
//
//        delete(c);
//    }

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
//
//    /**
//     * actually delete procedure for a concept; removes from indexes
//     * TODO return value
//     */
//    protected void delete(Concept c) {
//
////        Concept removedFromActive = getCycleProcess().remove(c);
////
////        if (c!=removedFromActive) {
////            throw new RuntimeException("another instances of active concept " + c + " detected on removal: " + removedFromActive);
////        }
//
//        Concept removedFromIndex = concepts.remove(c.getTerm());
//        if (removedFromIndex == null) {
//            throw new RuntimeException("concept " + c + " was not removed from memory");
//        }
//        /*if (c!=removedFromIndex) {
//            throw new RuntimeException("another instances of concept " + c + " detected on removal: " + removedFromActive);
//        }*/
//
//        c.delete();
//    }


    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Memory)) return false;
        return Memory.equals(this, (Memory) obj);
    }

    public static boolean equals(final Memory a, final Memory b) {

        //TODO
        //for now, just accept that they include the same set of terms

        Set<Term> aTerm = new LinkedHashSet();
        Set<Term> bTerm = new LinkedHashSet();
        a.concepts.forEach(ac -> aTerm.add(ac.getTerm()));
        b.concepts.forEach(bc -> bTerm.add(bc.getTerm()));
        if (!aTerm.equals(bTerm)) {
            /*System.out.println(aTerm.size() + " " + aTerm);
            System.out.println(bTerm.size() + " " + bTerm);*/
            return false;
        }


        /*if (!a.concepts.equals(b.concepts)) {

        }*/
        return true;
    }

    public final long time() {
        return getClock().time();
    }

//    public final void put(final Concept c) {
//        concepts.put(c);
//    }

    public final CacheBag<Term, Concept> getConcepts() {
        return concepts;
    }

    public final void cycle(int num) {

        //final Clock clock = this.clock;
        final Topic<Memory> end = eventCycleEnd;

        //synchronized (clock) {

            for (; num > 0; num--) {


                end.emit(this);

            }

        //}

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
        return getClass().getSimpleName() + ':' + nal() + "[@" + time() + ",C=" + size() + ']';
    }

    public final int size() {
        return concepts.size();
    }

//    /**
//     * identifies the type of memory as a string
//     */
//    String toTypeString() {
//        return getClass().getSimpleName();
//    }

    public void start() {
        this.concepts.start(this);
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

