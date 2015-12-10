package nars.term.transform;

import com.gs.collections.impl.factory.Sets;
import nars.$;
import nars.Global;
import nars.NAR;
import nars.Op;
import nars.nal.meta.TermPattern;
import nars.nar.Terminal;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.variable.Variable;
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

        for (int i = 0; i < 5; i++) {
            test(i, type, s1, s2, shouldSub);
        }
        return null;
    }

    Subst test(int seed, String s1, String s2, boolean shouldSub) {
        return test(test().nar, seed, Op.VAR_PATTERN, s1, s2, shouldSub);
    }

    Subst test(int seed, Op type, String s1, String s2, boolean shouldSub) {
        return test(test().nar, seed, type, s1, s2, shouldSub);
    }

    static Subst test(NAR nar, int seed, Op type, String s1, String s2, boolean shouldSub) {

        Global.DEBUG = true;
        //nar.believe(s1);
        //nar.believe(s2);
        //nar.frame(2);

        Term t1 = nar.term(s1);
        Term t2 = nar.term(s2);

        //this only tests assymetric matching:
        if ((type == Op.VAR_PATTERN && Variable.hasPatternVariable(t2)) || t2.hasAny(type)) {
            return null;
        }

        //a somewhat strict lower bound
        int power = 1 + t1.volume() * t2.volume();
        power *= power;

        XorShift1024StarRandom rng = new XorShift1024StarRandom(seed);

        TermPattern tp = new TermPattern(type, t1);

        //System.out.println(tp);

        FindSubst subst = new FindSubst(type, rng);
        boolean subbed = subst.next(tp, t2, power);

        //System.out.println();
        //System.out.println(t1 + " " + t2 + " " + subbed);
        //System.out.println(subst.xy());
        //System.out.println(subst.yx());

        verify(type, t1, t2, subst, subbed, shouldSub);


        return subst;
    }

    private static void verify(Op type, Term t1, Term t2, FindSubst subst, boolean wasSubbed, boolean shouldHaveSubbed) {
        if (shouldHaveSubbed && (t2 instanceof Compound) && (t1 instanceof Compound)) {
            Set<Term> t1u = ((Compound) t1).unique(type);
            Set<Term> t2u = ((Compound) t2).unique(type);

            int n1 = Sets.difference(t1u, t2u).size();
            int n2 = Sets.difference(t2u, t1u).size();

            assertTrue((n2) <= (subst.yx.size()));
            assertTrue((n1) <= (subst.xy.size()));
        }


        assertEquals(shouldHaveSubbed, wasSubbed);
    }


    FindSubst permuteTest(int seed, TermPattern tp, Term t2, int startPower) {

        Op type = Op.VAR_PATTERN;
//        NAR nar = test().nar;
//
//        Global.DEBUG = true;
//        nar.believe(s1);
//        nar.believe(s2);
//        nar.frame(2);

        XorShift1024StarRandom rng = new XorShift1024StarRandom(seed);



        FindSubst subst = new FindSubst(type, rng);

        boolean subbed = subst.next(tp, t2, startPower);

        //System.out.println(tp.term + " " + t2 + "\n\tXY: " + subst.xy + "\n\tYX: " + subst.yx);
        //System.out.println("\tsuccess: " + subbed);

        //int powerLoss = startPower - subst.branchPower.get();
        //System.out.println("\tpowerLoss: " + powerLoss);

        //System.out.println();

        //verify(type, t1, t2, subst, subbed, true);



        if (subbed) return subst;
        return null;
    }

    Set<String> permuteTest(String s1, String s2, int expectedPermutations) {

        Term t1 = $.$(s1);
        Term t2 = $.$(s2);
        TermPattern tp = new TermPattern(Op.VAR_PATTERN, t1);

        Set<String> found = Global.newHashSet(expectedPermutations);
        int success = 0, tries = 0;

        int v = t1.volume()+1;
        for (int p = 100; p < v * v * 8; p+=1) {
            for (int s = 1; s<16; s++) {
                FindSubst r = permuteTest(s, tp, t2, p);
                if (r!=null) {

                    String res = r.xy.toString();

                    //System.out.println(s + " " + p + " " + res);
                    found.add(res);

                    success++;
                }
                tries++;
            }
            //System.out.println(p + ":\t" + ( ((double)success)/tries) );
        }

        System.out.println(found + " " + success + " " + tries);
        assertEquals(expectedPermutations, found.size());
        return found;
    }

    @Test public void testPermutationPowerA() {
        Set<String> r = permuteTest(
                "<{%1,%2} <-> {a,b}>",
                "<{c,d} <-> {a,b}>", 2);
        assertEquals("[{%1=d, %2=c}, {%1=c, %2=d}]", r.toString());
    }

    @Test public void testPermutationPower3() {
        //combination of 4
        permuteTest(
                "<{%1,%2,%3} <-> {a,%1,%2,%3}>",
                "<{b,d,e} <-> {a,b,d,e}>", 6);
    }
    @Test public void testPermutationPower4() {
        //combination of 4
        permuteTest(
                "<{%1,%2,%3,%4} <-> {%1,%4,%2,%3}>",
                "<{b,d,e,c} <-> {b,c,d,e}>", 24);
    }
    @Test public void testPermutationPower4fixed1() {
        //combination of 4
        permuteTest(
                "<{%1,%2,%3,%4} <-> {a,%1,%4,%2,%3}>",
                "<{b,d,e,c} <-> {a,b,c,d,e}>", 12);
    }
    @Test public void testPermutationPowerC() {
        //one permutation, determined by the product
        permuteTest(
                "<{%1,%2,%3,%4} <-> (a,%1,%4,%2,%3)>",
                "<{b,d,e,c} <-> (a,b,c,d,e)>", 1);
    }


//    @Test public void testPermutationPower1() {
//        permuteTest(
//                "<{%1,%2} <-> {a,b,%1,%2,e,f,g,h}>",
//                "<{c,d} <-> {a,b,c,d,e,f,g,h}>");
//    }
//
    @Test public void testPermutationPowerEmbedded2() {
        permuteTest(
                "{%2,{%1,%3}}",
                "{a,{b,c}}", 2 );

        //System.out.println(s);
    }
    @Test public void testPermutationPowerEmbedded3() {
        permuteTest(
                "{%2, {%1, %3, {%4, %5}}}",
                "{a, {b, c, {d, e}}}", 4 );

        //System.out.println(s);
    }
    @Test public void testPermutationPowerEmbedded3b() {
        permuteTest(
                "{%2, {%1, %3, {%4, e}}}",
                "{a, {b, c, {d, e}}}", 2 );
    }

    //overrides
    @Test
    @Override
    public void pattern_trySubs_Indep_Var_2_product_and_common_depvar() {
    }

}
