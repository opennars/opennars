package nars.concept;

import junit.framework.TestCase;
import nars.NAR;
import nars.meter.BeliefAnalysis;
import nars.meter.MemoryBudgetState;
import nars.nal.nal7.Tense;
import nars.nar.Default;
import org.junit.Test;

/**
 * Created by me on 7/5/15.
 */
public class BeliefTableTest extends TestCase {


    public NAR newNAR(int maxBeliefs) {
        return newNAR(maxBeliefs, null);
    }

    public NAR newNAR(int maxBeliefs, BeliefTable.RankBuilder rb) {
        Default d = new Default() {

            /*
            @Override
            public BeliefTable.RankBuilder getConceptRanking() {
                if (rb == null)
                    return super.getConceptRanking();
                else
                    return rb;
            }
            */

        }.setInternalExperience(null);
        d.conceptBeliefsMax.set(maxBeliefs);
        return new NAR(d);
    }

    @Test
    public void testRevision() {

        NAR n = newNAR(4);

        BeliefAnalysis b = new BeliefAnalysis(n, "<a-->b>")
                .believe(1.0f, 0.9f).run(1)
                .believe(0.0f, 0.9f).run(1);

        assertEquals(2, b.size());

        n.frame(1);
        //b.print();

        assertEquals("revised", 3, b.size());

        n.frame(200);
        //b.print();

        assertEquals("no additional revisions", 3, b.size());



    }

    @Test
    public void testTruthOscillation() {

        NAR n = newNAR(4);
        n.param.duration.set(1);

        int offCycles = 2;

        BeliefAnalysis b = new BeliefAnalysis(n, "<a-->b>");

        assertEquals(0.0, (Double)b.energy().get(MemoryBudgetState.Budgeted.ActiveConceptPrioritySum), 0.001);

        b.believe(1.0f, 0.9f, Tense.Present);
        b.run(1);
        //b.printEnergy();

        b.run(1);
        //b.printEnergy();

        b.believe(0.0f, 0.9f, Tense.Present);
        b.run(1);
        //b.printEnergy();

        b.run(1);
        //b.printEnergy();

        b.print();
        assertEquals(4, b.size());

        b.believe(1.0f, 0.9f, Tense.Present).run(offCycles)
                .believe(0.0f, 0.9f, Tense.Present);

        /*for (int i = 0; i < 16; i++) {
            b.printEnergy();
            b.print();
            n.frame(1);
        }*/



    }

    @Test
    public void testTruthOscillationLongTerm() {

        NAR n = newNAR(16, (c, b) -> {
            return new BeliefTable.BeliefConfidenceAndCurrentTime(c);
        });
        n.param.duration.set(1);

        int period = 2;

        BeliefAnalysis b = new BeliefAnalysis(n, "<a-->b>");

        boolean state = true;

        //for (int i = 0; i < 16; i++) {
        for (int i = 0; i < 255; i++) {

            if (i % (period) == 0) {
                b.believe(state ? 1f : 0f, 0.9f, Tense.Present);
                state = !state;
            }
            else {
                //nothing
            }

            n.frame();

            if (i % 10 == 0) {
                b.printWave();
                b.printEnergy();
                b.print();
            }
        }


    }
}