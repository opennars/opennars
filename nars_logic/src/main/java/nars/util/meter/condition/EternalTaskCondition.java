package nars.util.meter.condition;


import nars.Global;
import nars.NAR;
import nars.Narsese;
import nars.task.DefaultTask;
import nars.task.Task;
import nars.truth.DefaultTruth;
import nars.truth.Stamp;
import nars.truth.Truth;
import nars.util.Texts;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EternalTaskCondition extends DefaultTask implements NARCondition, Predicate<Task>, Consumer<Task> {

    protected final NAR nar;
    boolean succeeded = false;
    long successTime = Stamp.TIMELESS;

    //@Expose

    //@JsonSerialize(using= JSONOutput.TermSerializer.class)
    //public  Term term;

    //@Expose
    //public  char punc;

    public  float freqMin;
    public  float freqMax;
    public  float confMin;
    public  float confMax;
    public  long creationStart, creationEnd; //-1 for not compared

    /*float tenseCost = 0.35f;
    float temporalityCost = 0.75f;*/


    //private final Observed.DefaultObserved.DefaultObservableRegistration taskRemoved;

    //@Expose
    //protected long creationTime;


    //@Expose
    //public Tense tense = Tense.Eternal;


    public List<Task> valid;


    transient int maxSimilars = 3;

    protected TreeMap<Float,Task> similar;
    transient private HashSet<Task> similarset;


    @Override
    public final Truth getTruth() {
        return DefaultTruth.NULL;
    }

    public EternalTaskCondition(NAR n, long creationStart, long creationEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax) throws Narsese.NarseseException {
        super(n.task(sentenceTerm + punc));

        this.nar = n;

        if (freqMax < freqMin) throw new RuntimeException("freqMax < freqMin");
        if (confMax < confMin) throw new RuntimeException("confMax < confMin");

        if (creationEnd - creationStart < 1) throw new RuntimeException("cycleEnd must be after cycleStart by at least 1 cycle");


        setCreationTime(n.time());
        this.creationStart = creationStart;
        this.creationEnd = creationEnd;
        setEternal();
        this.freqMax = Math.min(1.0f, freqMax);
        this.freqMin = Math.max(0.0f, freqMin);
        this.confMax = Math.min(1.0f, confMax);
        this.confMin = Math.max(0.0f, confMin);
        setPunctuation(punc);
        setTerm(n.term(sentenceTerm));
        setDuration(n.memory.duration());
    }

//    public double getAcceptableDistanceThreshold() {
//        return 0.01;
//    }

//    //how many multiples of the range it is away from the acceptable time interval
//    public static double rangeError(double value, double min, double max, boolean squash) {
//        double dt;
//        if (value < min)
//            dt = min - value;
//        else if (value > max)
//            dt = value - max;
//        else
//            return 0;
//
//        double result = dt/(max-min);
//
//        if (squash)
//            return Math.tanh(result);
//        else
//            return result;
//    }

//    //time distance function
//    public double getTimeDistance(long now) {
//        return rangeError(now, creationStart, creationEnd, true);
//    }

//    //truth distance function
//    public double getTruthDistance(Truth t) {
//        //manhattan distance:
//        return rangeError(t.getFrequency(), freqMin, freqMax, true) +
//                rangeError(t.getConfidence(), confMin, confMax, true);
//
//        //we could also calculate geometric/cartesian vector distance
//    }

////    public void setRelativeOccurrenceTime(Tense t, int duration) {
////        setRelativeOccurrenceTime(Stamp.getOccurrenceTime(t, duration), duration);
////    }
//    /** task's tense is compared by its occurence time delta to the current time when processing */
//    public void setRelativeOccurrenceTime(long ocRelative, int duration) {
//        //may be more accurate if duration/2
//
//        Tense tense;
//        final float ocRel = ocRelative; //cast to float for this compare
//        if (ocRel > duration/2f) tense = Tense.Future;
//        if (ocRel < -duration/2f) tense = Tense.Past;
//        else tense = Tense.Present;
//
//        setRelativeOccurrenceTime(tense, nar.memory.duration());
//
//    }


//
//    public void setRelativeOccurrenceTime(long creationTime, int ocRelative, int duration) {
//        setCreationTime(creationTime);
//        setRelativeOccurrenceTime(ocRelative, duration);
//    }



    public boolean matches(Task task) {
        if (task == null) {
            return false;
        }

        if (!task.getTerm().equals(getTerm())) return false;

        if (task.getPunctuation() != getPunctuation())
            return false;

        if (!timeMatches(task))
            return false;




        //require exact term
        return true;

    }

    public boolean timeMatches(Task t) {
        return creationTimeMatches(t) && occurrenceTimeMatches(t);
    }

    boolean creationTimeMatches(Task t) {
        long now = nar.time();
        return !(((creationStart != -1) && (now < creationStart)) ||
                ((creationEnd != -1) && (now > creationEnd)));
    }

    protected boolean occurrenceTimeMatches(Task t) {
        return (t.isEternal());
    }



    @Override
    public boolean test(Task task) {

        /** how many errors accumulated while testing it  */
        double distance = 0;

        if (!matches(task))
            distance = 1;










        //TODO use range of acceptable occurrenceTime's for non-eternal tests


        char punc = getPunctuation();
        if ((punc == '.') || (punc == '!')) {
            if(task.getTruth() == null) {
                return false;
            }
            float fr = task.getFrequency();
            float co = task.getConfidence();

            if ((co > confMax) || (co < confMin) || (fr > freqMax) || (fr < freqMin)) {
                distance ++;
            }
        }

        boolean match = (distance == 0);

        if (match) {
            //TODO record a different score for fine-tune optimization?
            ensureExact();
            valid.add(task);
            /*if (exact==null || (exact.size() < maxExact)) {
                ensureExact();
                exact.add(task);
                return true;
            }*/
        }
        else {
            recordSimilar(task);
        }
        return match;
    }

    public void recordSimilar(Task task) {
        final TreeMap<Float, Task> similar = this.similar;

        if (similarset!=null && similarset.contains(task))
            return;


        //TODO add the levenshtein distance of other task components
        final float worstDiff;
        if (similar!=null && similar.size() >= maxSimilars)
            worstDiff = similar.lastKey();
        else
            worstDiff = Float.POSITIVE_INFINITY;

        float difference = 0;
        difference +=
                task.getTerm()==getTerm() ? 0 : (getTerm().volume());
        if (difference > worstDiff)
            return;

        final float freqDiff = Math.min(
                Math.abs(task.getFrequency() - freqMin),
                Math.abs(task.getFrequency() - freqMax));
        difference += 2 * freqDiff;
        if (difference > worstDiff)
            return;

        final float confDiff = Math.min(
                Math.abs(task.getConfidence() - confMin),
                Math.abs(task.getConfidence() - confMax));
        difference += 1 * confDiff;
        if (difference > worstDiff)
            return;

        final float termDifference =
                Texts.levenshteinDistancePercent(
                    task.getTerm().toString(),
                    getTerm().toString());
        difference += 3 * termDifference;
        if (difference > worstDiff)
            return;

        {
            ensureSimilar();

            //TODO more efficient way than this

            this.similar.put(difference, task);
            this.similarset.add(task);


//            if (similar.size() > maxSimilars) {
//                similar.remove(similar.lastEntry().getKey());
//            }
        }
    }

    private void ensureExact() {
        if (valid == null) valid = Global.newArrayList(1);
    }
    private void ensureSimilar() {
        if (similar == null) {
            similar = new TreeMap();
            similarset = new HashSet<>();
        }
    }


//    public String getFalseReason() {
//        String x = "Unmatched; ";
//
//        if (similar!=null) {
//            x += "Similar:\n";
//            for (Map.Entry<Double,Task> et : similar.entrySet()) {
//                Task tt = et.getValue();
//                x += Texts.n4(et.getKey().floatValue()) + ' ' + tt.toString() + ' ' + tt.getLog() + '\n';
//            }
//        }
//        else {
//            x += "No similar: " + term;
//        }
//        return x;
//    }

    public Truth getTruthMean() {
        return new DefaultTruth(0.5f * (freqMax + freqMin), 0.5f * (confMax + confMin));
    }


//    public List<Task> getTrueReasons() {
//        return valid;
//        //if (!isTrue()) throw new RuntimeException(this + " is not true so has no true reasons");
//        /*return Lists.newArrayList("match at: " +
//
//                Iterables.transform(trueAt, new Function<Task, String>() {
//                    @Override
//                    public String apply(Task task) {
//                        return task.toString() + " @ " + task.sentence.getCreationTime();
//                    }
//                }));
//                */
//        //return exact;
//    }

//    @Override
//    public String toString() {
//        return succeeded  +": "  + JSONOutput.stringFromFields(this);
//    }

    @Override
    public final void accept(Task task) {
        if (!succeeded && test(task)) {
            succeeded = true;
            successTime = nar.time();
        }
    }

    @Override
    public final long getSuccessTime() {
        return successTime;
    }

    @Override
    public long getFinalCycle() {
        return creationEnd;
    }

    @Override
    public void report() {
        if (valid != null) {
            valid.forEach(t -> {
                System.out.println(t.getExplanation()
                );
            });
        }
    }

    /** calculates the "cost" of an execution according to certain evaluated condtions
     *  this is the soonest time at which all output conditions were successful.
     *  if any conditions were not successful, the cost is infinity
     * */
    public static double cost(Iterable<EternalTaskCondition> conditions) {
        long lastSuccess = Stamp.TIMELESS;
        for (EternalTaskCondition e : conditions) {
            long est = e.getSuccessTime();
            if (est != Stamp.TIMELESS) {
                if (lastSuccess < est) {
                    lastSuccess = est;
                }
            }
        }
        if (lastSuccess != Stamp.TIMELESS) {
            //score = 1.0 + 1.0 / (1+lastSuccess);
            return lastSuccess;
        }

        return Double.POSITIVE_INFINITY;
    }

    /** returns a function of the cost characterizing the optimality of the conditions
     *  monotonically increasing from -1..+1 (-1 if there were errors,
     *  0..1.0 if all successful.  limit 0 = takes forever, limit 1.0 = instantaneous
     */
    public static double score(List<EternalTaskCondition> requirements) {
        double cost = cost(requirements);
        if (Double.isFinite(cost))
            return 1.0 / (1.0 + cost);
        else
            return -1;

    }

    @Override
    public final boolean isTrue() {
        return succeeded;
    }

    @Override
    public String toConditionString() {
        return  "  freq in(" + freqMin + "," + freqMax +
                "), conf in(" + confMin + "," + confMax +
                "), creation in(" + creationStart + "," + creationEnd + ")";
    }

    @Override
    public void toString(PrintStream out) {
        out.println(isTrue() ? " OK" : "ERR" + "\t" + toString() + " " + toConditionString());

        BiConsumer<String,Task> printer = (label,s) -> {
            out.print("\t" + label + " ");
            out.println(s.getExplanation().replace("\n", "\n\t\t"));
        };

        if (valid!=null) {
            valid.forEach(s -> printer.accept("VALID", s));
        }
        if (similar!=null) {
            similar.values().forEach(s -> printer.accept("SIMILAR", s));
        }
    }
}
