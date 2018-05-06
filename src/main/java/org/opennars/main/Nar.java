/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.main;

import org.apache.commons.lang3.StringUtils;
import org.opennars.entity.*;
import org.opennars.io.Narsese;
import org.opennars.io.Narsese.InvalidInputException;
import org.opennars.io.Symbols;
import org.opennars.io.events.AnswerHandler;
import org.opennars.io.events.EventEmitter;
import org.opennars.io.events.EventEmitter.EventObserver;
import org.opennars.io.events.Events;
import org.opennars.io.events.Events.CyclesEnd;
import org.opennars.io.events.Events.CyclesStart;
import org.opennars.io.events.OutputHandler;
import org.opennars.io.events.OutputHandler.ERR;
import org.opennars.language.Inheritance;
import org.opennars.language.SetExt;
import org.opennars.language.Tense;
import org.opennars.language.Term;
import org.opennars.operator.Operator;
import org.opennars.plugin.Plugin;
import org.opennars.plugin.perception.SensoryChannel;
import org.opennars.storage.LevelBag;
import org.opennars.storage.Memory;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Non-Axiomatic Reasoner
 *
 * Instances of this represent a reasoner connected to a Memory, and set of Input and Output channels.
 *
 * All state is contained within Memory.  A Nar is responsible for managing I/O channels and executing
 * memory operations.  It executesa series sof cycles in two possible modes:
 *   * step mode - controlled by an outside system, such as during debugging or testing
 *   * thread mode - runs in a pausable closed-loop at a specific maximum framerate.
 */
public class Nar extends SensoryChannel implements Serializable,Runnable {
    public NarParameters narParameters = new NarParameters();

    /**
     * The information about the version and date of the project.
     */
    public static final String VERSION = "Open-NARS v1.6.6pre2";

    /**
     * The project web sites.
     */
    public static final String WEBSITE =
            " Open-NARS website:  http://code.google.com/p/open-org.opennars/ \n"
                    + "      NARS website:  http://sites.google.com/site/narswang/ \n" +
                    "    Github website:  http://github.com/opennars/ \n" +
                    "    IRC:  http://webchat.freenode.net/?channels=org.opennars \n";


    final Map<Term,SensoryChannel> sensoryChannels = new HashMap<>();
    public void addSensoryChannel(final String term, final SensoryChannel channel) {
        try {
            sensoryChannels.put(new Narsese(this).parseTerm(term), channel);
        } catch (final InvalidInputException ex) {
            Logger.getLogger(Nar.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Could not add sensory channel.", ex);
        }
    }
    
    public void SaveToFile(final String name) throws IOException {
        final FileOutputStream outStream = new FileOutputStream(name);
        final ObjectOutputStream stream = new ObjectOutputStream(outStream);
        stream.writeObject(this);
        outStream.close();
    }
    
    public static Nar LoadFromFile(final String name) throws IOException, ClassNotFoundException {
        final FileInputStream inStream = new FileInputStream(name);
        final ObjectInputStream stream = new ObjectInputStream(inStream);
        final Nar ret = (Nar) stream.readObject();
        ret.memory.event = new EventEmitter();
        ret.plugins = new ArrayList<>(); 
        for (final Operator o : Operators.get(ret))
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
    public final Memory memory;
    
    
    public static class Lock extends Object implements Serializable { }
    //Because AtomicInteger/Double ot supported by teavm
    public static class PortableInteger implements Serializable {
        public PortableInteger(){}
        final Lock lock = new Lock();
        int VAL = 0;
        public PortableInteger(final int VAL){synchronized(lock){this.VAL = VAL;}}
        public void set(final int VAL){synchronized(lock){this.VAL = VAL;}}
        public int get() {return this.VAL;}
        public float floatValue() {return (float)this.VAL;}
        public float doubleValue() {return (float)this.VAL;}
        public int intValue() {return this.VAL;}
        public int incrementAndGet(){int ret = 0; synchronized(lock){this.VAL++; ret=this.VAL;} return ret;}
    }
    public static class PortableDouble implements Serializable {
        final Lock lock = new Lock();
        public PortableDouble(){}
        double VAL = 0;
        public PortableDouble(final double VAL){synchronized(lock){this.VAL = VAL;}}
        public void set(final double VAL){synchronized(lock){this.VAL = VAL;}}
        public double get() {return this.VAL;}
        public float floatValue() {return (float)this.VAL;}
        public float doubleValue() {return (float)this.VAL;}
        public int intValue() {return (int)this.VAL;}
    }
    /*Nar Parameters which can be changed during runtime.*/
   public class RuntimeParameters implements Serializable {
       public RuntimeParameters() {    }
       public final PortableInteger noiseLevel = new PortableInteger(100);
       public final PortableDouble conceptForgetDurations = new PortableDouble(Parameters.CONCEPT_FORGET_DURATIONS);
       public final PortableDouble termLinkForgetDurations = new PortableDouble(Parameters.TERMLINK_FORGET_DURATIONS);
       public final PortableDouble taskLinkForgetDurations = new PortableDouble(Parameters.TASKLINK_FORGET_DURATIONS);
       public final PortableDouble eventForgetDurations = new PortableDouble(Parameters.EVENT_FORGET_DURATIONS);
   }
    public final RuntimeParameters param;

    public class PluginState implements Serializable {
        final public Plugin plugin;
        boolean enabled = false;

        public PluginState(final Plugin plugin) {
            this(plugin,true);
        }

        public PluginState(final Plugin plugin, final boolean enabled) {
            this.plugin = plugin;
            setEnabled(enabled);
        }

        public void setEnabled(final boolean enabled) {
            if (this.enabled == enabled) return;

            plugin.setEnabled(Nar.this, enabled);
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

    public Nar() {
        final Plugins b = new Plugins();
        final Memory m = new Memory(new RuntimeParameters(),
                new LevelBag(narParameters.CONCEPT_BAG_LEVELS, narParameters.CONCEPT_BAG_SIZE),
                new LevelBag<>(Parameters.NOVEL_TASK_BAG_LEVELS, Parameters.NOVEL_TASK_BAG_SIZE),
                new LevelBag<>(Parameters.SEQUENCE_BAG_LEVELS, Parameters.SEQUENCE_BAG_SIZE),
                new LevelBag<>(Parameters.OPERATION_BAG_LEVELS, Parameters.OPERATION_BAG_SIZE));
        this.memory = m;
        this.param = m.param;
        for (final Operator o : Operators.get(this))
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
            final String[] lines = text.split("\n");
            for(final String s : lines) {
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
        else
        if(text.startsWith("*decisionthreshold=")) {
            final Double value = Double.valueOf(text.split("decisionthreshold=")[1]);
            narParameters.DECISION_THRESHOLD = value.floatValue();
            return true;
        }
        else
        if(text.startsWith("*volume=")) {
            final Integer value = Integer.valueOf(text.split("volume=")[1]);
            param.noiseLevel.set(value);
            return true;
        }
        if(StringUtils.isNumeric(text)) {
            final Integer retVal = Integer.parseInt(text);
            if(!running) {
                for(int i=0;i<retVal;i++) {
                    this.cycle();
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
    public void addInput(String text) {
        text = text.trim();
        //Ignore any input that is just a comment
        if(text.startsWith("\'") || text.startsWith("//") ||text.trim().length() <= 0)
            return;

        final Narsese narsese = new Narsese(this);
        if(addMultiLineInput(text)) {
            return;
        }
        if(addCommand(text)) {
            return;
        }
        final Task task;
        try {
            task = narsese.parseTask(text);
        } catch (final InvalidInputException e) {
            throw new IllegalStateException("Invalid input: " + text, e);
        }
        //check if it should go to a sensory channel instead:
        final Term t = task.getTerm();
        if(t != null && t instanceof Inheritance) {
            final Term predicate = ((Inheritance) t).getPredicate();
            if(this.sensoryChannels.containsKey(predicate)) {
                final Inheritance inh = (Inheritance) task.sentence.term;
                final SetExt subj = (SetExt) inh.getSubject();
                //map to pei's -1 to 1 indexing schema
                if(subj.term[0].term_indices == null) {
                    final String variable = subj.toString().split("\\[")[0];
                    final String[] vals = subj.toString().split("\\[")[1].split("\\]")[0].split(",");
                    final double height = Double.parseDouble(vals[0]);
                    final double width = Double.parseDouble(vals[1]);
                    final int wval = (int) Math.round((width+1.0f)/2.0f*(this.sensoryChannels.get(predicate).width-1));
                    final int hval = (int) Math.round(((height+1.0f)/2.0f*(this.sensoryChannels.get(predicate).height-1)));
                    final String ev = task.sentence.isEternal() ? " " : " :|: ";
                    final String newInput = "<"+variable+"["+hval+","+wval+"]} --> " + predicate + ">" +
                                      task.sentence.punctuation + ev + task.sentence.truth.toString();
                    this.emit(OutputHandler.IN.class, task);
                    this.addInput(newInput);
                    return;
                }
                this.sensoryChannels.get(predicate).addInput(task);
                return;
            }
        }
        //else input into NARS directly:
        this.memory.inputTask(task);
    }
    
    public void addInputFile(final String s) {
        try (final BufferedReader br = new BufferedReader(new FileReader(s))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(!line.isEmpty()) {
                    this.addInput(line);
                }
            }
        } catch (final IOException ex) {
            Logger.getLogger(Nar.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Could not open specified file", ex);
        }
    }


    /** gets a concept if it exists, or returns null if it does not */
    public Concept concept(final String concept) throws InvalidInputException {
        return memory.concept(new Narsese(this).parseTerm(concept));
    }

    public Nar ask(final String termString, final AnswerHandler answered) throws InvalidInputException {

        final Task t;
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

    public Nar askNow(final String termString, final AnswerHandler answered) throws InvalidInputException {

        final Task t;
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

    public Nar addInput(final Task t) {
        this.memory.inputTask(t);
        return this;
    }

    /** attach event handler */
    public void on(final Class c, final EventObserver o) {
        memory.event.on(c, o);
    }

    /** remove event handler */
    public void off(final Class c, final EventObserver o) {
        memory.event.on(c, o);
    }

    /** set an event handler. useful for multiple events. */
    public void event(final EventObserver e, final boolean enabled, final Class... events) {
        memory.event.set(e, enabled, events);
    }

    public void addPlugin(final Plugin p) {
        if (p instanceof Operator) {
            memory.addOperator((Operator)p);
        }
        final PluginState ps = new PluginState(p);
        plugins.add(ps);
        emit(Events.PluginsChange.class, p, null);
    }

    public void removePlugin(final PluginState ps) {
        if (plugins.remove(ps)) {
            final Plugin p = ps.plugin;
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
                } catch (final InterruptedException e) {
                }
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
     * A frame, consisting of one or more Nar memory cycles
     */
    public void cycle() {
        try {
            memory.cycle(this);
        }
        catch (final Exception e) {
            if(Parameters.SHOW_REASONING_ERRORS) {
                emit(ERR.class, e);
            }
            throw e;
        }
    }

    @Override
    public String toString() {
        return memory.toString();
    }

    /**
     * Get the current time from the clock Called in {@link org.opennars.entity.Stamp}
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

    /** When b is true, Nar will call Thread.yield each run() iteration that minCyclePeriodMS==0 (no delay).
     *  This is for improving program responsiveness when Nar is run with no delay.
     */
    public void setThreadYield(final boolean b) {
        this.threadYield = b;
    }
}
