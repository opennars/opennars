package nars;

import nars.Events.FrameEnd;
import nars.Events.FrameStart;
import nars.Memory.Timing;
import nars.budget.Budget;
import nars.event.EventEmitter;
import nars.event.Reaction;
import nars.io.*;
import nars.io.narsese.InvalidInputException;
import nars.io.narsese.OldNarseseParser;
import nars.io.narsese.NarseseParser;
import nars.nal.*;
import nars.nal.concept.Concept;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operator;
import nars.nal.stamp.Stamp;
import nars.nal.term.Term;
import nars.operate.IOperator;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Non-Axiomatic Reasoner
 * 
 * Instances of this represent a reasoner connected to a Memory, and set of Input and Output channels.
 * 
 * All state is contained within Memory.  A NAR is responsible for managing I/O channels and executing
 * memory operations.  It executesa series sof cycles in two possible modes:
 *   * step mode - controlled by an outside system, such as during debugging or testing
 *   * thread mode - runs in a pausable closed-loop at a specific maximum framerate.
 */
public class NAR implements Runnable {

    /**
     * The information about the version and date of the project.
     */
    public static final String VERSION = "Open-NARS v1.6.3";
    
    /**
     * The project web sites.
     */
    public static final String WEBSITE =
              " Open-NARS website:  http://code.google.com/p/open-nars/ \n"
            + "      NARS website:  http://sites.google.com/site/narswang/ \n" +
              "    Github website:  http://github.com/opennars/ \n" + 
            "    IRC:  http://webchat.freenode.net/?channels=nars \n";
    private final NarseseParser narseseParser;


    @Deprecated private Thread thread = null;
    long minFramePeriodMS;
    
    /**
     * The name of the reasoner
     */
    protected String name;
    /**
     * The memory of the reasoner
     */
    public final Memory memory;
    public final Param param;
    

    @Deprecated public final OldNarseseParser narsese;
    public TextPerception textPerception;

    public void think(int delay) {
        memory.think(delay);
    }



    /** represents the state of an instance of a plugin: whether it is 'plugged in' or not, and methods to control that */
    public class PluggedIn implements Serializable {
        final public nars.operate.IOperator IOperator;
        boolean enabled = false;

        public PluggedIn(IOperator IOperator) {
            this(IOperator,true);
        }
        
        public PluggedIn(IOperator IOperator, boolean enabled) {
            this.IOperator = IOperator;
            setEnabled(enabled);
        }

        public void setEnabled(boolean enabled) {
            if (this.enabled == enabled) return;
            
            IOperator.setEnabled(NAR.this, enabled);
            this.enabled = enabled;
            emit(Events.PluginsChange.class, IOperator, enabled);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void off() {
            NAR.this.off(this);
        }

        public Memory getMemory() { return NAR.this.memory; }
    }
    
    protected final List<PluggedIn> plugins = new CopyOnWriteArrayList<>();
    
    /** Flag for running continuously  */
    private boolean running = false;
    
    
    /** used by stop() to signal that a running loop should be interrupted */
    private boolean stopped = false;
    
    private boolean threadYield = false;

    private int cyclesPerFrame = 1; //how many memory cycles to execute in one NAR cycle
    
    
    protected NAR(final Memory m) {
        this.memory = m;        
        this.param = m.param;


        this.narseseParser = NarseseParser.newParser(this);
        this.narsese = new OldNarseseParser(this, narseseParser);
        this.textPerception = new TextPerception(this, narsese, narseseParser);

        m.event.on(Events.ResetStart.class, togglePluginOnReset);
    }

    /**
     * Reset the system with an empty memory and reset clock.  Event handlers
     * will remain attached but enabled plugins will have been deactivated and
     * reactivated, a signal for them to empty their state (if necessary).
     */
    public void reset() {
        memory.reset(true);
    }

    final Reaction togglePluginOnReset = new Reaction() {

        final List<PluggedIn> toReEnable = new ArrayList();

        @Override
        public void event(Class event, Object[] args) {

            //toggle plugins

            //1. disable all
            for (PluggedIn p : getPlugins()) {
                if (p.isEnabled()) {
                    toReEnable.add(p);
                    p.setEnabled(false);
                }
            }
            //2. enable all
            for (PluggedIn p : toReEnable) {
                p.setEnabled(true);
            }

            toReEnable.clear();
        }
    };




    public TaskSource input(final File input) throws FileNotFoundException {
        return input(new TextInput(textPerception, input));
    }
    public TaskSource input(final InputStream input) {
        return input(new TextInput(textPerception,
                new BufferedReader(new InputStreamReader(input))));
    }

    /** inputs a task, only if the parsed text is valid; returns null if invalid */
    public Task inputTask(final String taskText) {
        try {
            Task t = task(taskText);
            input(t);
            return t;
        }
        catch (Exception e) {
            return null;
        }
    }

    /** parses and forms a Task from a string but doesnt input it */
    public Task task(String taskText) {
        Task t = narsese.parseTask(taskText);

        long now = time();
        if (!t.sentence.isEternal()) {
            t.getStamp().setTime(now, now + t.sentence.getOccurrenceTime());
        }
        else {
            t.getStamp().setTime(now, Stamp.ETERNAL);
        }
        return t;
    }


    public TextInput input(final String text) {
        final TextInput i = new TextInput(textPerception, text);
        input(i);
        return i;
    }
    
    public NAR input(final String taskText, float frequency, float confidence) throws InvalidInputException {
        return input(-1, -1, taskText, frequency, confidence);
    }

    public Term term(String t) throws InvalidInputException {
        return narsese.parseTerm(t);
    }

    public Concept concept(Term term) {
        return memory.concept(term);
    }
    /** gets a concept if it exists, or returns null if it does not */
    public Concept concept(String conceptTerm) throws InvalidInputException {
        return concept(narsese.parseTerm(conceptTerm));
    }



    public Task goal(String goalTerm, float freq, float conf) {
        return goal(Global.DEFAULT_GOAL_PRIORITY, Global.DEFAULT_GOAL_DURABILITY, goalTerm, freq, conf);
    }


    public Task believe(String termString, long when, float freq, float conf, float priority) throws InvalidInputException {
        return believe(priority, Global.DEFAULT_JUDGMENT_DURABILITY, termString, when, freq, conf);
    }

    public Task believe(String termString, Tense tense, float freq, float conf) throws InvalidInputException {
        return believe(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY, termString, tense, freq, conf);
    }
    public Task believe(String termString, float freq, float conf) throws InvalidInputException {
        return believe(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY, termString, Tense.Eternal, freq, conf);
    }
    public Task believe(String termString, float conf) throws InvalidInputException {
        return believe(termString, 1.0f, conf);
    }
    public Task believe(String termString) throws InvalidInputException {
        return believe(termString, 1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE);
    }



    public Task ask(String termString) throws InvalidInputException {
        //TODO remove '?' if it is attached at end
        return ask(termString, '?');
    }

    public Task quest(String questString) throws InvalidInputException {
        return ask(questString, Symbols.QUEST);
    }

    public Task goal(float pri, float dur, String goalTerm, float freq, float conf) throws InvalidInputException {
        final Task t;
        final TruthValue tv;
        input(
                t = new Task(
                        new Sentence(
                                narsese.parseCompoundTerm(goalTerm),
                                Symbols.GOAL,
                                tv = new TruthValue(freq, conf),
                                new Stamp(memory, Stamp.UNPERCEIVED, Stamp.ETERNAL)),
                        new Budget(
                                pri,
                                dur, BudgetFunctions.truthToQuality(tv)))
        );
        return t;
    }
    //getOccurrenceTime(creationTime, tense, memory.duration())

    public Task believe(float pri, float dur, String beliefTerm, Tense tense, float freq, float conf) throws InvalidInputException {
        return believe(pri, dur, beliefTerm, Stamp.getOccurrenceTime(time(), tense, memory.duration()), freq, conf);
    }
    public Task believe(float pri, float dur, String beliefTerm, long occurrenceTime, float freq, float conf) throws InvalidInputException {
        final Task t;
        final TruthValue tv;
        input(
                t = new Task(
                        new Sentence(
                                narsese.parseCompoundTerm(beliefTerm),
                                Symbols.JUDGMENT,
                                tv = new TruthValue(freq, conf),
                                new Stamp(memory, time(), occurrenceTime)),
                        new Budget(
                                pri,
                                dur, BudgetFunctions.truthToQuality(tv)))
        );
        return t;
    }

    public Task ask(String termString, char questionOrQuest) throws InvalidInputException {


        final Task t;
        input(
                t = new Task(
                        new Sentence(
                                narsese.parseCompoundTerm(termString),
                                questionOrQuest,
                                null,
                                new Stamp(memory, Stamp.UNPERCEIVED, Tense.Eternal)),
                        new Budget(
                                Global.DEFAULT_QUESTION_PRIORITY,
                                Global.DEFAULT_QUESTION_DURABILITY,
                                1))
        );

        return t;

        //ex: return new Answered(this, t);

    }
    
    public NAR input(final Sentence sentence) {
        return input(
                new Task(sentence, Budget.newDefault(sentence, memory))
        );
    }
    
    public NAR input(float priority, float durability, final String taskText, float frequency, float confidence) throws InvalidInputException {
        
        Task t = narsese.parseTask(taskText);
        if (frequency!=-1)
            t.sentence.truth.setFrequency(frequency);
        if (confidence!=-1)
            t.sentence.truth.setConfidence(confidence);
        if (priority!=-1)
            t.budget.setPriority(priority);
        if (durability!=-1)
            t.budget.setDurability(durability);
        
        return input(t);
    }
    
    public NAR input(final Task t) {
        input(new TaskInput(t));
        return this;
    }


    /** attach event handler to one or more event (classes) */
    public EventEmitter.Registrations on(Reaction o, Class... c) {
        return memory.event.on(o, c);
    }
    


    @Deprecated public int getCyclesPerFrame() {
        return cyclesPerFrame;
    }

    @Deprecated public void setCyclesPerFrame(int cyclesPerFrame) {
        this.cyclesPerFrame = cyclesPerFrame;
    }


    /** Adds an input channel for input from an external sense / sensor.
     *  Will remain added until it closes or it is explicitly removed. */
    public TaskSource input(final Input channel) {

        TaskSource i = new TaskSource(channel, 1.0f);

        memory.perception.accept(i);

        return i;
    }

//    /** Explicitly removes an input channel and notifies it, via Input.finished(true) that is has been removed */
//    public Input removeInput(Input channel) {
//        inputChannels.remove(channel);
//        channel.finished(true);
//        return channel;
//    }


    /** add and enable a plugin or operate */
    public PluggedIn on(IOperator p) {
        if (p instanceof Operator) {
            memory.operatorAdd((Operator) p);
        }
        PluggedIn ps = new PluggedIn(p);
        plugins.add(ps);
        return ps;
    }

    /** disable and remove a plugin or operate; use the PluginState instance returned by on(plugin) to .off() it */
    protected void off(PluggedIn ps) {
        if (plugins.remove(ps)) {
            IOperator p = ps.IOperator;
            if (p instanceof Operator) {
                memory.operatorRemove((Operator) p);
            }
            ps.setEnabled(false);
        }
    }
    
    public List<PluggedIn> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    
    @Deprecated public void start(final long minCyclePeriodMS, int cyclesPerFrame) {
        if (isRunning()) stop();
        this.minFramePeriodMS = minCyclePeriodMS;
        this.cyclesPerFrame = cyclesPerFrame;
        running = true;
        if (thread == null) {
            thread = new Thread(this, this.toString() + "_reasoner");
            thread.start();
        }
    }
    
    /**
     * Repeatedly execute NARS working cycle in a new thread with Iterative timing.
     * 
     * @param minCyclePeriodMS minimum cycle period (milliseconds).
     */    
    @Deprecated public void start(final long minCyclePeriodMS) {
        start(minCyclePeriodMS, getCyclesPerFrame());
    }

    
    public EventEmitter event() { return memory.event; }
    
    
    /**
     * Stop the logic process, killing its thread.
     */
    @Deprecated public void stop() {
        if (thread!=null) {
            thread.interrupt();
            thread = null;
        }
        stopped = true;
        running = false;
    }    

    /** steps 1 frame forward. cyclesPerFrame determines how many cycles this frame consists of */
    public double step() {
        return step(1);
    }

    /** Runs multiple frames, unless already running (then it return -1).
     * @return total time in seconds elapsed in realtime
     * */
    public double step(final int frames) {
        
        final boolean wasRunning = running;
        running = true;
        stopped = false;
        double elapsed = 0;
        for (int f = 0; (f < frames) && (!stopped); f++) {
            elapsed += frame(cyclesPerFrame);
        }
        running = wasRunning;
        return elapsed;
    }

    /**
     * Execute a minimum number of cycles, allowing additional cycles (less than maxCycles) for finishing any pending inputs
     * @param maxCycles max cycles, or -1 to allow any number of additional cycles until input finishes
     * */
    public NAR run(long minCycles, long maxCycles) {


        if (maxCycles <= 0) return this;
        if (minCycles > maxCycles)
            throw new RuntimeException("minCycles " + minCycles + " required <= maxCycles " + maxCycles);

        running = true;
        stopped = false;

        long cycleStart = time();
        do {
            step(1);

            long now = time();

            long elapsed = now - cycleStart;

            if (elapsed < minCycles)
                running = !stopped;
            else
                running = (!memory.perception.isEmpty()) && (!stopped) &&
                    (elapsed < maxCycles);
        }
        while (running);

        return this;
    }

    /** Execute a fixed number of cycles, then finish any remaining walking steps. */
    public NAR run(long cycles) {
        //TODO see if this entire method can be implemented as run(0, cycles);

        if (cycles <= 0) return this;
        
        running = true;
        stopped = false;

        //clear existing input
        
        long cycleStart = time();
        do {
            step(1);           
        }
        while ((!memory.perception.isEmpty()) && (!stopped));
                   
        long cyclesCompleted = time() - cycleStart;
        
        //queue additional cycles, 
        cycles -= cyclesCompleted;
        if (cycles > 0)
            memory.think(cycles);
        
        //finish all remaining cycles
        while (!memory.thinking() && (!stopped)) {
            step(1);
        }
        
        running = false;
        
        return this;
    }
    

    /** Main loop executed by the Thread.  Should not be called directly. */    
    @Override public void run() {
        //TODO use DescriptiveStatistics to track history of frametimes to slow down (to decrease speed rate away from desired) or speed up (to reach desired framerate).  current method is too nervous, it should use a rolling average

        running = true;
        stopped = false;
        
        while (running && !stopped) {      
            

            double frameTime = step(1); //in seconds

            if (minFramePeriodMS > 0) {
                
                double remainingTime = minFramePeriodMS - (frameTime/ 1.0E3);
                if (remainingTime > 0) {
                    try {
                        Thread.sleep(minFramePeriodMS);
                    } catch (InterruptedException ee) {
                        //error(ee);
                    }
                }
                else if (remainingTime < 0) {
                    minFramePeriodMS++;
                    System.err.println("Expected framerate not achieved: " + remainingTime + "ms too slow; incresing frame period to " + minFramePeriodMS + "ms");
                }
            }
            else if (threadYield) {
                Thread.yield();
            }
        }
    }
    
    

    /** returns the configured NAL level */
    public int nal() {
        return memory.nal();
    }


    
    public void emit(final Class c, final Object... o) {
        memory.emit(c, o);
    }


    protected void error(Throwable e)  {
        memory.error(e);
    }

    /**
     * A frame, consisting of one or more NAR memory cycles
     */
    public double frame(final int cycles) {


        memory.resource.FRAME_DURATION.start();

        emit(FrameStart.class);

        try {
            for (int i = 0; i < cycles; i++)
                memory.cycle(i==0);
        }
        catch (Throwable e) {
            Throwable c = e.getCause();
            if (c == null) c = e;
            error(c);
        }

        emit(FrameEnd.class);

        final double frameTime = memory.resource.FRAME_DURATION.stop();

        //in real-time mode, warn if frame consumed more time than reasoner duration
        if (memory.getTiming() == Timing.RealMS) {
            final int d = param.duration.get();

            if (frameTime > d) {
                emit(Events.ERR.class,
                        "Real-time consumed by frame (" +
                                frameTime + " ms) exceeds reasoner Duration (" + d + " cycles)" );
            }
        }

        return frameTime;
    }

    @Override
    public String toString() {
        return "NAR[" + memory.toString() + "]";
    }

     /**
     * Get the current time from the clock Called in {@link nars.logic.entity.stamp.Stamp}
     *
     * @return The current time
     */
    public long time() {
        return memory.time();
    }
    

    public boolean isRunning() {
        return running;
    }    

    public long getMinFramePeriodMS() {
        return minFramePeriodMS;
    }

    /** When b is true, NAR will call Thread.yield each run() iteration that minCyclePeriodMS==0 (no delay). 
     *  This is for improving program responsiveness when NAR is run with no delay.
     */
    public void setThreadYield(boolean b) {
        this.threadYield = b;
    }


    /** create a NAR given the class of a Build.  its default constructor will be used */
    public static NAR build(Class<? extends ProtoNAR> g) {
        try {
            return new NAR(g.newInstance());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /** normal way to construct a NAR, using a particular Build instance */
    public NAR(ProtoNAR b) {
        this(b.newMemory(b.param));
        b.init(this);
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