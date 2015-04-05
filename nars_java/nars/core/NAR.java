package nars.core;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import static com.google.common.collect.Iterators.singletonIterator;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import nars.core.EventEmitter.EventObserver;
import nars.core.Events.FrameEnd;
import nars.core.Events.FrameStart;
import nars.core.Events.Perceive;
import nars.core.Memory.TaskSource;
import nars.core.Memory.Timing;
import nars.core.control.AbstractTask;
import nars.core.control.NAL.DerivationFilter;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.gui.NARControls;
import nars.io.Answered;
import nars.io.InPort;
import nars.io.Input;
import nars.io.Output;
import nars.io.Output.ERR;
import nars.io.Output.IN;
import nars.io.Symbols;
import nars.io.TaskInput;
import nars.io.TextInput;
import nars.io.buffer.Buffer;
import nars.io.buffer.FIFO;
import nars.io.narsese.Narsese;
import nars.io.narsese.Narsese.InvalidInputException;
import nars.language.Tense;
import nars.operator.Operator;
import nars.operator.io.Echo;


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
public class NAR implements Runnable, TaskSource {

    /**
     * The information about the version and date of the project.
     */
    public static final String VERSION = "Open-NARS v1.6.4";
    
    /**
     * The project web sites.
     */
    public static final String WEBSITE =
              " Open-NARS website:  http://code.google.com/p/open-nars/ \n"
            + "      NARS website:  http://sites.google.com/site/narswang/ \n" +
              "    Github website:  http://github.com/opennars/ \n" + 
            "    IRC:  http://webchat.freenode.net/?channels=nars \n";    ;    


    
    private Thread thread = null;
    long minCyclePeriodMS;
    
    /**
     * The name of the reasoner
     */
    protected String name;
    /**
     * The memory of the reasoner
     */
    public final Memory memory;
    public final Param param;
    
    
    /** The addInput channels of the reasoner     */
    protected final List<InPort<Object,AbstractTask>> inputChannels;
    
    /** pending input and output channels to add on the next cycle. */
    private final List<InPort<Object,AbstractTask>> newInputChannels;

 
    
    

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
    
    
    private boolean inputting = true;
    private boolean threadYield;
    
    private int inputSelected = 0; //counter for the current selected input channel
    private boolean ioChanged;
    
    private int cyclesPerFrame = 1; //how many memory cycles to execute in one NAR cycle
    
    
    protected NAR(final Memory m) {
        this.memory = m;        
        this.param = m.param;
        
        //needs to be concurrent in case we change this while running
        inputChannels = new ArrayList();
        newInputChannels = new CopyOnWriteArrayList();

    }

    /**
     * Reset the system with an empty memory and reset clock. Called locally and
     * from {@link NARControls}.
     */     
    public void reset() {
        int numInputs = inputChannels.size();
        for (int i = 0; i < numInputs; i++) {
            InPort port = inputChannels.get(i);
            port.input.finished(true);
        }
        inputChannels.clear();        
        newInputChannels.clear();
        //newOutputChannels.clear();
        //oldOutputChannels.clear();
        ioChanged = false;
        memory.reset();        
    }

    /**
     * Convenience method for creating a TextInput and adding as Input Channel.
     * Generally the text will consist of Task's to be parsed in Narsese, but
     * may contain other commands recognized by the system. The creationTime
     * will be set to the current memory cycle time, but may be processed by
     * memory later according to the length of the input queue.
     */
    public TextInput addInput(final String text) {
        
        return addInput(text, text.contains("\n") ? -1 : time());
    }
    
    /** add text input at a specific time, which can be set to current time (regardless of when it will reach the memory), backdated, or forward dated */
    public TextInput addInput(final String text, long creationTime) {
        final TextInput i = new TextInput(text);
        
        ObjectTaskInPort ip = addInput(i);
        
        if (creationTime!=-1)
            ip.setCreationTimeOverride(creationTime);
        
        return i;
    }
    
    public NAR addInput(final String taskText, float frequency, float confidence) throws InvalidInputException {
        return addInput(-1, -1, taskText, frequency, confidence);
    }
    
   public NAR believe(float pri, float dur, String termString, Tense tense, float freq, float conf) throws InvalidInputException {
        
        return addInput(memory.newTask(new Narsese(this).parseTerm(termString),
                Symbols.JUDGMENT_MARK, freq, conf, pri, dur, tense));
    }

   
    public NAR believe(String termString, Tense tense, float freq, float conf) throws InvalidInputException {
        
        return believe(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, termString, tense, freq, conf);
    }

    
   /** gets a concept if it exists, or returns null if it does not */
    public Concept concept(String concept) throws InvalidInputException {
        return memory.concept(new Narsese(this).parseTerm(concept));
    }
    
    public NAR ask(String termString) throws InvalidInputException {
        return ask(termString, null);
    }
    
    public NAR ask(String termString, Answered answered) throws InvalidInputException {
        
        Task t;
        addInput(
                t = new Task(
                        new Sentence(
                                new Narsese(this).parseTerm(termString),
                                Symbols.QUESTION_MARK, 
                                null, 
                                new Stamp(memory, Tense.Eternal)), 
                        new BudgetValue(
                                Parameters.DEFAULT_QUESTION_PRIORITY, 
                                Parameters.DEFAULT_QUESTION_DURABILITY, 
                                1))
        );
        
        if (answered!=null) {
            answered.start(t, this);
        }
        return this;
        
    }
    
    public NAR addInput(final Sentence sentence) throws InvalidInputException {
        
        //TODO use correct default values depending on sentence punctuation
        float priority = 
                Parameters.DEFAULT_JUDGMENT_PRIORITY;
        float durability = 
                Parameters.DEFAULT_JUDGMENT_DURABILITY;
                
        return addInput(                
                new Task(sentence, new 
                    BudgetValue(priority, durability, sentence.truth))
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
        TaskInput ti = new TaskInput(t);
        addInput(ti);
        return this;
    }

  
    
    /** attach event handler */
    public void on(Class c, EventObserver o) {
        memory.event.on(c, o);
    }
    
    /** remove event handler */
    public void off(Class c, EventObserver o) {
        memory.event.on(c, o);
    }
    
    /** set an event handler. useful for multiple events. */
    public void event(EventObserver e, boolean enabled, Class... events) {
        memory.event.set(e, enabled, events);
    }

    
    public int getCyclesPerFrame() {
        return cyclesPerFrame;
    }
    
    final class ObjectTaskInPort extends InPort<Object,AbstractTask> {
        private long creationTime = -1;

        public ObjectTaskInPort(Input input, Buffer buffer, float initialAttention) {
            super(input, buffer, initialAttention);
        }

        @Override public void perceive(final Object x) {
            memory.emit(Perceive.class, this, x);
        }
        
        @Override
        public Iterator<AbstractTask> postprocess(final Iterator<AbstractTask> at) {
            try {
                if (creationTime == -1)
                    return at;
                else {                    
                    //Process tasks with overrides                    
                    final int duration = memory.param.duration.get();
                    
                    return Iterators.filter(at, new Predicate<AbstractTask>() {
                        @Override public boolean apply(AbstractTask at) {
                            if (at instanceof Task) {
                                Task t = (Task)at;
                                if (t.sentence!=null)
                                    if (t.sentence.stamp!=null) {
                                        t.sentence.stamp.setCreationTime(creationTime, duration);
                                    }
                            }
                            return true;
                        }                        
                    });
                }
            }
            catch (Throwable e) {
                if (Parameters.DEBUG)
                    throw e;
                return singletonIterator(new Echo(ERR.class, e));
            }
        }

        /** sets the 'creationTime' override for new Tasks.  sets the stamp to a particular
         * value upon its exit from the queue.          */
        public void setCreationTimeOverride(final long creationTime) {
            this.creationTime = creationTime;
        }

    }
    
    /** Adds an input channel.  Will remain added until it closes or it is explicitly removed. */
    public ObjectTaskInPort addInput(final Input channel) {
        ObjectTaskInPort i = new ObjectTaskInPort(channel, new FIFO(), 1.0f);
               
        try {
            i.update();
            newInputChannels.add(i);
        } catch (IOException ex) {  
            if (Parameters.DEBUG)
                throw new RuntimeException(ex.toString());
            emit(ERR.class, ex);
        }
        
        ioChanged = true;
        
        if (!running)
            updatePorts();

        return i;
    }

//    /** Explicitly removes an input channel and notifies it, via Input.finished(true) that is has been removed */
//    public Input removeInput(Input channel) {
//        inputChannels.remove(channel);
//        channel.finished(true);
//        return channel;
//    }


    public void addPlugin(Plugin p) {
        if (p instanceof Operator) {
            memory.addOperator((Operator)p);
        }
        if (p instanceof DerivationFilter) {
            param.defaultDerivationFilters.add((DerivationFilter)p);
        }
        PluginState ps = new PluginState(p);
        plugins.add(ps);
        emit(Events.PluginsChange.class, p, null);
    }
    
    public void removePlugin(PluginState ps) {
        if (plugins.remove(ps)) {
            Plugin p = ps.plugin;
            if (p instanceof Operator) {
                memory.removeOperator((Operator)p);
            }
            if (p instanceof DerivationFilter) {
                param.defaultDerivationFilters.remove((DerivationFilter)p);
            }
            ps.setEnabled(false);
            emit(Events.PluginsChange.class, null, p);
        }
    }
    
    public List<PluginState> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    
    @Deprecated public void start(final long minCyclePeriodMS, int cyclesPerFrame) {
        this.minCyclePeriodMS = minCyclePeriodMS;
        this.cyclesPerFrame = cyclesPerFrame;
        if (thread == null) {
            thread = new Thread(this, "Inference");
            thread.start();
        }
        running = true;        
    }
    
    /**
     * Repeatedly execute NARS working cycle in a new thread with Iterative timing.
     * 
     * @param minCyclePeriodMS minimum cycle period (milliseconds).
     */    
    public void start(final long minCyclePeriodMS) {
        start(minCyclePeriodMS, 1);
    }


    /** Can be used to pause/resume input */
    public void setInputting(boolean inputEnabled) {
        this.inputting = inputEnabled;
    }

    
    public EventEmitter event() { return memory.event; }
    
    
    /**
     * Stop the inference process, killing its thread.
     */
    public void stop() {
        if (thread!=null) {
            thread.interrupt();
            thread = null;
        }
        stopped = true;
        running = false;
    }    
    
    /** Execute a fixed number of frames. 
     * may execute more than requested cycles if cyclesPerFrame > 1 */
    public void step(final int frames) {
        /*if (thread!=null) {
            memory.stepLater(cycles);
            return;
        }*/
        
        final boolean wasRunning = running;
        running = true;
        stopped = false;
        for (int f = 0; (f < frames) && (!stopped); f++) {
            frame();
        }
        running = wasRunning;        
    }
    
    /** Execute a fixed number of cycles, then finish any remaining walking steps. */
    public NAR run(int cycles) {
        if (cycles <= 0) return this;
        
        running = true;
        stopped = false;

        updatePorts();

        //clear existing input
        
        long cycleStart = time();
        do {
            step(1);           
        }
        while ((!inputChannels.isEmpty()) && (!stopped));
                   
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
        stopped = false;
        
        while (running && !stopped) {      
            
            frame();
                        
            if (minCyclePeriodMS > 0) {
                try {
                    Thread.sleep(minCyclePeriodMS);
                } catch (InterruptedException e) { }
            }
            else if (threadYield) {
                Thread.yield();
            }
        }
    }
    
    
    private void debugTime() {
        //if (running || stepsQueued > 0 || !finishedInputs) {
            System.out.println("// doTick: "
                    //+ "walkingSteps " + stepsQueued
                    + ", clock " + time());

            System.out.flush();
        //}
    }

    protected void resetPorts() {
        for (InPort<Object, AbstractTask> i : getInPorts()) {
            i.reset();
        }
    }
    
    protected void updatePorts() {
        if (!ioChanged) {
            return;
        }
        
        ioChanged = false;        
        
        if (!newInputChannels.isEmpty()) {
            inputChannels.addAll(newInputChannels);
            newInputChannels.clear();
        }

    }
    
    /**     
     * Processes the next input from each input channel.  Removes channels that have finished.
     * @return whether to finish the reasoner afterward, which is true if any input exists.
     */
    @Override
    public AbstractTask nextTask() {                
        if ((!inputting) || (inputChannels.isEmpty()))
           return null;        
        
        int remainingChannels = inputChannels.size(); //remaining # of channels to poll

        while ((remainingChannels > 0) && (inputChannels.size() > 0)) {
            inputSelected %= inputChannels.size();

            final InPort<Object,AbstractTask> i = inputChannels.get(inputSelected++);
            remainingChannels--;
            
            if (i.finished()) {
                inputChannels.remove(i);
                continue;
            }

            try {
                i.update();
            } catch (IOException ex) {                    
                emit(ERR.class, ex);
            }                

            if (i.hasNext()) {
                AbstractTask task = i.next();
                if (task!=null) {
                    return task;
                }
            }

        }
            
        /** no available inputs */
        return null;
    }

    /** count of how many items are buffered */
    @Override public int getInputItemsBuffered() {
        int total = 0;
        for (final InPort i : inputChannels)
            total += i.getItemsBuffered();
        return total;
    }

    
    public void emit(final Class c, final Object... o) {
        memory.event.emit(c, o);
    }
    
    
    protected void frame() {
        frame(cyclesPerFrame);
    }
    
    /**
     * A frame, consisting of one or more NAR memory cycles
     */
    public void frame(int cycles) {
        
        long timeStart = System.currentTimeMillis();

        emit(FrameStart.class);

        updatePorts();

        try {
            for (int i = 0; i < cycles; i++)
                memory.cycle(this);
        }
        catch (Throwable e) {
            memory.error(e);
        }

        emit(FrameEnd.class);

        long timeEnd = System.currentTimeMillis();

        if (memory.getTiming() == Timing.Real) {
            long frameTime = timeEnd - timeStart;
            final int d = param.duration.get();

            //warn if frame consumed more time than reasoner duration
            if (frameTime > d) {
                emit(ERR.class, 
                        "@" + timeEnd + ": Real-time consumed by frame (" + 
                                frameTime + " ms) exceeds reasoner Duration (" + d + " cycles)" );
            }
        }
    }
    
    protected long getSimulationTimeCyclesPerFrame() {
        return minCyclePeriodMS;
    }

    @Override
    public String toString() {
        return memory.toString();
    }

     /**
     * Get the current time from the clock Called in {@link nars.entity.Stamp}
     *
     * @return The current time
     */
    public long time() {
        return memory.time();
    }
    

    public boolean isRunning() {
        return running;
    }    

    public long getMinCyclePeriodMS() {
        return minCyclePeriodMS;
    }

    /** When b is true, NAR will call Thread.yield each run() iteration that minCyclePeriodMS==0 (no delay). 
     *  This is for improving program responsiveness when NAR is run with no delay.
     */
    public void setThreadYield(boolean b) {
        this.threadYield = b;
    }

    /** stops ad empties all input channels into a receiver. this
        results in no pending input. 
        @return total number of items flushed
        */
    public int flushInput(Output receiver) {
        int total = 0;
        for (InPort c : inputChannels) {
            total += flushInput(c, receiver);
        }
        return total;
    }
    
    /** stops and empties an input channel into a receiver. 
     * this results in no pending input from this channel. */
    public int flushInput(InPort i, EventObserver receiver) {
        int total = 0;
        i.finish();
        
        while (i.hasNext()) {
            receiver.event(IN.class, new Object[] { i.next() });
            total++;
        }        
        
        return total;
    }

    public List<InPort<Object, AbstractTask>> getInPorts() {
        return inputChannels;
    }


    /** create a NAR given the class of a Build.  its default constructor will be used */
    public static NAR build(Class<? extends Build> g) {
        try {
            return new NAR(g.newInstance());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /** normal way to construct a NAR, using a particular Build instance */
    public NAR(Build b) {
        this(b.newMemory(b.param));
        b.init(this);
    }
    
}