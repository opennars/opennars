package nars.truth;

import nars.nal.nal7.Tense;
import nars.task.Task;
import nars.util.Texts;

/** chart-like representation of a belief state at each time cycle in a range of time */
public class TruthWave {

    //start and stop interval (in cycles)
    final long start;
    final long end;

    //expect[1][] is the mean positive expectation, expect[0][] is the mean negative expectation
    float[][] expect;

    //TODO total confidence

    //mean positive and negative eternal expectations
    float expectEternal0, expectEternal1;
    int numEternal, numTemporal;

    public TruthWave(Iterable<Task> beliefs) {

        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;

        //get min and max occurence time
        for (Task t : beliefs) {
            long o = t.getOccurrenceTime();
            if (o == Tense.ETERNAL) {
                expectEternal1 += t.getTruth().getExpectationPositive();
                expectEternal0 += t.getTruth().getExpectationNegative();
                numEternal++;
            }
            else {
                numTemporal++;
                if (o > max) max = o;
                if (o < min) min = o;
            }
        }

        if (numEternal > 0) {
            expectEternal1 /= numEternal;
            expectEternal0 /= numEternal;
        }

        start = min;
        end = max;

        int range = length();
        expect = new float[2][];
        expect[0] = new float[range+1];
        expect[1] = new float[range+1];

        if (numTemporal > 0) {
            for (Task t : beliefs) {
                long o = t.getOccurrenceTime();
                if (o != Tense.ETERNAL) {
                    int i = (int)(o - min);
                    expect[1][i] += t.getTruth().getExpectationPositive();
                    expect[0][i] += t.getTruth().getExpectationNegative();
                }
            }

            //normalize
            for (int i = 0; i < (max-min); i++) {
                expect[0][i] /= numTemporal;
                expect[1][i] /= numTemporal;
            }
        }

    }

    /**
     * 2 decimal representation of values between 0 and 1. only the tens and hundredth
     * decimal point are displayed - not the ones, and not a decimal point.
     * for compact display.
     * if the value=1.0, then 'aa' is the result
     */
    public static String n2u(float x) {
        if ((x < 0) || (x > 1)) throw new RuntimeException("values >=0 and <=1");
        int hundreds = (int) Texts.hundredths(x);
        if (x == 100) return "aa";
        return hundreds < 10 ? "0" + hundreds : Integer.toString(hundreds);
    }

    //TODO getFrequencyAnalysis
    //TODO getDistribution

    public int length() { return (int)(end-start); }

    public void print() {
        System.out.print("eternal=" + numEternal + ", temporal=" + numTemporal);


        if (length() == 0) {
            System.out.println();
            return;
        }
        System.out.println(" @ " + start + ".." + end);

        for (int c = 0; c < 2; c++) {
            for (int i = 0; i < length(); i++) {

                float v = expect[c][i];

                System.out.print(n2u(v) + ' ');

            }
            System.out.println();
        }
    }

}
