package nars.core;

import nars.io.Output;
import nars.io.TextOutput;
import nars.io.narsese.Narsese;
import nars.logic.entity.Task;
import nars.logic.nal7.Tense;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
* TODO use a countdown latch to provide early termination for successful tests
*/
public class TestNAR extends NAR {

    boolean showFail = true;
    boolean showSuccess = true;
    boolean showExplanations = false;
    boolean showOutput = false;


    /** "must" requirement conditions specification */
    public final List<TaskCondition> musts = new ArrayList();
    public final List<ExplainableTask> explanations = new ArrayList();
    private Exception error;


    public TestNAR(Build b) {
        super(b);
    }

    public ExplainableTask mustOutput(long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax) throws Narsese.InvalidInputException {
        float h = (freqMin!=-1) ? Parameters.TRUTH_EPSILON/2f : 0;

        TaskCondition tc = new TaskCondition(this, Output.OUT.class, cycleStart, cycleEnd, sentenceTerm, punc, freqMin-h, freqMax+h, confMin-h, confMax+h);
        musts.add(tc);

        ExplainableTask et = new ExplainableTask(tc);
        explanations.add(et);
        return et;
    }

    public ExplainableTask mustOutput(long withinCycles, String task) throws Narsese.InvalidInputException {
        Task t = narsese.parseTask(task);
        //TODO avoid reparsing term from string
        if (t.sentence.truth!=null)
            return mustOutput(withinCycles, t.getTerm().toString(), t.sentence.punctuation, t.sentence.getTruth().getFrequency(), t.sentence.getTruth().getConfidence());
        else
            return mustOutput(withinCycles, t.getTerm().toString(), t.sentence.punctuation, -1, -1);
    }

    public ExplainableTask mustOutput(long withinCycles, String term, char punc, float freq, float conf) throws Narsese.InvalidInputException {
        long now = time();
        return mustOutput(now, now + withinCycles, term, punc, freq, freq, conf, conf);
    }

    public ExplainableTask mustBelieve(long withinCycles, String term, float freqMin, float freqMax, float confMin, float confMax) throws Narsese.InvalidInputException {
        return mustOutput(0, withinCycles, term, '.', freqMin, freqMax, confMin, confMax);
    }
    public ExplainableTask mustBelieve(long withinCycles, String term, float freq, float confidence) throws Narsese.InvalidInputException {
        return mustOutput(withinCycles, term, '.', freq, confidence);
    }
    public ExplainableTask mustBelieve(long withinCycles, String term, float confidence) throws Narsese.InvalidInputException {
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

    @Override public ExplainableTask ask(String termString) throws Narsese.InvalidInputException {
        //Override believe to input beliefs that have occurrenceTime set on input
        // "lazy timing" appropriate for test cases that can have delays
        Task t = super.ask(termString);
        t.sentence.stamp.setNotYetPerceived();

        return explainable(t);
    }

    @Override
    public ExplainableTask believe(float pri, float dur, String termString, Tense tense, float freq, float conf) throws Narsese.InvalidInputException {
        //Override believe to input beliefs that have occurrenceTime set on input
        // "lazy timing" appropriate for test cases that can have delays
        Task t = super.believe(pri, dur, termString, tense, freq, conf);
        t.sentence.stamp.setNotYetPerceived();

        return explainable(t);
    }

    @Override
    public ExplainableTask believe(String termString) throws Narsese.InvalidInputException {
        return explainable(super.believe(termString));
    }


    @Override
    public ExplainableTask believe(String termString, float conf) throws Narsese.InvalidInputException {
        return explainable(super.believe(termString, conf));
    }

    @Override
    public ExplainableTask believe(String termString, float freq, float conf) throws Narsese.InvalidInputException {
        return explainable(super.believe(termString, freq, conf));
    }

    @Override
    public ExplainableTask believe(String termString, Tense tense, float freq, float conf) throws Narsese.InvalidInputException {
        return explainable(super.believe(termString, tense, freq, conf));
    }

    public void run() {

        long finalCycle = 0;
        for (TaskCondition tc : musts) {
            if (tc.cycleEnd > finalCycle)
                finalCycle = tc.cycleEnd;
        }

        error = null;

        if (showOutput)
            TextOutput.out(this);

        try {
            run(finalCycle - time());
        }
        catch (Exception e) {
            error = e;
        }

        assertTrue("time exceeded", time() <= finalCycle);

        int conditions = musts.size();

        int failures = 0;
        for (TaskCondition tc : musts) {
            if (!tc.isTrue()) {
                failures++;
            }
        }

        report(System.out, showFail, showSuccess, showExplanations);



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

            for (TaskCondition tc : musts) {
                if (!tc.isTrue()) {
                    if (showFail) {
                        out.println(tc.toString());
                        out.print('\t');
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
