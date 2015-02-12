package nars.io.condition;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import nars.core.Events;
import nars.core.NAR;
import nars.core.Parameters;
import nars.io.Texts;
import nars.io.narsese.InvalidInputException;
import nars.logic.entity.Task;
import nars.logic.entity.Term;
import nars.logic.entity.TruthValue;
import nars.logic.nal7.Tense;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;


public class TaskCondition extends OutputCondition implements Serializable {

    @Expose
    public final Class channel;
    @Expose
    public final Term term;
    @Expose
    public final char punc;
    @Expose
    public Tense tense;
    @Expose
    public final float freqMin;
    @Expose
    public final float freqMax;
    @Expose
    public final float confMin;
    @Expose
    public final float confMax;
    @Expose
    public final long cycleStart; //-1 for not compared
    @Expose
    public final long cycleEnd;  //-1 for not compared

    /** min and max occurrenceTime range (relative to current time which the task is output); not checked if tense==ETERNAL, checked otherwise */
    @Expose
    public int ocMin = -1,ocMax= -1;

    public final List<Task> trueAt = new ArrayList();
    public final Deque<Task> removals = new ArrayDeque();

    final int maxRemovals = 4;

    Task closest = null;
    double closestDistance = Double.POSITIVE_INFINITY;

    public TaskCondition(NAR n, Class channel, Task t)  {
        super(n, Events.OUT.class, Events.TaskRemove.class);

        //TODO verify that channel is included in the listened events

        this.channel = channel;
        this.tense = Tense.Eternal;
        this.cycleStart = this.cycleEnd = -1;
        if (t.sentence.truth!=null) {
            float f = t.sentence.truth.getFrequency();
            float c = t.sentence.truth.getConfidence();
            float e = Parameters.TESTS_TRUTH_ERROR_TOLERANCE/2f; //error tolerance epsilon
            this.freqMin = f - e;
            this.freqMax = f + e;
            this.confMin = c - e;
            this.confMax = c + e;
        }
        else {
            this.freqMin = this.freqMax =this.confMin = this.confMax = -1;
        }
        this.punc = t.sentence.punctuation;
        this.term = t.getTerm();
    }

    public TaskCondition(NAR n, Class channel, long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax) throws InvalidInputException {
        super(n);
        if (freqMax < freqMin) throw new RuntimeException("freqMax < freqMin");
        if (confMax < confMin) throw new RuntimeException("confMax < confMin");

        if (cycleEnd - cycleStart < 1) throw new RuntimeException("cycleEnd must be after cycleStart by at least 1 cycle");

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

    //continue examining output task in case a closer result appears
    @Override
    protected boolean continueAfterSuccess() { return true; }

    public double getAcceptableDistanceThreshold() {
        return 0.01;
    }

    //how many multiples of the range it is away from the acceptable time interval
    public static double rangeError(double value, double min, double max, boolean squash) {
        double dt;
        if (value < min)
            dt = min - value;
        else if (value > max)
            dt = value - max;
        else
            return 0;

        double result = dt/(max-min);

        if (squash)
            return Math.tanh(result);
        else
            return result;
    }

    //time distance function
    public double getTimeDistance(long now) {
        return rangeError(now, cycleStart, cycleEnd, true);
    }

    //truth distance function
    public double getTruthDistance(TruthValue t) {
        //manhattan distance:
        return rangeError(t.getFrequency(), freqMin, freqMax, true) +
                rangeError(t.getConfidence(), confMin, confMax, true);

        //we could also calculate geometric/cartesian vector distance
    }

    /** relative to the current time when processing */
    public void setOccurrenceTime(int min, int max) {
        this.ocMin = min;
        this.ocMax = max;
        this.tense = Tense.Present; //any tense will work here as long as it's not Eternal, so Present uesd here can still refer to past or future
    }

    public boolean matches(Task task) {
        if (task.sentence.punctuation != punc)
            return false;
        long now = nar.time();
        if (task.sentence.punctuation != punc)
            return false;


        //require right kind of tense
        if (tense==Tense.Eternal) {
            if (!task.sentence.isEternal())  return false;
        }
        else {
            if (task.sentence.isEternal()) return false;

            long oc = task.getOcurrenceTime() - now; //relative time
            if ((oc < ocMin) || (oc > ocMax)) return false;
        }

        Term tterm = task.getTerm();

        //require exact term
        if (!tterm.equals(this.term)) {
            return false;
        }

        return true;

    }

    @Override
    public void event(Class channel, Object... args) {
        if (!succeeded && (channel == Events.TaskRemove.class)) {
            Task task = (Task)args[0];
            //String reason = (String)args[1];

            if (matches(task)) {
                removals.addLast(task);
                if (removals.size() > maxRemovals)
                    removals.removeFirst();
            }
        }

        super.event(channel, args);


    }

    @Override
    public boolean condition(Class channel, Object signal) {

        if (channel == Events.OUT.class) {

            if (signal instanceof Task) {

                Task task = (Task) signal;

                if (!matches(task)) return false;

                double distance = 0;
                long now = nar.time();


                boolean match = false;

                if ( ((cycleStart!=-1) && (now < cycleStart)) ||
                        ((cycleEnd!=-1) && (now > cycleEnd)))  {
                    distance += getTimeDistance(now);
                    match = false;
                }


                //TODO use range of acceptable occurrenceTime's for non-eternal tests


                if ((punc == '.') || (punc == '!')) {
                    float fr = task.sentence.truth.getFrequency();
                    float co = task.sentence.truth.getConfidence();

                    if ((co > confMax) || (co < confMin) || (fr > freqMax) || (fr < freqMin)) {
                        match = false;
                        distance += getTruthDistance(task.sentence.truth);
                    }
                }

                if (!match && distance < getAcceptableDistanceThreshold())
                    match = true;

                if (match) {
                    //TODO record a different score for fine-tune optimization?
                    trueAt.add(task);
                }

                if (distance < closestDistance) {
                    closest = task;
                    closestDistance = distance;
                }

                return match;

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
            return new JsonPrimitive(((Class) o).getSimpleName());
        }

    };

    final static Gson j = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(Class.class, classString)
            .registerTypeAdapter(Term.class, asString)
            .excludeFieldsWithoutExposeAnnotation().create();

    @Override
    public String getFalseReason() {
        String x = "Unmatched: " + (closest==null ? "" :
                "  closest=" + closest + " dist=" + Texts.n4((float) closestDistance)) + " " + toString();

        if (!removals.isEmpty()) {
            x += "\n  Matching removals:\n";
            for (Task t : removals)
                x += t.toString() + " (" + t.getReason() + ")\n";
        }
        return x;
    }

    public TruthValue getTruthMean() {
        return new TruthValue(0.5f * (freqMax + freqMin), 0.5f * (confMax + confMin));
    }

    @Override
    public List<Task> getTrueReasons() {
        if (!isTrue()) throw new RuntimeException(this + " is not true so has no true reasons");
        /*return Lists.newArrayList("match at: " +

                Iterables.transform(trueAt, new Function<Task, String>() {
                    @Override
                    public String apply(Task task) {
                        return task.toString() + " @ " + task.sentence.getCreationTime();
                    }
                }));
                */
        return trueAt;
    }

    @Override
    public String toString() {
        return succeeded  +": "  +j.toJson(this);
    }
}
