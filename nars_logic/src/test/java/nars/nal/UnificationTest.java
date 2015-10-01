package nars.nal;

import nars.NAR;
import nars.Op;
import nars.concept.Concept;
import nars.link.TermLink;
import nars.meter.TestNAR;
import nars.term.Term;
import nars.term.transform.FindSubst;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Supplier;

//don't touch this file - patham9

@RunWith(Parameterized.class)
public class UnificationTest extends AbstractNALTest {

    public UnificationTest(Supplier<NAR> b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return AbstractNALTest.core6;
    }

    @Test
    public void unification1() throws Exception { //its not clear anymore how to build atomic terms so we will just get them out with hands crossed...

        String T1="<(*,$1,$1) --> wu>";
        String T2="<(*,a,b) --> wu>";
        TestNAR tester = test();
        tester.believe(T1); //.en("If robin is a type of bird then robin can fly.");
        tester.believe(T2); //.en("Robin is a type of bird.");
        tester.run(10);

        Concept ret = tester.nar.concept(T1);
        Concept ret2 = tester.nar.concept(T2);

        //these we wanted, but we had to do the crap above since I forgot how to construct terms by strings..
        Term Term1 = ret.getTerm();
        Term Term2 = ret2.getTerm();

        FindSubst wu = new FindSubst(Op.VAR_INDEPENDENT, new HashMap<Term,Term>(), new HashMap<Term,Term>(), new Random());
        boolean unifies = wu.next(Term1,Term2,1024);
        if(unifies)
            throw new Exception("Unification is doing nonsense");

    }

    @Test
    public void unification2() throws Exception { //its not clear anymore how to build atomic terms so we will just get them out with hands crossed...

        String T1="<(*,#1,$1) --> wu>";
        String T2="<(*,a,b) --> wu>";
        TestNAR tester = test();
        tester.believe(T1); //.en("If robin is a type of bird then robin can fly.");
        tester.believe(T2); //.en("Robin is a type of bird.");
        tester.run(10);

        Concept ret = tester.nar.concept(T1);
        Concept ret2 = tester.nar.concept(T2);

        //these we wanted, but we had to do the crap above since I forgot how to construct terms by strings..
        Term Term1 = ret.getTerm();
        Term Term2 = ret2.getTerm();

        FindSubst wu = new FindSubst(Op.VAR_PATTERN, new HashMap<Term,Term>(), new HashMap<Term,Term>(), new Random());
        boolean unifies = wu.next(Term1,Term2,1024);
        if(unifies)
            throw new Exception("Unification is doing nonsense");

    }

    @Test
    public void unification3() throws Exception { //its not clear anymore how to build atomic terms so we will just get them out with hands crossed...

        String T1="<(*,%1,%1,$1) --> wu>";
        String T2="<(*,lol,lol2,$1) --> wu>";
        TestNAR tester = test();
        tester.believe(T1); //.en("If robin is a type of bird then robin can fly.");
        tester.believe(T2); //.en("Robin is a type of bird.");
        tester.run(10);

        Concept ret = tester.nar.concept(T1);
        Concept ret2 = tester.nar.concept(T2);

        //these we wanted, but we had to do the crap above since I forgot how to construct terms by strings..
        Term Term1 = ret.getTerm();
        Term Term2 = ret2.getTerm();

        FindSubst wu = new FindSubst(Op.VAR_PATTERN, new HashMap<Term,Term>(), new HashMap<Term,Term>(), new Random());
        boolean unifies = wu.next(Term1,Term2,1024);
        if(unifies)
            throw new Exception("Unification is doing nonsense");

    }

    @Test
    public void unification4() throws Exception { //its not clear anymore how to build atomic terms so we will just get them out with hands crossed...

        String T1="<(*,%1,%2,%3,$1) --> wu>";
        String T2="<(*,%1,lol2,%1,$1) --> wu>";
        TestNAR tester = test();
        tester.believe(T1); //.en("If robin is a type of bird then robin can fly.");
        tester.believe(T2); //.en("Robin is a type of bird.");
        tester.run(10);

        Concept ret = tester.nar.concept(T1);
        Concept ret2 = tester.nar.concept(T2);

        //these we wanted, but we had to do the crap above since I forgot how to construct terms by strings..
        Term Term1 = ret.getTerm();
        Term Term2 = ret2.getTerm();

        FindSubst wu = new FindSubst(Op.VAR_PATTERN, new HashMap<Term,Term>(), new HashMap<Term,Term>(), new Random());
        boolean unifies = wu.next(Term1,Term2,1024);
        if(!unifies)
            throw new Exception("Unification is doing nonsense");

    }

}
