package nars.term.transform;

import com.gs.collections.impl.factory.Sets;
import nars.Global;
import nars.NAR;
import nars.Op;
import nars.nar.Default;
import nars.nar.Terminal;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.util.graph.TermLinkGraph;
import nars.util.meter.RuleTest;
import nars.util.meter.TestNAR;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/** "don't touch this file" - patham9 */
public class UnificationTest  {

    private TestNAR t;

    @Before public void start() {
        t = new TestNAR(new Terminal());
    }
    public TestNAR test() {
        return t;
    }

    FindSubst test(Op type, String s1, String s2, boolean shouldSub) {

        Global.DEBUG = true;
        TestNAR test = test();
        NAR nar = test.nar;
        nar.believe(s1);
        nar.believe(s2);
        nar.frame(2);

        Term t1 = nar.concept(s1).get();
        Term t2 = nar.concept(s2).get();

        //a somewhat strict lower bound
        int power = 4 * (1 + t1.volume() * t2.volume());
        //power*=power;

        AtomicBoolean subbed = new AtomicBoolean(false);

        FindSubst sub = new FindSubst(type, nar) {

//            @Override
//            public void onPartial() {
//                super.onPartial();
//                System.out.println(xy);
//            }

            @Override
            public boolean onMatch() {

                if (shouldSub) {
                    if ((t2 instanceof Compound) && (t1 instanceof Compound)) {
                        Set<Term> t1u = ((Compound) t1).unique(type);
                        Set<Term> t2u = ((Compound) t2).unique(type);

                        int n1 = Sets.difference(t1u, t2u).size();
                        int n2 = Sets.difference(t2u, t1u).size();

                        assertTrue( (n2) <= (yx.size()));
                        assertTrue( (n1) <= (xy.size()));
                    }
                } else {
                    assertFalse("match found but should not have", true);
                }

                subbed.set(true);

                return true;
            }
        };
        sub.matchAll(t1, t2);

        assertEquals(shouldSub, subbed.get());

        return sub;
    }



    @Test
    public void unificationP0()  {
        test(Op.VAR_PATTERN,
                "<%A =/> %B>",
                "<<a --> A> =/> <b --> B>>",
                true
        );
    }

    @Test
    public void unificationP1()  {
        test(Op.VAR_INDEP,
                "<(*,$1,$1) --> wu>",
                "<(*,a,b) --> wu>",
                false
        );
    }
    @Test
    public void unificationP2()  {
        test(Op.VAR_DEP,
                "<(*,#1,$1) --> wu>",
                "<(*,a,b) --> wu>",
                false
        );
    }
    @Test
    public void unificationP3()  {
        test(Op.VAR_PATTERN,
                "<(*,%1,%1,$1) --> wu>",
                "<(*,lol,lol2,$1) --> wu>",
                false
        );
    }
    @Test
    public void unificationP5()  {
        test(Op.VAR_DEP,
                "<#x --> lock>",
                "<{lock1} --> lock>",
                true
        );
    }

    @Test
    public void unification4()  {
        test(Op.VAR_PATTERN,
            "<(*,%1,%2,%3,$1) --> wu>",
            "<(*,%1,lol2,%1,$1) --> wu>",
            true
        );
    }


    @Test public void pattern_trySubs_Dep_Var()  {
        test(Op.VAR_PATTERN,
                "<%A =/> %B>",
                "<<#1 --> A> =/> <$1 --> B>>",
                true);
    }

    @Test public void pattern_trySubs_Indep_Var_2_parallel()  {
        test(Op.VAR_INDEP,
              "(&|,<($1,#2) --> on>,<(SELF,#2) --> at>)",
              "(&|,<({t002},#1) --> on>,<(SELF,#1) --> at>)",
              true);
    }
    @Test public void pattern_trySubs_Indep_Var_2_product_and_common_depvar()  {
        FindSubst sub = test(Op.VAR_INDEP,
                "(<($1,#2) --> on>,<(SELF,#2) --> at>)",
                "(<({t002},#1) --> on>,<(SELF,#1) --> at>)",
                true);

        //additional test that verifies correct common variable substitution result
        //assertEquals("{$1={t002}, #2=#1#2}", sub.xy().toString());
        //assertEquals("{#1=#1#2}", sub.yx().toString());
    }
    @Test public void pattern_trySubs_Indep_Var_2_product()  {
        test(Op.VAR_INDEP,
                "(<($1,x) --> on>,<(SELF,x) --> at>)",
                "(<({t002},x) --> on>,<(SELF,x) --> at>)",
                true);
    }
    @Test public void pattern_trySubs_Dep_Var_2_product()  {
        test(Op.VAR_DEP,
                "(<(#1,x) --> on>,<(SELF,x) --> at>)",
                "(<({t002},x) --> on>,<(SELF,x) --> at>)",
                true);
    }
    @Test public void pattern_trySubs_Indep_Var_2_set()  {
        test(Op.VAR_INDEP,
                "{<($1,x) --> on>,<(SELF,x) --> at>}",
                "{<({t002},x) --> on>,<(SELF,x) --> at>}",
                true);
    }
    @Test public void pattern_trySubs_Indep_Var_2_set2()  {
        test(Op.VAR_INDEP,
                "{<($1,x) --> on>,<(SELF,x) --> at>}",
                "{<(z,x) --> on>,<(SELF,x) --> at>}",
                true);
    }

    @Test public void pattern_trySubs_Pattern_Var_2_setSimple()  {
        test(Op.VAR_PATTERN,
                "{%1,y}",
                "{z,y}",
                true);

    }



    @Test public void pattern_trySubs_Pattern_Var_2_setSimpler()  {
        test(Op.VAR_PATTERN,
                "{%1}",
                "{z}",
                true);
    }

    @Test public void pattern_trySubs_Pattern_Var_2_setComplex0()  {
        test(Op.VAR_PATTERN,
                "{<(%1,x) --> on>,y}",
                "{<(z,x) --> on>,y}",
                true);
    }


    @Test public void pattern_trySubs_Pattern_Var_2_setComplex0_1()  {
        test(Op.VAR_PATTERN,
                "{<(%1,x) --> on>,<x-->y>}",
                "{<(z,x) --> on>,<x-->y>}",
                true);
    }
    @Test public void pattern_trySubs_Pattern_Var_2_setComplex0_2()  {
        test(Op.VAR_PATTERN,
                "{<(%1,x) --> on>,(a,b)}",
                "{<(z,x) --> on>,(a,b)}",
                true);
    }
    @Test public void pattern_trySubs_Pattern_Var_2_setComplex0_3()  {
        test(Op.VAR_PATTERN,
                "{<(%1,x) --> on>, c:(a)}",
                "{<(z,x) --> on>, c:(a)}",
                true);
    }

    @Test public void pattern_trySubs_Pattern_Var_2_setComplex0_4()  {
        test(Op.VAR_PATTERN,
                "{<(%1,x) --> on>, c:a:b}",
                "{<(z,x) --> on>, c:a:b}",
                true);
    }
    @Test public void pattern_trySubs_Pattern_Var_2_setComplex0_5()  {
        test(Op.VAR_PATTERN,
                "{<(%1,x) --> on>, c:(a,b)}",
                "{<(z,x) --> on>, c:(a,b)}",
                true);
    }
    @Test public void pattern_trySubs_Pattern_Var_2_setComplex0_5_n()  {
        test(Op.VAR_PATTERN,
                "{<(%1,x) --> on>, c:(a,b)}",
                "{<(z,x) --> on>, c:(b,a)}",
                false);
    }
    @Test public void pattern_trySubs_Pattern_Var_2_setComplex0_5_1()  {
        test(Op.VAR_PATTERN,
                "{<(%1,x) --> on>, c:(a &/ b)}",
                "{<(z,x) --> on>, c:(a &/ b)}",
                true);
    }
    @Test public void pattern_trySubs_Pattern_Var_2_setComplex0_5_c()  {
        test(Op.VAR_PATTERN,
                "{<(%1,x) --> on>, c:{a,b}}",
                "{<(z,x) --> on>, c:{a,b}}",
                true);
    }
    @Test public void pattern_trySubs_Pattern_Var_2_setComplex0_5_c1()  {
        test(Op.VAR_PATTERN,
                "{<(z,%1) --> on>, c:{a,b}}",
                "{<(z,x) --> on>, c:{a,b}}",
                true);
    }
    @Test public void pattern_trySubs_Pattern_Var_2_setComplex0_5_c2()  {
        test(Op.VAR_PATTERN,
                "{<(%1, z) --> on>, w:{a,b,c}}",
                "{<(x, z) --> on>, w:{a,b,c}}",
                true);
    }
    @Test public void pattern_trySubs_Pattern_Var_2_setComplex0_5_s()  {
        test(Op.VAR_PATTERN,
                "{<{%1,x} --> on>, c:{a,b}}",
                "{<{z,x} --> on>, c:{a,b}}",
                true);
    }


    @Test public void pattern_trySubs_Pattern_Var_2_setComplex1()  {
        test(Op.VAR_PATTERN,
                "{%1,<(SELF,x) --> at>}",
                "{z,<(SELF,x) --> at>}",
                true);
    }

    @Test public void pattern_trySubs_Pattern_Var_2_setComplex2()  {
        test(Op.VAR_PATTERN,
                "{<(%1,x) --> on>,<(SELF,x) --> at>}",
                "{<(z,x) --> on>,<(SELF,x) --> at>}",
                true);
    }

    @Test public void pattern_trySubs_Dep_Var_2_set()  {
        test(Op.VAR_DEP,
                "{<(#1,x) --> on>,<(SELF,x) --> at>}",
                "{<({t002},x) --> on>,<(SELF,x) --> at>}",
                true);
    }


    @Test public void pattern_trySubs_Indep_Var_32()  {
        test(Op.VAR_PATTERN,
                "<%A =|> <(*,SELF,$1) --> reachable>>",
                "<(&|,<(*,$1,#2) --> on>,<(*,SELF,#2) --> at>) =|> <(*,SELF,$1) --> reachable>>",
                true);
    }

    @Test public void pattern_trySubs_set3()  {
        test(Op.VAR_PATTERN,
                "{a,b,c}",
                "{%1,%2,%3}",
                true);
    }
    @Ignore @Test public void pattern_trySubs_set2_1()  {
        test(Op.VAR_PATTERN,
                "{a,b}", "{%1,b}",
                true);
    }

    @Test public void pattern_trySubs_set2_1_reverse()  {
        test(Op.VAR_PATTERN,
                "{%1,b}", "{a,b}",
                true);
    }
    @Test public void pattern_trySubs_set2_2()  {
        test(Op.VAR_PATTERN,
                "{a,b}", "{a,%1}",
                true);
    }
    @Ignore @Test public void pattern_trySubs_set3_1_b()  {
        test(Op.VAR_PATTERN,
                "{a,b,c}",
                "{%1,b,%2}",
                true);
    }
    @Test public void pattern_trySubs_set3_1_b_reverse()  {
        test(Op.VAR_PATTERN,
                "{%1,b,%2}",
                "{a,b,c}",
                true);
    }

    @Ignore @Test public void pattern_trySubs_set3_1_b_commutative_inside_statement()  {
        test(Op.VAR_PATTERN,
                "<{a,b,c} --> d>",
                "<{%1,b,%2} --> %3>",
                true);
    }
    @Ignore @Test public void pattern_trySubs_set3_1_statement_of_specific_commutatives()  {
        test(Op.VAR_PATTERN,
                "<{a,b} --> {c,d}>",
                "<{%1,b} --> {c,%2}>",
                true);
    }
    @Test public void pattern_trySubs_set3_1_statement_of_specific_commutatives_reverse()  {
        test(Op.VAR_PATTERN,
                "<{%1,b} --> {c,%2}>",
                "<{a,b} --> {c,d}>",
                true);
    }

    @Ignore @Test public void pattern_trySubs_set3_1_c()  {
        test(Op.VAR_PATTERN,
                "{a,b,c}",
                "{%1,%2,c}",
                true);
    }
    @Test public void pattern_trySubs_set3_1_c_reverse()  {
        test(Op.VAR_PATTERN,
                "{%1,%2,c}",
                "{a,b,c}",
                true);
    }
    @Test public void pattern_trySubs_set4()  {
        test(Op.VAR_PATTERN,
                "{a,b,c,d}",
                "{%1,%2,%3,%4}",
                true);
    }
    @Test public void diffVarTypes1()  {
        test(Op.VAR_DEP,
                "(a,$1)",
                "(#1,$1)",
                true);
    }
    @Test public void diffVarTypes1Reverse()  {
        test(Op.VAR_DEP,
                "(#1,$1)",
                "(a,$1)",
                true);
    }
    @Test public void impossibleMatch1()  {
        test(Op.VAR_DEP,
                "(a,#1)",
                "(b,b)",
                false);
    }

    @Test
    public void posNegQuestion() {
        //((p1, (--,p1), task("?")), (p1, (<BeliefNegation --> Truth>, <Judgment --> Punctuation>)))
        //  ((a:b, (--,a:b), task("?")), (a:b, (<BeliefNegation --> Truth>, <Judgment --> Punctuation>)))
        new RuleTest(
                "a:b?", "(--,a:b).",
                "a:b.",
                0,0,0.9f,0.9f);
    }

    @Test public void patternSimilarity1()  {
        test(Op.VAR_PATTERN,
                "<%1 <-> %2>",
                "<a <-> b>",
                true);
    }
    @Test public void patternNAL2Sample()  {
        test(Op.VAR_PATTERN,
                "(<%1 --> %2>, <%2 --> %1>)",
                "(<bird --> {?1}>, <bird --> swimmer>)",
                false);
    }
    @Test public void patternNAL2SampleSim()  {
        test(Op.VAR_PATTERN,
                "(<%1 <-> %2>, <%2 <-> %1>)",
                "(<bird <-> {?1}>, <bird <-> swimmer>)",
                false);
    }
    @Test public void patternLongSeq()  {
        test(Op.VAR_PATTERN,
                "(a,b,c,d,e,f,g,h,j)",
                "(x,b,c,d,e,f,g,h,j)",
                false);
    }
    @Test public void patternLongSeq2()  {
        test(Op.VAR_PATTERN,
                "(a,b,c,d,e,f,g,h,j)",
                "(a,b,c,d,e,f,g,h,x)",
                false);
    }

    @Test public void ellipsisCommutive1a() {
        test(Op.VAR_PATTERN,
                "{%X..+}",
                "{a}", true);
    }
    @Test public void ellipsisCommutive1b() {
        test(Op.VAR_PATTERN,
                "{a, %X..+}",
                "{a}", false);
    }
    @Test public void ellipsisCommutive1c() {
        test(Op.VAR_PATTERN,
                "{a, %X..*}",
                "{a}", true);
    }

    @Test public void ellipsisCommutive2a() {

        test(Op.VAR_PATTERN,
                "{a, %X..+}",
                "{a, b}", true);
    }

    @Test public void ellipsisCommutive2b() {
        test(Op.VAR_PATTERN,
                "{%X..+, a}",
                "{a, b, c, d}", true);
    }
    @Test public void ellipsisCommutive2c() {
        test(Op.VAR_PATTERN,
                "{a, %X..+, e}",
                "{a, b, c, d}", false);
    }

    @Test public void ellipsisLinear1() {
        test(Op.VAR_PATTERN,
                "(%X..+)",
                "(a)", true);
        test(Op.VAR_PATTERN,
                "(a, %X..+)",
                "(a)", false);
        test(Op.VAR_PATTERN,
                "(a, %X..*)",
                "(a)", true);

        test(Op.VAR_PATTERN,
                "(a, %X..+)",
                "(a, b, c, d)", true);
        test(Op.VAR_PATTERN,
                "(%X..+, a)",
                "(a, b, c, d)", false);
        test(Op.VAR_PATTERN,
                "(a, %X..+, a)",
                "(a, b, c, d)", false);
    }

    @Test public void ellipsisLinearRepeat1() {
        test(Op.VAR_PATTERN,
                "((a, %X..+), %X..+)",
                "((a, b, c, d), b, c, d)", true);
    }
    @Test public void ellipsisLinearRepeat2() {
        test(Op.VAR_PATTERN,
                "((a, %X..+), (z, %X..+))",
                "((a, b, c, d), (z, b, c, d))", true);
    }
    @Test public void ellipsisCommutiveRepeat() {
        test(Op.VAR_PATTERN,
                "{{a, %X..+}, %X..+}",
                "{{a, b, c, d}, b, c, d}", true);
    }
    @Test public void ellipsisCommutiveRepeat2_a() {
        //no X which can match exactly in both
        test(Op.VAR_PATTERN,
                "{{a, %X..+}, {z, %X..+}}",
                "{{a, b, c, d}, {z, b, c, d}}", true);
    }
    @Test public void ellipsisCommutiveRepeat2_aa() {
        //no X which can match exactly in both
        test(Op.VAR_PATTERN,
                "{{a, %X..+}, {z, b, %X..+}}",
                "{{a, b, c, d}, {z, b, c, d}}", false);
    }
    @Test public void ellipsisCommutiveRepeat2_b() {
        //no X which can match exactly in both
        test(Op.VAR_PATTERN,
                "{{a, %X..+, %B}, {z, %X..+, %A}}",
                "{{a, b, c, d}, {z, b, c, d}}", false);
    }
    @Test public void ellipsisCommutiveRepeat2_c() {
        //X and Y are different so they can match
        test(Op.VAR_PATTERN,
                "{{a, %X..+}, {b, %Y..+}}",
                "{{a, b, c}, {d, b, c}}", true);
    }
    @Test public void ellipsisCommutiveRepeat2_cc() {
        //X and Y are different so they can match
        test(Op.VAR_PATTERN,
                "{{a, %X..+}, {b, %Y..+}}",
                "{{a, b, c, d}, {z, b, c, d}}", true);
    }

    @Test public void ellipsisLinearInner()  {

            //TODO - lower priority
        /*test(Op.VAR_PATTERN,
                "(a, %X..+, d)",
                "(a, b, c, d)", true);*/
    }
    @Test public void patternImage()  {
        test(Op.VAR_PATTERN,
                "<A --> (/, _, %X)>",
                "<A --> (/, _, B)>", true);
        test(Op.VAR_PATTERN,
                "<A --> (/, %X, _)>",
                "<A --> (/, B, _)>", true);


        test(Op.VAR_PATTERN,
                "<A --> (/, %X, _)>",
                "<A --> (/, _, B)>", false);


        test(Op.VAR_PATTERN,
                "(&&,<F --> A>,<%X --> (/,_,C)>)",
                "(&&,<F --> A>,<E --> (/,_,C)>)", true);
        test(Op.VAR_PATTERN,
                "(&&,<F --> A>,<%X --> (/,C,D,_)>)",
                "(&&,<F --> A>,<E --> (/,C,D,_)>)", true);
        test(Op.VAR_PATTERN,
                "(&&,<F --> A>,<D --> (/,C,%X, _)>)",
                "(&&,<F --> A>,<D --> (/,C,E, _)>)", true);

    }

    @Test public void testImage2ShouldNotMatch() {
        test(Op.VAR_PATTERN,
                "(/, %X, _)",
                "(/, _, A)", false);
    }

    @Test public void ellipsisImage()  {
        test(Op.VAR_PATTERN,
                "<A --> (/,_, %X..+)>",
                "<A --> (/,_, B)>", true);
        test(Op.VAR_PATTERN,
                "<A --> (/,_, %X..+)>",
                "<A --> (/,_, B,C)>", true);

    }

    @Test public void testEllipsisImage2() {
        test(Op.VAR_PATTERN,
                "(/,_, B, %X..+)",
                "(/,_, B, C, D)", true);
        test(Op.VAR_PATTERN,
                "(/,_, %X..+)",
                "(/,_, B, C, D)", true);

        test(Op.VAR_PATTERN,
                "<A --> (/,_, B, %X..+)>",
                "<A --> (/,_, B, C, D)>", true);
        test(Op.VAR_PATTERN,
                "<A --> (/,_, E, %X..+)>",
                "<A --> (/,_, B, C, D)>", false);
        test(Op.VAR_PATTERN,
                "<A --> (/, B, _, %X..+)>",
                "<A --> (/, B, _, C, D)>", true);
    }


    @Test public void testImageRelationAfterEllipsis() {
        test(Op.VAR_PATTERN,
                "<A --> (/, B, %X..+, _)>",
                "<A --> (/, B, C, D, _)>", true);
        test(Op.VAR_PATTERN,
                "<A --> (/, B, %X..+, _)>",
                "<A --> (/, B, C, _, D)>", false);

    }

    @Ignore
    @Test public void testInnerEllipsis() {
        test(Op.VAR_PATTERN,
                "<A --> (/, B, %X..+, E, _)>",
                "<A --> (/, B, C, D, E, _)>", true);
    }

    @Test public void ellipsisSequence() {
        //TODO test for inclusion of intervals in matching
    }


    @Test public void testA() {
        String somethingIsBird = "bird:$x";
        String somethingIsAnimal = "animal:$x";
        testIntroduction(somethingIsBird, Op.IMPLICATION, somethingIsAnimal, "bird:robin", "animal:robin");
    }


    void testIntroduction(String subj, Op relation, String pred, String belief, String concl) {

        new TestNAR(new Default().nal(6))
                .believe('<' + subj + ' ' + relation + ' ' + pred + '>')
                .believe(belief)
                .mustBelieve(16, concl, 0.81f);
        //.next()
        //.run(1).assertTermLinkGraphConnectivity();

    }

    @Test
    public void testIndVarConnectivity() {

        String c = "<<$x --> bird> ==> <$x --> animal>>.";

        NAR n = new Default().nal(6);
        n.input(c);
        n.frame(1);

        TermLinkGraph g = new TermLinkGraph(n);
        assertTrue("termlinks form a fully connected graph:\n" + g.toString(), g.isConnected());

    }

}
