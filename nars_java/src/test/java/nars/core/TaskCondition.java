package nars.core;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.google.gson.annotations.Expose;
import nars.core.NAR;
import nars.io.condition.OutputCondition;
import nars.io.narsese.Narsese;
import nars.logic.entity.Task;
import nars.logic.entity.Term;
import nars.logic.entity.TruthValue;
import nars.logic.nal7.Tense;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
* Created by me on 1/14/15.
*/
public class TaskCondition extends OutputCondition implements Serializable {

    @Expose public final Class channel;
    @Expose public final Term term;
    @Expose public final char punc;
    @Expose public final Tense tense;
    @Expose public final float freqMin;
    @Expose public final float freqMax;
    @Expose public final float confMin;
    @Expose public final float confMax;
    @Expose public final long cycleStart;
    @Expose public final long cycleEnd;

    public final List<Long> trueAt = new ArrayList();

    public TaskCondition(NAR n, Class channel, long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax) throws Narsese.InvalidInputException {
        super(n);
        if (freqMax < freqMin)  throw new RuntimeException("freqMax < freqMin");
        if (confMax < confMin)  throw new RuntimeException("confMax < confMin");

        this.channel = channel;
        this.tense = Tense.Eternal;
        this.cycleStart = cycleStart;
        this.cycleEnd = cycleEnd;
        this.freqMax = Math.min(1.0f, freqMax);
        this.freqMin = Math.max(0f, freqMin);
        this.confMax = Math.min(1.0f, confMax);
        this.confMin = Math.max(0f, confMin);
        this.punc = punc;
        this.term = n.term(sentenceTerm);
    }

    @Override
    public boolean condition(Class channel, Object signal) {
        long t = nar.time();
        if ((t < cycleStart) || (t > cycleEnd))
            return false;

        if (channel == OUT.class) {

            if (signal instanceof Task) {

                Task task = (Task)signal;
                Term term = task.getTerm();

                if (task.sentence.punctuation == punc) {

                    if ((tense == Tense.Eternal) && (!task.sentence.isEternal()))
                       return false;
                    //TODO use range of acceptable occurrenceTime's for non-eternal tests

                    float fr = task.sentence.truth.getFrequency();
                    float co = task.sentence.truth.getConfidence();

                    if ((co <= confMax) && (co >= confMin) && (fr <= freqMax) && (fr >= freqMin)) {


                        if (term.equals(this.term)) {
                            trueAt.add(nar.time());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public final static JsonSerializer asString = new JsonSerializer() {

        @Override
        public JsonElement serialize(Object o, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(o.toString());
        }

    };

    public final static JsonSerializer classString = new JsonSerializer() {

        @Override
        public JsonElement serialize(Object o, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(((Class)o).getSimpleName());
        }

    };

    final static Gson j = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(Class.class, classString)
            .registerTypeAdapter(Term.class, asString)
            .excludeFieldsWithoutExposeAnnotation().create();

    @Override
    public String getFalseReason() {
        return "Unmatched: " + toString();
    }

    public TruthValue getTruthMean() {
        return new TruthValue( 0.5f * (freqMax + freqMin), 0.5f * (confMax + confMin) );
    }

    public List getTrueReasons() {
        if (!isTrue()) throw new RuntimeException(this + " is not true so has no true reasons");
        return Lists.newArrayList("match at: " + trueAt);
    }

    @Override
    public String toString() {
        return j.toJson(this);
    }
}
