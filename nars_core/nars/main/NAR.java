package nars.main;

import java.io.BufferedReader;
import nars.plugin.Plugin;
import nars.storage.Memory;
import nars.io.events.Events;
import nars.io.events.EventEmitter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nars.io.events.EventEmitter.EventObserver;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.language.Interval;
import nars.io.events.AnswerHandler;
import nars.io.events.OutputHandler.ERR;
import nars.io.Symbols;
import nars.io.Narsese;
import nars.io.Narsese.InvalidInputException;
import nars.language.Tense;
import nars.operator.Operator;
import nars.perception.SensoryChannel;
import nars.language.Term;
import nars.storage.LevelBag;
import nars.io.events.Events.CyclesEnd;
import nars.io.events.Events.CyclesStart;


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
public class NAR extends SensoryChannel implements Serializable,Runnable {

    /**
     * The information about the version and date of the project.
     */
    public static final String VERSION = "Open-NARS v1.6.6pre2";

    /**
     * The project web sites.
     */
    public static final String WEBSITE =
            " Open-NARS website:  http://code.google.com/p/open-nars/ \n"
                    + "      NARS website:  http://sites.google.com/site/narswang/ \n" +
                    "    Github website:  http://github.com/opennars/ \n" +
                    "    IRC:  http://webchat.freenode.net/?channels=nars \n";    ;


    Map<Term,SensoryChannel> sensoryChannels = new HashMap<Term,SensoryChannel>();
    public void addSensoryChannel(String term, SensoryChannel channel) {
        try {
            sensoryChannels.put(new Narsese(this).parseTerm(term), channel);
        } catch (InvalidInputException ex) {
            Logger.getLogger(NAR.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void SaveToFile(String name) throws FileNotFoundException, IOException {
        FileOutputStream outStream = new FileOutputStream(name);
        ObjectOutputStream stream = new ObjectOutputStream(outStream);
        stream.writeObject(this);
        outStream.close();
    }
    
    public static NAR LoadFromFile(String name) throws FileNotFoundException, IOException, ClassNotFoundException {
        FileInputStream inStream = new FileInputStream(name);
        ObjectInputStream stream = new ObjectInputStream(inStream);
        NAR ret = (NAR) stream.readObject();
        ret.memory.event = new EventEmitter();
        ret.plugins = new ArrayList<>(); 
        for (Operator o : Operators.get(ret))
            ret.memory.addOperator(o);
        new Plugins().init(ret);
        return ret;
    }

    private Thread thread = null;
    long minCyclePeriodMS;

    /**
     * The name of the reasoner
     */
    protected String name;
    /**
     * The memory of the reasoner
     */
    public Memory memory;
    
    /*NAR Parameters which can be changed during runtime.*/
   public class RuntimeParameters implements Serializable {
       public RuntimeParameters() {    }
       public final Interval.PortableInteger noiseLevel = new Interval.PortableInteger(100);
       public final Interval.AtomicDuration duration = new Interval.AtomicDuration(Parameters.DURATION);
       public final Interval.PortableDouble conceptForgetDurations = new Interval.PortableDouble(Parameters.CONCEPT_FORGET_DURATIONS);
       public final Interval.PortableDouble termLinkForgetDurations = new Interval.PortableDouble(Parameters.TERMLINK_FORGET_DURATIONS);
       public final Interval.PortableDouble taskLinkForgetDurations = new Interval.PortableDouble(Parameters.TASKLINK_FORGET_DURATIONS);
       public final Interval.PortableDouble eventForgetDurations = new Interval.PortableDouble(Parameters.EVENT_FORGET_DURATIONS);
       public final Interval.PortableDouble decisionThreshold = new Interval.PortableDouble(Parameters.DECISION_THRESHOLD);
   }
    public RuntimeParameters param;

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

    protected transient List<PluginState> plugins = new ArrayList<>(); //was CopyOnWriteArrayList

    /** Flag for running continuously  */
    private boolean running = false;
    /** used by stop() to signal that a running loop should be interrupted */
    private boolean stopped = false;
    private boolean threadYield;

    public NAR() {
        Plugins b = new Plugins();
        Memory m = new Memory(new RuntimeParameters(),
                new LevelBag(Parameters.CONCEPT_BAG_LEVELS, Parameters.CONCEPT_BAG_SIZE),
                new LevelBag<>(Parameters.NOVEL_TASK_BAG_LEVELS, Parameters.NOVEL_TASK_BAG_SIZE),
                new LevelBag<>(Parameters.SEQUENCE_BAG_LEVELS, Parameters.SEQUENCE_BAG_SIZE),
                new LevelBag<>(Parameters.OPERATION_BAG_LEVELS, Parameters.OPERATION_BAG_SIZE));
        this.memory = m;
        this.param = m.param;
        for (Operator o : Operators.get(this))
            this.memory.addOperator(o);
        new Plugins().init(this);
    }

    /**
     * Reset the system with an empty memory and reset clock. Called locally and
     * from {@link NARControls}.
     */
    public void reset() {
        memory.reset();
    }

    /**
     * Generally the text will consist of Task's to be parsed in Narsese, but
     * may contain other commands recognized by the system. The creationTime
     * will be set to the current memory cycle time, but may be processed by
     * memory later according to the length of the input queue.
     */
    private boolean addMultiLineInput(final String text) {
        if(text.contains("\n")) {
            String[] lines = text.split("\n");
            for(String s : lines) {
                addInput(s);
                if(!running) {
                    this.cycle();
                }
            }
            return true;
        }
        return false;
    }
    
    private boolean addCommand(final String text) {
        if(text.startsWith("**")) {
            this.reset();
            return true;
        }
        if(text.startsWith("*decisionthreshold=")) {
            Double value = Double.valueOf(text.split("decisionthreshold=")[1]);
            param.decisionThreshold.set(value);
            return true;
        }
        if(text.startsWith("*volume=")) {
            Integer value = Integer.valueOf(text.split("volume=")[1]);
            param.noiseLevel.set(value);
            return true;
        }
        try {
            Integer retVal = Integer.parseInt(text);
            if(!running) {
                for(int i=0;i<retVal;i++) {
                    this.cycle();
                }
            }
            return true;
        } catch (NumberFormatException ex) {} //usual input (TODO without exception)
        return false;
    }
    
    public void addInput(final String text) {
        Narsese narsese = new Narsese(this);
        if(addMultiLineInput(text)) {
            return;
        }
        try {
            if(addCommand(text)) {
                return;
            }
            Task t = narsese.parseTask(text.trim());
            this.memory.inputTask(t);
        } catch (Exception ex) {
            //Logger.getLogger(NAR.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addInputFile(String s) {
        try (BufferedReader br = new BufferedReader(new FileReader(s))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(!line.isEmpty()) {
                    this.addInput(line);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(NAR.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /** gets a concept if it exists, or returns null if it does not */
    public Concept concept(String concept) throws InvalidInputException {
        return memory.concept(new Narsese(this).parseTerm(concept));
    }

    public NAR ask(String termString, AnswerHandler answered) throws InvalidInputException {

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
                                1),
                        true)
        );

        if (answered!=null) {
            answered.start(t, this);
        }
        return this;

    }

    public NAR askNow(String termString, AnswerHandler answered) throws InvalidInputException {

        Task t;
        addInput(
                t = new Task(
                        new Sentence(
                                new Narsese(this).parseTerm(termString),
                                Symbols.QUESTION_MARK,
                                null,
                                new Stamp(memory, Tense.Present)),
                        new BudgetValue(
                                Parameters.DEFAULT_QUESTION_PRIORITY,
                                Parameters.DEFAULT_QUESTION_DURABILITY,
                                1),
                        true)
        );

        if (answered!=null) {
            answered.start(t, this);
        }
        return this;

    }

    public NAR addInput(final Task t) {
        this.memory.inputTask(t);
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

    public void addPlugin(Plugin p) {
        if (p instanceof Operator) {
            memory.addOperator((Operator)p);
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
            //TODO sensory channels can be plugins
            ps.setEnabled(false);
            emit(Events.PluginsChange.class, null, p);
        }
    }

    public List<PluginState> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }


    public void start(final long minCyclePeriodMS) {
        this.minCyclePeriodMS = minCyclePeriodMS;
        if (thread == null) {
            thread = new Thread(this, "Inference");
            thread.start();
        }
        running = true;
    }

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

    /** Execute a fixed number of cycles.*/
    public void cycles(final int cycles) {
        memory.allowExecution = true;
        emit(CyclesStart.class);
        final boolean wasRunning = running;
        running = true;
        stopped = false;
        for(int i=0;i<cycles;i++) {
            cycle();
        }
        running = wasRunning;
        emit(CyclesEnd.class);
    }

    /** Main loop executed by the Thread.  Should not be called directly. */
    @Override public void run() {
        stopped = false;

        while (running && !stopped) {
            emit(CyclesStart.class);
            cycle();
            emit(CyclesEnd.class);

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

    public void emit(final Class c, final Object... o) {
        memory.event.emit(c, o);
    }

    /**
     * A frame, consisting of one or more NAR memory cycles
     */
    public void cycle() {
        try {
            memory.cycle(this);
        }
        catch (Throwable e) {
            if(Parameters.SHOW_REASONING_ERRORS) {
                emit(ERR.class, e);
            }

            if (Parameters.DEBUG) {
                e.printStackTrace();
            }
        }
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
}