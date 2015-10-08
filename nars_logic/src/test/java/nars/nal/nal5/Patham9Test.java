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


  /*  @Test
    public void variable_elimination2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<$x --> bird> ==> <$x --> animal>>"); //en("If something is a bird, then it is an animal.");
        tester.believe("<tiger --> animal>"); //en("A tiger is an animal.");
        tester.mustBelieve(1000, "<tiger --> bird>", 1.00f,0.45f); //en("I guess that a tiger is a bird.");
        tester.run();
    }*/
/*
    @Test
    public void set_operations() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<planetX --> {Mars,Pluto,Venus}>",0.9f,0.9f); //.en("PlanetX is Mars, Pluto, or Venus.");
        tester.believe("<planetX --> {Pluto,Saturn}>", 0.7f,0.9f); //.en("PlanetX is probably Pluto or Saturn.");
        tester.mustBelieve(500, "<planetX --> {Mars,Pluto,Saturn,Venus}>", 0.97f ,0.81f); //.en("PlanetX is Mars, Pluto, Saturn, or Venus.");
        tester.mustBelieve(500, "<planetX --> {Pluto}>", 0.63f ,0.81f); //.en("PlanetX is probably Pluto.");
        tester.run();
    }*/

    /*@Test
    public void variable_elimination() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<$x --> bird> ==> <$x --> animal>>"); //en("If something is a bird, then it is an animal.");
        tester.believe("<robin --> bird>"); //en("A robin is a bird.");
        tester.mustBelieve(1000,"<robin --> animal>",1.00f,0.81f); //en("A robin is an animal.");
        tester.run();
    }*/

    @Test
    public void multiple_variable_elimination3() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("(&&,<#x --> lock>,<<$y --> key> ==> <#x --> (/,open,$y,_)>>)"); //en("There is a lock that can be opened by every key.");
        tester.believe("<{lock1} --> lock>"); //en("Lock-1 is a lock.");
        tester.mustBelieve(100, "<<$1 --> key> ==> <{lock1} --> (/,open,$1,_)>>", 1.00f, 0.42f); //en("I guess Lock-1 can be opened by every key.");
        tester.run();
    }

}
