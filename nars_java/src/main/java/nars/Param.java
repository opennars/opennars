package nars;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.*;
import nars.nal.TaskComparator;
import nars.nal.nal7.Interval.AtomicDuration;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NAR Parameters which can be changed during runtime.
 */
public class Param implements Serializable {


    private TaskComparator.Duplication derivationDuplicationMode = TaskComparator.Duplication.Or;

    public Param() {    }

    /** Silent threshold for task reporting, in [0, 100]. 
     *  Noise level = 100 - silence level; noise 0 = always silent, noise 100 = never silent
     */
    public final AtomicInteger outputVolume = new AtomicInteger();
    
    /** 
       Cycles per duration.
       Past/future tense usage convention;
       How far away "past" and "future" is from "now", in cycles.         
       The range of "now" is [-DURATION/2, +DURATION/2];      */
    public final AtomicDuration duration = new AtomicDuration();

    public final AtomicDouble confidenceThreshold = new AtomicDouble();

    public final AtomicInteger shortTermMemoryHistory = new AtomicDuration();

    public AtomicInteger temporalRelationsMax = new AtomicInteger();

    Memory.Timing timing;
    Memory.Forgetting forgetting;

    public static Param fromJSON(String json) {
        return Param.json.fromJson(json, Param.class);
    }
    

    /** converts durations to cycles */
    public final float cycles(AtomicDouble durations) {
        return cycles(durations.floatValue());
    }

    public final float cycles(float durations) {
        return duration.floatValue() * durations;
    }

    /**
     * How much a concept is activated.
     * 1.0 means all activation is applied,
     * 0.0 means none is.
     */
    public AtomicDouble conceptActivationFactor = new AtomicDouble(1.0);

    /** scaling factor for priority of input tasks */
    public AtomicDouble inputActivationFactor = new AtomicDouble(1.0);

    /**
     * Minimum required priority for a concept
     * to be fired if it is activated, otherwise
     * it just accumulates the priority in its budget.
     */
    public AtomicDouble conceptFireThreshold = new AtomicDouble(0.0);


    /** Concept decay rate in ConceptBag, in [1, 99].  originally: CONCEPT_FORGETTING_CYCLE 
     *  How many cycles it takes an item to decay completely to a threshold value (ex: 0.1).
     *  Lower means faster rate of decay.
     */
    public final AtomicDouble conceptForgetDurations = new AtomicDouble();
    
    /** TermLink decay rate in TermLinkBag, in [1, 99]. originally: TERM_LINK_FORGETTING_CYCLE */
    //TODO use separate termlink forget rates whether the termlink was actually selected for firing or not.
    public final AtomicDouble termLinkForgetDurations = new AtomicDouble();
    
    /** TaskLink decay rate in TaskLinkBag, in [1, 99]. originally: TASK_LINK_FORGETTING_CYCLE */
    public final AtomicDouble taskLinkForgetDurations = new AtomicDouble();
    
    public final AtomicDouble novelTaskForgetDurations = new AtomicDouble();

    /** The budget threshold rate for task to be accepted.
     *   Increasing this value decreases the resolution with which
     *   budgets are propagated or otherwise measured, which can result
     *   in a performance gain.
     *   TODO implement, but will require using this value as parameter to aboveThreshold() and greaterThan() budget functions
     */
    private final AtomicDouble budgetThreshold = new AtomicDouble();

    
    /** Minimum expectation for a desire value. 
     *  the range of "now" is [-DURATION, DURATION]; */
    public final AtomicDouble decisionThreshold = new AtomicDouble();


    /** How many concepts to fire each cycle; measures degree of parallelism in each cycle */
    public final AtomicInteger conceptsFiredPerCycle = new AtomicInteger(); //TODO Core implementations should obey this value
    
    /** Maximum TermLinks checked for novelty for each TaskLink in TermLinkBag */
    public final AtomicInteger termLinkMaxMatched = new AtomicInteger();
            


    /** Maximum TermLinks used in reasoning for each Task in Concept */
    public final AtomicInteger termLinkMaxReasoned = new AtomicInteger();

    /** Record-length for newly created TermLink's */
    public final AtomicInteger termLinkRecordLength = new AtomicInteger();
    
    /** Maximum number of beliefs kept in a Concept */
    public final AtomicInteger conceptBeliefsMax = new AtomicInteger();
    
    /** Maximum number of questions, and max # of quests kept in a Concept */
    public final AtomicInteger conceptQuestionsMax = new AtomicInteger();

    /** Maximum number of goals kept in a Concept */
    public final AtomicInteger conceptGoalsMax = new AtomicInteger();

    /** what this value represents was originally equal to the termlink record length (10), but we may want to adjust it or make it scaled according to duration since it has more to do with time than # of records.  it can probably be increased several times larger since each item should remain in the recording queue for longer than 1 cycle
     * prevents a termlinks from redundantly repeated firing */
    public AtomicInteger noveltyHorizon = new AtomicInteger();


    /** Reliance factor, the empirical confidence of analytical truth.
        the same as default confidence  */        
    public final AtomicDouble reliance = new AtomicDouble();

    /** max # of inputs to perceive per cycle; -1 means unlimited (attempts to drains input to empty each cycle) */
    public AtomicInteger inputsMaxPerCycle = new AtomicInteger(1);
    
    /** avoid calling this directly; use Default.simulationTime() which also sets the forgetting mode */
    public void setTiming(Memory.Timing time) {
        this.timing = time;
    }

    public Memory.Timing getTiming() {
        return timing;
    }

    public Memory.Forgetting getForgetMode() {
        return forgetting;
    }

    public void setForgetting(Memory.Forgetting forgetMode) {
        this.forgetting = forgetMode;
    }


    
    @Override
    public String toString() {
        return json.toJson(this);
    }
    
//    public double[] toGenome(String... excludeFields) {
//        JsonObject j = json.toJsonTree(this).getAsJsonObject();
//        TreeSet<Map.Entry<String, JsonElement>> fields = new TreeSet<>(j.entrySet());
//        
//        Set<String> excluded = new HashSet();
//        for (String e : excludeFields)
//            excluded.add(e);
//        
//        List<Double> l = new ArrayList();
//        for (Map.Entry<String, JsonElement> e : fields) {
//            String f = e.getKey();
//            if (excluded.contains(f))
//                continue;
//            JsonElement v = e.getValue();
//            if (v.isJsonPrimitive()) {
//                try {
//                    double d = v.getAsDouble();
//                    l.add(d);                
//                }
//                catch (NumberFormatException nfe) { }
//            }
//        }
//        return Doubles.toArray(l);
//    }
    
    static public final Gson json;
    static { 
        GsonBuilder b = new GsonBuilder();
        b.setPrettyPrinting();
        b.disableHtmlEscaping();
        b.serializeNulls();
        
        final JsonSerializer<AtomicDouble> atomicDoubleSerializer = new JsonSerializer<AtomicDouble>() {
            @Override public JsonElement serialize(AtomicDouble t, Type type, JsonSerializationContext jsc) {
                return new JsonPrimitive(t.get());
            }
        };
        
        JsonDeserializer<AtomicDouble> atomicDoubleDeserializer = new JsonDeserializer<AtomicDouble>() {
            @Override public AtomicDouble deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
                return new AtomicDouble(je.getAsDouble());
            }
        };
        
        b.registerTypeAdapter(AtomicDouble.class, atomicDoubleSerializer);
        b.registerTypeAdapter(AtomicDouble.class, atomicDoubleDeserializer);
        
        b.registerTypeAdapter(AtomicInteger.class, new JsonSerializer<AtomicInteger>() {
            @Override public JsonElement serialize(AtomicInteger t, Type type, JsonSerializationContext jsc) {
                return new JsonPrimitive(t.get());
            }            
        });
        b.registerTypeAdapter(AtomicInteger.class, new JsonDeserializer<AtomicInteger>() {
            @Override public AtomicInteger deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
                return new AtomicInteger(je.getAsInt());
            }           
        });

        
        json = b.create();            
    }

    public TaskComparator.Duplication getDerivationDuplicationMode() {
        return derivationDuplicationMode;
    }

    /** handling behavior for duplicate derivations in bulk processing */
    public void setDerivationDuplicationMode(TaskComparator.Duplication derivationDuplicationMode) {
        this.derivationDuplicationMode = derivationDuplicationMode;
    }
}
