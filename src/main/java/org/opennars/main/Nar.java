/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.main;

import org.apache.commons.lang3.StringUtils;
import org.opennars.entity.*;
import org.opennars.interfaces.Timable;
import org.opennars.interfaces.pub.Reasoner;
import org.opennars.io.ConfigReader;
import org.opennars.io.Narsese;
import org.opennars.io.Parser;
import org.opennars.io.Symbols;
import org.opennars.io.events.AnswerHandler;
import org.opennars.io.events.EventEmitter;
import org.opennars.io.events.EventEmitter.EventObserver;
import org.opennars.io.events.Events;
import org.opennars.io.events.Events.CyclesEnd;
import org.opennars.io.events.Events.CyclesStart;
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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opennars.language.SetInt;
import org.opennars.plugin.mental.Emotions;
import org.opennars.plugin.mental.InternalExperience;


/**
 * Non-Axiomatic Reasoner
 *
 * Instances of this represent a reasoner connected to a Memory, and set of Input and Output channels.
 *
 * All state is contained within Memory.  A Nar is responsible for managing I/O channels and executing
 * memory operations.  It executesa series sof cycles in two possible modes:
 *   * step mode - controlled by an outside system, such as during debugging or testing
 *   * thread mode - runs in a pausable closed-loop at a specific maximum framerate.
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public class Nar extends SensoryChannel implements Reasoner, Serializable, Runnable {
    public Parameters narParameters = new Parameters();

    /* System clock, relatively defined to guarantee the repeatability of behaviors */
    private Long cycle = new Long(0);

    /**
     * The information about the version and date of the project.
     */
    public static final String VERSION = "Open-NARS v3.0.0";

    /**
     * The project web sites.
     */
    public static final String WEBSITE =
            " Open-NARS website:  http://code.google.com/p/open-org.opennars/ \n"
                    + "      NARS website:  http://sites.google.com/site/narswang/ \n" +
                    "    Github website:  http://github.com/opennars/ \n" +
                    "    IRC:  http://webchat.freenode.net/?channels=org.opennars \n";

    private transient Thread[] threads = null;
    protected transient Map<Term,SensoryChannel> sensoryChannels = new HashMap<>();
    public void addSensoryChannel(final String term, final SensoryChannel channel) {
        try {
            sensoryChannels.put(new Narsese(this).parseTerm(term), channel);
        } catch (final Parser.InvalidInputException ex) {
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
    
    public static Nar LoadFromFile(final String name) throws IOException, ClassNotFoundException, 
            IllegalAccessException, ParseException, ParserConfigurationException, SAXException, 
            NoSuchMethodException, InstantiationException, InvocationTargetException {
        final FileInputStream inStream = new FileInputStream(name);
        final ObjectInputStream stream = new ObjectInputStream(inStream);
        final Nar ret = (Nar) stream.readObject();
        ret.memory.event = new EventEmitter();
        ret.plugins = new ArrayList<>();
        ret.sensoryChannels = new HashMap<>();
        List<Plugin> pluginsToAdd = ConfigReader.loadParamsFromFileAndReturnPlugins(ret.usedConfigFilePath, ret, ret.narParameters);
        for(Plugin p : pluginsToAdd) {
            ret.addPlugin(p);
        }
        return ret;
    }

    volatile long minCyclePeriodMS;

    /**
     * The name of the reasoner
     */
    protected String name;
    /**
     * The memory of the reasoner
     */
    public final Memory memory;

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
    private transient boolean running = false;
    /** used by stop() to signal that a running loop should be interrupted */
    private transient boolean stopped = false;
    private transient boolean threadYield;

    public static final String DEFAULTCONFIG_FILEPATH = "./config/defaultConfig.xml";
    
    /** constructs the NAR and loads a config from the default filepath
     *
     * @param narId inter NARS id of this NARS instance
     */
    public Nar(long narId) throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, 
            ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        this(narId, DEFAULTCONFIG_FILEPATH);
    }

    public String usedConfigFilePath = "";
    /** constructs the NAR and loads a config from the filepath
     *
     * @param narId inter NARS id of this NARS instance
     * @param relativeConfigFilePath (relative) path of the XML encoded config file
     */
    public Nar(long narId, String relativeConfigFilePath) throws IOException, InstantiationException, InvocationTargetException, 
            NoSuchMethodException, ParserConfigurationException, SAXException, IllegalAccessException, ParseException, ClassNotFoundException {
        List<Plugin> pluginsToAdd = ConfigReader.loadParamsFromFileAndReturnPlugins(relativeConfigFilePath, this, this.narParameters);
        final Memory m = new Memory(this.narParameters,
                new LevelBag(narParameters.CONCEPT_BAG_LEVELS, narParameters.CONCEPT_BAG_SIZE, this.narParameters),
                new LevelBag<>(narParameters.NOVEL_TASK_BAG_LEVELS, narParameters.NOVEL_TASK_BAG_SIZE, this.narParameters),
                new LevelBag<>(narParameters.SEQUENCE_BAG_LEVELS, narParameters.SEQUENCE_BAG_SIZE, this.narParameters),
                new LevelBag<>(narParameters.OPERATION_BAG_LEVELS, narParameters.OPERATION_BAG_SIZE, this.narParameters));
        this.memory = m;
        this.memory.narId = narId;
        this.usedConfigFilePath = relativeConfigFilePath;
        for(Plugin p : pluginsToAdd) { //adding after memory is constructed, as memory depends on the loaded params!!
            this.addPlugin(p);
        }
    }
    
    /** constructs the NAR and loads a config from the filepath
     *
     * @param relativeConfigFilePath (relative) path of the XML encoded config file
     */
    public Nar(String relativeConfigFilePath) throws IOException, InstantiationException, InvocationTargetException, 
            NoSuchMethodException, ParserConfigurationException, SAXException, IllegalAccessException, ParseException, ClassNotFoundException {
        this(java.util.UUID.randomUUID().getLeastSignificantBits(), relativeConfigFilePath);
    }
    
    /** constructs the NAR and loads a config from the default filepath
     *
     * Assigns a random id to the instance
     */
    public Nar() throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        this(DEFAULTCONFIG_FILEPATH);
    }

    /**
     * Reset the system with an empty memory and reset clock. Called locally.
     */
    public void reset() {
        cycle = (long) 0;
        memory.reset();
    }

    /**
     * Generally the text will consist of Task's to be parsed in Narsese, but
     * may contain other commands recognized by the system. The creationTime
     * will be set to the current memory cycle time, but may be processed by
     * memory later according to the length of the input queue.
     */
    private boolean addMultiLineInput(final String text) {
        final String[] lines = text.split("\n");
        for(final String s : lines) {
            addInput(s);
            if(!running) {
                this.cycle();
            }
        }
        return true;
    }
    
    private boolean addCommand(final String text) throws IOException {
        if(text.startsWith("**")) {
            this.reset();
            return true;
        }
        else
        if(text.startsWith("*decisionthreshold=")) { //TODO use reflection for narParameters, allow to set others too
            final Double value = Double.valueOf(text.split("decisionthreshold=")[1]);
            narParameters.DECISION_THRESHOLD = value.floatValue();
            return true;
        }
        else
        if(text.startsWith("*volume=")) {
            final Integer value = Integer.valueOf(text.split("volume=")[1]);
            narParameters.VOLUME = value;
            return true;
        }
        else
        if(text.startsWith("*threads=")) {
            final Integer value = Integer.valueOf(text.split("threads=")[1]);
            narParameters.THREADS_AMOUNT = value;
            return true;
        }
        else
        if(text.startsWith("*save=")) {
            final String filename = text.split("save=")[1];
            boolean wasRunning = this.isRunning();
            if(wasRunning) {
                this.stop();
            }
            this.SaveToFile(filename);
            if(wasRunning) {
                this.start(this.minCyclePeriodMS);
            }
            return true;
        }
        else
        if(text.startsWith("*speed=")) {
            final Integer value = Integer.valueOf(text.split("speed=")[1]);
            this.minCyclePeriodMS = value;
            return true;
        }
        else
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
        final Parser narsese = new Narsese(this);
        if(addMultiLineInput(text)) {
            return;
        }
        //Ignore any input that is just a comment
        if(text.startsWith("\'") || text.startsWith("//") || text.trim().length() <= 0) {
            if(text.trim().length() > 0) {
                emit(org.opennars.io.events.OutputHandler.ECHO.class, text);
            }
            return;
        }
        try {
            if(addCommand(text)) {
                return;
            }
        } catch (IOException ex) {
            throw new IllegalStateException("I/O command failed: " + text, ex);
        }
        Task task = null;
        try {
            task = narsese.parseTask(text);
        } catch (final Parser.InvalidInputException e) {
            if(MiscFlags.SHOW_INPUT_ERRORS) {
                emit(ERR.class, e);
            }
            if(!MiscFlags.INPUT_ERRORS_CONTINUE) {
                throw new IllegalStateException("Invalid input: " + text, e);
            }
            return;
        }
        //check if it should go to a sensory channel instead:
        final Term t = task.getTerm();
        if(t != null) {
            Term predicate = null;
            if(t instanceof Inheritance) {
                predicate = ((Inheritance) t).getPredicate();
            } else {
                predicate = SetInt.make(new Term("OBSERVED"));
            }
            if(this.sensoryChannels.containsKey(predicate)) {
                //Transform to channel-specific coordinate if available.
                int channelWidth = this.sensoryChannels.get(predicate).width;
                int channelHeight = this.sensoryChannels.get(predicate).height;
                if(channelWidth != 0 && channelHeight != 0 && (t instanceof Inheritance) && 
                        (((Inheritance )t).getSubject() instanceof SetExt)) {
                    final SetExt subj = (SetExt) ((Inheritance) t).getSubject();
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
                        //this.emit(OutputHandler.IN.class, task); too expensive to print each input task, consider vision :)
                        this.addInput(newInput);
                        return;
                    }
                }
                this.sensoryChannels.get(predicate).addInput(task, this);
                return;
            }
        }
        //else input into NARS directly:
        this.memory.inputTask(this, task);
    }
    
    public void addInputFile(final String s) {
        try (final BufferedReader br = new BufferedReader(new FileReader(s))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(!line.isEmpty()) {
                    //Loading experience file lines, or else just normal input lines
                    if(line.matches("([A-Za-z])+:(.*)")) {
                        //Extract creation time:
                        if(!line.startsWith("IN:")) {
                            continue; //ignore
                        }
                        String[] spl = line.replace("IN:", "").split("\\{");
                        int creationTime = Integer.parseInt(spl[spl.length-1].split(" :")[0].split("\\|")[0]);
                        while(this.time() < creationTime) {
                            this.cycles(1);
                        }
                        String lineReconstructed = ""; //the line but without the stamp info at the end
                        for(int i=0; i<spl.length-1; i++) {
                            lineReconstructed += spl[i] + "{";
                        }
                        lineReconstructed = lineReconstructed.substring(0, lineReconstructed.length()-1);
                        this.addInput(lineReconstructed.trim());
                    } else {
                        this.addInput(line);
                    }
                }
            }
        } catch (final Exception ex) {
            Logger.getLogger(Nar.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Loading experience file failed ", ex);
        }
    }


    /** gets a concept if it exists, or returns null if it does not */
    public Concept concept(final String concept) throws Parser.InvalidInputException {
        return memory.concept(new Narsese(this).parseTerm(concept));
    }

    public Nar ask(final String termString, final AnswerHandler answered) throws Parser.InvalidInputException {
        final Sentence sentenceForNewTask = new Sentence(
            new Narsese(this).parseTerm(termString),
            Symbols.QUESTION_MARK,
            null,
            new Stamp(this, memory, Tense.Eternal));
        final BudgetValue budget = new BudgetValue(
            narParameters.DEFAULT_QUESTION_PRIORITY,
            narParameters.DEFAULT_QUESTION_DURABILITY,
            1, narParameters);
        final Task t = new Task(sentenceForNewTask, budget, Task.EnumType.INPUT);

        addInput(t, this);

        if (answered!=null) {
            answered.start(t, this);
        }
        return this;

    }

    public Nar askNow(final String termString, final AnswerHandler answered) throws Parser.InvalidInputException {
        final Sentence sentenceForNewTask = new Sentence(
            new Narsese(this).parseTerm(termString),
            Symbols.QUESTION_MARK,
            null,
            new Stamp(this, memory, Tense.Present));
        final BudgetValue budgetForNewTask = new BudgetValue(
            narParameters.DEFAULT_QUESTION_PRIORITY,
            narParameters.DEFAULT_QUESTION_DURABILITY,
            1, narParameters);
        final Task t = new Task(sentenceForNewTask, budgetForNewTask, Task.EnumType.INPUT);

        addInput(t, this);

        if (answered!=null) {
            answered.start(t, this);
        }
        return this;

    }

    public Nar addInput(final Task t, final Timable time) {
        this.memory.inputTask(this, t);
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
        if(p instanceof SensoryChannel) {
            this.addSensoryChannel(((SensoryChannel) p).getName(), (SensoryChannel) p);
        }
        else
        if (p instanceof Operator) {
            memory.addOperator((Operator)p);
        }
        else
        if(p instanceof Emotions) {
            memory.emotion = (Emotions) p;
        }
        else
        if(p instanceof InternalExperience) {
            memory.internalExperience = (InternalExperience) p;
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
            if (p instanceof SensoryChannel) {
                sensoryChannels.remove(p);
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
        if (threads == null) {
            int n_threads = narParameters.THREADS_AMOUNT;
            threads = new Thread[n_threads];
            for(int i=0;i<n_threads;i++) {
                threads[i] = new Thread(this, "Inference"+i);
                threads[i].start();
            }
        }
        running = true;
    }
    public void start() {
        start(narParameters.MILLISECONDS_PER_STEP);
    }

    /**
     * Stop the inference process, killing its thread.
     */
    public void stop() {
        if (threads!=null) {
            for(Thread thread : threads) {
                thread.interrupt();
            }
            threads = null;
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

            synchronized (cycle) {
                cycle++;
            }
        }
        catch (final Exception e) {
            if(MiscFlags.SHOW_REASONING_ERRORS) {
                emit(ERR.class, e);
            }
            if(!MiscFlags.REASONING_ERRORS_CONTINUE) {
                throw new IllegalStateException("Reasoning error:\n", e);
            }
        }
    }

    @Override
    public String toString() {
        return memory.toString();
    }


    public long time() {
        if(narParameters.STEPS_CLOCK) {
            return cycle;
        } else {
            return System.currentTimeMillis();
        }
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
