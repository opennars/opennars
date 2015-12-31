package nars;


import com.google.common.collect.Sets;
import com.gs.collections.impl.tuple.Tuples;
import nars.concept.Concept;
import nars.nal.Level;
import nars.nal.nal7.Tense;
import nars.nal.nal8.AbstractOperator;
import nars.nal.nal8.Execution;
import nars.nal.nal8.Operator;
import nars.nal.nal8.PatternAnswer;
import nars.nal.nal8.operator.TermFunction;
import nars.task.MutableTask;
import nars.task.Task;
import nars.task.flow.Input;
import nars.task.flow.TaskQueue;
import nars.task.flow.TaskStream;
import nars.task.in.FileInput;
import nars.task.in.TextInput;
import nars.term.Term;
import nars.term.Termed;
import nars.term.atom.Atom;
import nars.term.compile.TermIndex;
import nars.term.compound.Compound;
import nars.time.Clock;
import nars.util.data.Util;
import nars.util.event.*;
import net.openhft.affinity.AffinityLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;
import java.util.stream.Stream;

import static nars.Symbols.*;
import static nars.nal.nal7.Tense.ETERNAL;


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
public abstract class NAR implements Serializable, Level {


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

//    float defaultJudgmentConfidence = Global.DEFAULT_JUDGMENT_CONFIDENCE;
//    protected float defaultJudgmentPriority = Global.DEFAULT_JUDGMENT_PRIORITY;
//    protected float defaultJudgmentDurability = Global.DEFAULT_JUDGMENT_DURABILITY;
//    float defaultGoalPriority = Global.DEFAULT_GOAL_PRIORITY;
//    float defaultGoalDurability = Global.DEFAULT_GOAL_DURABILITY;
//    float defaultQuestionPriority = Global.DEFAULT_QUESTION_PRIORITY;
//    float defaultQuestionDurability = Global.DEFAULT_QUESTION_DURABILITY;

    static final Logger logger = LoggerFactory.getLogger(NAR.class);




    /**
     * The memory of the reasoner
     * TODO dont expose as public
     */
    public final Memory memory;

    /**
     * The id/name of the reasoner
     * TODO
     */
    public final Atom self;

    /**
     * Flag for running continuously
     */
    public final AtomicBoolean running = new AtomicBoolean();


    //TODO use this to store all handler registrations, and decide if transient or not
    public final transient List<Object> regs = new ArrayList();


    private final transient Deque<Runnable> nextTasks = new ConcurrentLinkedDeque();

    static final ThreadPoolExecutor asyncs =
            (ThreadPoolExecutor) Executors.newCachedThreadPool();
    //Executors.newFixedThreadPool(1);

    private final int concurrency = 1;


    public NAR(Memory m) {

        memory = m;

        m.the(NAR.class, this);

        if (running())
            throw new RuntimeException("NAR must be stopped to change memory");


        self = Global.DEFAULT_SELF; //TODO make this parametreizable


        /** register some components in the dependency context, Container (which Memory subclasses from) */
        //m.the("memory", m);
        m.the("clock", m.clock);


        m.eventError.on(e -> {
            if (e instanceof Throwable) {
                Throwable ex = (Throwable) e;

                //TODO move this to a specific impl of error reaction:
                ex.printStackTrace();

                if (Global.DEBUG && Global.EXIT_ON_EXCEPTION) {
                    //throw the exception to the next lower stack catcher, or cause program exit if none exists
                    throw new RuntimeException(ex);
                }
            } else {
                memory.logger.error(e.toString());
            }
        });

        m.start();

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
//    /**
//     * Resets and deletes the entire system
//     */
//    public void delete() {
//
//        memory.delete();
//
//    }

    public FileInput input(File input) throws IOException {
        FileInput fi = new FileInput(this, input);
        input((Input) fi);
        return fi;
    }

    /**
     * inputs a task, only if the parsed text is valid; returns null if invalid
     */
    public Task inputTask(String taskText) {
        //try {
            Task t = task(taskText);
            t.setCreationTime(time());
            if (input(t))
                return t;
            return null;
        /*} catch (Exception e) {
            return null;
        }*/
    }

    /**
     * parses and forms a Task from a string but doesnt input it
     */
    public Task task(String taskText) {
        return Narsese.the().task(taskText, memory);
    }


    public List<Task> tasks(String parse) {
        List<Task> result = Global.newArrayList(1);
        Narsese.the().tasks(parse, result, memory);
        return result;
    }

    public TaskQueue inputs(String parse) {
        return input(tasks(parse));
    }

    public final TextInput input(String text) {
        TextInput i = new TextInput(this, text);
        if (i.size() == 0) {
            //TODO replace with real parser error
            error(new Narsese.NarseseException("Input syntax error: " + text));
        }
        input((Input) i);
        return i;
    }

    public final <T extends Termed> T term(String t) throws Narsese.NarseseException {

        T x = (T)Narsese.the().term(t, index());

        if (x == null) {
            logger.error("Term syntax error: '{}'", t);
        } else {

            //this is applied automatically when a task is entered.
            //it's only necessary here where a term is requested
            //TODO apply this in index on the original copy only
            x.term().setDuration(memory.duration());
        }
        return x;
    }



    /**
     * gets a concept if it exists, or returns null if it does not
     */
    public final Concept concept(String conceptTerm) throws Narsese.NarseseException {
        return memory.concept((Termed)term(conceptTerm));
    }

    /** ask question */
    public Task ask(String termString) throws Narsese.NarseseException {
        //TODO remove '?' if it is attached at end
        /*if (t instanceof Compound)
            return ((T)t).normalizeDestructively();*/
        return ask((Compound) Narsese.the().<Compound>term(termString));
    }

    /** ask question */
    public Task ask(Compound c)  {
        //TODO remove '?' if it is attached at end
        return ask(c, QUESTION);
    }

    /** ask quest */
    public Task should(String questString) throws Narsese.NarseseException {
        Term c = term(questString);
        if (c instanceof Compound)
            return should((Compound) c);
        return null;
    }

    /** ask quest */
    public Task should(Compound quest)  {
        return ask(quest, QUEST);
    }

    /** desire goal */
    public Task goal(Compound goalTerm, Tense tense, float freq, float conf) throws Narsese.NarseseException {
        return goal(
                memory.getDefaultPriority(GOAL),
                memory.getDefaultDurability(GOAL),
                goalTerm, time(tense), freq, conf);
    }


//    public Task believe(float priority, String termString, long when, float freq, float conf) throws InvalidInputException {
//        return believe(priority, termString, when, freq, conf);
//    }

    public NAR believe(Termed term, Tense tense, float freq, float conf) throws Narsese.NarseseException {
        believe(memory.getDefaultPriority(JUDGMENT), term, time(tense), freq, conf);
        return this;
    }

    public Task believe(float priority, Termed term, long when, float freq, float conf) throws Narsese.NarseseException {
        return believe(priority, memory.getDefaultDurability(JUDGMENT), term, when, freq, conf);
    }

    public NAR believe(Termed term, float freq, float conf) throws Narsese.NarseseException {
        return believe(term, Tense.Eternal, freq, conf);
    }



    public NAR believe(String term, Tense tense, float freq, float conf) throws Narsese.NarseseException {
        believe(memory.getDefaultPriority(JUDGMENT), term(term), time(tense), freq, conf);
        return this;
    }

    public final long time(Tense tense) {
        return Tense.getOccurrenceTime(tense, memory);
    }

    public NAR believe(String termString, float freq, float conf) throws Narsese.NarseseException {
        return believe((Termed)term(termString), freq, conf);
    }


    public NAR believe(String termString) throws Narsese.NarseseException {
        return believe((Termed)term(termString));
    }

    public NAR believe(Termed term) throws Narsese.NarseseException {
        return believe(term, 1.0f, memory.getDefaultConfidence(JUDGMENT));
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

    public Task believe(float pri, float dur, Termed term, long occurrenceTime, float freq, float conf) throws Narsese.NarseseException {
        return input(pri, dur, term, JUDGMENT, occurrenceTime, freq, conf);
    }

    /**
     * TODO add parameter for Tense control. until then, default is Now
     */
    public Task goal(float pri, float dur, Termed goal, long occurrence, float freq, float conf) throws Narsese.NarseseException {
        return input(pri, dur, goal, GOAL, occurrence, freq, conf);
    }

    public final Task input(float pri, float dur, Termed term, char punc, long occurrenceTime, float freq, float conf)  {

        if (term == null) {
            return null;
        }

        Task t = new MutableTask(term, punc)
            .truth(freq, conf)
            .budget(pri, dur)
            .time(time(), occurrenceTime);

        input(t);

        return t;
    }

    public <T extends Compound> Task ask(T term, char questionOrQuest) throws Narsese.NarseseException {


        //TODO use input method like believe uses which avoids creation of redundant Budget instance

        MutableTask t = new MutableTask(term);
        if (questionOrQuest == QUESTION)
            t.question();
        else if (questionOrQuest == QUEST)
            t.quest();
        else
            throw new RuntimeException("invalid punctuation");


        t.time(time(), ETERNAL);

        input(t);

        return t;

        //ex: return new Answered(this, t);

    }


    /** returns a validated task if valid, null otherwise */
    public Task validInput(Task t) {
        Memory m = memory;

//        if (t == null) {
//            throw new RuntimeException("null input");
//        }

        if (t.isCommand()) {
            //direct execution pathway for commands
            int n = execute(t);
            if (n == 0) {
                m.remove(t, "Unknown Command");
            }
            return null;
        }

        Task tNorm = t.normalize(m);
        if (tNorm==null) {
            m.remove(t, "Garbage");
            return null;
        }

        return tNorm;

    }


    /**
     * exposes the memory to an input, derived, or immediate task.
     * the memory then delegates it to its controller
     * <p>
     * return true if the task was processed
     * if the task was a command, it will return false even if executed
     */
    public final boolean input(Task t) {
        if (null == (t = validInput(t)))
            return false;

        memory.eventInput.emit(t);

        return true;
    }


    /**
     * Entry point for all potentially executable tasks.
     * Enters a task and determine if there is a decision to execute.
     *
     * @return number of invoked handlers
     */
    public final int execute(Task goal) {
        Term term = goal.term();

        if (Op.isOperation(term)) {

            Topic<Execution> tt = memory.exe.get(
                Operator.operatorName((Compound) term)
            );

            if (tt!=null && !tt.isEmpty()) {
                //enqueue after this frame, before next
                beforeNextFrame(
                    new Execution(this, goal, tt)
                );
                return 1;
            }

        }
        /*else {
            System.err.println("Unexecutable: " + goal);
        }*/

        return 0;
    }


    /**
     * register a singleton
     */
    public final <X> X the(Object key, X value) {
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
    public final TermIndex index() {
        return memory.index;
    }

    public TaskQueue input(Collection<Task> t) {
        TaskQueue tq = new TaskQueue(t);
        input((Input) tq);
        return tq;
    }

    public TaskQueue input(Task[] t) {
        TaskQueue tq = new TaskQueue(t);
        input((Input) tq);
        return tq;
    }


//    public final TermFunction onExecTerm(String operator, Function<Term[], Object> func) {
//        return onExecTerm(Atom.the(operator), func);
//    }
//
//    public final EventEmitter.Registrations onExec(String operator, Consumer<Term[]> func) {
//        return onExec(Atom.the(operator), func);
//    }

//    public final EventEmitter.Registrations onExec(Term operator, Consumer<Term[]> func) {
//        //wrap the procedure in a function, suboptimal but ok
//        return onExecTask(operator, (Task tt) -> {
//            func.accept(Compounds.opArgs(tt.term()).terms());
//            return null;
//        });
//    }

    public On onExecTask(String operator, Consumer<Execution> f) {
        return onExec(Atom.the(operator), f);
    }

    public On onExecTerm(String operator, Function<Term[], Object> f) {
        return onExecTerm(Atom.the(operator), f);
    }

    /**
     * creates a TermFunction operator from a supplied function, which can be a lambda
     */
    public On onExecTerm(Term operator, Function<Term[], Object> func) {
        return onExec(operator, new TermFunction(operator) {

            @Override
            public Object function(Compound x, TermIndex i) {
                return func.apply( x.terms() );
            }

        });
    }

    public final On onExec(AbstractOperator r) {
        return onExec(r.getOperatorTerm(), r);
    }

    public final On onExec(String op, Consumer<Execution> each) {
        return onExec(Atom.the(op), each);
    }

    public final On onExec(Term op, Consumer<Execution> each) {
        Topic<Execution> t = memory.exe.computeIfAbsent(op, (Term o) -> new DefaultTopic<Execution>() );
        return t.on(each);
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
    public Input input(Input i) {
//        Task t;
//
//        while ((t = i.get()) != null) {
//            input(t);
//        }
        i.input(this, 1);
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
        if (!running.compareAndSet(true, false)) {
            throw new RuntimeException("wasnt running");
        }
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
    public final NAR frame() {
        return frame(1);
    }

    /**
     * pins thread to a CPU core to improve performance while
     * running some frames.
     *
     * there is some overhead in acquiring the lock so it
     * will not make sense to use this method unless
     * the expected runtime for the given # of frames
     * is sufficiently high (ie. dont use this in a loop;
     * instead put the loop inside an AffinityLock)
     */
    public final NAR frameBatch(int frames) {

        AffinityLock al = AffinityLock.acquireLock();
        try {
            frame(frames);
        } finally {
            al.release();
        }

        return this;
    }

    public static final class AlreadyRunningException extends RuntimeException {
        public AlreadyRunningException() {
            super("already running");
        }
    }

    /**
     * Runs multiple frames, unless already running (then it return -1).
     *
     * @return total time in seconds elapsed in realtime
     */
    public final NAR frame(int frames) {


        if (!running.compareAndSet(false, true)) {
            throw new AlreadyRunningException();
        }

        Memory memory = this.memory;

        Topic<NAR> frameStart = memory.eventFrameStart;

        Clock clock = memory.clock;

        int cpf = getCyclesPerFrame();
        for (int f = 0; f < frames; f++) {

            frameStart.emit(this);

            clock.preFrame();

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

        return this;
    }

    public NAR trace(Appendable out, Predicate<String> includeKey) {
        return trace(out, includeKey, null);
    }

    /* Print all statically known events (discovered via reflection)
    *  for this reasoner to a stream
    * */
    public NAR trace(Appendable out, Predicate<String> includeKey, Predicate includeValue) {


        String[] previous = {null};

        Topic.all(memory, (k, v) -> {
            if ((includeValue!=null) && (!includeValue.test(v)))
                return;

            try {
                outputEvent(out, previous[0], k, v);
            } catch (IOException e) {
                error(e);
            }
        }, includeKey);

        return this;
    }

    public NAR trace(Appendable out) {
        return trace(out, (k) -> true);
    }

    static final Set<String> logEvents = Sets.newHashSet(
            "eventTaskProcess", "eventAnswer",
            "eventExecute", "eventRevision", /* eventDerive */ "eventError",
            "eventSpeak"
    );


    public NAR log() {
        return log(System.out);
    }

    public NAR log(Appendable out) {
        return log(out, null);
    }
    public NAR log(Appendable out, Predicate includeValue) {
        return trace(out, logEvents::contains, includeValue);
    }

    public void outputEvent(Appendable out, String previou, String k, Object v) throws IOException {
        //indent each cycle
        if (!"eventCycleStart".equals(k)) {
            out.append("  ");
        }

        String chan = k.toString();
        if (!chan.equals(previou)) {
            out
                    //.append(ANSI.COLOR_CONFIG)
                    .append(chan)
                    //.append(ANSI.COLOR_RESET )
                    .append(": ");
            previou = chan;
        } else {
            //indent
            for (int i = 0; i < chan.length() + 2; i++)
                out.append(' ');
        }

        if (v instanceof Object[])
            v = Arrays.toString((Object[]) v);
        else if (v instanceof Task)
            v = ((Task)v).toString(memory, true);

        out.append(v.toString());

//        if (v instanceof Concept) {
//            Concept c = (Concept) v;
//            out.append(' ').append(c.getBudget().toBudgetString());
//        }

        out.append('\n');
    }


    /** creates a new loop which begins paused */
    public final NARLoop loop() {
        return loop((int)-1);
    }

    public final NARLoop loop(float initialFPS) {
        float millisecPerFrame = 1000.0f / initialFPS;
        return loop((int) millisecPerFrame);
    }

    /**
     * Runs until stopped, at a given delay period between frames (0= no delay). Main loop
     *
     * @param initialFramePeriodMS in milliseconds
     */
    final NARLoop loop(int initialFramePeriodMS) {
//        //TODO use DescriptiveStatistics to track history of frametimes to slow down (to decrease speed rate away from desired) or speed up (to reach desired framerate).  current method is too nervous, it should use a rolling average

        return new NARLoop(this, initialFramePeriodMS);
    }


    /**
     * sets current maximum allowed NAL level (1..8)
     */
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
    public final void beforeNextFrame(Runnable t) {
        if (running.get()) {
            //in a frame, so schedule for after it
            nextTasks.addLast(t);
        } else {
            //not in a frame, can execute immediately
            t.run();
        }
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
        int originalSize = nextTasks.size();
        if (originalSize == 0) return;

        Util.run(nextTasks, originalSize, concurrency);
        nextTasks.clear();
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
        } catch (RejectedExecutionException e) {
            if (onError != null)
                onError.accept(e);
            return false;
        }
    }

//    public boolean execAsync(Consumer<NAR> t) {
//        return execAsync( /* Runnable */ () -> t.accept(NAR.this));
//    }
//
//    public <X> Future<X> execAsync(Function<NAR, X> t) {
//        return asyncs.submit( /* (Callable) */() -> t.apply(NAR.this));
//    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + memory.toString() + ']';
    }

    /**
     * Get the current time from the clock
     *
     * @return The current time
     */
    public final long time() {
        return memory.time();
    }

    public final boolean running() {
        return running.get();
    }



    public NAR answer(String question, Consumer<Task> recvSolution) {
        //question punctuation optional
        if (!(question.length() > 0 && question.charAt(question.length() - 1) == '?')) question = question + '?';
        Task qt = task(question);
        return answer(qt, recvSolution);
    }

    /**
     * inputs the question and observes answer events for a solution
     */
    public NAR answer(Task questionOrQuest, Consumer<Task> c) {
        new AnswerReaction(this, questionOrQuest) {

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
        for (String s : ss) input(s);
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

    public NAR forEachConceptTask(Consumer<Task> recip) {
        return forEachConceptTask(true, true, true, true, recip);
    }

    public NAR forEachConceptTask(boolean includeConceptBeliefs, boolean includeConceptQuestions, boolean includeConceptGoals, boolean includeConceptQuests, Consumer<Task> recip) {
        return forEachConceptTask(includeConceptBeliefs, includeConceptQuestions, includeConceptGoals, includeConceptQuests, false, 0, recip);
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

    public abstract NAR forEachConcept(Consumer<Concept> recip);

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
        long start = time();
        long[] next = new long[1];
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
     * @return an existing Concept, or a new one, or null
     */
    public Concept conceptualize(Termed termed) {
        return memory.concept(termed);
    }

    //protected abstract Concept doConceptualize(Term term);



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
        regs.add(memory.eventCycleEnd.on(receiver));
        return this;
    }

    public final NAR onEachFrame(Consumer<NAR> receiver) {
        regs.add(memory.eventFrameStart.on(receiver));
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

    public NAR on(Runnable receiver, Class[] signal) {
        NARReaction r = new RunnableStreamNARReaction(receiver, signal);
        return this;
    }

    public NAR on(Class signal, Runnable receiver) {
        NARReaction r = new RunnableStreamNARReaction(receiver, signal);
        return this;
    }

    public NAR trace() {

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
//
//
//    public NAR onConceptActive(final Consumer<Concept> c) {
//        regs.add(this.memory.eventConceptActivated.on(c));
//        return this;
//    }

    public final void input(Stream<Task> taskStream) {
        input((Input) new TaskStream(taskStream));
    }

    /**
     * execute a Task as a TaskProcess (synchronous)
     */
    public Concept process(Task task) {

//        if(task==null) {
//            return false;
//        }
//        Budget taskBudget = task.getBudget();
//        if (inputPriorityFactor != 1f) {
//            taskBudget.mulPriority(inputPriorityFactor);
//        }
//        if (!taskBudget.summaryGreaterOrEqual(memory.taskProcessThreshold)) {
//            memory.remove(task, "Insufficient Budget to TaskProcess");
//            return null;
//        }
//
//
//        if (!task.getTerm().levelValid(nal())) {
//            memory.remove(task, "Insufficient NAL level");
//            return false;
//        }

//        TaskProcess d = new TaskProcess(this, task);
//
//        d.run();

        Concept c = conceptualize(task.term());
        if (c == null) {
            memory.remove(task, "Inconceivable");
            return null;
        }

        memory.emotion.busy(task);

        if (c.process(task, this)) {
            if (!task.isDeleted()) {

                c.link(task, 1f, this);

                memory.eventTaskProcess.emit(task);

            }

            return c;
        } else {
            memory.remove(task, null /* "Unprocessable" */);
            return null;
        }

    }

    /**
     * convenience method shortcut for concept(t.getTerm())
     * when possible, try to provide an existing Concept instance
     * to avoid a lookup
     */
    public final Concept concept(Termed termed) {
        return memory.concept(termed);
    }

    public On onQuestion(PatternAnswer p) {
        return memory.eventTaskProcess.on(tp -> {
            Task question = tp.getTask();
            if (question.getPunctuation() == '?') {
                beforeNextFrame(() -> {
                    List<Task> l = p.apply(question);
                    if (l != null) {
                        l.forEach(answer -> memory.eventAnswer.emit(Tuples.twin(question, answer)));
                        input(l);
                    }
                });
            }
        });
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

    private abstract class StreamNARReaction extends NARReaction {

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

//    public byte[] toBytes() throws IOException, InterruptedException {
//        return new GenericJBossMarshaller().objectToByteBuffer(this);
//    }
//    public static <N extends NAR> N fromBytes(byte[] b) throws IOException, ClassNotFoundException {
//        return (N) new GenericJBossMarshaller().objectFromByteBuffer(b);
//    }
//
//    public NAR fork() throws Exception {
//        //TODO find more efficient way than this brute serialization/deserialization
//        final byte[] bb = toBytes();
//        NAR x = fromBytes(bb);
//        return x;
//
//    }

    @Override
    public boolean equals(Object obj) {
        //TODO compare any other stateful values from NAR class in addition to Memory
        return this == obj;
    }


}