package nars.core;

import com.google.common.collect.Iterators;
import nars.NAR;
import nars.bag.impl.CacheBag;
import nars.io.out.TextOutput;
import nars.model.cycle.DefaultCycle;
import nars.model.impl.Default;
import nars.nal.concept.Concept;
import nars.nal.term.Term;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 6/11/15.
 */
public class CacheBagTest {

    @Test
    public void testLatentKnowledge() {

        NAR n = new NAR(new Default());

        TextOutput.out(n);

        n.input("$0$ <a --> b>.");
        n.frame();

        DefaultCycle active = ((DefaultCycle) n.memory.cycle);
        CacheBag<Term, Concept> all = ((DefaultCycle) n.memory.cycle).index();


        assertEquals(3, all.size());
        assertEquals("[a, <a --> b>, b]", Iterators.toString(all.iterator()) );

        assertEquals(0, active.size());

        n.input("<b --> c>.");
        n.frame(); //input TaskProcess
        n.frame(); //next cycle: Conceputalization

        assertEquals(3, active.size());

        n.frame();
        n.frame();

        assertTrue("active input has activated forgotten knowledge",
                3 < ((DefaultCycle) n.memory.cycle).size());

        System.out.println(Iterators.toString(active.iterator()));
        System.out.println(Iterators.toString(all.iterator()));
        assertTrue(all.size() >= active.size());

    }
}
