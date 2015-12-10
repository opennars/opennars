package nars.concept;

import junit.framework.TestCase;
import nars.Global;
import nars.NAR;
import nars.nal.nal7.Tense;
import nars.nar.AbstractNAR;
import nars.nar.Default;
import nars.util.meter.BeliefAnalysis;
import nars.util.meter.MemoryBudget;
import org.junit.Test;

/**
 * Created by me on 7/5/15.
 */
public class BeliefTableTest extends TestCase {


//    @Test public void testRevisionBeliefs() {
//        NAR n = new Default();
//
//        //ArrayListBeliefTable t = new ArrayListBeliefTable(4);
//
//        Task pos = n.inputTask("<a --> b>. %1.00;0.90%");
//        n.frame(1);
//
//        Concept c = n.concept("<a -->b>");
//        BeliefTable b = c.getBeliefs();
//        assertEquals(b.toString(), 1, b.size());
//
//
//        //after the 2nd belief, a revision is created
//        //and inserted with the 2 input beliefs
//        //to produce two beliefs.
//        Task neg = n.inputTask("<a --> b>. %0.00;0.90%");
//
//        n.frame(100);
//        assertEquals(b.toString(), 3, b.size());
//
//        //sicne they are equal and opposite, the
//        //revised belief will be the average of them
//        //but with a higher confidence.
//
//        //assertTrue(p && n);
//        //assertTrue();
//
//        System.out.println(b);
//
//    }

    public AbstractNAR newNAR(int maxBeliefs) {
        AbstractNAR d = new Default(256,1,2,3).nal(7);// {

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
        d.memory.conceptBeliefsMax.set(maxBeliefs);
        return d;
    }

    @Test
    public void testRevision1() {
        //short term immediate test for correct revisionb ehavior
        testRevision(1);
    }
    @Test
    public void testRevision32() {
        //longer term test
        testRevision(32);
    }

    void testRevision(int delay1) {
        Global.DEBUG = true;

        AbstractNAR n = newNAR(6);


        //arbitrary time delays in which to observe that certain behavior does not happen
        int delay2 = delay1;

        //n.stdout();


        BeliefAnalysis b = new BeliefAnalysis(n, "<a-->b>")
                .believe(1.0f, 0.9f).run(1);

        assertEquals(1, b.size());

                b.believe(0.0f, 0.9f).run(1);

        b.run(delay1);

        b.print();
        //List<Task> bb = Lists.newArrayList( b.beliefs() );

        assertEquals("revised", 3, b.size());

        n.frame(delay2);

        assertEquals("no additional revisions", 3, b.size());



    }

    @Test
    public void testTruthOscillation() {

        NAR n = newNAR(4);
        n.memory.duration.set(1);

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


    @Test
    public void testTruthOscillation2() {

        Global.DEBUG = true;

        int maxBeliefs = 16;
        NAR n = newNAR(maxBeliefs);

        n.memory.duration.set(1);


        BeliefAnalysis b = new BeliefAnalysis(n, "<a-->b>");

        assertEquals(0.0, (Double)b.energy().get(MemoryBudget.Budgeted.ActiveConceptPrioritySum), 0.001);

        int period = 8;
        int loops = 4;
        for (int i = 0; i < loops; i++) {
            b.believe(1.0f, 0.9f, Tense.Present);


            b.run(period);
            //b.printEnergy();

            b.believe(0.0f, 0.9f, Tense.Present);

            b.run(period);
            //b.printEnergy();
            b.print();
        }

        b.run(period);

        b.print();

        //TODO test the belief table for something like the following:
        /*
        Beliefs[@72] 16/16
        <a --> b>. %0.27;0.98% [1, 2, 3, 4, 6] [Revision]
        <a --> b>. %0.38;0.98% [1, 2, 3, 4, 6, 7] [Revision]
        <a --> b>. %0.38;0.98% [1, 2, 3, 4, 5, 6] [Revision]
        <a --> b>. %0.23;0.98% [1, 2, 3, 4, 6, 8] [Revision]
        <a --> b>. %0.35;0.97% [1, 2, 3, 4] [Revision]
        <a --> b>. %0.52;0.95% [1, 2, 3] [Revision]
        <a --> b>. 56+0 %0.00;0.90% [8] [Input]
        <a --> b>. 48+0 %1.00;0.90% [7] [Input]
        <a --> b>. 40+0 %0.00;0.90% [6] [Input]
        <a --> b>. 32+0 %1.00;0.90% [5] [Input]
        <a --> b>. 24+0 %0.00;0.90% [4] [Input]
        <a --> b>. 16+0 %1.00;0.90% [3] [Input]
        <a --> b>. 8+0 %0.00;0.90% [2] [Input]
        <a --> b>. 0+0 %1.00;0.90% [1] [Input]
        <a --> b>. %0.09;0.91% [1, 2] [Revision]
        <a --> b>. 28-20 %0.00;0.18% [1, 2, 3] [((%1, <%1 </> %2>, shift_occurrence_forward(%2, "=/>")), (%2, (<Analogy --> Truth>, <Strong --> Desire>, <ForAllSame --> Order>)))]
         */


//        b.believe(1.0f, 0.9f, Tense.Present).run(offCycles)
//                .believe(0.0f, 0.9f, Tense.Present);

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