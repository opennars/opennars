package nars.io.test;

import nars.Events;
import nars.NAR;
import nars.ProtoNAR;
import nars.Global;
import nars.event.AbstractReaction;
import nars.io.TextOutput;
import nars.io.condition.OutputCondition;
import nars.io.condition.TaskCondition;
import nars.io.narsese.InvalidInputException;
import nars.nal.Task;
import nars.nal.stamp.Stamp;
import nars.nal.nal7.Tense;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


/**
* TODO use a countdown latch to provide early termination for successful tests
*/
public class TestNAR extends NAR {

    boolean showFail = true;
    boolean showSuccess = false;
    boolean showExplanations = false;
    boolean showOutput = false;


    /** "must" requirement conditions specification */
    public final List<OutputCondition> requires = new ArrayList();
    public final List<ExplainableTask> explanations = new ArrayList();
    private Exception error;
    private boolean exitOnAllSuccess = true;


    public TestNAR(ProtoNAR b) {
        super(b);

        if (exitOnAllSuccess) {
            new AbstractReaction(this, Events.CycleEnd.class) {

                final int checkResolution = 16; //every # cycles to check for completion
                int cycle = 0;

                @Override
                public void event(Class event, Object[] args) {
                    cycle++;
                    if (cycle % checkResolution == 0) {

                        if (requires.isEmpty())
                            return;

                        boolean finished = true;

                        for (OutputCondition oc : requires) {
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
            };
        }

    }


    public ExplainableTask mustOutput(long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax) throws InvalidInputException {
        return mustEmit(Events.OUT.class, cycleStart, cycleEnd, sentenceTerm, punc, freqMin, freqMax, confMin, confMax);
    }

    public ExplainableTask mustOutput(long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax, int ocRelative) throws InvalidInputException {
        return mustEmit(Events.OUT.class, cycleStart, cycleEnd, sentenceTerm, punc, freqMin, freqMax, confMin, confMax, ocRelative );
    }

    public ExplainableTask mustEmit(Class c, long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax) throws InvalidInputException {
        return mustEmit(c, cycleStart, cycleEnd, sentenceTerm, punc, freqMin, freqMax, confMin, confMax, Stamp.ETERNAL );
    }

    public ExplainableTask mustEmit(Class c, long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax, long ocRelative) throws InvalidInputException {
        float h = (freqMin!=-1) ? Global.TRUTH_EPSILON/ 2.0f : 0;

        if (freqMin == -1) freqMin = freqMax;

        TaskCondition tc = new TaskCondition(this, c, cycleStart, cycleEnd, sentenceTerm, punc, freqMin-h, freqMax+h, confMin-h, confMax+h);
        if (ocRelative!= Stamp.ETERNAL) {
            /** occurence time measured relative to the beginning */
            tc.setRelativeOccurrenceTime(cycleStart, (int)ocRelative, memory.duration());
        }
        requires.add(tc);

        ExplainableTask et = new ExplainableTask(tc);
        if (showExplanations) {
            explanations.add(et);
        }
        return et;
    }

    public Exception getError() {
        return error;
    }

    public ExplainableTask mustInput(long withinCycles, String task) {
        return mustEmit(withinCycles, task, Events.IN.class);
    }

    public ExplainableTask mustOutput(long withinCycles, String task) throws InvalidInputException {
        return mustEmit(withinCycles, task, Events.OUT.class);
    }

    public ExplainableTask mustEmit(long withinCycles, String task, Class channel) throws InvalidInputException {
        Task t = narsese.parseTask(task);
        //TODO avoid reparsing term from string

        final long now = time();
        final String termString = t.getTerm().toString();
        if (t.sentence.truth!=null) {
            final float freq = t.sentence.getTruth().getFrequency();
            final float conf = t.sentence.getTruth().getConfidence();
            long occurrence = t.sentence.getOccurrenceTime();
            return mustEmit(channel, now, now + withinCycles, termString, t.sentence.punctuation, freq, freq, conf, conf, occurrence);
        }
        else {
            return mustEmit(channel, now, now + withinCycles, termString, t.sentence.punctuation, -1, -1, -1, -1);
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
    public ExplainableTask mustBelieve(long cycleStart, long cycleStop, String term, float freq, float confidence) throws InvalidInputException {
        long now = time();
        return mustOutput(now + cycleStart, now + cycleStop, term, '.', freq, freq, confidence, confidence);
    }
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
        ExplainableTask t = new ExplainableTask(x);
        return explain(t);
    }

    @Override public ExplainableTask ask(String termString) throws InvalidInputException {
        //Override believe to input beliefs that have occurrenceTime set on input
        // "lazy timing" appropriate for test cases that can have delays
        Task t = super.ask(termString);

        return explainable(t);
    }

    @Override
    public ExplainableTask believe(float pri, float dur, String beliefTerm, Tense tense, float freq, float conf) throws InvalidInputException {
        //Override believe to input beliefs that have occurrenceTime set on input
        // "lazy timing" appropriate for test cases that can have delays
        Task t = super.believe(pri, dur, beliefTerm, tense, freq, conf);

        return explainable(t);
    }

    @Override
    public ExplainableTask believe(String termString) throws InvalidInputException {
        return explainable(super.believe(termString));
    }


    @Override
    public ExplainableTask believe(String termString, float conf) throws InvalidInputException {
        return explainable(super.believe(termString, conf));
    }

    @Override
    public ExplainableTask believe(String termString, float freq, float conf) throws InvalidInputException {
        return explainable(super.believe(termString, freq, conf));
    }

    @Override
    public ExplainableTask believe(String termString, Tense tense, float freq, float conf) throws InvalidInputException {
        return explainable(super.believe(termString, tense, freq, conf));
    }

    public void run() {
        long finalCycle = 0;
        for (OutputCondition oc : requires) {
            if (oc instanceof TaskCondition) {
                TaskCondition tc = (TaskCondition) oc;
                if (tc.cycleEnd > finalCycle)
                    finalCycle = tc.cycleEnd+1;
            }
        }

        runUntil(finalCycle);
    }

    public NAR run(long cycles) {
        return runUntil(time() + cycles);
    }

    public NAR runUntil(long finalCycle) {

        error = null;

        if (showOutput)
            TextOutput.out(this);


        try {
            super.run(finalCycle - time());
        }
        catch (Exception e) {
            error = e;
        }


        return this;
    }


    /** returns null if there is no error, or a non-null String containing report if error */
    @Deprecated public String evaluate() {
        //TODO use report(..)

        int conditions = requires.size();
        int failures = getError()!=null ? 1 : 0;

        for (OutputCondition oc : requires) {
            if (oc instanceof TaskCondition) {
                TaskCondition tc = (TaskCondition) oc;
                if (!tc.isTrue()) {
                    failures++;
                }
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

            for (OutputCondition tc : requires) {

                out.println(tc.toString());

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



}
