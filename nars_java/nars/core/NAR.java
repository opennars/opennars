package nars.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import nars.entity.AbstractTask;
import nars.gui.NARControls;
import nars.io.InPort;
import nars.io.Input;
import nars.io.Output;
import nars.io.TextInput;
import nars.io.TextPerception;
import nars.io.buffer.FIFO;
import nars.language.Term;
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
    protected final List<InPort> inputChannels;
    
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
    
    

    private final Perception perception;
    
    
    private boolean inputting = true;
    private boolean threadYield;
    
    private int inputSelected = 0; //counter for the current selected input channel

    protected NAR(Memory m, Perception p) {
        this.memory = m;
        this.perception = p;
        
        m.setOutput(this);                
        
        //needs to be concurrent in case NARS makes changes to the channels while running
        inputChannels = new CopyOnWriteArrayList<>();
        outputChannels = new CopyOnWriteArrayList<>();
    }

    /**
     * Reset the system with an empty memory and reset clock. Called locally and
     * from {@link NARControls}.
     */     
    public void reset() {
            
        for (InPort port : inputChannels) {
            port.input.finished(true);
        }
        inputChannels.clear();               
        
        memory.reset();
        
    }

    /** Convenience method for creating a TextInput and adding as Input Channel */
    public TextInput addInput(final String text) {
        final TextInput i = new TextInput(text);
        addInput(i);
        return i;
    }
    
    /** Adds an input channel.  Will remain added until it closes or it is explicitly removed. */
    public Input addInput(Input channel) {
        InPort i = new InPort(perception, channel, new FIFO(), 1.0f);
               
        try {
            i.update();
            inputChannels.add(i);
        } catch (IOException ex) {                    
            output(ERR.class, ex);
        }
        
        return channel;
    }

//    /** Explicitly removes an input channel and notifies it, via Input.finished(true) that is has been removed */
//    public Input removeInput(Input channel) {
//        inputChannels.remove(channel);
//        channel.finished(true);
//        return channel;
//    }

    /** Adds an output channel */
    public Output addOutput(Output channel) {
        outputChannels.add(channel);
        return channel;
    }

    /** Removes an output channel */
    public Output removeOutput(Output channel) {
        outputChannels.remove(channel);
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
            
            int remainingInputTasks = memory.param.cycleInputTasks.get();
            int remainingChannels = inputChannels.size(); //remaining # of channels to poll
                        
            while ((remainingChannels > 0) && (remainingInputTasks >0) && (inputChannels.size() > 0)) {
                inputSelected %= inputChannels.size();
                 
                final InPort i = inputChannels.get(inputSelected);
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
                    try {
                        Object input = i.next();
                            
                        AbstractTask task = perception.perceive(input);

                        if (task!=null) {
                            memory.inputTask(task);
                            remainingInputTasks--;
                            inputPerceived = true;
                        }
                    }
                    catch (IOException e) {
                        output(ERR.class, e);
                    }
                    
                }
                
                
                inputSelected++;
                
                remainingChannels--;
            }
            
                    
        }    
        return inputPerceived;
    }
    


    

    /**
     * A clock tick, consisting of 1) processing input, 2) one cycleMemory.
     */
    private void cycle() {
        if (DEBUG) {
            debugTime();            
        }
        
        int inputCycles = memory.param.cycleInputTasks.get();
        int memCycles = memory.param.cycleMemory.get();
        
        try {
            
            if (memory.getCyclesQueued()==0) {
                
                for (int i = 0; i < inputCycles; i++)
                    cycleInput();
                
            }

            
            for (int i = 0; i < memCycles; i++) {
                memory.cycle();
            }
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
            return perception.text.parseTerm(s);
        } catch (TextPerception.InvalidInputException ex) {
            output(ERR.class, ex);
        }
        return null;
    }
    
}