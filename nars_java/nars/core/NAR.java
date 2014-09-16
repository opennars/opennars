package nars.core;

import static com.google.common.collect.Iterators.singletonIterator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import nars.core.Memory.TaskSource;
import nars.entity.AbstractTask;
import nars.gui.NARControls;
import nars.io.InPort;
import nars.io.Input;
import nars.io.Output;
import nars.io.TextInput;
import nars.io.buffer.Buffer;
import nars.io.buffer.FIFO;
import nars.io.narsese.Narsese;
import nars.language.Term;
import nars.operator.io.Speak;


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
public class NAR implements Runnable, Output, TaskSource {

    
    private Thread thread = null;
    long minCyclePeriodMS;
    
    /**
     * global DEBUG print switch
     */
    public static boolean DEBUG = false;
    /**
     * The name of the reasoner
     */
    protected String name;
    /**
     * The memory of the reasoner
     */
    public final Memory memory;
    
    
    /** The addInput channels of the reasoner     */
    protected final List<InPort<Object,AbstractTask>> inputChannels;
    
    /** The output channels of the reasoner     */
    protected List<Output> outputChannels;
    
    /** pending input and output channels to add on the next cycle. */
    private final List<InPort> newInputChannels;
    private final List<Output> newOutputChannels;
    
    /** pending niput and output channels to remove on the next cycle */
    //protected final List<InPort> oldInputChannels;
    protected final List<Output> oldOutputChannels;

    
    /**
     * Flag for running continuously
     */
    private boolean running = false;
    
    
    
    /**
     * determines the end of {@link NARSBatch} program (set but not accessed in
     * this class)
     */
    private boolean finishedInputs;

    
    /**arbitrary data associated with this particular NAR instance can be stored here */
    public final HashMap data = new HashMap();
    

    /**
     *  Parameters which can be changed at runtime
    */
    //public final Param param;
    
    

    public final Perception perception;
    
    
    private boolean inputting = true;
    private boolean threadYield;
    
    private int inputSelected = 0; //counter for the current selected input channel
    private boolean ioChanged;
    

    public NAR(Memory m, Perception p) {
        this.memory = m;
        this.perception = p;
        
        m.setOutput(this);                
        
        //needs to be concurrent in case we change this while running
        inputChannels = new ArrayList();
        newInputChannels = new CopyOnWriteArrayList();
        //oldInputChannels = new ArrayList();
        outputChannels = new ArrayList();
        newOutputChannels = new CopyOnWriteArrayList();
        oldOutputChannels = new CopyOnWriteArrayList();
    
        this.perception.start(this);

    }

    /**
     * Reset the system with an empty memory and reset clock. Called locally and
     * from {@link NARControls}.
     */     
    public synchronized void reset() {
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

    /** Convenience method for creating a TextInput and adding as Input Channel */
    public TextInput addInput(final String text) {
        final TextInput i = new TextInput(text);
        addInput(i);
        return i;
    }
    
    final class ObjectTaskInPort extends InPort<Object,AbstractTask> {

        public ObjectTaskInPort(Input input, Buffer buffer, float initialAttention) {
            super(input, buffer, initialAttention);
        }

        @Override
        public Iterator<AbstractTask> process(final Object x) {
            try {
                return perception.perceive(x);
            }
            catch (Throwable e) {
                return singletonIterator(new Speak(ERR.class, e));
            }
        }
    }
    
    /** Adds an input channel.  Will remain added until it closes or it is explicitly removed. */
    public Input addInput(final Input channel) {
        InPort i = new ObjectTaskInPort(channel, new FIFO(), 1.0f);
               
        try {
            i.update();
            newInputChannels.add(i);
        } catch (IOException ex) {                    
            output(ERR.class, ex);
        }
        ioChanged = true;
        return channel;
    }

//    /** Explicitly removes an input channel and notifies it, via Input.finished(true) that is has been removed */
//    public Input removeInput(Input channel) {
//        inputChannels.remove(channel);
//        channel.finished(true);
//        return channel;
//    }

    /** Adds an output channel */
    public Output addOutput(final Output channel) {
        newOutputChannels.add(channel);
        ioChanged = true;
        return channel;
    }

    /** Removes an output channel */
    public Output removeOutput(final Output channel) {
        oldOutputChannels.remove(channel);
        ioChanged = true;
        return channel;
    }

    
    
    
    /**
     * Repeatedly execute NARS working cycle in a new thread.
     * 
     * @param minCyclePeriodMS minimum cycle period (milliseconds).
     */    
    public void start(final long minCyclePeriodMS) {
        if (thread == null) {
            thread = new Thread(this, "Inference");
            thread.start();
        }        
        this.minCyclePeriodMS = minCyclePeriodMS;
        running = true;
    }


    /** Can be used to pause/resume input */
    public void setInputting(boolean inputEnabled) {
        this.inputting = inputEnabled;
    }

    
    
    /**
     * Stop the inference process, killing its thread.
     */
    public void stop() {
        if (thread!=null) {
            thread.interrupt();
            thread = null;
        }
        running = false;
    }    
    
    /** Execute a fixed number of cycles. */
    public void step(final int cycles) {
        if (thread!=null) {
            memory.stepLater(cycles);
            return;
        }
        
        final boolean wasRunning = running;
        running = true;
        for (int i = 0; i < cycles; i++) {
            cycle();            
        }
        running = wasRunning;
    }
    
    /** Execute a fixed number of cycles, then finish any remaining walking steps. */
    public void finish(final int cycles) {
        finish(cycles, false);
    }
    
    /** Run a fixed number of cycles, then finish any remaining walking steps.  Debug parameter sets debug.*/
    public void finish(final int cycles, final boolean debug) {
        DEBUG = debug; 
        running = true;

        updatePorts();
        
        //clear input
        while (!inputChannels.isEmpty()) {
            step(1);
        }
        
        
        //queue additional cycles
        memory.stepLater(cycles);
        
        //finish all remaining cycles
        while (memory.getCyclesQueued() > 0) {
            step(1);
        }
        running = false;
    }
    

    /** Main loop executed by the Thread.  Should not be called directly. */
    @Override public void run() {
        
        while (running) {      
            
            cycle();
                        
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
                    + ", clock " + getTime());

            System.out.flush();
        //}
    }

    protected void updatePorts() {
        if (!ioChanged) {
            return;
        }
        
        ioChanged = false;        
        
        if (!newInputChannels.isEmpty()) {
            for (final InPort n : newInputChannels)
                inputChannels.add(n);
            newInputChannels.clear();
        }
        if (!oldOutputChannels.isEmpty()) {
            for (final Output n : oldOutputChannels)
                outputChannels.remove(n);
            oldOutputChannels.clear();
        }
        if (!newOutputChannels.isEmpty()) {
            for (final Output n : newOutputChannels)
                outputChannels.add(n);
            newOutputChannels.clear();
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
                output(ERR.class, ex);
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


    /**
     * A clock tick, consisting of 1) processing input, 2) one cycleMemory.
     */
    private void cycle() {
        if (DEBUG) {
            debugTime();            
        }        

        updatePorts();
        
        try {
            memory.cycle(this);
        }
        catch (Throwable e) {
            output(ERR.class, e);

            System.err.println(e);
            e.printStackTrace();
        }        
        
    }
    
    
    /**
     * Outputs an object to the output channels, via a specific Channel that signifying 
     * the mode of this output. (IN, OUT, ERR, etc..)
     * 
     * @param channel
     * @param o 
     */
    @Override
    public void output(final Class channel, final Object o) {
        
        updatePorts();
        
//        if (o instanceof Sentence) {
//            System.err.println("output should receive Task, not a Sentence");
//            new Exception().printStackTrace();;
//        }        
//        System.out.println(o.getClass().getSimpleName());
        for (int i = 0; i < outputChannels.size(); i++)        
            outputChannels.get(i).output(channel, o);
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
    public long getTime() {
        return memory.getTime();
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



    public Param param() {
        return memory.param;
    }
    
    /** parses and returns a Term from a string; or null if parsing error */
    public Term term(final String s) {        
        try {
            return perception.getText().narsese.parseTerm(s);
        } catch (Narsese.InvalidInputException ex) {
            output(ERR.class, ex);
        }
        return null;
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
    public int flushInput(InPort i, Output receiver) {
        int total = 0;
        i.finish();
        
        while (i.hasNext()) {
            receiver.output(IN.class, i.next());
            total++;
        }        
        
        return total;
    }

    public List<InPort<Object, AbstractTask>> getInPorts() {
        return inputChannels;
    }
}