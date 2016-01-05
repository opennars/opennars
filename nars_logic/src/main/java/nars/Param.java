package nars;

import nars.budget.BudgetFunctions;
import nars.data.Range;
import nars.nal.Level;
import nars.task.MutableTask;
import nars.truth.DefaultTruth;
import nars.truth.Truth;
import nars.util.data.MutableInteger;
import objenome.Container;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.concurrent.atomic.AtomicInteger;

import static nars.Symbols.GOAL;
import static nars.Symbols.JUDGMENT;

/**
 * NAR Parameters which can be changed during runtime.
 */
public abstract class Param extends Container implements Level {


    public final MutableInteger cyclesPerFrame = new MutableInteger(1);

    /**
     * Perfection determines a minimum budget prioritization of items
     * in proportion to the budgeted quality. This means that the
     * attention of a reasoner in higher perfection state
     * will be more dominated by high-quality items.  In other words,
     * it is driven more towards maximization of quality rather than
     * allowing for exploration of that which has less quality.
     *
     * previously called "bag threshold" and had default value = 0.1
     */
    @Range(min=0, max=1f)
    public MutableFloat perfection = new MutableFloat(0.1);


    //    /** Silent threshold for task reporting, in [0, 100].
//     *  Noise level = 100 - silence level; noise 0 = always silent, noise 100 = never silent
//     */
//    public final AtomicInteger outputVolume = new AtomicInteger();

    /** 
       Cycles per duration.
       Past/future tense usage convention;
       How far away "past" and "future" is from "now", in cycles.         
       The range of "now" is [-DURATION/2, +DURATION/2];      */
    public final MutableInteger duration = new MutableInteger();

    public final MutableInteger shortTermMemoryHistory = new MutableInteger();



//    /** converts durations to cycles */
//    public final float durationToCycles(MutableFloat durations) {
//        return durationToCycles(durations.floatValue());
//    }
//
//    public final float durationToCycles(float durations) {
//        return duration.floatValue() * durations;
//    }







//    /** Concept decay rate in ConceptBag, in [1, 99].  originally: CONCEPT_FORGETTING_CYCLE
//     *  How many cycles it takes an item to decay completely to a threshold value (ex: 0.1).
//     *  Lower means faster rate of decay.
//     */
//    @Deprecated public final MutableFloat conceptForgetDurations = new MutableFloat();
    
    /** TermLink decay rate in TermLinkBag, in [1, 99]. originally: TERM_LINK_FORGETTING_CYCLE */
    //TODO use separate termlink forget rates whether the termlink was actually selected for firing or not.
    //@Deprecated public final MutableFloat termLinkForgetDurations = new MutableFloat();
    
    /** TaskLink decay rate in TaskLinkBag, in [1, 99]. originally: TASK_LINK_FORGETTING_CYCLE */
    @Deprecated public final MutableFloat linkForgetDurations = new MutableFloat();

    @Deprecated public final MutableFloat conceptForgetDurations = new MutableFloat();
    

     /*
     BUDGET THRESHOLDS
     * Increasing this value decreases the resolution with which
     *   budgets are propagated or otherwise measured, which can result
     *   in a performance gain.      */


//    /** budget summary necessary to Conceptualize. this will compare the summary of the task during the TaskProcess */
//    public final AtomicDouble newConceptThreshold = new AtomicDouble(0);

    /** budget durability threshold  necessary to form a derived task. */
    public final MutableFloat derivationDurabilityThreshold = new MutableFloat(0);


//    /** budget summary necessary to execute a desired Goal */
//    public final AtomicDouble questionFromGoalThreshold = new AtomicDouble(0);

    /** budget summary necessary to run a TaskProcess for a given Task
     *  this should be equal to zero to allow subconcept seeding. */
    public final MutableFloat taskProcessThreshold = new MutableFloat(0);

    /** budget summary necessary to propagte tasklink activation */
    public final MutableFloat taskLinkThreshold = new MutableFloat(0);

    /** budget summary necessary to propagte termlink activation */
    public final MutableFloat termLinkThreshold = new MutableFloat(0);

    /** Minimum expectation for a desire value.
     *  the range of "now" is [-DURATION, DURATION]; */
    public final MutableFloat executionExpectationThreshold = new MutableFloat();




    /** Maximum number of beliefs kept in a Concept */
    public final AtomicInteger conceptBeliefsMax = new AtomicInteger();
    
    /** Maximum number of questions, and max # of quests kept in a Concept */
    public final AtomicInteger conceptQuestionsMax = new AtomicInteger();

    /** Maximum number of goals kept in a Concept */
    public final AtomicInteger conceptGoalsMax = new AtomicInteger();

    public float getDefaultConfidence(char punctuation) {

        switch (punctuation) {
            case JUDGMENT:
                return DEFAULT_JUDGMENT_CONFIDENCE;

            case GOAL:
                return DEFAULT_GOAL_CONFIDENCE;

            default:
                throw new RuntimeException("Invalid punctuation " + punctuation + " for a TruthValue");
        }
    }

    public void applyDefaultBudget(MutableTask t) {

        char punc = t.getPunctuation();
        t.setPriority(getDefaultPriority(punc));
        t.setDurability(getDefaultDurability(punc));

        if (t.isJudgmentOrGoal()) {

            /** if q was not specified, and truth is, then we can calculate q from truthToQuality */
            if (Float.isNaN(t.getQuality())) {
                t.setQuality(BudgetFunctions.truthToQuality(t.getTruth()));
            }
        } else if (t.isQuestion()) {
            t.setQuality(DEFAULT_QUESTION_QUALITY);
        } else if (t.isQuest()) {
            t.setQuality(DEFAULT_QUEST_QUALITY);
        }

    }

    /** Default confidence of input judgment. */
    float DEFAULT_JUDGMENT_CONFIDENCE = 0.9f;

    /** Default priority of input judgment */
    float DEFAULT_JUDGMENT_PRIORITY = 0.5f;
    /** Default durability of input judgment */
    float DEFAULT_JUDGMENT_DURABILITY = 0.5f; //was 0.8 in 1.5.5; 0.5 after
    /** Default priority of input question */
    float DEFAULT_QUESTION_PRIORITY = 0.5f;
    /** Default durability of input question */
    float DEFAULT_QUESTION_DURABILITY = 0.5f;


    /** Default confidence of input goal. */
    float DEFAULT_GOAL_CONFIDENCE = 0.9f;
    /** Default priority of input judgment */
    float DEFAULT_GOAL_PRIORITY = 0.5f;
    /** Default durability of input judgment */
    float DEFAULT_GOAL_DURABILITY = 0.5f;
    /** Default priority of input question */
    float DEFAULT_QUEST_PRIORITY = 0.5f;
    /** Default durability of input question */
    float DEFAULT_QUEST_DURABILITY = 0.5f;

    float DEFAULT_QUESTION_QUALITY = 0.5f;
    float DEFAULT_QUEST_QUALITY = 0.5f;

    float getDefaultPriority(char punctuation) {
        switch (punctuation) {
            case Symbols.JUDGMENT:
                return DEFAULT_JUDGMENT_PRIORITY;

            case Symbols.QUEST:
                return DEFAULT_QUEST_PRIORITY;

            case Symbols.QUESTION:
                return DEFAULT_QUESTION_PRIORITY;

            case Symbols.GOAL:
                return DEFAULT_GOAL_PRIORITY;
        }
        throw new RuntimeException("Unknown sentence type: " + punctuation);
    }

    float getDefaultDurability(char punctuation) {
        switch (punctuation) {
            case Symbols.JUDGMENT:
                return DEFAULT_JUDGMENT_DURABILITY;
            case Symbols.QUEST:
                return DEFAULT_QUEST_DURABILITY;
            case Symbols.QUESTION:
                return DEFAULT_QUESTION_DURABILITY;
            case Symbols.GOAL:
                return DEFAULT_GOAL_DURABILITY;
        }
        throw new RuntimeException("Unknown sentence type: " + punctuation);
    }
    float getDefaultQuality(char punctuation) {
        switch (punctuation) {
            case Symbols.QUESTION:
                return DEFAULT_QUESTION_DURABILITY;
            case Symbols.GOAL:
                return DEFAULT_GOAL_DURABILITY;
        }
        throw new RuntimeException("Use truthToQuality for: " + punctuation);
    }

    //decision threshold is enough for now
    float EXECUTION_SATISFACTION_TRESHOLD = 0;

    public float getExecutionSatisfactionThreshold() {
        return EXECUTION_SATISFACTION_TRESHOLD;
    }

    public final Truth newDefaultTruth(char p) {
        switch (p) {
            case GOAL:
            case JUDGMENT: return new DefaultTruth(1.0f, getDefaultConfidence(p));
            default:
                return null;
        }
    }


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
