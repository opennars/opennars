package nars.nal.nal2;


import nars.NAR;
import nars.nal.JavaNALTest;
import nars.nar.Default;
import nars.nar.experimental.DefaultAlann;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

public class NAL2Test extends JavaNALTest {

    public NAL2Test(NAR b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                //{new Default()}, //NAL8 + NAL9 didnt solve it
                //{new Default().level(3)}, //needs 3 for sets
                //{new Default().setInternalExperience(null)},


                {new Default() },
                {new Default().nal(2) },
                //{new Equalized().setInternalExperience(null)},
                //{new Equalized().setInternalExperience(null).level(4)},

                {new DefaultAlann(64)}

                //{new DefaultMicro() },
                //{new Classic()}

        });
    }


    @Test
    public void revision() throws InvalidInputException {
        tester.believe("<robin <-> swan>").en("Robin is similar to swan.");
        tester.believe("<robin <-> swan>", 0.1f, 0.6f);
        tester.mustBelieve(100,"<robin <-> swan>",0.87f,0.91f).en("Robin is probably similar to swan.");
        tester.run();
    }

    @Test
    public void comparison() throws InvalidInputException {
        tester.believe("<swan --> swimmer>",0.9f,0.9f).en("Swan is a type of swimmer.");
        tester.believe("<swan --> bird>").en("Swan is a type of bird.");
        tester.mustBelieve(30,"<bird <-> swimmer>",0.9f,0.45f).en("I guess that bird is similar to swimmer.");
        tester.run();
    }

    @Test
    public void comparison2() throws InvalidInputException {
        tester.believe("<sport --> competition>").en("Sport is a type of competition.");
        tester.believe("<chess --> competition>",0.9f,0.9f).en("Chess is a type of competition.");
        tester.mustBelieve(30,"<chess <-> sport>",0.9f,0.45f).en("I guess chess is similar to sport.");
        tester.run();
    }

    @Test
    public void analogy() throws InvalidInputException {
        tester.believe("<swan --> swimmer>").en("Swan is a type of swimmer.");
        tester.believe("<gull <-> swan>").en("Gull is similar to swan.");
        tester.mustBelieve(30,"<gull --> swimmer>",1.0f,0.81f).en("I think gull is a type of swimmer.");
        tester.run();
    }

    @Test
    public void analogy2() throws InvalidInputException {
        tester.believe("<gull --> swimmer>").en("Gull is a type of swimmer.");
        tester.believe("<gull <-> swan>").en("Gull is similar to swan.");
        tester.mustBelieve(100,"<swan --> swimmer>",1.0f,0.81f).en("I believe a swan is a type of swimmer.");
        tester.run();
    }

    @Test
    public void resemblance() throws InvalidInputException {
        tester.believe("<robin <-> swan>").en("Robin is similar to swan.");
        tester.believe("<gull <-> swan>").en("Gull is similar to swan.");
        tester.mustBelieve(30, "<gull <-> robin>", 1.0f, 0.81f).en("Gull is similar to robin.");
        tester.run();
    }

    @Test
    public void inheritanceToSimilarity() throws InvalidInputException {
        tester.believe("<swan --> bird>").en("Swan is a type of bird. ");
        tester.believe("<bird --> swan>",0.1f,0.9f).en("Bird is not a type of swan.");
        tester.mustBelieve(150,"<bird <-> swan>",0.1f,0.81f).en("Bird is different from swan.");
        tester.run();
    }

    @Test
    public void inheritanceToSimilarity2() throws InvalidInputException {
        tester.believe("<swan --> bird>").en("Swan is a type of bird.");
        tester.believe("<bird <-> swan>",0.1f,0.9f).en("Bird is different from swan.");
        tester.mustBelieve(150,"<bird --> swan>",0.1f,0.73f).en("Bird is probably not a type of swan.");
        tester.run();
    }

    @Test
    public void inheritanceToSimilarity3() throws InvalidInputException {
        tester.believe("<swan --> bird>",0.9f,0.9f).en("Swan is a type of bird.");
        tester.ask("<bird <-> swan>").en("Is bird similar to swan?");
        tester.mustBelieve(150,"<bird <-> swan>",0.9f,0.47f).en("I guess that bird is similar to swan.");
        tester.run();
    }

    @Test
    public void inheritanceToSimilarity4() throws InvalidInputException {
        tester.believe("<bird <-> swan>",0.9f,0.9f).en("a bird is similar to a swan.");
        tester.ask("<swan --> bird>").en("Is swan a type of bird?");
        tester.mustBelieve(150,"<swan --> bird>",0.9f,0.81f).en("A swan is a type of bird.");
        tester.run();
    }

   /* @Test
    public void instanceToInheritance() throws InvalidInputException {
        n.believe("<Tweety {-- bird>").en("Tweety is a bird.");
        n.mustBelieve(50,"<{Tweety} --> bird>",1.0f,0.9f).en("Tweety is a bird.");
        n.run();
    }*/

   /*@Test
    public void propertyToInheritance() throws InvalidInputException {
        n.believe("<raven --] black>").en("Ravens are black.");
        n.mustBelieve(50,"<raven --> [black]>",1.0f,0.9f).en("Ravens are black.");
        n.run();
    }*/

   /* @Test
    public void instancePropertyToInheritance() throws InvalidInputException {
        n.believe("<Tweety {-] yellow>").en("Tweety is yellow.");
        n.mustBelieve(50,"<{Tweety} --> [yellow]>",1.0f,0.9f).en("Tweety is yellow.");
        n.run();
    }*/

    @Test
    public void setDefinition() throws InvalidInputException {
        tester.believe("<{Tweety} --> {Birdie}>").en("Tweety is Birdie.");
        tester.mustBelieve(50,"<{Birdie} <-> {Tweety}>",1.0f,0.9f).en("Birdie is similar to Tweety.");
        tester.run();
    }

    @Test
    public void setDefinition2() throws InvalidInputException {
        tester.believe("<[smart] --> [bright]>").en("Smart thing is a type of bright thing.");
        tester.mustBelieve(50,"<[bright] <-> [smart]>",1.0f,0.9f).en("Bright thing is similar to smart thing.");
        tester.run();
    }

    @Test
    public void setDefinition3() throws InvalidInputException {
        tester.believe("<{Birdie} <-> {Tweety}>").en("Birdie is similar to Tweety.");
        tester.mustBelieve(50,"<Birdie <-> Tweety>",1.0f,0.9f).en("Birdie is similar to Tweety.");
        tester.mustBelieve(50,"<{Tweety} --> {Birdie}>",1.0f,0.9f).en("Tweety is Birdie.");
        tester.run();
    }

    @Test
    public void setDefinition4() throws InvalidInputException {
        tester.believe("<[bright] <-> [smart]>").en("Bright thing is similar to smart thing.");
        tester.mustBelieve(50,"<bright <-> smart>",1.0f,0.9f).en("Bright is similar to smart.");
        tester.mustBelieve(50,"<[bright] --> [smart]>",1.0f,0.9f).en("Bright thing is a type of smart thing.");
        tester.run();
    }

    @Test
    public void structureTransformation() throws InvalidInputException {
        tester.believe("<Birdie <-> Tweety>", 0.9f, 0.9f).en("Birdie is similar to Tweety.");
        tester.ask("<{Birdie} <-> {Tweety}>").en("Is Birdie similar to Tweety?");
        tester.mustBelieve(320, "<{Birdie} <-> {Tweety}>", 0.9f,0.9f).en("Birdie is similar to Tweety.");
        tester.run();
    }

    @Test
    public void structureTransformation2() throws InvalidInputException {
        tester.believe("<bright <-> smart>", 0.9f, 0.9f).en("Bright is similar to smart.");
        tester.ask("<[bright] --> [smart]>").en("Is bright thing a type of smart thing?");
        tester.mustBelieve(320, "<[bright] --> [smart]>", 0.9f,0.9f).en("Bright thing is a type of smart thing.");
        tester.run();
    }

    @Test
    public void backwardInference() throws InvalidInputException {
        tester.believe("<bird --> swimmer>").en("Bird is a type of swimmer. ");
        tester.ask("<{?x} --> swimmer>").en("What is a swimmer?");
        tester.mustOutput(100, "<{?1} --> bird>?").en("What is a bird?");
        tester.run();
    }
}

