package nars;

import com.google.common.util.concurrent.AtomicDouble;
import nars.nal.nal7.AtomicDuration;
import nars.util.data.MutableInteger;
import objenome.Container;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NAR Parameters which can be changed during runtime.
 */
public abstract class Param extends Container implements Serializable {


    public final MutableInteger cyclesPerFrame = new MutableInteger(1);


    public Param() {    }

//    /** Silent threshold for task reporting, in [0, 100].
//     *  Noise level = 100 - silence level; noise 0 = always silent, noise 100 = never silent
//     */
//    public final AtomicInteger outputVolume = new AtomicInteger();

    /** 
       Cycles per duration.
       Past/future tense usage convention;
       How far away "past" and "future" is from "now", in cycles.         
       The range of "now" is [-DURATION/2, +DURATION/2];      */
    public final AtomicDuration duration = new AtomicDuration();

    public final AtomicInteger shortTermMemoryHistory = new AtomicDuration();



    /** converts durations to cycles */
    public final float durationToCycles(AtomicDouble durations) {
        return durationToCycles(durations.floatValue());
    }

    public final float durationToCycles(float durations) {
        return duration.floatValue() * durations;
    }


    /**
     * How much a concept is activated.
     * 1.0 means all activation is applied,
     * 0.0 means none is.
     */
    public AtomicDouble conceptActivationFactor = new AtomicDouble(1.0);

//    /** scaling factor for priority of input tasks */
//    public AtomicDouble inputActivationFactor = new AtomicDouble(1.0);

    /** minimum expectation necessary to create a concept
     *  original value: 0.66
     * */
    public AtomicDouble conceptCreationExpectation = new AtomicDouble();





    /** Concept decay rate in ConceptBag, in [1, 99].  originally: CONCEPT_FORGETTING_CYCLE 
     *  How many cycles it takes an item to decay completely to a threshold value (ex: 0.1).
     *  Lower means faster rate of decay.
     */
    @Deprecated public final AtomicDouble conceptForgetDurations = new AtomicDouble();
    
    /** TermLink decay rate in TermLinkBag, in [1, 99]. originally: TERM_LINK_FORGETTING_CYCLE */
    //TODO use separate termlink forget rates whether the termlink was actually selected for firing or not.
    @Deprecated public final AtomicDouble termLinkForgetDurations = new AtomicDouble();
    
    /** TaskLink decay rate in TaskLinkBag, in [1, 99]. originally: TASK_LINK_FORGETTING_CYCLE */
    @Deprecated public final AtomicDouble taskLinkForgetDurations = new AtomicDouble();
    

     /*
     BUDGET THRESHOLDS
     * Increasing this value decreases the resolution with which
     *   budgets are propagated or otherwise measured, which can result
     *   in a performance gain.      */


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
    public final AtomicDouble questionFromGoalThreshold = new AtomicDouble(0);

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




    /** Maximum number of beliefs kept in a Concept */
    public final AtomicInteger conceptBeliefsMax = new AtomicInteger();
    
    /** Maximum number of questions, and max # of quests kept in a Concept */
    public final AtomicInteger conceptQuestionsMax = new AtomicInteger();

    /** Maximum number of goals kept in a Concept */
    public final AtomicInteger conceptGoalsMax = new AtomicInteger();




//    /** Reliance factor, the empirical confidence of analytical truth.
//        (generally, the same as default judgment confidence)  */
//    public final AtomicDouble reliance = new AtomicDouble();




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

}
