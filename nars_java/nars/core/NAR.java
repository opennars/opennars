package nars.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import nars.entity.Sentence;
import nars.gui.NARControls;
import nars.io.DefaultTextPerception;
import nars.io.Input;
import nars.io.Output;
import nars.io.TextInput;
import nars.io.TextPerception;
import nars.storage.Memory;


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
public class NAR implements Runnable, Output {

    
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
    protected final List<Input> inputChannels;
    
    /** Stores a list of input channels to be removed. */
    private final List<Input> deadInputs = new ArrayList();

    /** The output channels of the reasoner     */
    protected List<Output> outputChannels;
    
        
    
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
    
    

    private final TextPerception textPerception;
    
    
    private boolean inputting = true;
    private boolean threadYield;

    protected NAR(Memory m) {
        this.memory = m;
        
        m.setOutput(this);
        
        textPerception = new DefaultTextPerception(this);
        
        //needs to be concurrent in case NARS makes changes to the channels while running
        inputChannels = new CopyOnWriteArrayList<Input>();
        outputChannels = new CopyOnWriteArrayList<Output>();
    }

    /**
     * Reset the system with an empty memory and reset clock. Called locally and
     * from {@link NARControls}.
     */     
    public void reset() {
            
        for (Input i : inputChannels) {
            i.finished(true);
        }
        inputChannels.clear();               
        
        memory.reset();
        
        if (memory.getRecorder().isActive()) {
            memory.getRecorder().append("Memory Reset");
        }
                        
    }

    /** Convenience method for creating a TextInput and adding as Input Channel */
    public TextInput addInput(final String text) {
        final TextInput i = new TextInput(text);
        addInput(i);
        return i;
    }
    
    /** Adds an input channel.  Will remain added until it closes or it is explicitly removed. */
    public void addInput(Input channel) {
        inputChannels.add(channel);        
    }

    /** Explicitly removes an input channel and notifies it, via Input.finished(true) that is has been removed */
    public void removeInput(Input channel) {
        inputChannels.remove(channel);
        channel.finished(true);
    }

    /** Adds an output channel */
    public void addOutput(Output channel) {
        outputChannels.add(channel);
    }

    /** Removes an output channel */
    public void removeOutput(Output channel) {
        outputChannels.remove(channel);
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

    /**     
     * Processes the next input from each input channel.  Removes channels that have finished.
     * @return whether to finish the reasoner afterward, which is true if any input exists.
     */
    protected boolean cycleInput() {
        boolean inputPerceived = false;
        
        if ((inputting) && (!inputChannels.isEmpty())) {
            
            for (int j = 0; j < inputChannels.size(); j++) {                
                final Input i = inputChannels.get(j);
                
                if (i.finished(false)) {
                    deadInputs.add(i);
                }
                else {
                    try {
                        Object o = i.next();
                        if (o!=null) {
                            perceive(i, o);
                            inputPerceived = true;                        
                        }
                    }
                    catch (Exception e) {
                        output(ERR.class, e);
                        i.finished(true);
                        deadInputs.add(i);                        
                    }
                }
            }            
            
            inputChannels.removeAll(deadInputs);
            deadInputs.clear();
                    
        }    
        return inputPerceived;
    }
    


    /* Perceive an input object by calling an appropriate perception system according to the object type. */
    protected void perceive(final Input i, final Object o) {
        if (o instanceof String) {
            textPerception.perceive(i, (String)o);
        }
        else if (o instanceof Sentence) {
            //TEMPORARY
            Sentence s = (Sentence)o;
            textPerception.perceive(i, s.content.toString() + s.punctuation + " " + s.truth.toString());
        }
        else {
            output(ERR.class, "Unrecognized input (" + o.getClass() + "): " + o);
        }
    }
    

    /**
     * A clock tick, consisting of 1) processing input, 2) one cycleMemory.
     */
    private void cycle() {
        if (DEBUG) {
            debugTime();            
        }
        
        try {
            if (memory.getCyclesQueued()==0)
                cycleInput();

            memory.cycle();
        }
        catch (Exception e) {
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


    public static void resetStatics() {
        Memory.randomNumber = new Random(1);
    }

    public Param param() {
        return memory.param;
    }
    
}