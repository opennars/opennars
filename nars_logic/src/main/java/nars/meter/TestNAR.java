package nars.meter;

import nars.NAR;
import nars.event.CycleReaction;
import nars.io.in.TextInput;
import nars.io.out.TextOutput;
import nars.meter.condition.TaskCondition;
import nars.nal.nal7.Tense;
import nars.narsese.InvalidInputException;
import nars.task.Task;
import nars.task.stamp.Stamp;
import nars.truth.DefaultTruth;
import nars.util.event.Topic;
import nars.util.meter.event.HitMeter;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
* TODO use a countdown latch to provide early termination for successful tests
*/
public class TestNAR  {

    public final Map<Object, HitMeter> eventMeters;
    public final NAR nar;
    boolean showFail = true;
    boolean showSuccess = false;
    boolean showExplanations = false;
    boolean showOutput = false;

    boolean resetOnStop = true; //should help GC if successively run


    /** "must" requirement conditions specification */
    public final List<TaskCondition> requires = new ArrayList();
    public final List<ExplainableTask> explanations = new ArrayList();
    private Exception error;
    final transient private boolean exitOnAllSuccess = true;
    public List<Task> inputs = new ArrayList();
    private int temporalTolerance = 0;


    public TestNAR(NAR nar) {
        super();

        nar.reset();
        this.nar = nar;

        if (exitOnAllSuccess) {
            new EarlyExit(1);
        }

        eventMeters = new EventCount(nar).eventMeters;

    }

    /** returns the "cost", which can be considered the inverse of a "score".
     * it is proportional to the effort (ex: # of cycles) expended by
     * this reasoner in attempts to satisfy success conditions.
     * If the conditions are not successful, the result will be INFINITE,
     * though this can be normalized to a finite value in comparing multiple tests
     * by replacing the INFINITE result with a maximum # of cycles limit,
     * which will be smaller in cases where the success conditions are
     * completed prior to the limit.
     * */
    public double getCost() {
        return TaskCondition.cost(requires);
    }

    public ExplainableTask mustDerive(long a, long b, String s, char c, float v, float v1, float v2, float v3, int i2) {
        return mustOutput(nar.memory().eventDerived, a, b, s, c, v, v1, v2, v3, i2 );
    }

    class EarlyExit extends CycleReaction {

        final int checkResolution; //every # cycles to check for completion
        int cycle = 0;

        public EarlyExit(int checkResolution) {
            super(nar);
            this.checkResolution = checkResolution;
        }

        @Override
        public void onCycle() {
            cycle++;
            if (cycle % checkResolution == 0) {

                if (requires.isEmpty())
                    return;

                boolean finished = true;

                int nr = requires.size();
                for (int i = 0; i < nr; i++) {
                    final TaskCondition oc = requires.get(i);
                    if (!oc.isTrue()) {
                        finished = false;
                        break;
                    }
                }

                if (finished) {
                    stop();

                }

            }
        }
    }


    public void stop() {
        nar.stop();
        if (resetOnStop) {
            nar.memory.delete();
        }
    }

    public ExplainableTask mustOutput(long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax) throws InvalidInputException {
        return mustEmit(nar.memory().eventDerived, cycleStart, cycleEnd, sentenceTerm, punc, freqMin, freqMax, confMin, confMax);
    }

    public ExplainableTask mustOutput(Topic<Task> c, long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax, int ocRelative) throws InvalidInputException {
        return mustEmit(c, cycleStart, cycleEnd, sentenceTerm, punc, freqMin, freqMax, confMin, confMax, ocRelative );
    }

    public ExplainableTask mustEmit(Topic<Task> c, long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax) throws InvalidInputException {
        return mustEmit(c, cycleStart, cycleEnd, sentenceTerm, punc, freqMin, freqMax, confMin, confMax, Stamp.ETERNAL );
    }

    public ExplainableTask mustEmit(Topic<Task> c, long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax, long ocRelative) throws InvalidInputException {

        float h = (freqMin!=-1) ? DefaultTruth.DEFAULT_TRUTH_EPSILON / 2.0f : 0;

        if (freqMin == -1) freqMin = freqMax;

        int tt = getTemporalTolerance();

        cycleStart -= tt;
        cycleEnd += tt;

        TaskCondition tc;
        c.on(tc = new TaskCondition(nar,
                cycleStart,
                cycleEnd,
                sentenceTerm, punc, freqMin-h, freqMax+h, confMin-h, confMax+h)
        );

        if (ocRelative!= Stamp.ETERNAL) {
            /** occurence time measured relative to the beginning */
            tc.setRelativeOccurrenceTime(cycleStart, (int)ocRelative, nar.memory().duration());
        }
        requires.add(tc);

        ExplainableTask et = new ExplainableTask(tc);
        if (showExplanations) {
            explanations.add(et);
        }
        return et;
    }

    /** padding to add to specified time limitations to allow correct answers;
     *  default=0 having no effect  */
    public int getTemporalTolerance() {
        return temporalTolerance;
    }

    public void setTemporalTolerance(int temporalTolerance) {
        this.temporalTolerance = temporalTolerance;
    }

    public Exception getError() {
        return error;
    }

    public ExplainableTask mustInput(long withinCycles, String task) {
        return mustEmit(nar.memory.eventInput, withinCycles, task);
    }

    public ExplainableTask mustOutput(long withinCycles, String task) throws InvalidInputException {
        return mustEmit(nar.memory.eventDerived, withinCycles, task);
    }

    public final long time() { return nar.time(); }

    public ExplainableTask mustEmit(Topic<Task> c, long withinCycles, String task) throws InvalidInputException {
        Task t = nar.task(task);
        //TODO avoid reparsing term from string

        final long now = time();
        final String termString = t.getTerm().toString();
        if (t.getTruth()!=null) {
            final float freq = t.getFrequency();
            final float conf = t.getConfidence();
            long occurrence = t.getOccurrenceTime();
            return mustEmit(c, now, now + withinCycles, termString, t.getPunctuation(), freq, freq, conf, conf, occurrence);
        }
        else {
            return mustEmit(c, now, now + withinCycles, termString, t.getPunctuation(), -1, -1, -1, -1);
        }
    }

    public ExplainableTask mustOutput(long withinCycles, String term, char punc, float freq, float conf) throws InvalidInputException {
        long now = time();
        return mustOutput(now, now + withinCycles, term, punc, freq, freq, conf, conf);
    }

    public ExplainableTask mustBelieve(long withinCycles, String term, float freqMin, float freqMax, float confMin, float confMax) throws InvalidInputException {
        long now = time();
        return mustOutput(now, now + withinCycles, term, '.', freqMin, freqMax, confMin, confMax);
    }
    public ExplainableTask mustBelievePast(long withinCycles, String term, float freqMin, float freqMax, float confMin, float confMax, int maxPastWindow) throws InvalidInputException {
        long now = time();
        return mustOutput(now, now + withinCycles, term, '.', freqMin, freqMax, confMin, confMax);
    }
//    public ExplainableTask mustBelieve(long cycleStart, long cycleStop, String term, float freq, float confidence) throws InvalidInputException {
//        long now = time();
//        return mustOutput(now + cycleStart, now + cycleStop, term, '.', freq, freq, confidence, confidence);
//    }
    public ExplainableTask mustBelieve(long withinCycles, String term, float freq, float confidence) throws InvalidInputException {
        return mustOutput(withinCycles, term, '.', freq, confidence);
    }
    public ExplainableTask mustBelieve(long withinCycles, String term, float confidence) throws InvalidInputException {
        return mustBelieve(withinCycles, term, 1.0f, confidence);
    }

    public ExplainableTask mustDesire(long withinCycles, String goalTerm, float freq, float conf) {
        return mustOutput(withinCycles, goalTerm, '!', freq, conf);
    }

    public ExplainableTask explain(ExplainableTask t) {
        explanations.add(t);
        return t;
    }
    public ExplainableTask explainable(Task x) {
        ExplainableTask t = new ExplainableTask(x.normalized());
        return explain(t);
    }

    public ExplainableTask ask(String termString) throws InvalidInputException {
        //Override believe to input beliefs that have occurrenceTime set on input
        // "lazy timing" appropriate for test cases that can have delays
        Task t = nar.ask(termString);

        return explainable(t);
    }

    public ExplainableTask believe(float pri, float dur, String beliefTerm, Tense tense, float freq, float conf) throws InvalidInputException {
        //Override believe to input beliefs that have occurrenceTime set on input
        // "lazy timing" appropriate for test cases that can have delays
        Task t = nar.believe(pri, dur, nar.term(beliefTerm), tense, freq, conf);

        return explainable(t);
    }

    public ExplainableTask believe(String termString) throws InvalidInputException {
        return explainable(nar.believe(termString));
    }



    public ExplainableTask believe(String termString, float conf) throws InvalidInputException {
        return explainable(nar.believe(termString, conf));
    }


    public ExplainableTask believe(String termString, float freq, float conf) throws InvalidInputException {
        return explainable(nar.believe(termString, freq, conf));
    }


    public ExplainableTask believe(String termString, Tense tense, float freq, float conf) throws InvalidInputException {
        return explainable(nar.believe(termString, tense, freq, conf));
    }

    public void run() {
        long finalCycle = 0;
        for (TaskCondition oc : requires) {
            if (oc.cycleEnd > finalCycle)
                finalCycle = oc.cycleEnd+1;
        }

        runUntil(finalCycle);
    }

    public NAR run(long extraCycles) {
        return runUntil(time() + extraCycles);
    }

    public NAR runUntil(long finalCycle) {

        error = null;

        if (showOutput)
            TextOutput.out(nar);


        //try {
        nar.frame((int)(finalCycle - time()));
        /*}
        catch (Exception e) {
            error = e;
        }*/


        return nar;
    }


    /** returns null if there is no error, or a non-null String containing report if error */
    @Deprecated public String evaluate() {
        //TODO use report(..)

        int conditions = requires.size();
        int failures = getError()!=null ? 1 : 0;

        for (TaskCondition tc : requires) {
            if (!tc.isTrue()) {
                failures++;
            }
        }

        int successes = conditions - failures;


        if (error!=null || failures > 0) {
            String result = "";

            if (error!=null) {
                result += error.toString() + " ";
            }

            if (failures > 0) {
                result += successes + "/ " + conditions + " conditions passed";
            }

            return result;
        }

        return null;

    }

    public void report(PrintStream out, boolean showFail, boolean showSuccess, boolean showExplanations) {

        boolean output = false;



        if (showFail || showSuccess) {

            for (TaskCondition tc : requires) {

                if (!tc.isTrue()) {
                    if (showFail) {
                        out.println(tc.getFalseReason());
                        output = true;
                    }
                } else {
                    if (showSuccess) {
                        out.println(tc.getTrueReasons());
                        output = true;
                    }
                }
            }

        }

        if (error!=null) {
            error.printStackTrace();
            output = true;
        }

        if (showExplanations) {
            for (ExplainableTask x : explanations ) {
                x.printMeaning(out);
                output = true;
            }
        }

        if (output)
            out.println();
    }


    public void inputTest(String script) {

        if (script == null)
            throw new RuntimeException("null input");

        nar.input( new TestInput(script) );

    }

    class TestInput extends TextInput {
        public TestInput(String script) {
            super(nar, script);
        }

        @Override
        public void accept(Task task) {
            super.accept(task);
            inputs.add(task);
        }
    }
}
