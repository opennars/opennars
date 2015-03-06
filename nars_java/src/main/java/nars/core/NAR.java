package nars.core;

import nars.core.Events.FrameEnd;
import nars.core.Events.FrameStart;
import nars.core.Memory.Timing;
import nars.event.EventEmitter;
import nars.event.Reaction;
import nars.io.*;
import nars.io.narsese.InvalidInputException;
import nars.io.narsese.Narsese;
import nars.logic.BudgetFunctions;
import nars.logic.entity.*;
import nars.logic.entity.stamp.Stamp;
import nars.logic.nal7.Tense;
import nars.logic.nal8.Operator;
import reactor.event.registry.Registration;

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


    private Thread thread = null;
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
    

    public final Narsese narsese;
    public TextPerception textPerception;






    public class PluginState implements Serializable {
        final public Plugin plugin;
        boolean enabled = false;

        public PluginState(Plugin plugin) {
            this(plugin,true);
        }
        
        public PluginState(Plugin plugin, boolean enabled) {
            this.plugin = plugin;
            setEnabled(enabled);
        }

        public void setEnabled(boolean enabled) {
            if (this.enabled == enabled) return;
            
            plugin.setEnabled(NAR.this, enabled);
            this.enabled = enabled;
            emit(Events.PluginsChange.class, plugin, enabled);
        }

        public boolean isEnabled() {
            return enabled;
        }
    }
    
    protected final List<PluginState> plugins = new CopyOnWriteArrayList<>();
    
    /** Flag for running continuously  */
    private boolean running = false;
    
    
    /** used by stop() to signal that a running loop should be interrupted */
    private boolean stopped = false;
    
    private boolean threadYield = false;

    private int cyclesPerFrame = 1; //how many memory cycles to execute in one NAR cycle
    
    
    protected NAR(final Memory m) {
        this.memory = m;        
        this.param = m.param;
        

        this.narsese = new Narsese(this);
        this.textPerception = new TextPerception(this, narsese);

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

        final List<PluginState> toReEnable = new ArrayList();

        @Override
        public void event(Class event, Object[] args) {

            //toggle plugins

            //1. disable all
            for (PluginState p : getPlugins()) {
                if (p.isEnabled()) {
                    toReEnable.add(p);
                    p.setEnabled(false);
                }
            }
            //2. enable all
            for (PluginState p : toReEnable) {
                p.setEnabled(true);
            }

            toReEnable.clear();
        }
    };


    /**
     * Convenience method for creating a TextInput and adding as Input Channel.
     * Generally the text will consist of Task's to be parsed in Narsese, but
     * may contain other commands recognized by the system. The creationTime
     * will be set to the current memory cycle time, but may be processed by
     * memory later according to the length of the input queue.
     */
    public TextInput addInput(final String text) {
        
        //return addInput(text, text.contains("\n") ? Stamp.UNPERCEIVED : time());
        return addInput(text, Stamp.UNPERCEIVED);
    }

    public SensorPort addInput(final File input) throws FileNotFoundException {
        return addInput( new TextInput(textPerception, input) );
    }
    public SensorPort addInput(final InputStream input) {
        return addInput( new TextInput(textPerception,
                new BufferedReader(new InputStreamReader( input ) ) ) );
    }

    /** add text input at a specific time, which can be set to current time (regardless of when it will reach the memory), backdated, or forward dated
     * if creationTime == Stamp.UNPERCEIVED, creationTime will not be changed
     * */
    public TextInput addInput(final String text, long creationTime) {
        final TextInput i = new TextInput(textPerception, text);

        addInput(i, creationTime);

        return i;
    }
    
    public NAR addInput(final String taskText, float frequency, float confidence) throws InvalidInputException {
        return addInput(-1, -1, taskText, frequency, confidence);
    }

    public Term term(String t) throws InvalidInputException {
        return narsese.parseTerm(t);
    }

    public Concept concept(Term term) {
        return memory.concept(term);
    }
    /** gets a concept if it exists, or returns null if it does not */
    public Concept concept(String concept) throws InvalidInputException {
        return concept(new Narsese(this).parseTerm(concept));
    }


    public Task goal(String goalTerm, float freq, float conf) {
        return goal(Parameters.DEFAULT_GOAL_PRIORITY, Parameters.DEFAULT_GOAL_DURABILITY, goalTerm, freq, conf);
    }


    public Task believe(String termString, Tense tense, float freq, float conf) throws InvalidInputException {
        return believe(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, termString, tense, freq, conf);
    }
    public Task believe(String termString, float freq, float conf) throws InvalidInputException {
        return believe(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, termString, Tense.Eternal, freq, conf);
    }
    public Task believe(String termString, float conf) throws InvalidInputException {
        return believe(termString, 1.0f, conf);
    }
    public Task believe(String termString) throws InvalidInputException {
        return believe(termString, 1.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
    }

    

    public Task ask(String termString) throws InvalidInputException {
        //TODO remove '?' if it is attached at end
        return ask(termString, null);
    }

    public Task quest(String questString) throws InvalidInputException {
        return ask(questString, null, Symbols.QUEST);
    }
    public Task ask(String termString, Answered answered) throws InvalidInputException {
        return ask(termString, answered, Symbols.QUESTION);
    }

    public Task goal(float pri, float dur, String goalTerm, float freq, float conf) throws InvalidInputException {
        final Task t;
        final TruthValue tv;
        addInput(
                t = new Task(
                        new Sentence(
                                narsese.parseCompoundTerm(goalTerm),
                                Symbols.GOAL,
                                tv = new TruthValue(freq, conf),
                                new Stamp(memory, Stamp.UNPERCEIVED, Stamp.ETERNAL)),
                        new BudgetValue(
                                pri,
                                dur, BudgetFunctions.truthToQuality(tv)))
        );
        return t;
    }

    public Task believe(float pri, float dur, String beliefTerm, Tense tense, float freq, float conf) throws InvalidInputException {
        final Task t;
        final TruthValue tv;
        addInput(
                t = new Task(
                        new Sentence(
                                narsese.parseCompoundTerm(beliefTerm),
                                Symbols.JUDGMENT,
                                tv = new TruthValue(freq, conf),
                                new Stamp(memory, Stamp.UNPERCEIVED, tense)),
                        new BudgetValue(
                                pri,
                                dur, BudgetFunctions.truthToQuality(tv)))
        );
        return t;
    }

    public Task ask(String termString, Answered answered, char questionOrQuest) throws InvalidInputException {


        final Task t;
        addInput(
                t = new Task(
                        new Sentence(
                                narsese.parseCompoundTerm(termString),
                                questionOrQuest,
                                null, 
                                new Stamp(memory, Stamp.UNPERCEIVED, Tense.Eternal)),
                        new BudgetValue(
                                Parameters.DEFAULT_QUESTION_PRIORITY, 
                                Parameters.DEFAULT_QUESTION_DURABILITY, 
                                1))
        );
        
        if (answered!=null) {
            answered.start(t, this);
        }
        return t;
        
    }
    
    public NAR addInput(final Sentence sentence) {
        return addInput(
                new Task(sentence, BudgetValue.newDefault(sentence, memory))
        );
    }
    
    public NAR addInput(float priority, float durability, final String taskText, float frequency, float confidence) throws InvalidInputException {
        
        Task t = new Narsese(this).parseTask(taskText);        
        if (frequency!=-1)
            t.sentence.truth.setFrequency(frequency);
        if (confidence!=-1)
            t.sentence.truth.setConfidence(confidence);
        if (priority!=-1)
            t.budget.setPriority(priority);
        if (durability!=-1)
            t.budget.setDurability(durability);
        
        return addInput(t);
    }
    
    public NAR addInput(final Task t) {
        addInput( new TaskInput(t) );
        return this;
    }

  
    
    /** attach event handler */
    public Registration on(Class c, Reaction o) {
        return memory.event.on(c, o);
    }
    


    @Deprecated public int getCyclesPerFrame() {
        return cyclesPerFrame;
    }

    @Deprecated public void setCyclesPerFrame(int cyclesPerFrame) {
        this.cyclesPerFrame = cyclesPerFrame;
    }


    /** Adds an input channel for input from an external sense / sensor.
     *  Will remain added until it closes or it is explicitly removed. */
    public SensorPort addInput(final Input channel) {
        return addInput(channel, -1);
    }

    /** provides a specific creationTime that the input's generated tasks will be overriden with */
    public SensorPort addInput(final Input channel, long creationTime) {

        SensorPort i = new SensorPort(channel, 1.0f);

        if (creationTime!=Stamp.UNPERCEIVED)
            i.setCreationTimeOverride(creationTime);

        memory.perception.accept(i);

        return i;
    }

//    /** Explicitly removes an input channel and notifies it, via Input.finished(true) that is has been removed */
//    public Input removeInput(Input channel) {
//        inputChannels.remove(channel);
//        channel.finished(true);
//        return channel;
//    }


    /** add and enable a plugin or operator */
    public PluginState addPlugin(Plugin p) {
        if (p instanceof Operator) {
            memory.addOperator((Operator)p);
        }
        PluginState ps = new PluginState(p);
        plugins.add(ps);
        return ps;
    }

    /** disable and remove a plugin or operator */
    public void removePlugin(PluginState ps) {
        if (plugins.remove(ps)) {
            Plugin p = ps.plugin;
            if (p instanceof Operator) {
                memory.removeOperator((Operator)p);
            }
            ps.setEnabled(false);
        }
    }
    
    public List<PluginState> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    
    @Deprecated public void start(final long minCyclePeriodMS, int cyclesPerFrame) {
        this.minFramePeriodMS = minCyclePeriodMS;
        this.cyclesPerFrame = cyclesPerFrame;
        if (thread == null) {
            thread = new Thread(this, this.toString() + "_reasoner");
            thread.start();
        }
        running = true;        
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
    public void stop() {
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

    /** Execute a fixed number of frames.  cyclesPerFrame determines how many cycles each frame consists of
     * may execute more than requested cycles if cyclesPerFrame > 1
     * @return total time in seconds elapsed in realtime
     * */
    public double step(final int frames) {
        /*if (thread!=null) {
            memory.stepLater(cycles);
            return;
        }*/
        
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
            memory.stepLater(cycles);
        
        //finish all remaining cycles
        while (!memory.isProcessingInput() && (!stopped)) {
            step(1);
        }
        
        running = false;
        
        return this;
    }
    

    /** Main loop executed by the Thread.  Should not be called directly. */    
    @Override public void run() {
        //TODO use DescriptiveStatistics to track history of frametimes to slow down (to decrease speed rate away from desired) or speed up (to reach desired framerate).  current method is too nervous, it should use a rolling average
        
        stopped = false;
        
        while (running && !stopped) {      
            

            double frameTime = step(1); //in seconds

            if (minFramePeriodMS > 0) {
                
                double remainingTime = minFramePeriodMS - (frameTime/1E3);
                if (remainingTime > 0) {
                    try {
                        Thread.sleep(minFramePeriodMS);
                    } catch (InterruptedException ee) {
                        onError(ee);
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


    protected void onError(Throwable e)  {
        /*if (e.getCause()!=null)
            e.getCause().printStackTrace();
        else*/
            e.printStackTrace();

        memory.error(e);

        if (Parameters.EXIT_ON_EXCEPTION) {
            //throw the exception to the next lower stack catcher, or cause program exit if none exists
            throw new RuntimeException(e);
        }
    }

    /**
     * A frame, consisting of one or more NAR memory cycles
     */
    public double frame(final int cycles) {

        memory.resource.FRAME_DURATION.start();

        emit(FrameStart.class);

        try {
            for (int i = 0; i < cycles; i++)
                memory.cycle();
        }
        catch (Throwable e) {
            onError(e);
        }

        emit(FrameEnd.class);

        final double frameTime = memory.resource.FRAME_DURATION.stop();

        //in real-time mode, warn if frame consumed more time than reasoner duration
        if (memory.getTiming() == Timing.Real) {
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
    public static NAR build(Class<? extends NewNAR> g) {
        try {
            return new NAR(g.newInstance());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /** normal way to construct a NAR, using a particular Build instance */
    public NAR(NewNAR b) {
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