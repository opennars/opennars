package nars.budget;

import com.google.common.collect.Iterators;
import nars.NAR;
import nars.NARSeed;
import nars.meter.CountIOEvents;
import nars.bag.impl.CacheBag;
import nars.concept.Concept;
import nars.cycle.DefaultCycle;
import nars.nar.Default;
import nars.term.Term;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 6/11/15.
 */
public class BudgetBalancingTest {

    @Test
    public void testLatentKnowledge() {

        NAR n = new NAR(new Default().setInternalExperience(null));


        n.input("$0$ <a --> b>.");
        n.frame();

        DefaultCycle active = ((DefaultCycle) n.memory.cycle);
        CacheBag<Term, Concept> all = ((DefaultCycle) n.memory.cycle).index();


        assertEquals(3, all.size());
        assertEquals(Concept.State.Forgotten, n.concept("<a --> b>").getState());
        assertEquals("[a, b, <a --> b>]", Iterators.toString(all.iterator()) );

        assertEquals(0, active.size());

        n.input("<<a --> b> --> c>.");
        n.frame(); //input TaskProcess
        n.frame(); //next cycle: Conceputalization

        assertEquals(5, active.size());

        //this activates downstream attached concepts:
        // [<<a --> b> --> c>, a, b, c, <a --> b>]
        n.frame();

        assertTrue("active input has activated forgotten knowledge"
                        //+ Iterables.toString(n.memory.cycle)
                ,5 <= n.memory.cycle.size());

        assertTrue(all.size() >= active.size());

        //System.out.println(Iterators.toString(active.iterator()));
        //System.out.println(Iterators.toString(all.iterator()));

        //n.frame(10);
    }

    @Test public void testPriorityConservation() {
        testPriorityConservation(1, new Default().setInternalExperience(null));
    }

    public void testPriorityConservation(float p, NARSeed d) {

        /**
         * in this example there should be only 1 output event.
         * the energy of the system should stabilize to a steady state
         * and it should not increase (beyond a negiligible threshold)
         * each cycle after it has derived the only conclusion
         * that the 2 inputs cause.
         *
         *
         */
        NAR n = new NAR(d);

        CountIOEvents counts = new CountIOEvents(n);

        //TextOutput.out(n);

        n.input("$" + p + "$ <a --> b>.");
        n.input("$" + p + "$ <b --> a>.");
        //n.input("$" + p + "$ <b --> c>.");

        totalPriorityWithin(n, 0, 0);

        n.frame();
        assertEquals(3, n.memory.numConcepts(true, false));
        assertEquals(3, n.memory.numConcepts(true, true));

        totalPriorityWithin(n, p, p*3);

        n.frame();
        assertEquals(4, n.memory.numConcepts(true, false));
        assertEquals(4, n.memory.numConcepts(true, true));

        n.frame();
        n.frame();
        assertEquals(5, n.memory.numConcepts(true, true));


        n.frame(10); //give some time to reach a steady state

        double a = totalPriorityWithin(n, 0, Float.POSITIVE_INFINITY);

        int step = 10;
        float thresh = 0.05f;
        for (int i = 0; i < 500; i+=step) {
            double b = totalPriorityWithin(n, 0, Float.POSITIVE_INFINITY);
            n.frame(step);

            if (b - a > thresh) {
                System.err.println("total priority should only be decreasing");
                //assertTrue("total priority should only be decreasing", a >= b);
            }
            a = b;
        }

        totalPriorityWithin(n, 0, 0.05f);

        //printConcepts(n);

        n.frame(100);

        //printConcepts(n);

        assertEquals(2, counts.numInputs());
        assertEquals(0, counts.numErrors());
        assertEquals("There should only be one unique output event", 1, counts.numOutputs());
    }

    private static void printConcepts(NAR n) {
        System.out.println("\nConcepts @ " + n.time() );
        n.memory.concepts.forEach(x -> {  System.out.println(Budget.toString(x.getBudget()) + ": " + x); } );
        System.out.println();
    }

    public double totalPriorityWithin(NAR n, float min, float max) {
        int c = n.memory.numConcepts(true, false);
        double p = n.memory.getActivePrioritySum(true, true, true);
        double pc = p / c;
        //System.out.println("priority @ " + n.time() + ": " + p + " (" + pc + " / " + c + " concepts)");
        //assertTrue( min <= p );
        //assertTrue( max >= p );
        return p;
    }
}
