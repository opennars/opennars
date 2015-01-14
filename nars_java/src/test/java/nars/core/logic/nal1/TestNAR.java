package nars.core.logic.nal1;

import nars.core.Build;
import nars.core.NAR;
import nars.core.Parameters;
import nars.io.Output;
import nars.io.narsese.Narsese;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
* Created by me on 1/14/15.
*/
public class TestNAR extends NAR {

    boolean showFail = true;

    /** "must" requirement conditions specification */
    public final List<TaskCondition> musts = new ArrayList();

    public TestNAR(Build b) {
        super(b);
    }

    public void mustOutput(long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax) throws Narsese.InvalidInputException {
        musts.add(new TaskCondition(this, Output.OUT.class, cycleStart, cycleEnd, sentenceTerm, punc, freqMin, freqMax, confMin, confMax));
    }

    public void mustOutput(long withinCycles, String sentence, char punc, float freq, float conf) throws Narsese.InvalidInputException {
        float h = Parameters.TRUTH_EPSILON/2f;
        long now = time();
        mustOutput(now, now + withinCycles, sentence, punc, freq-h, freq+h, conf-h, conf+h);
    }

    public void mustBelieve(int withinCycles, String term, float freq, float confidence) throws Narsese.InvalidInputException {
        mustOutput(withinCycles, term, '.', freq, confidence);
    }
    public void mustBelieve(int withinCycles, String term, float confidence) throws Narsese.InvalidInputException {
        mustBelieve(withinCycles, term, 1.0f, confidence);
    }

    public void run() {
        Parameters.DEBUG = true;

        long finalCycle = 0;
        for (TaskCondition tc : musts) {
            if (tc.cycleEnd > finalCycle)
                finalCycle = tc.cycleEnd;
        }

        Exception error = null;

        try {
            run(0, finalCycle);
        }
        catch (Exception e) {
            error = e;
        }

        assertTrue(time() <= finalCycle);

        int conditions = musts.size();

        int failures = 0;
        for (TaskCondition tc : musts) {
            if (!tc.isTrue()) {
                if (showFail) {
                    System.out.println(tc.getFalseReason());
                }
                failures++;
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


}
