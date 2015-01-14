package nars.core.logic.nal1;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import nars.core.NAR;
import nars.io.condition.OutputCondition;
import nars.io.narsese.Narsese;
import nars.logic.entity.Task;
import nars.logic.entity.Term;
import nars.logic.nal7.Tense;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
* Created by me on 1/14/15.
*/
public class TaskCondition extends OutputCondition implements Serializable {

    @Expose public final long cycleStart;
    @Expose public final long cycleEnd;
    @Expose public final Term sentenceTerm;
    @Expose public final char punc;
    @Expose public final float freqMin;
    @Expose public final float freqMax;
    @Expose public final float confMin;
    @Expose public final float confMax;
    @Expose public final Class channel;
    @Expose public final Tense tense;

    public TaskCondition(NAR n, Class channel, long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax) throws Narsese.InvalidInputException {
        super(n);
        this.channel = channel;
        this.tense = Tense.Eternal;
        this.cycleStart = cycleStart;
        this.cycleEnd = cycleEnd;
        this.freqMax = freqMax;
        this.freqMin = freqMin;
        this.confMax = confMax;
        this.confMin = confMin;
        this.punc = punc;
        this.sentenceTerm = n.term(sentenceTerm);
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


                        if (term.equals(sentenceTerm)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    final static Gson j = new GsonBuilder().registerTypeAdapter(Class.class, new JsonSerializer() {

        @Override
        public JsonElement serialize(Object o, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(((Class)o).getSimpleName());
        }

    }).excludeFieldsWithoutExposeAnnotation().create();

    @Override
    public String getFalseReason() {
        return "Unmatched: " + j.toJson(this);
    }
}
