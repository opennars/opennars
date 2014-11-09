package nars.core;

import java.util.concurrent.atomic.AtomicInteger;
import nars.core.Memory.Forgetting;
import nars.core.Memory.Timing;
import nars.language.Interval.AtomicDuration;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * NAR Parameters which can be changed during runtime.
 */
public class Param implements Serializable {
    

    public Param() {    }

    /** Silent threshold for task reporting, in [0, 100]. 
     *  Noise level = 100 - silence level; noise 0 = always silent, noise 100 = never silent
     */
    public final AtomicInteger noiseLevel = new AtomicInteger();
    
    /** 
       Cycles per duration.
       Past/future tense usage convention;
       How far away "past" and "future" is from "now", in cycles.         
       The range of "now" is [-DURATION/2, +DURATION/2];      */
    public final AtomicDuration duration = new AtomicDuration();

    Timing timing;
    Forgetting forgetting;
    
    public static Param fromJSON(String json) {
        return Param.json.fromJson(json, Param.class);
    }
    

    /** converts durations to cycles */
    public final float cycles(AtomicDouble durations) {
        return duration.floatValue() * durations.floatValue();
    }
    
    /** Concept decay rate in ConceptBag, in [1, 99].  originally: CONCEPT_FORGETTING_CYCLE 
     *  How many cycles it takes an item to decay completely to a threshold value (ex: 0.1).
     *  Lower means faster rate of decay.
     */
    public final AtomicDouble conceptForgetDurations = new AtomicDouble();
    
    /** TermLink decay rate in TermLinkBag, in [1, 99]. originally: TERM_LINK_FORGETTING_CYCLE */
    public final AtomicDouble termLinkForgetDurations = new AtomicDouble();
    
    /** TaskLink decay rate in TaskLinkBag, in [1, 99]. originally: TASK_LINK_FORGETTING_CYCLE */
    public final AtomicDouble taskLinkForgetDurations = new AtomicDouble();
    
    public final AtomicDouble novelTaskForgetDurations = new AtomicDouble();

    
    /** Minimum expectation for a desire value. 
     *  the range of "now" is [-DURATION, DURATION]; */
    public final AtomicDouble decisionThreshold = new AtomicDouble();


    /** How many concepts to fire each cycle; measures degree of parallelism in each cycle */
    public final AtomicInteger conceptsFiredPerCycle = new AtomicInteger();
    
    /** Maximum TermLinks checked for novelty for each TaskLink in TermLinkBag */
    public final AtomicInteger termLinkMaxMatched = new AtomicInteger();
            
    
//    //let NARS use NARS+ ideas (counting etc.)
//    public final AtomicBoolean experimentalNarsPlus = new AtomicBoolean();
//
//    //let NARS use NAL9 operators to perceive its own mental actions
//    public final AtomicBoolean internalExperience = new AtomicBoolean();
    
    //these two are AND-coupled:
    //when a concept is important and exceeds a syntactic complexity, let NARS name it: 
    public final AtomicInteger abbreviationMinComplexity = new AtomicInteger();
    public final AtomicDouble abbreviationMinQuality = new AtomicDouble();
    
    /** Maximum TermLinks used in reasoning for each Task in Concept */
    public final AtomicInteger termLinkMaxReasoned = new AtomicInteger();

    /** Record-length for newly created TermLink's */
    public final AtomicInteger termLinkRecordLength = new AtomicInteger();
    
    /** Maximum number of beliefs kept in a Concept */
    public final AtomicInteger conceptBeliefsMax = new AtomicInteger();
    
    /** Maximum number of questions kept in a Concept */
    public final AtomicInteger conceptQuestionsMax = new AtomicInteger();

    /** Maximum number of goals kept in a Concept */
    public final AtomicInteger conceptGoalsMax = new AtomicInteger();
    
    /** Reliance factor, the empirical confidence of analytical truth.
        the same as default confidence  */        
    public final AtomicDouble reliance = new AtomicDouble();
    
    
    public void setTiming(Timing time) {
        this.timing = time;
    }

    public Timing getTiming() {
        return timing;
    }

    public Forgetting getForgetMode() {
        return forgetting;
    }

    public void setForgetting(Forgetting forgetMode) {
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
    
}
