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
import nars.concept.Concept;
import nars.nal.nal8.ExecutionResult;
import nars.nal.nal8.Operation;
import nars.process.ConceptProcess;
import nars.process.TaskProcess;
import nars.task.Task;
import nars.term.*;
import nars.term.compile.TermIndex;
import nars.term.transform.CompoundTransform;
import nars.time.Clock;
import nars.util.data.random.XorShift1024StarRandom;
import nars.util.event.DefaultTopic;
import nars.util.event.EventEmitter;
import nars.util.event.Topic;
import nars.util.meter.EmotionMeter;
import nars.util.meter.LogicMeter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

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
    transient public final Topic<Task> eventRevision = new DefaultTopic<>();

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

    public final TermIndex terms = new MyTermIndex();

    public final CacheBag<Term, Concept> concepts;


    /** maximum NAL level currently supported by this memory, for restricting it to activity below NAL8 */
    int level;

    /** for creating new stamps
     * TODO move this to and make this the repsonsibility of Clock implementations
     * */
    long currentStampSerial = 1;


    public Memory(Clock clock, CacheBag<Term, Concept> concepts) {
        this(clock, new XorShift1024StarRandom(1), concepts);
    }

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


    public final Atom self() {
        return self;
    }

    public void setSelf(Atom t) {
        this.self = t;
    }


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
        //if (!t.isNormalized()) {
        final Term u = t.normalized();
        if (u == null) return null;
        //}
        return concepts.get(u);
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


    /** current temporal perception duration of the reasoner */
    public final int duration() {
        return this.duration.intValue();
    }


    /* ---------- new task entries ---------- */

    /**
     * called anytime a task has been removed, deleted, discarded, ignored, etc.
     */
    public final void remove(final Task task, final String removalReason) {

        final boolean willBeReceived = eventTaskRemoved.size() > 0;

        if (willBeReceived && removalReason!=null)
            task.log(removalReason);

        if (task.delete()) {

            if (willBeReceived) {

                if (Global.DEBUG_DERIVATION_STACKTRACES && Global.DEBUG_TASK_LOG)
                    task.log(Premise.getStack());

                eventTaskRemoved.emit(task);
            }
            /* else: a more destructive cleanup of the discarded task? */
        }

    }


    /**
     * produces a new stamp serial #, used to uniquely identify inputs
     */
    public final long newStampSerial() {
        //TODO maybe AtomicLong ?
        return currentStampSerial++;
    }


    public final Clock getClock() {
        return clock;
    }

    public final long time() {
        return getClock().time();
    }


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


    @Override
    public String toString() {
        return getClass().getSimpleName() + ':' + nal() + "[@" + time() + ",C=" + size() + ']';
    }

    public final int size() {
        return concepts.size();
    }


    public void start() {
        this.concepts.start(this);

    }

    private static class MyTermIndex implements TermIndex {

       // final Map<Term,Term> terms = new HashMap(4096); //TODO try weakref identity hash map etc

        @Override public final Termed get(Term t) {

            //if (t instanceof TermMetadata) {
                return t.normalized(this); //term instance will remain unique because it has attached metadata
            //}

           /* return terms.compute(t, (k,vExist) -> {
                if (vExist == null) return k.normalized(this);
                else
                    return vExist;
            }); */
            //return terms.computeIfAbsent(t, n -> n.normalized(this));
        }

        final CompoundTransform<Compound,Term> ct = new CompoundTransform<Compound,Term>() {

            @Override
            public final boolean test(Term term) {
                return true;
            }

            @Override
            public final Term apply(Compound c, Term subterm, int depth) {
                return get(subterm).getTerm();
            }
        };

        @Override
        public final CompoundTransform getCompoundTransformer() {
            return ct;
        }

        @Override
        public void forEachTerm(Consumer<Termed> c) {
          //  terms.forEach((k,v)->c.accept(v));
        }
    }

}

