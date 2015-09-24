package nars.nal.nal2;


import nars.NAR;
import nars.meter.TestNAR;
import nars.nal.AbstractNALTest;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Supplier;

@RunWith(Parameterized.class)
public class NAL2Test extends AbstractNALTest {

    public NAL2Test(Supplier<NAR> b) { super(b); }

    final static int cycles = 150;

    @Parameterized.Parameters(name = "{index}:{0}")
    public static Iterable<Supplier<NAR>> configurations() {
        return AbstractNALTest.core6;
    }

    @Test
    public void revision() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<robin <-> swan>") ;//;//Robin is similar to swan.");
        tester.believe("<robin <-> swan>", 0.1f, 0.6f);
        tester.mustBelieve(cycles,"<robin <-> swan>",0.87f,0.91f) ;//;//Robin is probably similar to swan.");
        tester.run();
    }

    @Test
    public void comparison() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<swan --> swimmer>",0.9f,0.9f);//Swan is a type of swimmer.");
        tester.believe("<swan --> bird>");//Swan is a type of bird.");
        tester.mustBelieve(cycles,"<bird <-> swimmer>",0.9f,0.45f);//I guess that bird is similar to swimmer.");
        tester.run();
    }

    @Test public void comparison2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<sport --> competition>"); //Sport is a type of competition.");
        tester.believe("<chess --> competition>", 0.9f, 0.9f);//Chess is a type of competition.");
        tester.mustBelieve(cycles, "<chess <-> sport>", 0.9f, 0.45f);//I guess chess is similar to sport.");
        tester.run();
    }

    @Test
    public void analogy() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<swan --> swimmer>");//Swan is a type of swimmer.");
        tester.believe("<gull <-> swan>");//Gull is similar to swan.");
        tester.mustBelieve(cycles,"<gull --> swimmer>",1.0f,0.81f);//I think gull is a type of swimmer.");
        tester.run();
    }

    @Test
    public void analogy2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<gull --> swimmer>");//Gull is a type of swimmer.");
        tester.believe("<gull <-> swan>");//Gull is similar to swan.");
        tester.mustBelieve(cycles, "<swan --> swimmer>",1.0f,0.81f);//I believe a swan is a type of swimmer.");
        tester.run();
    }

    @Test
    public void resemblance() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<robin <-> swan>");//Robin is similar to swan.");
        tester.believe("<gull <-> swan>");//Gull is similar to swan.");
        tester.mustBelieve(cycles, "<gull <-> robin>", 1.0f, 0.81f);//Gull is similar to robin.");
        tester.run();
    }

    @Test
    public void inheritanceToSimilarity() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<swan --> bird>");//Swan is a type of bird. ");
        tester.believe("<bird --> swan>",0.1f,0.9f);//Bird is not a type of swan.");
        tester.mustBelieve(cycles,"<bird <-> swan>",0.1f,0.81f);//Bird is different from swan.");
        tester.run();
    }

    @Test
    public void inheritanceToSimilarity2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<swan --> bird>");//Swan is a type of bird.");
        tester.believe("<bird <-> swan>",0.1f,0.9f);//Bird is different from swan.");
        tester.mustBelieve(cycles,"<bird --> swan>",0.1f,0.73f);//Bird is probably not a type of swan.");
        tester.run();
    }

    @Test
    public void inheritanceToSimilarity3() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<swan --> bird>",0.9f,0.9f);//Swan is a type of bird.");
        tester.ask("<bird <-> swan>");//Is bird similar to swan?");
        tester.mustBelieve(cycles,"<bird <-> swan>",0.9f,0.47f);//I guess that bird is similar to swan.");
        tester.run();
    }

    @Test
    public void inheritanceToSimilarity4() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<bird <-> swan>",0.9f,0.9f);//a bird is similar to a swan.");
        tester.ask("<swan --> bird>");//Is swan a type of bird?");
        tester.mustBelieve(cycles,"<swan --> bird>",0.9f,0.81f);//A swan is a type of bird.");
        tester.run();
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
    public void setDefinition() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<{Tweety} --> {Birdie}>");//Tweety is Birdie.");
        tester.mustBelieve(cycles,"<{Tweety} <-> {Birdie}>",1.0f,0.9f);//Birdie is similar to Tweety.");
        tester.run();
    }

    @Test
    public void setDefinition2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<[smart] --> [bright]>");//Smart thing is a type of bright thing.");
        tester.mustBelieve(cycles,"<[bright] <-> [smart]>",1.0f,0.9f);//Bright thing is similar to smart thing.");
        tester.run();
    }

    @Test
    public void setDefinition3() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<{Birdie} <-> {Tweety}>");//Birdie is similar to Tweety.");
        tester.mustBelieve(cycles,"<Birdie <-> Tweety>",1.0f,0.9f);//Birdie is similar to Tweety.");
        tester.mustBelieve(cycles,"<{Tweety} --> {Birdie}>",1.0f,0.9f);//Tweety is Birdie.");
        tester.run();
    }

    @Test
    public void setDefinition4() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<[bright] <-> [smart]>");//Bright thing is similar to smart thing.");
        tester.mustBelieve(cycles,"<bright <-> smart>",1.0f,0.9f);//Bright is similar to smart.");
        tester.mustBelieve(cycles,"<[bright] --> [smart]>",1.0f,0.9f);//Bright thing is a type of smart thing.");
        tester.run();
    }

    @Test
    public void structureTransformation() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<Birdie <-> Tweety>", 0.9f, 0.9f);//Birdie is similar to Tweety.");
        tester.ask("<{Birdie} <-> {Tweety}>");//Is Birdie similar to Tweety?");
        tester.mustBelieve(cycles, "<{Birdie} <-> {Tweety}>", 0.9f,0.9f);//Birdie is similar to Tweety.");
        tester.run();
    }

    @Test
    public void structureTransformation2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<bright <-> smart>", 0.9f, 0.9f);//Bright is similar to smart.");
        tester.ask("<[bright] --> [smart]>");//Is bright thing a type of smart thing?");
        tester.mustBelieve(cycles, "<[bright] --> [smart]>", 0.9f,0.9f);//Bright thing is a type of smart thing.");
        tester.run();
    }

    @Test
    public void backwardInference() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<bird --> swimmer>");//Bird is a type of swimmer. ");
        tester.ask("<{?x} --> swimmer>");//What is a swimmer?");
        tester.mustOutput(cycles, "<{?1} --> bird>?");//What is a bird?");
        tester.run();
    }
}

