package nars.nal.nal5;

import nars.NAR;
import nars.Op;
import nars.bag.Bag;
import nars.concept.Concept;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.meter.TestNAR;
import nars.nal.AbstractNALTest;
import nars.narsese.InvalidInputException;
import nars.term.Atom;
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
public class Patham9Test extends AbstractNALTest {

    public Patham9Test(Supplier<NAR> b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return AbstractNALTest.core6;
    }

    public void ProperlyLinkedTest(String premise1, String premise2) {
        TestNAR tester = test();
        tester.believe(premise1); //.en("If robin is a type of bird then robin can fly.");
        tester.believe(premise2); //.en("Robin is a type of bird.");
        tester.run(10);

        Concept ret = tester.nar.concept(premise1);
        boolean passed = false;
        for(TermLink entry : ret.getTermLinks()) {
            Term w = entry.getTerm();
            if(w.toString().equals(premise2)) {
                passed = true;
            }
        }

        Concept ret2 = tester.nar.concept(premise2);
        boolean passed2 = false;
        for(TermLink entry : ret2.getTermLinks()) {
            Term w = entry.getTerm();
            if(w.toString().equals(premise1)) {
                passed2 = true;
            }
        }

        if(passed && passed2) { //dummy to pass the test:
            tester.believe("<a --> b>");
        }
        tester.mustBelieve(10,"<a --> b>",0.9f);
    }

/*
    //don't touch this file - patham9
    @Test
    public void Linkage_NAL5_abduction() {
        ProperlyLinkedTest("<<robin --> bird> ==> <robin --> animal>>","<robin --> animal>");
    }


    @Test
    public void Linkage_NAL5_detachment() {
        ProperlyLinkedTest("<<robin --> bird> ==> <robin --> animal>>","<robin --> bird>");
    }

    @Test
    public void detachment() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin can fly.");
        tester.believe("<robin --> bird>"); //.en("Robin is a type of bird.");
        tester.mustBelieve(100,"<robin --> animal>",1.00f,0.81f); //.en("Robin is a type of animal.");
        tester.run();
    }

    @Test
    public void detachment2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>",0.70f,0.90f); //.en("Usually if robin is a type of bird then robin is a type of animal.");
        tester.believe("<robin --> animal>"); //.en("Robin is a type of animal.");
        tester.mustBelieve(100,"<robin --> bird>",1.00f,0.36f); //.en("I guess robin is a type of bird.");
        tester.run();
    }


    @Test
    public void Linkage_NAL6() {
        ProperlyLinkedTest("<<$x --> bird> ==> <$x --> animal>>","<tiger --> animal>");
    }

*/
/*
    @Test
    public void Linkage_NAL6_variable_elimination2() {
        ProperlyLinkedTest("<<$x --> bird> ==> <$x --> animal>>","<tiger --> animal>");
    }*/

    @Test
    public void variable_elimination2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<$x --> bird> ==> <$x --> animal>>"); //en("If something is a bird, then it is an animal.");
        tester.believe("<tiger --> animal>"); //en("A tiger is an animal.");
        tester.mustBelieve(1000, "<tiger --> bird>", 1.00f,0.45f); //en("I guess that a tiger is a bird.");
        tester.run();
    }
}
