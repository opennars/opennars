package nars;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import nars.bag.Bag;
import nars.bag.impl.CacheBag;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.concept.Concept;
import nars.event.NARReaction;
import nars.io.ANSI;
import nars.io.in.FileInput;
import nars.io.in.Input;
import nars.io.in.TextInput;
import nars.io.qa.AnswerReaction;
import nars.link.TaskLink;
import nars.meter.EmotionMeter;
import nars.meter.LogicMeter;
import nars.nal.nal7.AbstractInterval;
import nars.nal.nal7.Tense;
import nars.nal.nal8.ImmediateOperator;
import nars.nal.nal8.OpReaction;
import nars.nal.nal8.Operation;
import nars.narsese.InvalidInputException;
import nars.narsese.NarseseParser;
import nars.process.CycleProcess;
import nars.task.DefaultTask;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.task.stamp.Stamp;
import nars.term.*;
import nars.truth.DefaultTruth;
import nars.truth.Truth;
import nars.util.event.EventEmitter;
import nars.util.event.Reaction;
import nars.util.event.Topic;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.LongPredicate;
import java.util.function.Predicate;


/**
 * Non-Axiomatic Reasoner
 * <p>
 * Instances of this represent a reasoner connected to a Memory, and set of Input and Output channels.
 * <p>
 * All state is contained within Memory.  A NAR is responsible for managing I/O channels and executing
 * memory operations.  It executesa series sof cycles in two possible modes:
 * * step mode - controlled by an outside system, such as during debugging or testing
 * * thread mode - runs in a pausable closed-loop at a specific maximum framerate.
 */
abstract public class NAR {


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

    public final NarseseParser narsese;

    /**
     * The memory of the reasoner
     */
    public Memory memory;

    /**
     * The name of the reasoner
     */
    protected String name;


    /**
     * Flag for running continuously
     */
    private boolean running = false;

    private int cyclesPerFrame = 1; //how many memory cycles to execute in one NAR cycle
    /**
     * memory activity enabled
     */
    private boolean enabled = true;

    final static Consumer<Object> onError = e -> {
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
     * normal way to construct a NAR, using a particular Build instance
     */
    public NAR(NARSeed b) {
        this(b.newMemory());
        //b.init(this);
    }

    public NAR(final Memory m) {
        super();

        setMemory(m);


        this.narsese = NarseseParser.the();

    }


    public final Memory memory() {
        return memory;
    }


    public NAR input(ImmediateOperator o) {
        input(o.newTask());
        return this;
    }

    /**
     * Reset the system with an empty memory and reset clock.  Event handlers
     * will remain attached but enabled plugins will have been deactivated and
     * reactivated, a signal for them to empty their state (if necessary).
     */
    public synchronized NAR reset() {
        nextTasks.clear();

        if (laterTasks != null) {
            laterTasks.shutdown();
            laterTasks = null;
        }

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
        return narsese.task(taskText, memory);
    }

    public <T extends Compound> TaskSeed task(T t) {
        return TaskSeed.make(memory, t);
    }

    public List<Task> tasks(final String parse) {
        List<Task> result = Global.newArrayList(1);
        narsese.tasks(parse, result, memory);
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
        return narsese.term(t);
    }

    public Concept concept(final Term term) {
        return memory.concept(term);
    }


    /**
     * gets a concept if it exists, or returns null if it does not
     */
    public Concept concept(final String conceptTerm) throws InvalidInputException {
        return concept((Term) narsese.term(conceptTerm));
    }

    public Task goal(final String goalTerm, final float freq, final float conf) {
        return goal(Global.DEFAULT_GOAL_PRIORITY, Global.DEFAULT_GOAL_DURABILITY, goalTerm, freq, conf);
    }

    public Task ask(String termString) throws InvalidInputException {
        //TODO remove '?' if it is attached at end
        return ask((Compound) narsese.compound(termString));
    }

    public Task ask(Compound c) throws InvalidInputException {
        //TODO remove '?' if it is attached at end
        return ask(c, Symbols.QUESTION);
    }

    public Task quest(String questString) throws InvalidInputException {
        return ask(narsese.term(questString), Symbols.QUEST);
    }

    public Task goal(float pri, float dur, String goalTerm, float freq, float conf) throws InvalidInputException {

        final Truth tv;

        Task t = new DefaultTask(

                narsese.compound(goalTerm),
                Symbols.GOAL,
                tv = new DefaultTruth(freq, conf),


                pri,
                dur, BudgetFunctions.truthToQuality(tv)

        );
        t.setCreationTime(time());

        input(t);
        return t;
    }

    public Task believe(float priority, String termString, long when, float freq, float conf) throws InvalidInputException {
        return believe(priority, Global.DEFAULT_JUDGMENT_DURABILITY, termString, when, freq, conf);
    }

    public Task believe(float priority, Compound term, long when, float freq, float conf) throws InvalidInputException {
        return believe(priority, Global.DEFAULT_JUDGMENT_DURABILITY, term, when, freq, conf);
    }

    public Task believe(String termString, Tense tense, float freq, float conf) throws InvalidInputException {
        return believe((Compound) term(termString), tense, freq, conf);
    }

    public Task believe(Compound term, float freq, float conf) throws InvalidInputException {
        return believe(term, Tense.Eternal, freq, conf);
    }

    public Task believe(Compound term, Tense tense, float freq, float conf) throws InvalidInputException {
        return believe(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY, term, tense, freq, conf);
    }

    public Task believe(String termString, float freq, float conf) throws InvalidInputException {
        return believe((Compound) term(termString), freq, conf);
    }

    public Task believe(String termString, float conf) throws InvalidInputException {
        return believe(termString, 1.0f, conf);
    }

    public Task believe(String termString) throws InvalidInputException {
        return believe(termString, 1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE);
    }

    public Task believe(Compound term) throws InvalidInputException {
        return believe(term, 1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE);
    }

    public Task believe(float pri, float dur, Compound beliefTerm, Tense tense, float freq, float conf) throws InvalidInputException {
        return believe(pri, dur, beliefTerm, Stamp.getOccurrenceTime(time(), tense, memory.duration()), freq, conf);
    }

    public Task believe(float pri, float dur, String beliefTerm, long occurrenceTime, float freq, float conf) throws InvalidInputException {
        return believe(pri, dur, (Compound) term(beliefTerm), occurrenceTime, freq, conf);
    }

    public <C extends Compound> Task<C> believe(float pri, float dur, C belief, long occurrenceTime, float freq, float conf) throws InvalidInputException {

        final Truth tv;

        Task<C> t = new DefaultTask<>(belief,
                Symbols.JUDGMENT,
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
                Global.DEFAULT_QUESTION_PRIORITY,
                Global.DEFAULT_QUESTION_DURABILITY,
                1);
        t.setCreationTime(time());
        input(t);

        return t;

        //ex: return new Answered(this, t);

    }


    protected boolean process(Task t) {
        throw new RuntimeException("this should be overridden in subclasses of NAR");
    }


    /**
     * exposes the memory to an input, derived, or immediate task.
     * the memory then delegates it to its controller
     * <p>
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

        if (!Terms.levelValid(t, nal())) {
            m.remove(t, "Insufficient NAL level");
            return false;
        }

        m.eventInput.emit(t);

        return true;
    }


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

    /**
     * returns the global concept index
     */
    public final CacheBag<Term, Concept> concepts() {
        return memory().getConcepts();
    }

    transient ExecutorService laterTasks = null;
    transient private final Deque<Runnable> nextTasks = new ConcurrentLinkedDeque();


    @Deprecated
    public int numConcepts(boolean active, boolean inactive) {
        int total = 0;
        if (active && !inactive) return -1; //numActiveConcepts();
        else if (!active && inactive) return -1; //return memory().concepts.size() - ();
        else if (active && inactive)
            return memory().concepts.size();
        else
            return 0;
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


//    /** input a task via direct TaskProcessing
//     * @return the TaskProcess, after it has executed (synchronously) */
//    public Premise inputDirect(final Task t) {
//        return TaskProcess.queue(this, t);
//    }

    /**
     * attach event handler to one or more event (classes)
     */
    public EventEmitter.Registrations on(Reaction<Class, Object[]> o, Class... c) {
        return memory.event.on(o, c);
    }

    public void on(Class<? extends Reaction<Class, Object[]>>... x) {

        for (Class<? extends Reaction<Class, Object[]>> c : x) {
            Reaction<Class, Object[]> v = memory().the(c);
            memory().the(c, v); //register singleton
            on(v);
        }
    }

    public void on(Class<? extends OpReaction> c) {
        //for (Class<? extends OpReaction> c : x) {
        OpReaction v = memory().the(c);
        on(v);
        //}
    }


    public EventEmitter.Registrations on(Reaction<Term, Operation> o, Term... c) {
        return memory.exe.on(o, c);
    }

    public EventEmitter.Registrations on(OpReaction o) {
        Term a = o.getTerm();
        EventEmitter.Registrations reg = on(o, a);
        o.setEnabled(this, true);
        return reg;
    }


    @Deprecated
    public int getCyclesPerFrame() {
        return cyclesPerFrame;
    }

    @Deprecated
    public void setCyclesPerFrame(int cyclesPerFrame) {
        this.cyclesPerFrame = cyclesPerFrame;
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

    @Deprecated
    public void start(final long minCyclePeriodMS, int cyclesPerFrame) {
        throw new RuntimeException("WARNING: this threading model is not safe and deprecated");

//        if (isRunning()) stop();
//        this.minFramePeriodMS = minCyclePeriodMS;
//        this.cyclesPerFrame = cyclesPerFrame;
//        running = true;
//        if (thread == null) {
//            thread = new Thread(this, this.toString() + "_reasoner");
//            thread.start();
//        }
    }

    /**
     * Repeatedly execute NARS working cycle in a new thread with Iterative timing.
     *
     * @param minCyclePeriodMS minimum cycle period (milliseconds).
     */
    @Deprecated
    public void start(final long minCyclePeriodMS) {
        start(minCyclePeriodMS, getCyclesPerFrame());
    }

    public EventEmitter event() {
        return memory.event;
    }

    /**
     * Exits an iteration loop if running
     */
    public void stop() {
        running = false;
        //enabled = false;
    }

    /**
     * steps 1 frame forward. cyclesPerFrame determines how many cycles this frame consists of
     */
    public void frame() {
        frame(1);
    }

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

    /**
     * Runs multiple frames, unless already running (then it return -1).
     *
     * @return total time in seconds elapsed in realtime
     */
    public void frame(final int frames) {

        final boolean wasRunning = running;

        running = true;

        final int cpf = cyclesPerFrame;
        for (int f = 0; (f < frames) && running; f++) {

            if (!isEnabled())
                throw new RuntimeException("NAR disabled");

            if (memory.resource != null)
                memory.resource.FRAME_DURATION.start();

            memory.clock.preFrame(memory);

            memory.cycle(cpf);

            runNextTasks();

            memory.eventFrameEnd.emit(this);

        }

        running = wasRunning;

    }

    /* Print all statically known events (discovered via reflection)
    *  for this reasoner to a stream
    * */
    public NAR trace(Appendable out) {


        Topic.all(memory(), (k, v) -> {
            try {

                //indent each cycle
                if (!k.equals("eventCycleStart")) {
                    out.append("  ");
                }

                out
                    .append(ANSI.COLOR_CONFIG)
                    .append(k.toString())
                    .append(ANSI.COLOR_RESET )
                    .append(": ")
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
     * Run until stopped, at full speed
     */
    public void run() {
        loop(0);
    }

    /**
     * Runs until stopped, at a given delay period between frames (0= no delay). Main loop
     */
    final public void loop(long minFramePeriodMS) {
        //TODO use DescriptiveStatistics to track history of frametimes to slow down (to decrease speed rate away from desired) or speed up (to reach desired framerate).  current method is too nervous, it should use a rolling average

        running = true;

        while (running) {

            final long start = System.currentTimeMillis();

            frame(1); //in seconds

            final long frameTimeMS = System.currentTimeMillis() - start;

            if (minFramePeriodMS > 0) {
                minFramePeriodMS = throttle(minFramePeriodMS, frameTimeMS);
            }
        }
    }

    protected long throttle(long minFramePeriodMS, long frameTimeMS) {
        double remainingTime = (minFramePeriodMS - frameTimeMS) / 1.0E3;
        if (remainingTime > 0) {
            onLoopLag(minFramePeriodMS);
        } else if (remainingTime < 0) {

            System.err.println(Thread.currentThread() + " loop lag: " + remainingTime + "ms too slow");

            //minFramePeriodMS++;
            //; incresing frame period to " + minFramePeriodMS + "ms");
        }
        return minFramePeriodMS;
    }

    private void onLoopLag(long minFramePeriodMS) {
        try {
            Thread.sleep(minFramePeriodMS);
        } catch (InterruptedException ee) {
            System.err.println(ee);
        }
    }

    /**
     * returns the configured NAL level
     */
    public NAR nal(int level) {
        memory.nal(level);
        return this;
    }

    public int nal() {
        return memory.nal();
    }

    public void emit(final Class c) {
        memory.emit(c);
    }

    public void emit(final Class c, final Object... o) {
        memory.emit(c, o);
    }


    /**
     * enable/disable all I/O and memory processing. CycleStart and CycleStop
     * events will continue to be generated, allowing the memory to be used as a
     * clock tick while disabled.
     */
    public void enable(boolean e) {
        this.enabled = e;
    }

    public boolean isEnabled() {
        return enabled;
    }


    /**
     * adds a task to the queue of task which will be executed in batch
     * at the end of the current cycle.
     * checks if the queue already contains the pending
     * Runnable instance to ensure duplicates don't
     * don't accumulate
     */
    final public void taskNext(final Runnable t) {
        if (!nextTasks.contains(t))
            nextTasks.addLast(t);
    }

    /**
     * runs all the tasks in the 'Next' queue
     */
    protected final void runNextTasks() {
        int originalSize = nextTasks.size();
        if (originalSize == 0) return;

        CycleProcess.run(nextTasks, originalSize);
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
    public void taskLater(Runnable t) {
        if (laterTasks == null) {
            laterTasks = Executors.newFixedThreadPool(1);
        }

        //laterTasks.submit(t);
        laterTasks.execute(t);
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

    public boolean isRunning() {
        return running;
    }


    /**
     * returns the Atom for the given string. since the atom is unique to itself it can be considered 'the' the
     */
    public Atom atom(final String s) {
        return Atom.the(s);
    }


    private final Multimap<Set<Class>, Reaction> reactions = HashMultimap.create();
    private List<Object> regs = new ArrayList();


    public Iterator<Concept> iterator() {
        return mem().getConcepts().iterator();
    }


    public void conceptPriorityHistogram(double[] bins) {
        if (bins != null)
            mem().getConcepts().getPriorityHistogram(bins);
    }


    public Concept put(Concept concept) {
        mem().put(concept);
        return concept;
    }


    public Concept remove(Term key) {
        return remove(mem().get(key));
    }


    public Concept get(final Term key) {
        return concepts().get(key);
    }


    public final void setMemory(Memory m) {

        m.the(NAR.class, this);

        if (isRunning())
            throw new RuntimeException("NAR must be stopped to change memory");

        memory = m;

        m.eventError.on(onError);

    }


    public final Memory mem() {
        return memory;
    }


    protected boolean active(Term t) {
        return mem().get(t) != null;
    }


    /**
     * get all tasks in the system by iterating all newTasks, novelTasks; does not change or remove any
     * Concept TaskLinks
     */
    public void getTasks(boolean includeTaskLinks, boolean includeNewTasks, boolean includeNovelTasks, Set<Task> target) {


        if (includeTaskLinks) {
            forEachConcept(c -> {
                Bag<Sentence, TaskLink> tl = c.getTaskLinks();
                if (tl != null)
                    tl.forEach(t -> target.add(t.targetTask));
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


    public void forEachTask(boolean includeTaskLinks, Consumer<Task> each) {
        forEachConcept(c -> {
            if (c.getTaskLinks() != null)
                c.getTaskLinks().forEach(tl -> {
                    each.accept(tl.getTask());
                });
        });
    }


    public NAR answer(String question, Consumer<Task> recvSolution) {
        //question punctuation optional
        if (!question.endsWith("?")) question = question + "?";
        Task qt = this.task(question);
        return answer(qt, recvSolution);
    }

    public NAR answer(Task question, Consumer<Task> recvSolution) {
        new AnswerReaction(this, question) {

            @Override
            public void onSolution(Task belief) {
                recvSolution.accept(belief);
            }

//            @Override
//            public void setActive(boolean b) {
//                super.setActive(b);
//                manage(reg, b);
//            }

        };
        return this;
    }

    protected final void ensureNotRunning() {
        if (this.isRunning())
            throw new RuntimeException("NAR is already running");
    }

//    public NAR loop(long periodMS) {
//        ensureNotRunning();
//
//        nar.loop(periodMS);
//
//        return nar;
//    }

    /**
     * blocks until finished
     */
    public NAR run(int frames) {
        ensureNotRunning();

        this.frame(frames);

        return this;
    }

    public NAR fork(Consumer<NAR> clone) {
        ensureNotRunning();
        return this; //TODO
    }

    public NAR save(ObjectOutputStream clone) {
        ensureNotRunning();
        return this; //TODO
    }

    public NAR load(ObjectInputStream clone) {
        ensureNotRunning();
        return this; //TODO
    }

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

    public NAR forEachConceptTask(boolean b, boolean q, boolean g, boolean _q,
                                  int maxPerConcept,
                                  Consumer<Task> recip) {
        forEachConcept(c -> {
            if (b && c.hasBeliefs()) c.getBeliefs().top(maxPerConcept, recip);
            if (q && c.hasQuestions()) c.getQuestions().top(maxPerConcept, recip);
            if (g && c.hasBeliefs()) c.getGoals().top(maxPerConcept, recip);
            if (_q && c.hasQuests()) c.getQuests().top(maxPerConcept, recip);
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

    public NAR meterLogic(Consumer<LogicMeter> recip) {
        recip.accept(this.memory.logic);
        return this;
    }

    public NAR meterEmotion(Consumer<EmotionMeter> recip) {
        recip.accept(this.memory.emotion);
        return this;
    }


    public NAR resetEvery(long minPeriodOfCycles) {
        onEachPeriod(minPeriodOfCycles, this::reset);
        return this;
    }

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
     * <p>
     * Existing concept: apply tasklink activation (remove from bag, adjust
     * budget, reinsert) New concept: set initial activation, insert Subconcept:
     * extract from cache, apply activation, insert
     * <p>
     * If failed to insert as a result of null bag, returns null
     * <p>
     * A displaced Concept resulting from insert is forgotten (but may be stored
     * in optional subconcept memory
     *
     * @param term indicating the concept
     * @return an existing Concept, or a new one, or null
     */
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
        }

        if ((term = term.normalized()) == null)
            return null;

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


    public NAR resetIf(Predicate<NAR> resetCondition) {
        onEachFrame((n) -> {
            if (resetCondition.test(n)) reset();
        });
        return this;
    }

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
        regs.add(this.memory.eventFrameEnd.on(receiver));
        return this;
    }

    public NAR onEachNthFrame(Runnable receiver, int frames) {
        return onEachFrame((n) -> {
            if (n.time() % frames == 0)
                receiver.run();
        });
    }

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

    public NAR output(ObjectOutputStream o, Class... signal) throws Exception {

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

    public NAR spawnThread(long periodMS, Consumer<Thread> t) {
        ensureNotRunning();

        t.accept(new Thread(() -> {
            loop(periodMS);
        }));

        return this;
    }

    public NAR onConceptActive(final Consumer<Concept> c) {
        regs.add(this.memory.eventConceptActivated.on(c));
        return this;
    }

    public NAR onConceptForget(final Consumer<Concept> c) {
        regs.add(this.memory.eventConceptForget.on(c));
        return this;
    }

//    public NAR onAfterFrame(final Runnable r) {
//        return onEachFrame(() -> {
//           taskNext(r);
//        });
//    }

    abstract private class StreamNARReaction extends NARReaction {

        public StreamNARReaction(Class... signal) {
            super((NAR) NAR.this, signal);
        }

        @Override
        public void setActive(boolean b) {
            super.setActive(b);
            manage(this, b);
        }
    }

    protected void manage(NARReaction r, boolean b) {
//        if (!b) {
//            reactions.remove(Sets.newHashSet(r.getEvents()), r);
//        } else {
//            reactions.put(Sets.newHashSet(r.getEvents()), r);
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

}