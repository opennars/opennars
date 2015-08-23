package nars;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import nars.concept.Concept;
import nars.event.MemoryReaction;
import nars.event.NARReaction;
import nars.io.out.Output;
import nars.io.out.TextOutput;
import nars.io.qa.AnswerReaction;
import nars.meter.EmotionMeter;
import nars.meter.LogicMeter;
import nars.task.Task;
import nars.util.event.Observed;
import nars.util.event.Reaction;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

/**
 * Provides high-level stream-like NAR reasoner functionality in fluent API
 */
public class NARStream  {

    public final NAR nar;

    private final Multimap<Set<Class>, Reaction> reactions = HashMultimap.create();
    private List<Object> regs = new ArrayList();


    public NARStream(NAR nar) {
        this.nar = nar;
    }

    /**
     * multiple streams can be attached to an existing NAR
     */
    public NARStream(NARSeed s) {
        this(new NAR(s));
    }




    public NARStream answer(String question, Consumer<Task> recvSolution) {
        //question punctuation optional
        if (!question.endsWith("?")) question = question + "?";
        Task qt = nar.task(question);
        return answer(qt, recvSolution);
    }

    public NARStream answer(Task question, Consumer<Task> recvSolution) {
        new AnswerReaction(nar, question) {

            @Override public void onSolution(Task belief) {
                recvSolution.accept(belief);
            }

            @Override public void setActive(boolean b) {
                super.setActive(b);
                manage(this, b);
            }

        };
        return this;
    }

    protected final void ensureNotRunning() {
        if (nar.isRunning())
            throw new RuntimeException("NAR is already running");
    }

    public NAR loop(long periodMS) {
        ensureNotRunning();

        nar.loop(periodMS);

        return nar;
    }

    /** blocks until finished */
    public NARStream run(int frames) {
        ensureNotRunning();

        nar.frame(frames);

        return this;
    }

    public NARStream fork(Consumer<NARStream> clone) {
        ensureNotRunning();
        return this; //TODO
    }
    public NARStream save(ObjectOutputStream clone) {
        ensureNotRunning();
        return this; //TODO
    }
    public NARStream load(ObjectInputStream clone) {
        ensureNotRunning();
        return this; //TODO
    }

    public NARStream reset() {
        nar.reset();
        return this;
    }

    public NARStream input(String... ss) {
        for (String s : ss) nar.input(s);
        return this;
    }

    public NARStream input(Task... tt) {
        for (Task t : tt) nar.input(t);
        return this;
    }

    public NARStream inputAt(long time, String... tt) {
        return at(t -> t == time, () -> input(tt) );
    }

    public NARStream inputAt(LongPredicate timeCondition, Task... tt) {
        return at(timeCondition, () -> input(tt) );
    }

    public NARStream inputAt(long time, Task... tt) {
        return at(t -> t == time, () -> input(tt) );
    }

    public NARStream forEachConceptTask(boolean b, boolean q, boolean g, boolean _q,
                                        int maxPerConcept,
                                        Consumer<Task> recip) {
        forEachConcept(c -> {
            if (b && c.hasBeliefs())   c.getBeliefs().top(maxPerConcept, recip);
            if (q && c.hasQuestions()) c.getQuestions().top(maxPerConcept, recip);
            if (g && c.hasBeliefs())   c.getGoals().top(maxPerConcept, recip);
            if (_q && c.hasQuests())   c.getQuests().top(maxPerConcept, recip);
        });
        return this;
    }

    public NARStream forEachConcept(Consumer<Concept> recip) {
        nar.memory.concepts.forEach(recip);
        return this;
    }

    public NARStream forEachConceptActive(Consumer<Concept> recip) {
        nar.memory.getControl().forEach(recip);
        return this;
    }

    public NARStream conceptIterator(Consumer<Iterator<Concept>> recip) {
        recip.accept( nar.memory.concepts.iterator() );
        return this;
    }
    public NARStream conceptActiveIterator(Consumer<Iterator<Concept>> recip) {
        recip.accept( nar.memory.getControl().iterator() );
        return this;
    }

    //TODO iterate/query beliefs, etc

    public NARStream meterLogic(Consumer<LogicMeter> recip) {
        recip.accept( nar.memory.logic );
        return this;
    }
    public NARStream meterEmotion(Consumer<EmotionMeter> recip) {
        recip.accept( nar.memory.emotion );
        return this;
    }



    public NARStream resetEvery(long minPeriodOfCycles) {
        forEachPeriod(minPeriodOfCycles, this::reset);
        return this;
    }

    public NARStream forEachPeriod(long minPeriodOfCycles, Runnable action) {
        final long start = nar.time();
        final long[] next = new long[1];
        next[0] = start + minPeriodOfCycles;
        forEachCycle(() -> {
            long n = nar.time();
            if (n >= next[0]) {
                action.run();
            }
        });
        return this;
    }

    public NARStream resetIf(Predicate<NAR> resetCondition) {
        forEachCycle(() -> {
           if (resetCondition.test(nar)) reset();
        });
        return this;
    }

    public NARStream stopIf(BooleanSupplier stopCondition) {
        forEachCycle(() -> {
            if (stopCondition.getAsBoolean()) stop();
        });
        return this;
    }

    public NARStream stopAt(LongPredicate timeStopCondition) {
        at(timeStopCondition, this::stop);
        return this;
    }

    public NARStream at(LongPredicate timeCondition, Runnable action) {
        forEachCycle(() -> {
            if (timeCondition.test(nar.time())) {
                action.run();
            }
        });
        return this;
    }

    protected void stop() {
        nar.stop();
        delete();
    }

    /**
     * called when finished, removes event handlers if NAR is still running
     */
    protected void delete() {
        //TODO
        nar.delete();
    }

    public NARStream forEachCycle(Runnable receiver) {
        regs.add(nar.memory.eventCycleEnd.on( m -> {
            receiver.run();
        }));
        return this;
    }

    public NARStream forEachFrame(Runnable receiver) {
        return on(Events.FrameEnd.class, receiver);
    }

    public NARStream forEachNthFrame(Runnable receiver, int frames) {
        return forEachFrame(() -> {
            if (nar.time() % frames == 0)
                receiver.run();
        });
    }

    public NARStream forEachDerived(Consumer<Task> receiver) {
        NARReaction r = new ConsumedStreamNARReaction(receiver, Events.OUT.class);
        return this;
    }

    public <X> NARStream on(Class signal, Consumer<X> receiver) {
        NARReaction r = new ConsumedStreamNARReaction(receiver, signal);
        return this;
    }

    public NARStream on(Runnable receiver, Class... signal) {
        NARReaction r = new RunnableStreamNARReaction(receiver, signal);
        return this;
    }

    public NARStream on(Class signal, Runnable receiver) {
        NARReaction r = new RunnableStreamNARReaction(receiver, signal);
        return this;
    }

    public NARStream stdout() {
        try {
            forEachEvent(System.out, Output.DefaultOutputEvents);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
    public NARStream stdoutTrace() {
        try {
            forEachEvent(System.out, MemoryReaction.memoryEvents);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public NARStream forEachEvent(Appendable o, Class... signal) throws Exception {
        NARReaction r = new StreamNARReaction(signal) {
            @Override public void event(Class event, Object... args) {
                try {
                    TextOutput.append(o, event, args, "\n", true, true, 0, nar);

                    if (o instanceof OutputStream)
                        ((OutputStream)o).flush();

                } catch (IOException e) {
                    nar.emit(e);
                }
            }
        };
        return this;
    }

    public NARStream output(ObjectOutputStream o, Class... signal) throws Exception {

        NARReaction r = new StreamNARReaction(signal) {

            @Override
            public void event(Class event, Object... args) {
                if (args instanceof Serializable) {
                    //..
                }
            }
        };

        return this;
    }

    public NARStream spawnThread(long periodMS, Consumer<Thread> t) {
        ensureNotRunning();

        t.accept( new Thread(() -> {
            loop(periodMS);
        }) );

        return this;
    }




    abstract private class StreamNARReaction extends NARReaction {

        public StreamNARReaction(Class... signal) {
            super(nar, signal);
        }

        @Override public void setActive(boolean b) {
            super.setActive(b);
            manage(this, b);
        }
    }

    protected void manage(NARReaction r, boolean b) {
        if (!b) {
            reactions.remove(Sets.newHashSet(r.getEvents()), r);
        } else {
            reactions.put(Sets.newHashSet(r.getEvents()), r);
        }
    }

    private class ConsumedStreamNARReaction<X> extends StreamNARReaction {

        private final Consumer<X> receiver;

        public ConsumedStreamNARReaction(Consumer<X> receiver, Class... signal) {
            super(signal);
            this.receiver = receiver;
        }

        @Override
        public void event(Class event, Object... args) {
            receiver.accept((X) args);
        }

    }

    /** ignores any event arguments and just invokes a Runnable when something
     * is received (ex: cycle)
     * @param <X>
     */
    private class RunnableStreamNARReaction<X> extends StreamNARReaction {

        private final Runnable invoked;

        public RunnableStreamNARReaction(Runnable invoked, Class... signal) {
            super(signal);
            this.invoked = invoked;
        }

        @Override
        public void event(Class event, Object... args) {
            invoked.run();
        }

    }
}
