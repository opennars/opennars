package nars.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.HashMap;
import java.util.Random;
import nars.entity.Sentence;

import nars.entity.Stamp;
import nars.gui.NARControls;
import nars.io.Input;
import nars.io.Output;
import nars.io.TextInput;
import nars.io.TextPerception;
import nars.storage.Memory;

/**
 * Non-Axiomatic Reasoner (core class)
 * <p>
 * An instantation of a NARS logic processor, useful for batch functionality; 
*/
public class NAR implements Runnable, Output {

    
    private Thread thread = null;
    long minTickPeriodMS;
    
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
    /**
     * The addInput channels of the reasoner
     */
    protected final List<Input> inputChannels;
    
    private final List<Input> deadInputs = new ArrayList();

    /**
     * The output channels of the reasoner
     */
    protected List<Output> outputChannels;
    
        
    /**
     * System clock, relatively defined to guarantee the repeatability of
     * behaviors
     */
    private long clock;
    /**
     * Flag for running continuously
     */
    private boolean running = false;
    /**
     * The remaining number of steps to be carried out (stepLater mode)
     */
    private int stepsQueued;
    
    /**
     * determines the end of {@link NARSBatch} program (set but not accessed in
     * this class)
     */
    private boolean finishedInputs;

    
    /**arbitrary data associated with this particular NAR instance can be stored here */
    public final HashMap data = new HashMap();
    
    
    /**
     *  Design parameters which do not change at runtime
     */
    public final NARBuilder config;
    
    /**
     *  Parameters which can be changed at runtime
    */
    public final NARParams param;
    
    

    private final TextPerception textPerception;
    
    private boolean working = true;
    private boolean inputting = true;
    private boolean threadYield;

    @Deprecated public NAR() {
        this(new DefaultNARBuilder());
    }
    
    protected NAR(NARBuilder p) {
        this.config = p;
        this.param = p.newInitialParams();
        
        memory = new Memory(this);
        textPerception = new TextPerception(this);
        
        //needs to be concurrent in case NARS makes changes to the channels while running
        inputChannels = new CopyOnWriteArrayList<>();
        outputChannels = new CopyOnWriteArrayList<>();
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
        
        stepsQueued = 0;
        clock = 0;
        memory.init();
        Stamp.init();
        
        output(OUT.class, "reset");
                
    }

    /** Convenience method for creating a TextInput and adding as Input Channel */
    public TextInput addInput(final String text) {
        final TextInput i = new TextInput(this, text);
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
     * Queue additional tick()'s to the inference process.
     *
     * @param cycles The number of inference steps
     */
    public void stepLater(final int cycles) {
        stepsQueued += cycles;
    }
    
    
    /**
     * Repeatedly execute NARS working cycle in a new thread.
     * 
     * @param minTickPeriodMS minimum tick period (milliseconds).
     */    
    public void start(final long minTickPeriodMS) {
        if (thread == null) {
            thread = new Thread(this, "Inference");
            thread.start();
        }        
        this.minTickPeriodMS = minTickPeriodMS;
        running = true;
    }
    
    /** Can be used to pause/resume inference, without killing the running thread. */
    public void setWorking(boolean b) {
        this.working = b;
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
            stepLater(cycles);
            return;
        }
        running = true;
        for (int i = 0; i < cycles; i++) {
            tick();
        }
        running = false;
    }
    
    /** Execute a fixed number of cycles, then finish any remaining walking steps. */
    public void finish(final int cycles) {
        finish(cycles, false);
    }
    
    /** Run a fixed number of cycles, then finish any remaining walking steps.  Debug parameter sets debug.*/
    public void finish(final int cycles, final boolean debug) {
        DEBUG = debug; 
        step(cycles);
        running = true;
        while ((stepsQueued!=0) && (!inputChannels.isEmpty())) {
            tick();
        } 
        running = false;
    }
    

    /** Main loop executed by the Thread.  Should not be called directly. */
    @Override public void run() {
        
        while (running) {            
            try {
                tick();
            } catch (RuntimeException re) {                
                output(ERR.class, re);
                if (DEBUG) {
                    System.err.println(re);                
                    re.printStackTrace();
                }
            }
            catch (Exception e) {
                output(ERR.class, e);
                
                System.err.println(e);
                e.printStackTrace();
            }
            
            if (minTickPeriodMS > 0) {
                try {
                    Thread.sleep(minTickPeriodMS);
                } catch (InterruptedException e) { }
            }
            else if (threadYield) {
                Thread.yield();
            }
        }
    }
    
    
    private void debugTime() {
        if (running || stepsQueued > 0 || !finishedInputs) {
            System.out.println("// doTick: "
                    + "walkingSteps " + stepsQueued
                    + ", clock " + clock);

            System.out.flush();
        }
    }

    /**     
     * Processes the next input from each input channel.  Removes channels that have finished.
     * @return whether to finish the reasoner afterward, which is true if any input exists.
     */
    protected boolean processInput() {
        boolean inputPerceived = false;
        
        if ((inputting) && (stepsQueued == 0) && (!inputChannels.isEmpty())) {
            
            for (int j = 0; j < inputChannels.size(); j++) {                
                final Input i = inputChannels.get(j);
                
                if (i.finished(false)) {
                    deadInputs.add(i);
                }
                else {
                    Object o = i.next();
                    if (o!=null) {
                        perceive(i, o);
                        inputPerceived = true;                        
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
            textPerception.perceive(i, s.getContent().toString() + s.getPunctuation() + " " + s.getTruth().toString());
        }
        else {
            output(ERR.class, "Unrecognized input (" + o.getClass() + "): " + o);
        }
    }
    
    /** Execute a workCycle */
    protected void workCycle() {
        if ((working) && ((running || (stepsQueued > 0)))) {
            clock++;
            try {
                memory.workCycle(clock);
            }
            catch (RuntimeException e) {
                output(ERR.class, e);
                if (DEBUG)
                    e.printStackTrace();
            }
            if (stepsQueued > 0) {
                stepsQueued--;
            }
        }        
    }

    /**
     * A clock tick, consisting of 1) processing input, 2) one workCycle.
     */
    private void tick() {
        if (DEBUG) {
            debugTime();            
        }
        
        processInput();
        workCycle();                
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
        return clock;
    }

    public boolean isRunning() {
        return running;
    }    

    public long getMinTickPeriodMS() {
        return minTickPeriodMS;
    }

    public boolean isWorking() {
        return working;
    }

    /** When b is true, NAR will call Thread.yield each run() iteration that minTickPeriodMS==0 (no delay). 
     *  This is for improving program responsiveness when NAR is run with no delay.
     */
    public void setThreadYield(boolean b) {
        this.threadYield = b;
    }


    public static void resetStatics() {
        Memory.randomNumber = new Random(1);
        Stamp.currentSerial = 0;
    }
    
}