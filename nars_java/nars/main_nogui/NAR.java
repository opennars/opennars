package nars.main_nogui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import nars.entity.Stamp;
import nars.entity.Task;
import nars.gui.MainWindow;
import nars.io.ExperienceReader;
import nars.io.InputChannel;
import nars.io.OutputChannel;
import nars.io.StringParser;
import nars.io.Symbols;
import nars.storage.Memory;

/**
 * Non-Axiomatic Reasoner (core class)
 * <p>
 * An instantation of a NARS logic processor, useful for batch functionality; 
*/
public class NAR implements Runnable {

    private Thread narsThread = null;
    long minTickPeriodMS;
    
    /**
     * global DEBUG print switch
     */
    public static final boolean DEBUG = false;
    /**
     * The name of the reasoner
     */
    protected String name;
    /**
     * The memory of the reasoner
     */
    protected Memory memory;
    /**
     * The input channels of the reasoner
     */
    protected List<InputChannel> inputChannels;
    private List<InputChannel> closedInputChannels;

    /**
     * The output channels of the reasoner
     */
    protected List<OutputChannel> outputChannels;
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
     * The remaining number of steps to be carried out (walk mode)
     */
    private int walkingSteps;
    /**
     * determines the end of {@link NARSBatch} program (set but not accessed in
     * this class)
     */
    private boolean finishedInputs;
    /**
     * System clock - number of cycles since last output
     */
    private long timer;
    private AtomicInteger silenceValue = new AtomicInteger(Parameters.SILENT_LEVEL);

    public NAR() {
        memory = new Memory(this);
        
        //needs to be concurrent in case NARS makes changes to the channels while running
        inputChannels = new CopyOnWriteArrayList<>();
        outputChannels = new CopyOnWriteArrayList<>();
        closedInputChannels = new CopyOnWriteArrayList<>();
    }

    /**
     * Reset the system with an empty memory and reset clock. Called locally and
     * from {@link MainWindow}.
     */
    final String resetMessage = " OUT: reset";
    
    public void reset() {
            
        walkingSteps = 0;
        clock = 0;
        memory.init();
        Stamp.init();
        
        output(resetMessage);
                
    }

    public Memory getMemory() {
        return memory;
    }

    public void addInputChannel(InputChannel channel) {
        inputChannels.add(channel);
    }

    public void removeInputChannel(InputChannel channel) {
        inputChannels.remove(channel);
    }

    public void addOutputChannel(OutputChannel channel) {
        outputChannels.add(channel);
    }

    public void removeOutputChannel(OutputChannel channel) {
        outputChannels.remove(channel);
    }

    /**
     * Get the current time from the clock Called in {@link nars.entity.Stamp}
     *
     * @return The current time
     */
    public long getTime() {
        return clock;
    }


    /**
     * Will carry the inference process for a certain number of steps
     *
     * @param n The number of inference steps to be carried
     */
    public void walk(int n) {
        output(" OUT: thinking " + n + (n > 1 ? " cycles" : " cycle"));
        walkingSteps = n;
    }

    
    /**
     * Repeatedly execute NARS working cycle. This method is called when the
     * Runnable's thread is started.
     */
    
    public void start(long minTickPeriodMS) {
        if (narsThread == null) {
            this.minTickPeriodMS = minTickPeriodMS;
            narsThread = new Thread(this, "Inference");
            narsThread.start();
            running = true;
        }
        
    }
    
    /**
     * Will stop the inference process
     */
    public void stop() {
        if (narsThread!=null) {
            narsThread.interrupt();
            narsThread = null;
        }
        running = false;
    }    
    
    @Override public void run() {
        //final Thread thisThread = Thread.currentThread();
        
        while (/*(narsThread == thisThread) && */ running) {
            try {
                // NOTE: try/catch not necessary for input errors , but may be useful for other troubles
                tick();
            } catch (RuntimeException re) {
                String errorMsg = "ERROR: " + re;
                output(errorMsg);
                System.err.println(errorMsg);
                //re.printStackTrace();
            }
            catch (Exception e) {
                System.err.println("run: " + e);
                e.printStackTrace();
            }
            if (minTickPeriodMS > 0) {
                try {
                    Thread.sleep(minTickPeriodMS);
                } catch (InterruptedException e) {            }
                Thread.yield();
            }
        }
    }
    
    

    /**
     * A clock tick. Run one working workCycle or read input. Called from NARS
     * only.
     */
    public void tick() {
        if (DEBUG) {
            if (running || walkingSteps > 0 || !finishedInputs) {
                System.out.println("// doTick: "
                        + "walkingSteps " + walkingSteps
                        + ", clock " + clock
                        + ", getTimer " + getTimer()
                        + "\n//    memory.getExportStrings() " + memory.getExportStrings());
                System.out.flush();
            }
        }
                
        if (walkingSteps == 0) {
            boolean reasonerShouldRun = false;

            for (InputChannel c : closedInputChannels) {
                inputChannels.remove(c);
            }
            closedInputChannels.clear();


            for (InputChannel channelIn : inputChannels) {
                reasonerShouldRun = reasonerShouldRun || channelIn.nextInput();  
                if (channelIn.isClosed())
                    closedInputChannels.add(channelIn);
            }
            finishedInputs = !reasonerShouldRun;
        }
                
        // forward to output Channels
        final ArrayList<String> output = memory.getExportStrings();
        if (!output.isEmpty()) {
            output(output);
            output.clear();	// this will trigger display the current value of timer in Memory.report()
        }

        if (running || walkingSteps > 0) {
            clock++;
            tickTimer();
            memory.workCycle(clock);
            if (walkingSteps > 0) {
                walkingSteps--;
            }
        }

                
    }
    
    public void output(final ArrayList<String> output) {
        for (final OutputChannel channelOut : outputChannels)
            channelOut.nextOutput(output);
    }
    public void output(final String o) {       
        final ArrayList<String> l = new ArrayList();
        l.add(o);
        output(l);
    }


    /**
     * determines the end of {@link NARSBatch} program
     */
    public boolean isFinishedInputs() {
        return finishedInputs;
    }

    /**
     * To process a line of input text
     *
     * @param text
     * @return whether to continue processing in this input batch, or interrupt (false)
     */
    public boolean textInputLine(String text) throws StringParser.InvalidInputException  {
        if (text.isEmpty()) {
            return true;
        }
        char c = text.charAt(0);
        if (c == Symbols.RESET_MARK) {
            reset();
            return false;
        }
        else if (c == Symbols.URL_INCLUDE_MARK) {            
            includeURL(text.substring(1));
        }
        else if (c == Symbols.ECHO_MARK) {
            String echoString = text.substring(1);
            output('\"' + echoString + '\"');
        }
        else if (c != Symbols.COMMENT_MARK) {
            // read NARS language or an integer : TODO duplicated code
            try {
                int i = Integer.parseInt(text);
                walk(i);
            } catch (NumberFormatException e) {
                Task task = StringParser.parseExperience(new StringBuffer(text), memory, clock);
                if (task != null) {
                    memory.report(task.getSentence(), true);    // report input
                    memory.inputTask(task);
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return memory.toString();
    }

    /**
     * Report Silence Level
     */
    public AtomicInteger getSilenceValue() {
        return silenceValue;
    }

    /**
     * To get the timer value and then to
     * reset it by {@link #initTimer()};
     * plays the same role as {@link nars.gui.MainWindow#updateTimer()} 
     *
     * @return The previous timer value
     */
    public long updateTimer() {
        long i = getTimer();
        initTimer();
        return i;
    }

    /**
     * Reset timer;
     * plays the same role as {@link nars.gui.MainWindow#initTimer()} 
     */
    public void initTimer() {
        setTimer(0);
    }

    /**
     * Update timer
     */
    public void tickTimer() {
        setTimer(getTimer() + 1);
    }

    /** @return System clock : number of cycles since last output */
    public long getTimer() {
        return timer;
    }

    /** set System clock : number of cycles since last output */
    private void setTimer(long timer) {
        this.timer = timer;
    }

    public boolean isRunning() {
        return running;
    }    

    public void includeURL(final String url) {
        try {
            URL u = new URL(url);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(u.openStream()));            
            addInputChannel(new ExperienceReader(this, in));
        } catch (Exception ex) {
            output("includeURL: " + ex.toString());
        }
    }
    
}