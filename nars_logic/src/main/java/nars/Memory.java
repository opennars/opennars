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
import nars.bag.Bag;
import nars.bag.impl.CurveBag;
import nars.concept.AtomConcept;
import nars.concept.Concept;
import nars.concept.DefaultConcept;
import nars.nal.nal7.CyclesInterval;
import nars.nal.nal8.Execution;
import nars.process.ConceptProcess;
import nars.task.Task;
import nars.term.Term;
import nars.term.Termed;
import nars.term.atom.Atom;
import nars.term.compile.TermIndex;
import nars.term.variable.Variable;
import nars.time.Clock;
import nars.util.data.random.XorShift128PlusRandom;
import nars.util.event.DefaultTopic;
import nars.util.event.EventEmitter;
import nars.util.event.Topic;
import nars.util.meter.EmotionMeter;
import nars.util.meter.LogicMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

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
    public final transient EventEmitter<Class, Object[]> event;

    public final transient Topic<Task> eventTaskRemoved = new DefaultTopic<>();
    public final transient Topic<ConceptProcess> eventConceptProcess = new DefaultTopic<>();
    public final transient Topic<Task> eventRevision = new DefaultTopic<>();

    public final transient Topic<Memory> eventReset = new DefaultTopic<>();

    public final transient Topic<NAR> eventFrameStart = new DefaultTopic<>();

    /**
     * fired at the end of each memory cycle
     */
    public final transient Topic<Memory> eventCycleEnd = new DefaultTopic<>(); //eventCycleStart; //new DefaultObserved();

    public final transient Topic<Task> eventTaskProcess = new DefaultTopic<>();

    public static final Logger logger = LoggerFactory.getLogger(Memory.class);

    /**
     * used for reporting or informing outside. consists of additional notes
     * or data which could annotate a log or summary of system activity
     */
    public final transient Topic<Serializable> eventSpeak = new DefaultTopic<>();



    public final transient Topic<Task> eventInput = new DefaultTopic<>();
    public final transient Topic<Serializable> eventError = new DefaultTopic<>();

    /** all derivations, even if they do not eventually reach the memory via input;
     *  this generates many events, use with caution
     */
    public final transient Topic<Task> eventDerived = new DefaultTopic<>();

    public final transient Topic<Twin<Task>> eventAnswer = new DefaultTopic<>();
    public final transient Topic<Concept> eventConceptChanged = new DefaultTopic();

    /** executables (incl. operators) */
    public final transient Map<Term, Topic<Execution>> exe = new HashMap();



    //TODO move these to separate components, not part of Memory:
    public final transient EmotionMeter emotion;
    public final transient LogicMeter logic;

    public final Clock clock;

    /** holds known Term's and Concept's */
    public final TermIndex index;


    /** maximum NAL level currently supported by this memory, for restricting it to activity below NAL8 */
    int level;

    /** for creating new stamps
     * TODO move this to and make this the repsonsibility of Clock implementations
     * */
    long currentStampSerial = 1;


    public Memory(Clock clock, TermIndex index) {
        this(clock, new XorShift128PlusRandom(1), index);
    }

    /**
     * Create a new memory
     */
    public Memory(Clock clock, Random rng, TermIndex index) {

        random = rng;

        level = 8;

        this.clock = clock;
        clock.clear(this);

        this.index = index;


        self = Global.DEFAULT_SELF; //default value

        event = new EventEmitter.DefaultEventEmitter();


        //temporary
        logic = new LogicMeter(this);
        emotion = new EmotionMeter(this);


    }



    @Override
    public final int nal() {
        return level;
    }

    public final void nal(int newLevel) {
        level = newLevel;
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
        self = t;
    }


//    public void delete() {
//        clear();
//
//        event.delete();
//    }


    @Override
    public synchronized void clear() {

        eventReset.emit(this);

        clock.clear(this);

        index.clear();

        emotion.clear();

    }


//    Concept concept(Compound c, CompoundTransform transform) {
//        return concept(index.transform(c, transform));
//    }

    static boolean validConceptTerm(Term term) {
        return !((term instanceof Variable) || (term instanceof CyclesInterval));
    }

    Concept newDefaultConcept(Term t) {

        int termLinkBagSize = 32;
        int taskLinkBagSize = 32;

        Bag<Task> taskLinks =
                new CurveBag<Task>(taskLinkBagSize, random).mergePlus();

        Bag<Termed> termLinks =
                new CurveBag<Termed>(termLinkBagSize, random).mergePlus();

        //Budget b = new UnitBudget();
        //Budget b = new BagAggregateBudget(taskLinks);

        return t instanceof Atom ?

                new AtomConcept(
                        t,
                        termLinks, taskLinks) :

                new DefaultConcept(
                        t,
                        taskLinks, termLinks, this);

    }

    public Concept concept(Termed t) {
        if (t instanceof Concept) return ((Concept)t);

        Term tt = t.term();
        if (!validConceptTerm(tt)) return null;
        if (!tt.isNormalized()) {
            t = index.normalized(tt);
            if (t instanceof Concept)
                return ((Concept)t);
            tt = t.term();
        }

        Function<Term, Termed> build = this::newDefaultConcept;

        //TODO ? put the unnormalized term for cached future normalizations?

        Termed exists = index.apply(tt, build);

        if (exists instanceof Concept) {
            Concept c = ((Concept)exists);
            return c;
        }

        if (exists==null)
            exists = t;

        //attempt replace entry from term to concept
        Termed tx = build.apply(exists.term());
        if (tx instanceof Concept) {
            index.putTerm(tx);
            return (Concept) tx;
        }


        return null;
    }


    public final Concept taskConcept(Termed t) {
        Concept c = concept(t);

        if (c == null || (!c.levelValid( nal()) ) || (!Task.validTaskTerm(c.term())))
            return null;

        return c;
    }



//    public void add(final Iterable<Task> source) {
//        for (final Task t : source)
//            add((Task) t);
//    }

    /** current temporal perception duration of the reasoner */
    public final int duration() {
        return duration.intValue();
    }






    /* ---------- new task entries ---------- */

    /**
     * called anytime a task has been removed, deleted, discarded, ignored, etc.
     */
    public final void remove(Task task, Object removalReason) {

        boolean willBeReceived = !eventTaskRemoved.isEmpty();

        if (willBeReceived && removalReason!=null)
            task.log(removalReason);

        if (!task.isDeleted()) {

            task.getBudget().delete();

            if (willBeReceived) {

                /*if (Global.DEBUG_DERIVATION_STACKTRACES && Global.DEBUG_TASK_LOG)
                    task.log(Premise.getStack());*/

                eventTaskRemoved.emit(task);
            }
            /* else: a more destructive cleanup of the discarded task? */
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
        //TODO maybe AtomicLong ?
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


//    @Override
//    public final boolean equals(Object obj) {
//        if (this == obj) return true;
//        if (!(obj instanceof Memory)) return false;
//        return Memory.equals(this, (Memory) obj);
//    }

//    public static boolean equals(final Memory a, final Memory b) {
//
//        //TODO
//        //for now, just accept that they include the same set of terms
//
//        Set<Term> aTerm = new LinkedHashSet();
//        Set<Term> bTerm = new LinkedHashSet();
//        a.concepts.forEach(ac -> aTerm.add(ac.getTerm()));
//        b.concepts.forEach(bc -> bTerm.add(bc.getTerm()));
//        if (!aTerm.equals(bTerm)) {
//            /*System.out.println(aTerm.size() + " " + aTerm);
//            System.out.println(bTerm.size() + " " + bTerm);*/
//            return false;
//        }
//
//
//        /*if (!a.concepts.equals(b.concepts)) {
//
//        }*/
//        return true;
//    }

    public final long time() {
        return clock.time();
    }

//    public final void put(final Concept c) {
//        concepts.put(c);
//    }

    public final void cycle(int num) {

        //final Clock clock = this.clock;
        Topic<Memory> eachCycle = eventCycleEnd;

        //synchronized (clock) {

            for (; num > 0; num--) {


                eachCycle.emit(this);

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
        return index.size();
    }

//    /**
//     * identifies the type of memory as a string
//     */
//    String toTypeString() {
//        return getClass().getSimpleName();
//    }

    public void start() {

    }



    //    public byte[] toBytes() throws IOException, InterruptedException {
//        //TODO probably will want to do something more careful
//        return new JBossMarshaller().objectToByteBuffer(this);
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

