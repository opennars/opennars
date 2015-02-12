package nars.logic;

import nars.core.*;
import nars.event.AbstractReaction;
import nars.io.TextOutput;
import nars.io.condition.OutputCondition;
import nars.io.condition.TaskCondition;
import nars.io.narsese.InvalidInputException;
import nars.logic.entity.Task;
import nars.logic.nal7.Tense;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

/**
* TODO use a countdown latch to provide early termination for successful tests
*/
public class TestNAR extends NAR {

    boolean showFail = true;
    boolean showSuccess = false;
    boolean showExplanations = false;
    boolean showOutput = false;


    /** "must" requirement conditions specification */
    public final List<OutputCondition> musts = new ArrayList();
    public final List<ExplainableTask> explanations = new ArrayList();
    private Exception error;
    private boolean exitOnAllSuccess = true;


    public TestNAR(NewNAR b) {
        super(b);

        if (exitOnAllSuccess) {
            new AbstractReaction(this, Events.CycleEnd.class) {

                final int checkResolution = 16; //every # cycles to check for completion
                int cycle = 0;

                @Override
                public void event(Class event, Object[] args) {
                    cycle++;
                    if (cycle % checkResolution == 0) {

                        if (musts.isEmpty())
                            return;

                        boolean finished = true;

                        for (OutputCondition oc : musts) {
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

    public ExplainableTask mustOutput(long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax, int minOccurrenceDelta, int maxOccurrenceDelta) throws InvalidInputException {
        float h = (freqMin!=-1) ? Parameters.TRUTH_EPSILON/2f : 0;

        TaskCondition tc = new TaskCondition(this, Events.OUT.class, cycleStart, cycleEnd, sentenceTerm, punc, freqMin-h, freqMax+h, confMin-h, confMax+h);
        tc.setOccurrenceTime(minOccurrenceDelta, maxOccurrenceDelta);
        musts.add(tc);


        ExplainableTask et = new ExplainableTask(tc);
        if (showExplanations) {
            explanations.add(et);
        }
        return et;
    }
    public ExplainableTask mustOutput(long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax) throws InvalidInputException {
        float h = (freqMin!=-1) ? Parameters.TRUTH_EPSILON/2f : 0;

        TaskCondition tc = new TaskCondition(this, Events.OUT.class, cycleStart, cycleEnd, sentenceTerm, punc, freqMin-h, freqMax+h, confMin-h, confMax+h);
        musts.add(tc);

        ExplainableTask et = new ExplainableTask(tc);
        if (showExplanations) {
            explanations.add(et);
        }
        return et;
    }

    public Exception getError() {
        return error;
    }

    public ExplainableTask mustOutput(long withinCycles, String task) throws InvalidInputException {
        Task t = narsese.parseTask(task);
        //TODO avoid reparsing term from string
        if (t.sentence.truth!=null)
            return mustOutput(withinCycles, t.getTerm().toString(), t.sentence.punctuation, t.sentence.getTruth().getFrequency(), t.sentence.getTruth().getConfidence());
        else
            return mustOutput(withinCycles, t.getTerm().toString(), t.sentence.punctuation, -1, -1);
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
        t.sentence.stamp.setNotYetPerceived();

        return explainable(t);
    }

    @Override
    public ExplainableTask believe(float pri, float dur, String termString, Tense tense, float freq, float conf) throws InvalidInputException {
        //Override believe to input beliefs that have occurrenceTime set on input
        // "lazy timing" appropriate for test cases that can have delays
        Task t = super.believe(pri, dur, termString, tense, freq, conf);
        t.sentence.stamp.setNotYetPerceived();

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
        for (OutputCondition oc : musts) {
            if (oc instanceof TaskCondition) {
                TaskCondition tc = (TaskCondition) oc;
                if (tc.cycleEnd > finalCycle)
                    finalCycle = tc.cycleEnd;
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


    public void evaluate() {
        int conditions = musts.size();
        int failures = getError()!=null ? 1 : 0;

        for (OutputCondition oc : musts) {
            if (oc instanceof TaskCondition) {
                TaskCondition tc = (TaskCondition) oc;
                if (!tc.isTrue()) {
                    failures++;
                }
            }
        }

        int successes = conditions - failures;

        if (error!=null) {
            assertTrue(error.toString(), false);
        }
        if (failures > 0) {
            assertTrue(successes + "/ " + conditions + " conditions passed", false);
        }


    }

    public void report(PrintStream out, boolean showFail, boolean showSuccess, boolean showExplanations) {

        boolean output = false;

        if (showFail || showSuccess) {

            for (OutputCondition tc : musts) {
                if (!tc.isTrue()) {
                    if (showFail) {
                        out.println(tc.getFalseReason());
                        output = true;
                    }
                } else {
                    if (showSuccess) {
                        out.println(tc.toString());
                        out.print('\t');
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
