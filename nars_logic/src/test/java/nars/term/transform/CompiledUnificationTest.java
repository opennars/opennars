package nars.term.transform;

import com.gs.collections.impl.factory.Sets;
import nars.Global;
import nars.NAR;
import nars.Op;
import nars.nal.meta.TermPattern;
import nars.nar.Terminal;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.util.data.random.XorShift1024StarRandom;
import nars.util.meter.TestNAR;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * "don't touch this file" - patham9
 */
public class CompiledUnificationTest extends UnificationTest {

    private TestNAR t;

    @Before
    public void start() {
        t = new TestNAR(new Terminal());
    }

    public TestNAR test() {
        return t;
    }

    @Override
    FindSubst test(Op type, String s1, String s2, boolean shouldSub) {

        TestNAR test = test();
        NAR nar = test.nar;
        for (int i = 0; i < 5; i++) {
            test(nar, i, type, s1, s2, shouldSub);
        }
        return null;
    }

    static Subst test(NAR nar, int seed, Op type, String s1, String s2, boolean shouldSub) {

        Global.DEBUG = true;
        nar.believe(s1);
        nar.believe(s2);
        nar.frame(2);

        Term t1 = nar.concept(s1).getTerm();
        Term t2 = nar.concept(s2).getTerm();

        //this only tests assymetric matching:
        if ((type == Op.VAR_PATTERN && Variable.hasPatternVariable(t2)) || t2.hasAny(type)) {
            return null;
        }

        //a somewhat strict lower bound
        int power = 1 + t1.volume() * t2.volume();
        power *= power;

        final XorShift1024StarRandom rng = new XorShift1024StarRandom(seed);

        TermPattern tp = new TermPattern(type, t1);

        System.out.println(tp);

        FindSubst frame = new FindSubst(type, rng);
        boolean subbed = frame.next(tp, t2, power);

        System.out.println();
        System.out.println(t1 + " " + t2 + " " + subbed);
        //System.out.println(frame.xy());
        //System.out.println(frame.yx());

        if (shouldSub && (t2 instanceof Compound) && (t1 instanceof Compound)) {
            Set<Term> t1u = ((Compound) t1).unique(type);
            Set<Term> t2u = ((Compound) t2).unique(type);

            int n1 = Sets.difference(t1u, t2u).size();
            int n2 = Sets.difference(t2u, t1u).size();

            assertTrue((n2) <= (frame.yx.size()));
            assertTrue((n1) <= (frame.xy.size()));
        }


        assertEquals(shouldSub, subbed);


        return null;
    }

    //overrides
    @Test
    @Override
    public void pattern_trySubs_Indep_Var_2_product_and_common_depvar() {
    }

}
