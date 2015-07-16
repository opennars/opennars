package nars;

import com.google.common.util.concurrent.AtomicDouble;
import nars.clock.Clock;
import nars.concept.BeliefTable;
import nars.concept.Concept;
import nars.io.Perception;
import nars.nal.nal7.Interval.AtomicDuration;
import nars.process.DerivationReaction;
import nars.task.TaskComparator;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NAR Parameters which can be changed during runtime.
 */
public abstract class Param implements Serializable {

    //TODO only SequentialCycle implementations should be using these and should be moved to that:

    /** How many concepts to fire each cycle; measures degree of parallelism in each cycle */
    @Deprecated public final AtomicInteger conceptsFiredPerCycle = new AtomicInteger();

    /** max # of inputs to perceive per cycle; -1 means unlimited (attempts to drains input to empty each cycle) */
    @Deprecated public final AtomicInteger inputsMaxPerCycle = new AtomicInteger(1);

    //-------------------





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

    protected Clock clock;
    

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

    /** minimum expectation necessary to create a concept
     *  original value: 0.66
     * */
    public AtomicDouble conceptCreationExpectation = new AtomicDouble();





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

     /*
     BUDGET THRESHOLDS
     * Increasing this value decreases the resolution with which
     *   budgets are propagated or otherwise measured, which can result
     *   in a performance gain.      */


    /** budget summary necessary for perception  */
    public final AtomicDouble perceptThreshold = new AtomicDouble();

    /** budget summary necessary to Conceptualize */
    public final AtomicDouble newConceptThreshold = new AtomicDouble(0);

    /**
     * Minimum required priority for a concept
     * to be allowed to process if it has been sampled from the bag,
     * (TODO otherwise it accumulates the priority in its budget?)
     */
    public AtomicDouble conceptFireThreshold = new AtomicDouble(0.0);

    /** budget summary necessary for Concept to be set ACTIVE.
     *  if it is not enough, the concept will be immediately forgotten
     *  into subconcepts. */
    public final AtomicDouble activeConceptThreshold = new AtomicDouble(0);

    /** budget summary necessary to execute a desired Goal */
    public final AtomicDouble goalThreshold = new AtomicDouble(0);

    /** budget summary necessary to run a TaskProcess for a given Task
     *  this should be equal to zero to allow subconcept seeding. */
    public final AtomicDouble taskProcessThreshold = new AtomicDouble(0);

    /** budget summary necessary to propagte tasklink activation */
    public final AtomicDouble taskLinkThreshold = new AtomicDouble(0);

    /** budget summary necessary to propagte termlink activation */
    public final AtomicDouble termLinkThreshold = new AtomicDouble(0);

    /** Minimum expectation for a desire value. 
     *  the range of "now" is [-DURATION, DURATION]; */
    public final AtomicDouble executionThreshold = new AtomicDouble();




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

    /**
     * this value is multiplied by the size of the termlink bag to determine
     * how long ago a termlink will be considered completely novel for pairing
     * with a tasklink during fire,
     *
     * a value of 1.0 then means that it should take N cycles before a term
     * is considered completely novel to a Tasklink.
     * */
    public AtomicDouble noveltyHorizon = new AtomicDouble();

    /** probability that a completely non-novel termlink/tasklink pair (older than novelty horizon) will be selected */
    public static float NOVELTY_FLOOR = 0.05f;


    /** Reliance factor, the empirical confidence of analytical truth.
        (generally, the same as default judgment confidence)  */
    public final AtomicDouble reliance = new AtomicDouble();


    

    public Clock getTiming() {
        return clock;
    }




/*    public static Param fromJSON(String json) {
        return Param.json.fromJson(json, Param.class);
    }*/
//    @Override
//    public String toString() {
//        return Json.toJson(this);
//    }
    
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
    
//    static public final Gson json;
//    static {
//        GsonBuilder b = new GsonBuilder();
//        b.setPrettyPrinting();
//        b.disableHtmlEscaping();
//        b.serializeNulls();
//
//        final JsonSerializer<AtomicDouble> atomicDoubleSerializer = new JsonSerializer<AtomicDouble>() {
//            @Override public JsonElement serialize(AtomicDouble t, Type type, JsonSerializationContext jsc) {
//                return new JsonPrimitive(t.get());
//            }
//        };
//
//        JsonDeserializer<AtomicDouble> atomicDoubleDeserializer = new JsonDeserializer<AtomicDouble>() {
//            @Override public AtomicDouble deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
//                return new AtomicDouble(je.getAsDouble());
//            }
//        };
//
//        b.registerTypeAdapter(AtomicDouble.class, atomicDoubleSerializer);
//        b.registerTypeAdapter(AtomicDouble.class, atomicDoubleDeserializer);
//
//        b.registerTypeAdapter(AtomicInteger.class, new JsonSerializer<AtomicInteger>() {
//            @Override public JsonElement serialize(AtomicInteger t, Type type, JsonSerializationContext jsc) {
//                return new JsonPrimitive(t.get());
//            }
//        });
//        b.registerTypeAdapter(AtomicInteger.class, new JsonDeserializer<AtomicInteger>() {
//            @Override public AtomicInteger deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
//                return new AtomicInteger(je.getAsInt());
//            }
//        });
//
//
//        json = b.create();
//    }


    public Clock getClock() {
        return clock;
    }

}
