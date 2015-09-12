package nars.nal.nal1;

import nars.Global;
import nars.NAR;
import nars.meter.experiment.DeductiveChainTest;
import nars.nal.JavaNALTest;
import nars.nar.Default;
import nars.nar.experimental.DefaultAlann;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


public class NAL1Test extends JavaNALTest {

    public NAL1Test(NAR b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                //{new Default()},
//                {new Default().setInternalExperience(null)},

                //{new NewDefault()},
                {new Default().nal(1)},
                {new Default().nal(2)},
                {new Default() },

//                {new Default().level(2)}, //why does this need level 2 for some tests?
//                {new DefaultMicro().level(2) },
//                {new Classic()},

                {new DefaultAlann(48)},

                //{new Solid(1, 48, 1, 2, 1, 3).level(1)},
                //{new Solid(1, 64, 1, 2, 1, 3).level(2)},
        });
    }

//
//    @Before
//    public void setup() {
//
//        //tester.setTemporalTolerance(50 /* cycles */);
//    }

    @Test public void revision() throws InvalidInputException {

        final String birdIsATypeOfSwimmer = "<bird --> swimmer>";
        tester.believe(birdIsATypeOfSwimmer)
                .en("bird is a type of swimmer.");
        tester.believe(birdIsATypeOfSwimmer, 0.10f, 0.60f)
                .en("bird is probably not a type of swimmer.");
        /*n.mustOutput(3, birdIsATypeOfSwimmer, '.', 0.87f, 0.91f)
                .en("bird is very likely to be a type of swimmer.");*/
        tester.mustBelieve(3, birdIsATypeOfSwimmer, 0.87f, 0.91f)
                .en("bird is very likely to be a type of swimmer.");
        tester.run();
    }

    @Test
    public void deduction() throws InvalidInputException {

        tester.believe("<bird --> animal>")
                .en("bird is a type of animal.")
                .es("bird es un tipo de animal.")
                .de("bird ist eine art des animal.");
        tester.believe("<robin --> bird>")
                .en("robin is a type of bird.");
        tester.mustBelieve(123, "<robin --> animal>", 0.81f)
                .en("robin is a type of animal.");
        tester.run();
    }

    @Test
    public void abduction() throws InvalidInputException {

        tester.mustBelieve(123, "<sport --> chess>", 1.0f, 0.42f)
                .en("I guess sport is a type of chess.")
                .en("sport is possibly a type of chess.")
                .es("es posible que sport es un tipo de chess.");
        tester.mustBelieve(123, "<chess --> sport>", 0.90f, 0.45f)
                .en("I guess chess is a type of sport");

        tester.believe("<sport --> competition>")
                .en("sport is a type of competition.");
        tester.believe("<chess --> competition>", 0.90f, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                .en("chess is a type of competition.");

        tester.run();
    }

    @Test
    public void induction() throws InvalidInputException {
        tester.believe("<swan --> swimmer>", 0.90f, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                .en("Swan is a type of swimmer.");
        tester.believe("<swan --> bird>")
                .en("Swan is a type of bird.");

        tester.mustBelieve(123, "<bird --> swimmer>", 0.90f, 0.45f)
                .en("I guess bird is a type of swimmer.");
        tester.mustBelieve(123, "<swimmer --> bird>", 1f, 0.42f)
                .en("I guess swimmer is a type of bird.");
        tester.run();
    }

    @Test
    public void exemplification() throws InvalidInputException {
        tester.believe("<robin --> bird>");
        tester.believe("<bird --> animal>");
        tester.mustOutput(125, "<animal --> robin>. %1.00;0.45%")
                .en("I guess animal is a type of robin.");
        tester.run();
    }



    @Test
    public void conversion() throws InvalidInputException {
        //TextOutput.out(nar);

        long time = /*tester.nal() <= 2 ? 15 :*/ 305;
        tester.believe("<bird --> swimmer>");
        tester.ask("<swimmer --> bird>")
                .en("Is swimmer a type of bird?");
        tester.mustOutput(time, "<swimmer --> bird>. %1.00;0.47%");
        tester.run();
    }

    @Test
    public void yesnoQuestion() throws InvalidInputException {
        tester.believe("<bird --> swimmer>");
        tester.ask("<bird --> swimmer>");
        tester.mustOutput(25, "<bird --> swimmer>. %1.00;0.90%");
        tester.run();
    }


    @Test
    public void whQuestion() throws InvalidInputException {
//        System.out.println("\n\n\n START ------------");
//        TextOutput.out(n);
//        NARTrace.out(n);

        tester.believe("<bird --> swimmer>", 1.0f, 0.8f);
        tester.ask("<?x --> swimmer>").en("What is a type of swimmer?");
        tester.mustOutput(150, "<bird --> swimmer>. %1.00;0.80%");
        tester.run();
    }


    @Test
    public void backwardInference() throws InvalidInputException {
        long time = /*nar instanceof Solid ? 15 :*/ 350;

        tester.mustOutput(time, "<?1 --> bird>?").en("What is a type of bird?");
        tester.mustOutput(time, "<bird --> ?1>?").en("What is the type of bird?");

        tester.believe("<bird --> swimmer>", 1.0f, 0.8f);
        tester.ask("<?1 --> swimmer>");
        tester.run();
    }



    public void multistepN(int length)  {
        new DeductiveChainTest(length).apply(tester, length*200);
        tester.run();
    }

    @Test public void multistep2() { multistepN(2);     }
    @Test public void multistep3() { multistepN(3);     }
    @Test public void multistep4() { multistepN(4);     }

    @Test
    public void multistep() throws InvalidInputException {
        long time = 150;

        //TextOutput.out(n);

        //we know also 73% is the theoretical maximum it can reach
        if (tester.nar.nal() <= 2)
            tester.mustBelieve(time, "<a --> d>", 1f, 1f, 0.25f, 0.99f);
        else
            //originally checked for 0.25% exact confidence
            tester.mustBelieve(time, "<a --> d>", 1f, 1f, 0.25f, 0.99f);

        tester.believe("<a --> b>", 1.0f, 0.9f);
        tester.believe("<b --> c>", 1.0f, 0.9f);
        tester.believe("<c --> d>", 1.0f, 0.9f);




        tester.run();
    }


}
