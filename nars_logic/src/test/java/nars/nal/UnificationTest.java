package nars.nal;

import com.gs.collections.impl.factory.Sets;
import nars.Global;
import nars.NAR;
import nars.Op;
import nars.concept.Concept;
import nars.nar.Default;
import nars.nar.Terminal;
import nars.term.Compound;
import nars.term.Term;
import nars.term.transform.FindSubst;
import nars.util.graph.TermLinkGraph;
import nars.util.meter.RuleTest;
import nars.util.meter.TestNAR;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

        Term t1 = nar.concept(s1).getTerm();
        Term t2 = nar.concept(s2).getTerm();

        //a somewhat strict lower bound
        int power = 1 + t1.volume() * t2.volume();
        power*=power;

        FindSubst sub = new FindSubst(type, nar);
        boolean subbed = sub.next(t1, t2, power);

        System.out.println();
        System.out.println(t1 + " " + t2 + " " + subbed);
        System.out.println(sub.xy);
        System.out.println(sub.yx);

        assertEquals(shouldSub, subbed);



        if (shouldSub && (t2 instanceof Compound) && (t1 instanceof Compound)) {
            Set<Term> t1u = ((Compound) t1).unique(type);
            Set<Term> t2u = ((Compound) t2).unique(type);

            int n1 = Sets.difference(t1u, t2u).size();
            int n2 = Sets.difference(t2u, t1u).size();

            assertTrue( (n2) <= (sub.yx.size()));
            assertTrue( (n1) <= (sub.xy.size()));
        }

        return sub;
    }

    @Test
    public void unification1()  { 

        String T1 = "<(*,$1,$1) --> wu>";
        String T2 = "<(*,a,b) --> wu>";
        TestNAR tester = test();
        tester.believe(T1); 
        tester.believe(T2); 
        tester.run(10);

        Concept ret = tester.nar.concept(T1);
        Concept ret2 = tester.nar.concept(T2);

        //these we wanted, but we had to do the crap above since I forgot how to construct terms by strings..
        Term Term1 = ret.getTerm();
        Term Term2 = ret2.getTerm();

        FindSubst wu = new FindSubst(Op.VAR_INDEPENDENT, tester.nar.memory.random);
        boolean unifies = wu.next(Term1, Term2, 1024);
        if (unifies)
            assertTrue("Unification is nonsensical", false);

    }

    @Test
    public void unification2()  { 

        String T1 = "<(*,#1,$1) --> wu>";
        String T2 = "<(*,a,b) --> wu>";
        TestNAR tester = test();
        tester.believe(T1); 
        tester.believe(T2); 
        tester.run(10);

        Concept ret = tester.nar.concept(T1);
        Concept ret2 = tester.nar.concept(T2);

        //these we wanted, but we had to do the crap above since I forgot how to construct terms by strings..
        Term Term1 = ret.getTerm();
        Term Term2 = ret2.getTerm();

        FindSubst wu = new FindSubst(Op.VAR_PATTERN, tester.nar.memory.random);
        boolean unifies = wu.next(Term1, Term2, 1024);
        if (unifies)
            assertTrue("Unification is nonsensical", false);

    }

    @Test
    public void unification3()  { 

        String T1 = "<(*,%1,%1,$1) --> wu>";
        String T2 = "<(*,lol,lol2,$1) --> wu>";
        TestNAR tester = test();
        tester.believe(T1); 
        tester.believe(T2); 
        tester.run(10);

        Concept ret = tester.nar.concept(T1);
        Concept ret2 = tester.nar.concept(T2);

        //these we wanted, but we had to do the crap above since I forgot how to construct terms by strings..
        Term Term1 = ret.getTerm();
        Term Term2 = ret2.getTerm();

        FindSubst wu = new FindSubst(Op.VAR_PATTERN, tester.nar.memory.random);
        boolean unifies = wu.next(Term1, Term2, 1024);
        if (unifies)
            assertTrue("Unification is nonsensical", false);

    }

    @Test
    public void unification4()  {
        test(Op.VAR_PATTERN,
            "<(*,%1,%2,%3,$1) --> wu>",
            "<(*,%1,lol2,%1,$1) --> wu>",
            true
        );
    }

    @Test
    public void unification_multiple_variable_elimination4()  { 

        String T1 = "<#x --> lock>";
        String T2 = "<{lock1} --> lock>";
        TestNAR tester = test();
        tester.believe(T1); 
        tester.believe(T2); 
        tester.run(10);

        Concept ret = tester.nar.concept(T1);
        Concept ret2 = tester.nar.concept(T2);

        
        Term Term1 = ret.getTerm();
        Term Term2 = ret2.getTerm();

        FindSubst wu = new FindSubst(Op.VAR_DEPENDENT, tester.nar.memory.random);
        boolean unifies = wu.next(Term1, Term2, 1024);
        if (!unifies)
            assertTrue("Unification is nonsensical", false);

    }

    @Test
    public void pattern_trySubs_atomic()  {
        TestNAR test = test();
        NAR nar = test.nar;

        String s1 = "<%A =/> %B>";
        String s2 = "<<a --> A> =/> <b --> B>>";
        nar.input(s1 + ".");
        nar.input(s2 + ".");
        nar.frame(1000);
        Term t1 = nar.concept(s1).getTerm();
        Term t2 = nar.concept(s2).getTerm();

        FindSubst sub = new FindSubst(Op.VAR_PATTERN, nar.memory.random);
        if (!sub.next(t1, t2, 99999)) {
            assertTrue("Unification is nonsensical", false);
        }
    }

    @Test
    public void pattern_trySubs_Indep_Var()  {
        TestNAR test = test();
        NAR nar = test.nar;

        String s1 = "<%A =/> %B>";
        String s2 = "<<$1 --> A> =/> <$1 --> B>>";
        nar.input(s1 + ".");
        nar.input(s2 + ".");
        nar.frame(1000);
        Term t1 = nar.concept(s1).getTerm();
        Term t2 = nar.concept(s2).getTerm();

        FindSubst sub = new FindSubst(Op.VAR_PATTERN, nar);
        if (!sub.next(t1, t2, 99999)) {
            assertTrue("Unification with pattern variable failed", false);
        }
    }

    @Test public void pattern_trySubs_Dep_Var()  {
        test(Op.VAR_PATTERN,
                "<%A =/> %B>",
                "<<#1 --> A> =/> <$1 --> B>>",
                true);
    }

    @Test public void pattern_trySubs_Indep_Var_2_parallel()  {
        test(Op.VAR_INDEPENDENT,
              "(&|,<($1,#2) --> on>,<(SELF,#2) --> at>)",
              "(&|,<({t002},#1) --> on>,<(SELF,#1) --> at>)",
              true);
    }
    @Test public void pattern_trySubs_Indep_Var_2_product_and_common_depvar()  {
        FindSubst sub = test(Op.VAR_INDEPENDENT,
                "(<($1,#2) --> on>,<(SELF,#2) --> at>)",
                "(<({t002},#1) --> on>,<(SELF,#1) --> at>)",
                true);

        //additional test that verifies correct common variable substitution result
        assertEquals("{$1={t002}, #2=#1#2}", sub.xy.toString());
        assertEquals("{#1=#1#2}", sub.yx.toString());
    }
    @Test public void pattern_trySubs_Indep_Var_2_product()  {
        test(Op.VAR_INDEPENDENT,
                "(<($1,x) --> on>,<(SELF,x) --> at>)",
                "(<({t002},x) --> on>,<(SELF,x) --> at>)",
                true);
    }
    @Test public void pattern_trySubs_Dep_Var_2_product()  {
        test(Op.VAR_DEPENDENT,
                "(<(#1,x) --> on>,<(SELF,x) --> at>)",
                "(<({t002},x) --> on>,<(SELF,x) --> at>)",
                true);
    }
    @Test public void pattern_trySubs_Indep_Var_2_set()  {
        test(Op.VAR_INDEPENDENT,
                "{<($1,x) --> on>,<(SELF,x) --> at>}",
                "{<({t002},x) --> on>,<(SELF,x) --> at>}",
                true);
    }
    @Test public void pattern_trySubs_Indep_Var_2_set2()  {
        test(Op.VAR_INDEPENDENT,
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
                "{<(%1, z) --> on>, c:{a,b,c}}",
                "{<(x, z) --> on>, c:{a,b,c}}",
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
        test(Op.VAR_DEPENDENT,
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
    @Test public void pattern_trySubs_set2_1()  {
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
    @Test public void pattern_trySubs_set3_1_b()  {
        test(Op.VAR_PATTERN,
                "{a,b,c}",
                "{%1,b,%2}",
                true);
    }
    @Test public void pattern_trySubs_set3_1_b_revesre()  {
        test(Op.VAR_PATTERN,
                "{%1,b,%2}",
                "{a,b,c}",
                true);
    }
    @Test public void pattern_trySubs_set3_1_b_commutative_inside_statement()  {
        test(Op.VAR_PATTERN,
                "<{a,b,c} --> d>",
                "<{%1,b,%2} --> %3>",
                true);
    }
    @Test public void pattern_trySubs_set3_1_statement_of_specific_commutatives()  {
        test(Op.VAR_PATTERN,
                "<{a,b} --> {c,d}>",
                "<{%1,b} --> {c,%2}>",
                true);
    }
    @Test public void pattern_trySubs_set3_1_c()  {
        test(Op.VAR_PATTERN,
                "{a,b,c}",
                "{%1,%2,c}",
                true);
    }
    @Test public void pattern_trySubs_set4()  {
        test(Op.VAR_PATTERN,
                "{a,b,c,d}",
                "{%1,%2,%3,%4}",
                true);
    }
    @Test public void diffVarTypes1()  {
        test(Op.VAR_DEPENDENT,
                "(a,$1)",
                "(#1,$1)",
                true);
    }
    @Test public void impossibleMatch1()  {
        test(Op.VAR_DEPENDENT,
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
                0,0,0.9f,0.9f)
                .run();
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



    @Test public void testA() {
        final String somethingIsBird = "bird:$x";
        final String somethingIsAnimal = "animal:$x";        testIntroduction(somethingIsBird, Op.IMPLICATION, somethingIsAnimal, "bird:robin", "animal:robin");
    }


    void testIntroduction(String subj, Op relation, String pred, String belief, String concl) {

        new TestNAR(new Default().nal(6))
                .believe("<" + subj + " " + relation + " " + pred + ">")
                .believe(belief)
                .mustBelieve(16, concl, 0.81f)
                .run();
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
