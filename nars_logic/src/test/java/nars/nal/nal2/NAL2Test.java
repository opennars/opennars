package nars.nal.nal2;


import nars.NAR;
import nars.Narsese;
import nars.nal.AbstractNALTester;
import nars.util.meter.TestNAR;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Supplier;

@RunWith(Parameterized.class)
public class NAL2Test extends AbstractNALTester {

    public NAL2Test(Supplier<NAR> b) { super(b); }

    static final int cycles = 600;

    @Parameterized.Parameters(name = "{index}:{0}")
    public static Iterable<Supplier<NAR>> configurations() {
        return AbstractNALTester.nars(2, false);
    }

    @Test
    public void revision() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<robin <-> swan>") ;//;//Robin is similar to swan.");
        tester.believe("<robin <-> swan>", 0.1f, 0.6f);
        tester.mustBelieve(cycles,"<robin <-> swan>",0.87f,0.91f) ;//;//Robin is probably similar to swan.");

    }

    @Test
    public void comparison() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<swan --> swimmer>",0.9f,0.9f);//Swan is a type of swimmer.");
        tester.believe("<swan --> bird>");//Swan is a type of bird.");
        tester.mustBelieve(cycles,"<bird <-> swimmer>",0.9f,0.45f);//I guess that bird is similar to swimmer.");

    }

    @Test public void comparison2() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<sport --> competition>"); //Sport is a type of competition.");
        tester.believe("<chess --> competition>", 0.9f, 0.9f);//Chess is a type of competition.");
        tester.mustBelieve(cycles, "<chess <-> sport>", 0.9f, 0.45f);//I guess chess is similar to sport.");

    }

    @Test
    public void analogy() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<swan --> swimmer>");//Swan is a type of swimmer.");
        tester.believe("<gull <-> swan>");//Gull is similar to swan.");
        tester.mustBelieve(cycles,"<gull --> swimmer>",1.0f,0.81f);//I think gull is a type of swimmer.");

    }

    @Test
    public void analogy2() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<gull --> swimmer>");//Gull is a type of swimmer.");
        tester.believe("<gull <-> swan>");//Gull is similar to swan.");
        tester.mustBelieve(cycles, "<swan --> swimmer>",1.0f,0.81f);//I believe a swan is a type of swimmer.");

    }

    @Test
    public void resemblance() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<robin <-> swan>");//Robin is similar to swan.");
        tester.believe("<gull <-> swan>");//Gull is similar to swan.");
        tester.mustBelieve(cycles, "<gull <-> robin>", 1.0f, 0.81f);//Gull is similar to robin.");

    }

    @Test
    public void inheritanceToSimilarity() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<swan --> bird>");//Swan is a type of bird. ");
        tester.believe("<bird --> swan>",0.1f,0.9f);//Bird is not a type of swan.");
        tester.mustBelieve(cycles,"<bird <-> swan>",0.1f,0.81f);//Bird is different from swan.");

    }

    @Test
    public void inheritanceToSimilarity2() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<swan --> bird>");//Swan is a type of bird.");
        tester.believe("<bird <-> swan>",0.1f,0.9f);//Bird is different from swan.");
        tester.mustBelieve(cycles,"<bird --> swan>",0.1f,0.73f);//Bird is probably not a type of swan.");
    }

    @Test
    public void inheritanceToSimilarity3() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<swan --> bird>",0.9f,0.9f);//Swan is a type of bird.");
        tester.ask("<bird <-> swan>");//Is bird similar to swan?");
        tester.mustBelieve(cycles,"<bird <-> swan>",0.9f,0.45f);//I guess that bird is similar to swan.");

    }

    @Test
    public void inheritanceToSimilarity4() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<bird <-> swan>",0.9f,0.9f);//a bird is similar to a swan.");
        tester.ask("<swan --> bird>");//Is swan a type of bird?");
        tester.mustBelieve(cycles,"<swan --> bird>",0.9f,0.81f);//A swan is a type of bird.");

    }

    /* Handled by parser, this copula is just syntactic sugar
    @Test
    public void instanceToInheritance() throws InvalidInputException {
        test()
        .believe("<Tweety {-- bird>")//Tweety is a bird.");
        .mustBelieve(cycles,"<{Tweety} --> bird>",1.0f,0.9f)//Tweety is a bird.");
        .run();
    }*/

    /* Handled by parser, this copula is just syntactic sugar
    @Test
    public void propertyToInheritance() throws InvalidInputException {
        test().believe("<raven --] black>")//Ravens are black.");
        .mustBelieve(cycles,"<raven --> [black]>",1.0f,0.9f)//Ravens are black.");
        .run();
    }*/

    /* Handled by parser, this copula is just syntactic sugar
    @Test
    public void instancePropertyToInheritance() throws InvalidInputException {
        test().believe("<Tweety {-] yellow>") //Tweety is yellow.");
        .mustBelieve(cycles,"<{Tweety} --> [yellow]>",1.0f,0.9f)//Tweety is yellow.");
        .run();
    }
*/

    @Test
    public void setDefinition() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.believe("<{Tweety} --> {Birdie}>");//Tweety is Birdie.");
        tester.mustBelieve(cycles,"<{Tweety} <-> {Birdie}>",1.0f,0.9f);//Birdie is similar to Tweety.");


    }

    @Test
    public void setDefinition2() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<[smart] --> [bright]>");//Smart thing is a type of bright thing.");
        tester.mustBelieve(cycles,"<[bright] <-> [smart]>",1.0f,0.9f);//Bright thing is similar to smart thing.");

    }

    @Test
    public void setDefinition3() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<{Birdie} <-> {Tweety}>");//Birdie is similar to Tweety.");
        tester.mustBelieve(cycles,"<Birdie <-> Tweety>",1.0f,0.9f);//Birdie is similar to Tweety.");
        tester.mustBelieve(cycles,"<{Tweety} --> {Birdie}>",1.0f,0.9f);//Tweety is Birdie.");

    }

    @Test
    public void setDefinition4() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<[bright] <-> [smart]>");//Bright thing is similar to smart thing.");
        tester.mustBelieve(cycles, "<bright <-> smart>", 1.0f, 0.9f);//Bright is similar to smart.");
        tester.mustBelieve(cycles,"<[bright] --> [smart]>",1.0f,0.9f);//Bright thing is a type of smart thing.");

    }

    @Test
    public void structureTransformation() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<Birdie <-> Tweety>", 0.9f, 0.9f);//Birdie is similar to Tweety.");
        tester.ask("<{Birdie} <-> {Tweety}>");//Is Birdie similar to Tweety?");
        tester.mustBelieve(cycles, "<{Birdie} <-> {Tweety}>", 0.9f,0.9f);//Birdie is similar to Tweety.");

    }

    @Test
    public void structureTransformation2() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<bright <-> smart>", 0.9f, 0.9f);//Bright is similar to smart.");
        tester.ask("<[bright] --> [smart]>");//Is bright thing a type of smart thing?");
        tester.mustBelieve(cycles, "<[bright] --> [smart]>", 0.9f,0.9f);//Bright thing is a type of smart thing.");

    }

    @Test
    public void structureTransformation3() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<bright <-> smart>", 0.9f, 0.9f);//Bright is similar to smart.");
        tester.ask("<{bright} --> {smart}>");//Is bright thing a type of smart thing?");
        tester.mustBelieve(cycles, "<{bright} --> {smart}>", 0.9f,0.9f);//Bright thing is a type of smart thing.");

    }

    @Test
    public void backwardInference() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<bird --> swimmer>");//Bird is a type of swimmer. ");
        tester.ask("<{?x} --> swimmer>");//What is a swimmer?");
        tester.mustOutput(cycles, "<{?1} --> bird>?");//What is a bird?");

    }

    @Test
    public void missingEdgeCase1() {
        //		((<%1 --> %2>, <%2 <-> %3>, not_equal(%3, %1)), (<%1 --> %3>, (<Analogy --> Truth>, <Strong --> Desire>, <AllowBackward --> Derive>)))
        //((<%1 --> %2>, <%2 <-> %3>, not_equal(%3, %1)),
        //      (<%1 --> %3>,
        //((<p1 --> p2>, <p2 <-> p3>, not_equal(p3, p1)),
        //      (<p1 --> p3>,
        //        TestNAR tester = test();
        TestNAR tester = test();
        tester.believe("<p1 --> p2>");
        tester.believe("<p2 <-> p3>");
        tester.mustBelieve(100, "<p1 --> p3>",
                1.0f, 1.0f, 0.81f, 1.0f);
        tester.debug();
    }

}

