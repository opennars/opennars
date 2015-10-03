package nars.concept;

import junit.framework.TestCase;
import nars.Global;
import nars.NAR;
import nars.meter.BeliefAnalysis;
import nars.meter.MemoryBudget;
import nars.nal.nal7.Tense;
import nars.nar.Default;
import org.junit.Test;

/**
 * Created by me on 7/5/15.
 */
public class BeliefTableTest extends TestCase {

//
//    @Test public void testArrayListBeliefTable1() {
//        NAR n = new Terminal();
//
//        ArrayListBeliefTable t = new ArrayListBeliefTable(4);
//
//        Task pos = n.inputTask("<a --> b>. %1.00;0.90%");
//        Task neg = n.inputTask("<a --> b>. %0.00;0.90%");
//
//        BeliefTable.Ranker ranker = BeliefTable.BeliefConfidenceOrOriginality;
//
//        assertTrue( t.add(pos, ranker, n.memory) );
//        assertEquals(1, t.size());
//
//        //after the 2nd belief, a revision is created
//        //and inserted with the 2 input beliefs
//        //to produce two beliefs.
//        assertTrue( t.tryAdd(neg, ranker, n.memory) );
//        assertEquals(3, t.size());
//
//        //sicne they are equal and opposite, the
//        //revised belief will be the average of them
//        //but with a higher confidence.
//
//        //assertTrue(p && n);
//        //assertTrue();
//
//        System.out.println(t);
//
//    }

    public NAR newNAR(int maxBeliefs) {
        Default d = new Default();// {

            /*
            @Override
            public BeliefTable.RankBuilder getConceptRanking() {
                if (rb == null)
                    return super.getConceptRanking();
                else
                    return rb;
            }
            */

        //}
        d.memory().conceptBeliefsMax.set(maxBeliefs);
        return d;
    }

    @Test
    public void testRevision() {

        Global.DEBUG = true;

        NAR n = newNAR(6);


        //arbitrary time delays in which to observe that certain behavior does not happen
        int delay1 = 32;
        int delay2 = delay1;

        BeliefAnalysis b = new BeliefAnalysis(n, "<a-->b>")
                .believe(1.0f, 0.9f)
                .believe(0.0f, 0.9f).run(delay1);

        b.print();

        assertEquals("revised", 3, b.size());

        n.frame(delay2);

        assertEquals("no additional revisions", 3, b.size());



    }

    @Test
    public void testTruthOscillation() {

        NAR n = newNAR(4);
        n.memory().duration.set(1);

        int offCycles = 2;

        BeliefAnalysis b = new BeliefAnalysis(n, "<a-->b>");

        assertEquals(0.0, (Double)b.energy().get(MemoryBudget.Budgeted.ActiveConceptPrioritySum), 0.001);

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
        assertEquals(3, b.size());

        b.believe(1.0f, 0.9f, Tense.Present).run(offCycles)
                .believe(0.0f, 0.9f, Tense.Present);

        /*for (int i = 0; i < 16; i++) {
            b.printEnergy();
            b.print();
            n.frame(1);
        }*/



    }

//    @Ignore
//    @Test
//    public void testTruthOscillationLongTerm() {
//
//        NAR n = newNAR(16, (c, b) -> {
//            return new BeliefTable.BeliefConfidenceAndCurrentTime(c);
//        });
//        n.memory().duration.set(1);
//
//        int period = 2;
//
//        BeliefAnalysis b = new BeliefAnalysis(n, "<a-->b>");
//
//        boolean state = true;
//
//        //for (int i = 0; i < 16; i++) {
//        for (int i = 0; i < 255; i++) {
//
//            if (i % (period) == 0) {
//                b.believe(state ? 1f : 0f, 0.9f, Tense.Present);
//                state = !state;
//            }
//            else {
//                //nothing
//            }
//
//            n.frame();
//
//            /*if (i % 10 == 0) {
//                b.printWave();
//                b.printEnergy();
//                b.print();
//            }*/
//        }
//
//
//    }
}