package nars.io.condition;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import nars.core.Events;
import nars.core.NAR;
import nars.core.Parameters;
import nars.io.narsese.InvalidInputException;
import nars.logic.entity.Stamp;
import nars.logic.entity.Task;
import nars.logic.entity.Term;
import nars.logic.entity.TruthValue;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;


public class TaskCondition extends OutputCondition implements Serializable {

    @Expose
    public final Class channel;
    @Expose
    public final Term term;
    @Expose
    public final char punc;

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
    private final long creationTime;

    /** min and max occurrenceTime range (relative to current time which the task is output); not checked if tense==ETERNAL, checked otherwise */
    @Expose
    public long ocMin = -1,ocMax= -1;

    public final List<Task> trueAt = new ArrayList();
    public final Deque<Task> removals = new ArrayDeque();

    final int maxClose = 7;
    public final TreeMap<Double,Task> close = new TreeMap();

    final int maxRemovals = 2;


    public TaskCondition(NAR n, Class channel, Task t, long occurenceTimeOffset)  {
        super(n, Events.OUT.class, Events.TaskRemove.class);

        //TODO verify that channel is included in the listened events

        this.channel = channel;


        this.creationTime = occurenceTimeOffset;

        if (t.sentence.isEternal()) {
            setEternal();
        }
        else {
            //prcise:
            long oc = t.getOcurrenceTime() + occurenceTimeOffset;
            long dur = n.memory.getDuration();
            setOccurrenceTime(oc - dur, oc + dur);


            //open interval NOT CORRECT YET:
            /*
            long oc = t.getOcurrenceTime() + t.getCreationTime();
            if (oc > 0) {
                setOccurrenceTime(Integer.MIN_VALUE, occurenceTimeOffset);
            }
            else if (oc < 0) {
                setOccurrenceTime(occurenceTimeOffset, Integer.MAX_VALUE);
            }
            else {
                long dur = n.memory.getDuration();
                setOccurrenceTime(occurenceTimeOffset-dur/2, occurenceTimeOffset + dur/2);
            }*/

        }


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

        this.creationTime = n.time();
        this.channel = channel;
        this.cycleStart = cycleStart;
        this.cycleEnd = cycleEnd;
        setEternal();
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
    public void setOccurrenceTime(long min, long max) {
        this.ocMin = min;
        this.ocMax = max;
    }


    public void setEternal() { this.ocMin = this.ocMax = Stamp.ETERNAL; }
    public boolean isEternal() { return this.ocMin == Stamp.ETERNAL; }

    public boolean matches(Task task) {
        if (task.sentence.punctuation != punc)
            return false;
        //long now = nar.time();

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


                float tenseCost = 0.5f;

                //require right kind of tense
                if (isEternal()) {
                    if (!task.sentence.isEternal()) {
                        distance += tenseCost;
                        match = false;
                    }
                }
                else {
                    if (task.sentence.isEternal()) {
                        distance += tenseCost - 0.01;
                        match = false;
                    }
                    else {
                        long oc = task.getOcurrenceTime() - task.getCreationTime(); //relative time
                        if ((oc < ocMin) || (oc > ocMax)) {
                            distance += rangeError(oc, ocMin, ocMax, true) * tenseCost;
                            match = false;
                        }
                    }
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

                if (distance > 0) {
                    close.put(distance, task);
                    if (close.size() > maxClose) {
                        close.remove( close.lastEntry().getKey() );
                    }
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
        String x = "Unmatched; ";

        if (!close.isEmpty()) {
            x += "Similar:\n";
            for (Map.Entry<Double,Task> et : close.entrySet())
                x += et.getKey() + " (" + et.getValue() + ")\n";
        }
        else {
            x += "No similar\n";
        }
        if (!removals.isEmpty()) {
            x += "Matching removals:\n";
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

    public long getCreationTime() {
        return creationTime;
    }
    public long getMeanOccurrenceTime() {
        if (isEternal()) return Stamp.ETERNAL;
        return (ocMin + ocMax) / 2L;
    }
}
