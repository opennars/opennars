package nars.io;


import nars.build.Default;
import nars.core.NAR;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertTrue;

public class BufferedOutputTest {


    @Test
    public void testBufferedOutput1() {

        final int maxBufferCost = 10;
        final int minOutputInterval = 5;
        NAR n = new NAR(new Default().setInternalExperience(null));
        new BufferedOutput(n, 1, minOutputInterval, maxBufferCost) {

            SummaryStatistics in = new SummaryStatistics();
            SummaryStatistics ex = new SummaryStatistics();

            @Override
            protected void output(List<OutputItem> buffer) {
                /*System.out.println(n.time() + ": " + buffer);
                System.out.println("  in:" + in.getMean() + "," + in.getSum()
                        + " ex:" + ex.getMean() + "," + ex.getSum());
                */

                if (in.getN() > 0 && ex.getN() > 0) {
                    assertTrue(in.getSum() <= maxBufferCost);
                    assertTrue(in.getMean() <= ex.getMean());
                    assertTrue(!toString(buffer).isEmpty());
                }
                in.clear();
                ex.clear();

            }

            @Override
            protected void included(OutputItem o) {
                in.addValue(o.cost);
            }

            @Override
            protected void excluded(OutputItem o) {
                ex.addValue(o.cost);
            }
        };
        n.input("<a --> [b]>.\n <b --> c>. \n <{c} --> a>.\n");

        n.run(250);

    }
}
