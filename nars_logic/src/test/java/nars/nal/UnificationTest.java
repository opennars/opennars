package nars.nal;

import nars.NAR;
import nars.Op;
import nars.concept.Concept;
import nars.meter.TestNAR;
import nars.term.Term;
import nars.term.transform.FindSubst;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** "don't touch this file" - patham9 */
@RunWith(Parameterized.class)
public class UnificationTest extends AbstractNALTest {

    public UnificationTest(Supplier<NAR> b) {
        super(b);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable configurations() {
        return terminal();
    }

    void test(Op type, String s1, String s2, boolean shouldSub) {

        TestNAR test = test();
        NAR nar = test.nar;
        nar.believe(s1);
        nar.believe(s2);
        nar.frame(2);

        Term t1 = nar.concept(s1).getTerm();
        Term t2 = nar.concept(s2).getTerm();

        int power = 1 + t1.volume() * t2.volume();

        FindSubst sub = new FindSubst(type, nar);
        boolean subbed = sub.next(t1, t2, power);
        assertEquals(subbed, shouldSub);
    }

    @Test
    public void unification1()  { //its not clear anymore how to build atomic terms so we will just get them out with hands crossed...

        String T1 = "<(*,$1,$1) --> wu>";
        String T2 = "<(*,a,b) --> wu>";
        TestNAR tester = test();
        tester.believe(T1); //.en("If robin is a type of bird then robin can fly.");
        tester.believe(T2); //.en("Robin is a type of bird.");
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
    public void unification2()  { //its not clear anymore how to build atomic terms so we will just get them out with hands crossed...

        String T1 = "<(*,#1,$1) --> wu>";
        String T2 = "<(*,a,b) --> wu>";
        TestNAR tester = test();
        tester.believe(T1); //.en("If robin is a type of bird then robin can fly.");
        tester.believe(T2); //.en("Robin is a type of bird.");
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
    public void unification3()  { //its not clear anymore how to build atomic terms so we will just get them out with hands crossed...

        String T1 = "<(*,%1,%1,$1) --> wu>";
        String T2 = "<(*,lol,lol2,$1) --> wu>";
        TestNAR tester = test();
        tester.believe(T1); //.en("If robin is a type of bird then robin can fly.");
        tester.believe(T2); //.en("Robin is a type of bird.");
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
    public void unification4()  { //its not clear anymore how to build atomic terms so we will just get them out with hands crossed...

        String T1 = "<(*,%1,%2,%3,$1) --> wu>";
        String T2 = "<(*,%1,lol2,%1,$1) --> wu>";
        TestNAR tester = test();
        tester.believe(T1); //.en("If robin is a type of bird then robin can fly.");
        tester.believe(T2); //.en("Robin is a type of bird.");
        tester.run(10);

        Concept ret = tester.nar.concept(T1);
        Concept ret2 = tester.nar.concept(T2);

        //these we wanted, but we had to do the crap above since I forgot how to construct terms by strings..
        Term Term1 = ret.getTerm();
        Term Term2 = ret2.getTerm();

        FindSubst wu = new FindSubst(Op.VAR_PATTERN, tester.nar.memory.random);
        boolean unifies = wu.next(Term1, Term2, 1024);
        if (!unifies)
            assertTrue("Unification is nonsensical", false);

    }

    @Test
    public void unification_multiple_variable_elimination4()  { //its not clear anymore how to build atomic terms so we will just get them out with hands crossed...

        String T1 = "<#x --> lock>";
        String T2 = "<{lock1} --> lock>";
        TestNAR tester = test();
        tester.believe(T1); //.en("If robin is a type of bird then robin can fly.");
        tester.believe(T2); //.en("Robin is a type of bird.");
        tester.run(10);

        Concept ret = tester.nar.concept(T1);
        Concept ret2 = tester.nar.concept(T2);

        //these we wanted, but we had to do the crap above since I forgot how to construct terms by strings..
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

    @Test public void pattern_trySubs_Indep_Var_2()  {
        test(Op.VAR_INDEPENDENT,
              "(&|,<($1,#2) --> on>,<(SELF,#2) --> at>)",
              "(&|,<({t002},#1) --> on>,<(SELF,#1) --> at>)",
              true);
    }




    @Test public void pattern_trySubs_Indep_Var_32()  {
        test(Op.VAR_PATTERN,
                "<%A =|> <(*,SELF,$1) --> reachable>>",
                "<(&|,<(*,$1,#2) --> on>,<(*,SELF,#2) --> at>) =|> <(*,SELF,$1) --> reachable>>",
                true);
    }

}
