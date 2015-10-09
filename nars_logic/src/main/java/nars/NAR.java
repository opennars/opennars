package nars;

import nars.bag.impl.CacheBag;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.concept.Concept;
import nars.event.AnswerReaction;
import nars.event.NARReaction;
import nars.io.in.FileInput;
import nars.io.in.Input;
import nars.io.in.TextInput;
import nars.nal.nal7.AbstractInterval;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.nal.nal8.OperatorReaction;
import nars.nal.nal8.operator.TermFunction;
import nars.narsese.InvalidInputException;
import nars.narsese.NarseseParser;
import nars.process.Level;
import nars.process.TaskProcess;
import nars.task.DefaultTask;
import nars.task.Task;
import nars.task.stamp.Stamp;
import nars.term.*;
import nars.truth.DefaultTruth;
import nars.truth.Truth;
import nars.util.NARLoop;
import nars.util.data.Util;
import nars.util.event.EventEmitter;
import nars.util.event.Reaction;
import nars.util.event.Topic;
import org.infinispan.commons.marshall.jboss.GenericJBossMarshaller;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongPredicate;
import java.util.stream.Stream;


/**
 * Non-Axiomatic Reasoner
 * <p/>
 * Instances of this represent a reasoner connected to a Memory, and set of Input and Output channels.
 * <p/>
 * All state is contained within Memory.  A NAR is responsible for managing I/O channels and executing
 * memory operations.  It executesa series sof cycles in two possible modes:
 * * step mode - controlled by an outside system, such as during debugging or testing
 * * thread mode - runs in a pausable closed-loop at a specific maximum framerate.
 */
abstract public class NAR implements Serializable, Level {


    /**
     * The information about the version and date of the project.
     */
    public static final String VERSION = "Open-NARS v1.7.0";
    /**
     * The project web sites.
     */
    public static final String WEBSITE =
            " Open-NARS website:  http://code.google.com/p/open-nars/ \n" +
                    "      NARS website:  http://sites.google.com/site/narswang/ \n" +
                    "    Github website:  http://github.com/opennars/ \n" +
                    "               IRC:  http://webchat.freenode.net/?channels=nars \n";

    float defaultJudgmentConfidence = Global.DEFAULT_JUDGMENT_CONFIDENCE;
    protected float defaultJudgmentPriority = Global.DEFAULT_JUDGMENT_PRIORITY;
    protected float defaultJudgmentDurability = Global.DEFAULT_JUDGMENT_DURABILITY;
    float defaultGoalPriority = Global.DEFAULT_GOAL_PRIORITY;
    float defaultGoalDurability = Global.DEFAULT_GOAL_DURABILITY;
    float defaultQuestionPriority = Global.DEFAULT_QUESTION_PRIORITY;
    float defaultQuestionDurability = Global.DEFAULT_QUESTION_DURABILITY;

    final static Consumer<Serializable> onError = e -> {
        if (e instanceof Throwable) {
            Throwable ex = (Throwable) e;

            //TODO move this to a specific impl of error reaction:
            ex.printStackTrace();

            if (Global.DEBUG && Global.EXIT_ON_EXCEPTION) {
                //throw the exception to the next lower stack catcher, or cause program exit if none exists
                throw new RuntimeException(ex);
            }
        } else {
            System.err.println(e);
        }
    };


    /**
     * The memory of the reasoner
     * TODO dont expose as public
     */
    public Memory memory;
    /**
     * The name of the reasoner
     */
    public String name;
    /**
     * Flag for running continuously
     */
    public AtomicBoolean running = new AtomicBoolean();



    //TODO use this to store all handler registrations, and decide if transient or not
    public final transient List<Object> regs = new ArrayList();


    transient private final Deque<Runnable> nextTasks = new ConcurrentLinkedDeque();

    static final ThreadPoolExecutor asyncs =
            (ThreadPoolExecutor) Executors.newCachedThreadPool();
                        //Executors.newFixedThreadPool(1);

    private int concurrency = 1;


    public NAR(final Memory m) {
        super();

        setMemory(m);


        /** register some components in the dependency context, Container (which Memory subclasses from) */
        //m.the("memory", m);
        m.the("clock", m.clock);

    }

    public final Memory memory() {
        return memory;
    }


    /**
     * Reset the system with an empty memory and reset clock.  Event handlers
     * will remain attached but enabled plugins will have been deactivated and
     * reactivated, a signal for them to empty their state (if necessary).
     */
    public synchronized NAR reset() {
        runNextTasks();

        nextTasks.clear();

        asyncs.shutdown();

        memory.clear();

        return this;
    }

    /**
     * Resets and deletes the entire system
     */
    public void delete() {

        memory.delete();

    }

    public Input input(final File input) throws IOException {
        return input(new FileInput(this, input));
    }

    /**
     * inputs a task, only if the parsed text is valid; returns null if invalid
     */
    public Task inputTask(final String taskText) {
        try {
            Task t = task(taskText);
            t.setCreationTime(time());
            input(t);
            return t;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * parses and forms a Task from a string but doesnt input it
     */
    public Task task(String taskText) {
        return NarseseParser.the().task(taskText, memory);
    }


    public List<Task> tasks(final String parse) {
        List<Task> result = Global.newArrayList(1);
        NarseseParser.the().tasks(parse, result, memory);
        return result;
    }

    public List<Task> inputs(final String parse) {
        return input(tasks(parse));
    }

    public TextInput input(final String text) {
        final TextInput i = new TextInput(this, text);
        input(i);
        return i;
    }

    public <S extends Term, T extends S> T term(final String t) throws InvalidInputException {
        return NarseseParser.the().term(t);
    }

    public Concept concept(final Term term) {
        return memory.concept(term);
    }

    /**
     * gets a concept if it exists, or returns null if it does not
     */
    public Concept concept(final String conceptTerm) throws InvalidInputException {
        return concept((Term) NarseseParser.the().term(conceptTerm));
    }


    public Task ask(String termString) throws InvalidInputException {
        //TODO remove '?' if it is attached at end
        return ask((Compound) NarseseParser.the().compound(termString));
    }

    public Task ask(Compound c) throws InvalidInputException {
        //TODO remove '?' if it is attached at end
        return ask(c, Symbols.QUESTION);
    }

    public Task quest(String questString) throws InvalidInputException {
        return ask(NarseseParser.the().term(questString), Symbols.QUEST);
    }


    public Task goal(Compound goalTerm, Tense tense, float freq, float conf) throws InvalidInputException {
        return goal(defaultGoalPriority, defaultGoalDurability, goalTerm, time(tense), freq, conf);
    }



//    public Task believe(float priority, String termString, long when, float freq, float conf) throws InvalidInputException {
//        return believe(priority, termString, when, freq, conf);
//    }

    public Task believe(float priority, Compound term, long when, float freq, float conf) throws InvalidInputException {
        return believe(priority, defaultJudgmentDurability, term, when, freq, conf);
    }


    public NAR believe(Compound term, float freq, float conf) throws InvalidInputException {
        return believe(term, Tense.Eternal, freq, conf);
    }

    public NAR believe(Compound term, Tense tense, float freq, float conf) throws InvalidInputException {
        believe(defaultJudgmentPriority, term, time(tense), freq, conf);
        return this;
    }

    final long time(Tense tense) {
        return Stamp.getOccurrenceTime(tense, memory);
    }

    public NAR believe(String termString, float freq, float conf) throws InvalidInputException {
        return believe((Compound) term(termString), freq, conf);
    }


    public NAR believe(String termString) throws InvalidInputException {
        return believe((Compound) term(termString));
    }

    public NAR believe(Compound term) throws InvalidInputException {
        return believe(term, 1.0f, defaultJudgmentConfidence);
    }

//    public NAR believe(Compound beliefTerm, Tense tense, float freq, float conf) throws InvalidInputException {
//        believe(NAR.defaultJudgmentPriority, NAR.defaultJudgmentDurability, beliefTerm, Stamp.getOccurrenceTime(time(), tense, memory.duration()), freq, conf);
//        return this;
//    }


//    public static class InputBuffer {
//
//        private final Seq<Task> stream;
//
//        public InputBuffer(Memory m) {
//            this.stream = m.eventInput.stream();
//
//            stream.forEach(t -> {
//            });
//        }
//    }

//
//        if (process(t)) {
//
//
//            emit(t.isInput() ? Events.IN.class : Events.OUT.class, t);
//
//
//            //NOTE: if duplicate outputs happen, the budget wil have changed
//            //but they wont be displayed.  to display them,
//            //we need to buffer unique TaskAdd ("OUT") tasks until the end
//            //of the cycle
//
//
//            m.logic.TASK_ADD_NEW.hit();
//            return true;
//        }
//        else {
//            m.removed(t, "Ignored");
//        }
//
//        return false;
//    }

//    public Task believe(float pri, String beliefTerm, long occurrenceTime, float freq, float conf) throws InvalidInputException {
//        return believe(pri, NAR.defaultJudgmentDurability, (Compound) term(beliefTerm), occurrenceTime, freq, conf);
//    }

    public <C extends Compound> Task<C> believe(float pri, float dur, C term, long occurrenceTime, float freq, float conf) throws InvalidInputException {
        return input(pri, dur, term, Symbols.JUDGMENT, occurrenceTime, freq, conf);
    }

    /**
     * TODO add parameter for Tense control. until then, default is Now
     */
    public <T extends Compound> Task<T> goal(float pri, float dur, T goal, long occurrence, float freq, float conf) throws InvalidInputException {
        return input(pri, dur, goal, Symbols.GOAL, occurrence, freq, conf);
    }

    final public <C extends Compound> Task<C> input(float pri, float dur, C belief, char punc, long occurrenceTime, float freq, float conf) throws InvalidInputException {

        final Truth tv;

        Task<C> t = new DefaultTask<>(belief,
                punc,
                tv = new DefaultTruth(freq, conf),
                pri, dur, BudgetFunctions.truthToQuality(tv));
        t.setCreationTime(time());
        t.setOccurrenceTime(occurrenceTime);

        input(t);

        return t;
    }

    public Task ask(Compound term, char questionOrQuest) throws InvalidInputException {


        //TODO use input method like believe uses which avoids creation of redundant Budget instance

        final Task<?> t = new DefaultTask<>(
                term,
                questionOrQuest,
                null,
                defaultQuestionPriority,
                defaultQuestionDurability,
                1);
        t.setCreationTime(time());
        input(t);

        return t;

        //ex: return new Answered(this, t);

    }

    protected boolean process(Task t) {
        return true;
    }

    /**
     * exposes the memory to an input, derived, or immediate task.
     * the memory then delegates it to its controller
     * <p/>
     * return true if the task was processed
     * if the task was a command, it will return false even if executed
     */
    public final boolean input(final Task t) {

        final Memory m = memory();

        if (t == null || !t.init(m)) {
            m.remove(t, "Garbage");
            return false;
        }

        if (t.isCommand()) {
            int n = m.execute(t);
            if (n == 0) {
                m.remove(t, "Unknown Command");
            }
            return false;
        }

        if (!t.getTerm().levelValid(nal())) {
            m.remove(t, "Insufficient NAL level");
            return false;
        }

        m.eventInput.emit(t);

        return true;
    }


    /** register a singleton */
    final public <X> X the(Object key, X value) {
        return memory.the(key, value);
    }

//    /** input a task via direct TaskProcessing
//     * @return the TaskProcess, after it has executed (synchronously) */
//    public Premise inputDirect(final Task t) {
//        return TaskProcess.queue(this, t);
//    }

    /**
     * returns the global concept index
     */
    public final CacheBag<Term, Concept> concepts() {
        return memory().getConcepts();
    }

    public List<Task> input(final List<Task> t) {
        t.forEach(x -> input(x));
        return t;
    }

    public Task[] input(final Task[] t) {
        for (Task x : t)
            input(x);
        return t;
    }

    /**
     * attach event handler to one or more event (classes)
     */
    public EventEmitter.Registrations on(Reaction<Class, Object[]> o, Class... c) {
        return memory.event.on(o, c);
    }



    //TODO use specific names for these types of functons in this class
    public void onExec(String operator, Function<Task<Operation>,List<Task>> f)  {
        on(new OperatorReaction(operator) {
            @Override public List<Task> apply(Task<Operation> t) {
                return f.apply(t);
            }
        });
    }

    public void on(Class<? extends Reaction<Class, Object[]>>... x) {

        for (Class<? extends Reaction<Class, Object[]>> c : x) {
            Reaction<Class, Object[]> v = memory().the(c);
            memory().the(c, v); //register singleton
            on(v);
        }
    }

    public final TermFunction on(String operator, Function<Term[], Object> func) {
        return on(Atom.the(operator), func);
    }

    public final TermFunction on(String operator, Consumer<Term[]> func) {
        return on(Atom.the(operator), func);
    }

    public final TermFunction on(Term operator, Consumer<Term[]> func) {
        //wrap the procedure in a function, suboptimal but ok
        return on(operator, (tt) -> {
            func.accept(tt);
            return null;
        });
    }

    /** creates a TermFunction operator from a supplied function, which can be a lambda */
    public TermFunction on(Term operator, Function<Term[], Object> func) {
        TermFunction f = new TermFunction(operator) {

            @Override
            public final Object function(Operation x) {
                return func.apply(x.args());
            }
        };
        on(f);
        return f;
    }


    public void on(Class<? extends OperatorReaction> c) {
        //for (Class<? extends OperatorReaction> c : x) {
        OperatorReaction v = memory().the(c);
        on(v);
        //}
    }

    public final EventEmitter.Registrations on(Reaction<Term, Task<Operation>> o, Term... c) {
        return memory.exe.on(o, c);
    }

//    /** Explicitly removes an input channel and notifies it, via Input.finished(true) that is has been removed */
//    public Input removeInput(Input channel) {
//        inputChannels.remove(channel);
//        channel.finished(true);
//        return channel;
//    }

//
//    /** add and enable a plugin or operate */
//    public OperatorRegistration on(IOperator p) {
//        if (p instanceof Operator) {
//            memory.operatorAdd((Operator) p);
//        }
//        OperatorRegistration ps = new OperatorRegistration(p);
//        plugins.add(ps);
//        return ps;
//    }
//
//    /** disable and remove a plugin or operate; use the PluginState instance returned by on(plugin) to .off() it */
//    protected void off(OperatorRegistration ps) {
//        if (plugins.remove(ps)) {
//            IOperator p = ps.IOperator;
//            if (p instanceof Operator) {
//                memory.operatorRemove((Operator) p);
//            }
//            ps.setEnabled(false);
//        }
//    }
//
//    public List<OperatorRegistration> getPlugins() {
//        return Collections.unmodifiableList(plugins);
//    }

    public EventEmitter.Registrations on(OperatorReaction o) {
        EventEmitter.Registrations reg = on(o, o.getOperatorTerm());
        o.setEnabled(this, true);
        return reg;
    }


    public final int getCyclesPerFrame() {
        return memory.cyclesPerFrame.intValue();
    }


    public final void setCyclesPerFrame(int cyclesPerFrame) {
        memory.cyclesPerFrame.set(cyclesPerFrame);
    }

    /**
     * Adds an input channel for input from an external sense / sensor.
     * Will remain added until it closes or it is explicitly removed.
     */
    public Input input(final Input i) {
        Task t;

        int i1 = 0;
        while ((t = i.get()) != null) {
            i1 += input(t) ? 1 : 0;
        }
        return i;
    }

//    @Deprecated
//    public void start(final long minCyclePeriodMS, int cyclesPerFrame) {
//        throw new RuntimeException("WARNING: this threading model is not safe and deprecated");
//
////        if (isRunning()) stop();
////        this.minFramePeriodMS = minCyclePeriodMS;
////        this.cyclesPerFrame = cyclesPerFrame;
////        running = true;
////        if (thread == null) {
////            thread = new Thread(this, this.toString() + "_reasoner");
////            thread.start();
////        }
//    }
//
//    /**
//     * Repeatedly execute NARS working cycle in a new thread with Iterative timing.
//     *
//     * @param minCyclePeriodMS minimum cycle period (milliseconds).
//     */
//    @Deprecated
//    public void start(final long minCyclePeriodMS) {
//        start(minCyclePeriodMS, getCyclesPerFrame());
//    }

//    /**
//     * Execute a minimum number of cycles, allowing additional cycles (less than maxCycles) for finishing any pending inputs
//     *
//     * @param maxCycles max cycles, or -1 to allow any number of additional cycles until input finishes
//     */
//    public NAR runWhileNewInput(long minCycles, long maxCycles) {
//
//
//        if (maxCycles <= 0) return this;
//        if (minCycles > maxCycles)
//            throw new RuntimeException("minCycles " + minCycles + " required <= maxCycles " + maxCycles);
//
//        running = true;
//
//        long cycleStart = time();
//        do {
//            frame(1);
//
//            long now = time();
//
//            long elapsed = now - cycleStart;
//
//            if (elapsed >= minCycles)
//                running = (!memory.perception.isEmpty()) &&
//                        (elapsed < maxCycles);
//        }
//        while (running);
//
//        return this;
//    }

    public EventEmitter event() {
        return memory.event;
    }

    /**
     * Exits an iteration loop if running
     */
    public void stop() {
        running.set( false );
    }

//    /**
//     * Execute a fixed number of cycles, then finish any remaining walking steps.
//     */
//    @Deprecated public NAR runWhileNewInputOLD(long extraCycles) {
//        //TODO see if this entire method can be implemented as run(0, cycles);
//
//        if (extraCycles <= 0) return this;
//
//        running = true;
//        enabled = true;
//
//        //clear existing input
//
//        long cycleStart = time();
//
//        do {
//            frame(1);
//        }
//        while (/*(!memory.perception.isEmpty()) && */ running && enabled);
//
//        long cyclesCompleted = time() - cycleStart;
//
//        //queue additional cycles,
//        extraCycles -= cyclesCompleted;
//        if (extraCycles > 0)
//            memory.think(extraCycles);
//
//        //finish all remaining cycles
//        while (!memory.isInputting() && running && enabled) {
//            frame(1);
//        }
//
//        running = false;
//
//        return this;
//    }

    /**
     * steps 1 frame forward. cyclesPerFrame determines how many cycles this frame consists of
     */
    public void frame() {
        frame(1);
    }

    /**
     * Runs multiple frames, unless already running (then it return -1).
     *
     * @return total time in seconds elapsed in realtime
     */
    public void frame(final int frames) {

        if (!running.compareAndSet(false, true)) {
            throw new RuntimeException("already running");
        }

        final Memory memory = this.memory;

        Topic<NAR> frameStart = memory.eventFrameStart;


        final int cpf = getCyclesPerFrame();
        for (int f = 0; f < frames; f++) {

            frameStart.emit(this);
            memory.clock.preFrame(memory);

            memory.cycle(cpf);

            runNextTasks();
        }

        running.compareAndSet(true, false);


        //TODO rewrite ResourceMeter to use event handler
        /*
        final ResourceMeter resourceMeter = memory.resource;
        if (resourceMeter != null)
            resourceMeter.FRAME_DURATION.start();
        */
    }

    /* Print all statically known events (discovered via reflection)
    *  for this reasoner to a stream
    * */
    public NAR trace(Appendable out) {


        final String[] previous = {null};

        Topic.all(memory(), (k, v) -> {
            try {

                //indent each cycle
                if (!k.equals("eventCycleStart")) {
                    out.append("  ");
                }

                String chan = k.toString();
                if (!chan.equals(previous[0])) {
                    out
                            //.append(ANSI.COLOR_CONFIG)
                            .append(chan)
                            //.append(ANSI.COLOR_RESET )
                            .append(": ");
                    previous[0] = chan;
                } else {
                    //indent
                    for (int i = 0; i < chan.length() + 2; i++)
                        out.append(' ');
                }

                out
                        .append(v.toString());

                if (v instanceof Concept) {
                    Concept c = (Concept) v;
                    out.append(' ').append(c.getBudget().toBudgetString());
                }

                out.append('\n');

            } catch (IOException e) {
                error(e);
            }

        });

        return this;
    }


//    /** creates a new loop which begins paused */
    final public NARLoop loop() {
        return loop(-1);
    }

    final public NARLoop loop(final float initialFPS) {
        final float millisecPerFrame = 1000f / initialFPS;
        return loop((long)millisecPerFrame);
    }

    /**
     * Runs until stopped, at a given delay period between frames (0= no delay). Main loop
     * @param initialFramePeriodMS in milliseconds
     */
    final public NARLoop loop(int initialFramePeriodMS) {
//        //TODO use DescriptiveStatistics to track history of frametimes to slow down (to decrease speed rate away from desired) or speed up (to reach desired framerate).  current method is too nervous, it should use a rolling average

        return new NARLoop(this, initialFramePeriodMS);
    }


    /** sets current maximum allowed NAL level (1..8) */
    public NAR nal(int level) {
        memory.nal(level);
        return this;
    }

    /**
     * returns the current level
     */
    public int nal() {
        return memory.nal();
    }



    /**
     * adds a task to the queue of task which will be executed in batch
     * after the end of the current frame before the next frame.
     */
    final public void beforeNextFrame(final Runnable t) {
        nextTasks.addLast(t);
    }

//    /*
//     * checks if the queue already contains the pending
//     * Runnable instance to ensure duplicates don't
//     * don't accumulate
//     */
//    final public void beforeNextFrameUnlessDuplicate(final Runnable t) {
//        if (!nextTasks.contains(t))
//            nextTasks.addLast(t);
//    }


    /**
     * runs all the tasks in the 'Next' queue
     */
    protected final void runNextTasks() {
        final int originalSize = nextTasks.size();
        if (originalSize == 0) return;

        Util.run(nextTasks, originalSize, concurrency);
    }

    /**
     * signals an error through one or more event notification systems
     */
    protected void error(Throwable ex) {
        memory.eventError.emit(ex);

    }

    /**
     * queues a task to (hopefully) be executed at an unknown time in the future,
     * in its own thread in a thread pool
     */
    public final boolean execAsync(Runnable t) {
        return execAsync(t, null);
    }
    public boolean execAsync(Runnable t, Consumer<RejectedExecutionException> onError) {
        try {
            memory.eventSpeak.emit("execAsync " + t.toString());
            memory.eventSpeak.emit("pool: " + asyncs.getActiveCount() + " running, " + asyncs.getTaskCount() + " pending");

            asyncs.execute(t);

            return true;
        }
        catch (RejectedExecutionException e) {
            if (onError!=null)
                onError.accept(e);
            return false;
        }
    }
    public boolean execAsync(Consumer<NAR> t) {
        return execAsync( /* Runnable */ () -> { t.accept(NAR.this); } );
    }

    public <X> Future<X> execAsync(Function<NAR,X> t) {
        return asyncs.submit( /* (Callable) */() -> {
            return t.apply(NAR.this);
        });
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + memory.toTypeString() + "]";
    }

    /**
     * Get the current time from the clock
     *
     * @return The current time
     */
    public long time() {
        return memory.time();
    }

    public boolean running() {
        return running.get();
    }

    /**
     * returns the Atom for the given string. since the atom is unique to itself it can be considered 'the' the
     */
    public Atom atom(final String s) {
        return Atom.the(s);
    }

    public Iterator<Concept> iterator() {
        return memory().getConcepts().iterator();
    }


    public void conceptPriorityHistogram(double[] bins) {
        if (bins != null)
            memory().getConcepts().getPriorityHistogram(bins);
    }


    public Concept put(Concept concept) {
        memory().put(concept);
        return concept;
    }


    public Concept remove(Term key) {
        return remove(memory().get(key));
    }


    public Concept get(final Term key) {
        return concepts().get(key);
    }


    public final void setMemory(Memory m) {

        m.the(NAR.class, this);

        if (running())
            throw new RuntimeException("NAR must be stopped to change memory");

        memory = m;

        m.eventError.on(onError);

    }


    protected boolean active(Term t) {
        return memory().get(t) != null;
    }





    public NAR answer(String question, Consumer<Task> recvSolution) {
        //question punctuation optional
        if (!question.endsWith("?")) question = question + "?";
        Task qt = this.task(question);
        return answer(qt, recvSolution);
    }

    /** inputs the question and observes answer events for a solution */
    public NAR answer(Task question, Consumer<Task> c) {
        new AnswerReaction(this, question) {

            @Override
            public void onSolution(Task belief) {
                c.accept(belief);
            }

        };
        return this;
    }


    //    public NAR fork(Consumer<NAR> clone) {
//        ensureNotRunning();
//        return this; //TODO
//    }


    public NAR input(String... ss) {
        for (String s : ss) this.input(s);
        return this;
    }

//    public NAR input(Task... tt) {
//        for (Task t : tt) nar.input(t);
//        return this;
//    }

    public NAR inputAt(long time, String... tt) {
        return at(t -> t == time, () -> input(tt));
    }

    public NAR inputAt(LongPredicate timeCondition, Task... tt) {
        return at(timeCondition, () -> input(tt));
    }

    public NAR inputAt(long time, Task... tt) {
        return at(t -> t == time, () -> input(tt));
    }

    public NAR forEachConceptTask(boolean includeConceptBeliefs, boolean includeConceptQuestions, boolean includeConceptGoals, boolean includeConceptQuests,
                                  boolean includeTaskLinks, int maxPerConcept,
                                  Consumer<Task> recip) {
        forEachConcept(c -> {
            if (includeConceptBeliefs && c.hasBeliefs()) c.getBeliefs().top(maxPerConcept, recip);
            if (includeConceptQuestions && c.hasQuestions()) c.getQuestions().top(maxPerConcept, recip);
            if (includeConceptGoals && c.hasBeliefs()) c.getGoals().top(maxPerConcept, recip);
            if (includeConceptQuests && c.hasQuests()) c.getQuests().top(maxPerConcept, recip);

            if (includeTaskLinks && c.getTaskLinks() != null)
                c.getTaskLinks().forEach(maxPerConcept, tl -> recip.accept(tl.getTask()));
        });

        return this;
    }

    public NAR forEachConcept(Consumer<Concept> recip) {
        this.memory.concepts.forEach(recip);
        return this;
    }

//    public NAR forEachConceptActive(Consumer<Concept> recip) {
//        nar.memory.getCycleProcess().forEachConcept(recip);
//        return this;
//    }

//    public NAR conceptIterator(Consumer<Iterator<Concept>> recip) {
//        recip.accept( nar.memory.concepts.iterator() );
//        return this;
//    }

//    public NAR conceptActiveIterator(Consumer<Iterator<Concept>> recip) {
//        recip.accept( nar.memory.getCycleProcess().iterator() );
//        return this;
//    }

    //TODO iterate/query beliefs, etc
//
//    public NAR meterLogic(Consumer<LogicMeter> recip) {
//        recip.accept(this.memory.logic);
//        return this;
//    }
//
//    public NAR meterEmotion(Consumer<EmotionMeter> recip) {
//        recip.accept(this.memory.emotion);
//        return this;
//    }
//
//
//    public NAR resetEvery(long minPeriodOfCycles) {
//        onEachPeriod(minPeriodOfCycles, this::reset);
//        return this;
//    }

    public NAR onEachPeriod(long minPeriodOfCycles, Runnable action) {
        final long start = this.time();
        final long[] next = new long[1];
        next[0] = start + minPeriodOfCycles;
        onEachCycle((m) -> {
            long n = m.time();
            if (n >= next[0]) {
                action.run();
            }
        });
        return this;
    }

    /**
     * Get the Concept associated to a Term, or create it.
     * <p/>
     * Existing concept: apply tasklink activation (remove from bag, adjust
     * budget, reinsert) New concept: set initial activation, insert Subconcept:
     * extract from cache, apply activation, insert
     * <p/>
     * If failed to insert as a result of null bag, returns null
     * <p/>
     * A displaced Concept resulting from insert is forgotten (but may be stored
     * in optional subconcept memory
     *
     * @return an existing Concept, or a new one, or null
     */
    public Concept conceptualize(Termed termed, final Budget budget) {

        if (termed == null)
            return null;

        //validation here is to avoid checking a term if we know it is already normalized
        final boolean needsValidation;

        needsValidation = true;
        //needsValidation = (termed instanceof Term);


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
            if (!validConceptTerm(term)) {
                throw new RuntimeException("invalid term attempts to conceptualize: " + term);
                //return null;
            }
        }

        if ((term = term.normalized()) == null) {
            //throw new RuntimeException("unnormalized term attempts to conceptualize: " + term);
            return null;
        }

        final Concept c = doConceptualize(term, budget);
        if (c == null) {
            throw new RuntimeException("unconceptualizable: " + termed + " , " + budget);
        }
        memory.eventConceptActivated.emit(c);
        return c;
    }

    abstract protected Concept doConceptualize(Term term, Budget budget);

    private boolean validConceptTerm(Term term) {
        return !((term instanceof Variable) || (term instanceof AbstractInterval));
    }


//    public NAR resetIf(Predicate<NAR> resetCondition) {
//        onEachFrame((n) -> {
//            if (resetCondition.test(n)) reset();
//        });
//        return this;
//    }

    public NAR stopIf(BooleanSupplier stopCondition) {
        onEachFrame((n) -> {
            if (stopCondition.getAsBoolean()) stop();
        });
        return this;
    }


    public NAR at(LongPredicate timeCondition, Runnable action) {
        onEachCycle((m) -> {
            if (timeCondition.test(m.time())) {
                action.run();
            }
        });
        return this;
    }


    public final NAR onEachCycle(Consumer<Memory> receiver) {
        regs.add(this.memory.eventCycleEnd.on(receiver));
        return this;
    }

    public final NAR onEachFrame(Consumer<NAR> receiver) {
        regs.add(this.memory.eventFrameStart.on(receiver));
        return this;
    }

//    public NAR onEachNthFrame(Runnable receiver, int frames) {
//        return onEachFrame((n) -> {
//            if (n.time() % frames == 0)
//                receiver.run();
//        });
//    }

//    public NAR onEachDerived(Consumer<Object[] /* TODO: Task*/> receiver) {
//        NARReaction r = new ConsumedStreamNARReaction(receiver, Events.OUT.class);
//        return this;
//    }

    public <X> NAR on(Class signal, Consumer<X> receiver) {
        NARReaction r = new ConsumedStreamNARReaction(receiver, signal);
        return this;
    }

    public NAR on(Runnable receiver, Class... signal) {
        NARReaction r = new RunnableStreamNARReaction(receiver, signal);
        return this;
    }

    public NAR on(Class signal, Runnable receiver) {
        NARReaction r = new RunnableStreamNARReaction(receiver, signal);
        return this;
    }

    public NAR stdout() {

        trace(System.out);

        /*
        try {
            forEachEvent(System.out, Output.DefaultOutputEvents);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return this;
    }


//    public NAR forEachEvent(Appendable o, Class... signal) throws Exception {
//        NARReaction r = new StreamNARReaction(signal) {
//            @Override
//            public void event(Class event, Object... args) {
//                try {
//                    TextOutput.append(o, event, args, "\n", true, true, 0, (NAR) NAR.this);
//
//                    if (o instanceof OutputStream)
//                        ((OutputStream) o).flush();
//
//                } catch (IOException e) {
//                    error(e);
//                }
//            }
//        };
//        return this;
//    }
//
//    public NAR output(ObjectOutputStream o, Class... signal) {
//
//        NARReaction r = new StreamNARReaction(signal) {
//
//            @Override
//            public void event(Class event, Object... args) {
//                if (args instanceof Serializable) {
//                    //..
//                }
//            }
//        };
//
//        return this;
//    }


    public NAR onConceptActive(final Consumer<Concept> c) {
        regs.add(this.memory.eventConceptActivated.on(c));
        return this;
    }

    public NAR onConceptForget(final Consumer<Concept> c) {
        regs.add(this.memory.eventConceptForget.on(c));
        return this;
    }

    public final void input(final Stream<Task> taskStream) {
        taskStream.forEach(this::input);
    }

    /** execute a Task as a TaskProcess (synchronous) */
    public TaskProcess exec(Task task) {

        final Budget taskBudget = task.getBudget();

//        if (inputPriorityFactor != 1f) {
//            taskBudget.mulPriority(inputPriorityFactor);
//        }

        if (!taskBudget.summaryGreaterOrEqual(memory().taskProcessThreshold)) {
            memory().remove(task, "Insufficient Budget to TaskProcess");
            return null;
        }

        TaskProcess d = new TaskProcess(this, task);

        d.process(null);

        return d;

    }

    /** convenience method shortcut for concept(t.getTerm()) */
    public final Concept concept(Termed termed) {
        return concept(termed.getTerm());
    }

    public String getName() {
        return WEBSITE;
    }

//    public NAR onAfterFrame(final Runnable r) {
//        return onEachFrame(() -> {
//           taskNext(r);
//        });
//    }

//    protected void manage(NARReaction r, boolean b) {
////        if (!b) {
////            reactions.remove(Sets.newHashSet(r.getEvents()), r);
////        } else {
////            reactions.put(Sets.newHashSet(r.getEvents()), r);
////        }
//    }
//
//    /** starts the new running thread immediately */
//    public void spawnThread(int msDelay) {
//        spawnThread(msDelay, Thread::start);
//    }

    abstract private class StreamNARReaction extends NARReaction {

        public StreamNARReaction(Class... signal) {
            super((NAR) NAR.this, signal);
        }

//        @Override
//        public void setActive(boolean b) {
//            super.setActive(b);
//            //manage(this, b);
//        }
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

    /**
     * ignores any event arguments and just invokes a Runnable when something
     * is received (ex: cycle)
     *
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


//    private void debugTime() {
//        //if (running || stepsQueued > 0 || !finishedInputs) {
//            System.out.println("// doTick: "
//                    //+ "walkingSteps " + stepsQueued
//                    + ", clock " + time());
//
//            System.out.flush();
//        //}
//    }

    public byte[] toBytes() throws IOException, InterruptedException {
        return new GenericJBossMarshaller().objectToByteBuffer(this);
    }

    public static <N extends NAR> N fromBytes(byte[] b) throws IOException, ClassNotFoundException {
        return (N) new GenericJBossMarshaller().objectFromByteBuffer(b);
    }

    public NAR fork() throws Exception {
        //TODO find more efficient way than this brute serialization/deserialization
        final byte[] bb = toBytes();
        NAR x = fromBytes(bb);
        return x;

    }

    @Override
    public boolean equals(Object obj) {
        //TODO compare any other stateful values from NAR class in addition to Memory
        return this==obj;
    }


}