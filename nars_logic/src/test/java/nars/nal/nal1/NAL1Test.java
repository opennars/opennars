package nars.nal.nal1;

import nars.Global;
import nars.NARSeed;
import nars.nal.JavaNALTest;
import nars.nar.Classic;
import nars.nar.Default;
import nars.nar.DefaultMicro;
import nars.nar.experimental.Solid;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


public class NAL1Test extends JavaNALTest {

    public NAL1Test(NARSeed b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default()},
                {new Default().setInternalExperience(null)},
                {new Default().level(2)}, //why does this need level 2 for some tests?
                {new DefaultMicro() },
                {new Classic()},
                {new Solid(1, 64, 1, 4, 1, 3).setInternalExperience(null)},
                {new Solid(1, 64, 1, 4, 1, 3).level(2)},
                {new Solid(1, 64, 1, 4, 1, 3)}
                //{new Neuromorphic(4).setMaxInputsPerCycle(1).level(4)},
        });
    }



    @Test public void revision() throws InvalidInputException {


        final String birdIsATypeOfSwimmer = "<bird --> swimmer>";
        n.believe(birdIsATypeOfSwimmer)
                .en("bird is a type of swimmer.");
        n.believe(birdIsATypeOfSwimmer, 0.10f, 0.60f)
                .en("bird is probably not a type of swimmer.");
        /*n.mustOutput(3, birdIsATypeOfSwimmer, '.', 0.87f, 0.91f)
                .en("bird is very likely to be a type of swimmer.");*/
        n.mustBelieve(3, birdIsATypeOfSwimmer, 0.87f, 0.91f)
                .en("bird is very likely to be a type of swimmer.");
        n.run();
    }

    @Test
    public void deduction() throws InvalidInputException {


        n.believe("<bird --> animal>")
                .en("bird is a type of animal.")
                .es("bird es un tipo de animal.")
                .de("bird ist eine art des animal.");
        n.believe("<robin --> bird>")
                .en("robin is a type of bird.");
        n.mustBelieve(123, "<robin --> animal>", 0.81f)
                .en("robin is a type of animal.");
        n.run();
    }

    @Test
    public void abduction() throws InvalidInputException {
        n.believe("<sport --> competition>")
                .en("sport is a type of competition.");
        n.believe("<chess --> competition>", 0.90f, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                .en("chess is a type of competition.");

        n.mustBelieve(123, "<sport --> chess>", 1.0f, 0.42f)
                .en("I guess sport is a type of chess.")
                .en("sport is possibly a type of chess.")
                .es("es posible que sport es un tipo de chess.");
        n.mustBelieve(123, "<chess --> sport>", 0.90f, 0.45f)
                .en("I guess chess is a type of sport");
        n.run();
    }

    @Test
    public void induction() throws InvalidInputException {
        n.believe("<swan --> swimmer>", 0.90f, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                .en("Swan is a type of swimmer.");
        n.believe("<swan --> bird>")
                .en("Swan is a type of bird.");

        n.mustBelieve(123, "<bird --> swimmer>", 0.90f, 0.45f)
                .en("I guess bird is a type of swimmer.");
        n.mustBelieve(123, "<swimmer --> bird>", 1f, 0.42f)
                .en("I guess swimmer is a type of bird.");
        n.run();
    }

    @Test
    public void exemplification() throws InvalidInputException {
        n.believe("<robin --> bird>");
        n.believe("<bird --> animal>");
        n.mustOutput(125, "<animal --> robin>. %1.00;0.45%")
                .en("I guess animal is a type of robin.");
        n.run();
    }



    @Test
    public void conversion() throws InvalidInputException {
        //TextOutput.out(nar);

        long time = n.nal() == 1 ? 15 : 305;
        n.believe("<bird --> swimmer>");
        n.ask("<swimmer --> bird>")
                .en("Is swimmer a type of bird?");
        n.mustOutput(time, "<swimmer --> bird>. %1.00;0.47%");
        n.run();
    }

    @Test
    public void yesnoQuestion() throws InvalidInputException {
        n.believe("<bird --> swimmer>");
        n.ask("<bird --> swimmer>");
        n.mustOutput(25, "<bird --> swimmer>. %1.00;0.90%");
        n.run();
    }


    @Test
    public void whQuestion() throws InvalidInputException {
//        System.out.println("\n\n\n START ------------");
//        TextOutput.out(n);
//        NARTrace.out(n);

        n.mustOutput(150, "<bird --> swimmer>. %1.00;0.80%");
        n.believe("<bird --> swimmer>", 1.0f, 0.8f);
        n.ask("<?x --> swimmer>")
                .en("What is a type of swimmer?");
        n.run();
    }


    @Test
    public void backwardInference() throws InvalidInputException {
        long time = build instanceof Solid ? 15 : 350;

        n.mustOutput(time, "<?1 --> bird>?").en("What is a type of bird?");
        n.mustOutput(time, "<bird --> ?1>?").en("What is the type of bird?");

        n.believe("<bird --> swimmer>", 1.0f, 0.8f);
        n.ask("<?1 --> swimmer>");
        n.run();
    }


    @Test
    public void multistep() throws InvalidInputException {
        long time = build instanceof Solid ? 15 : 1750;

        //we know also 73% is the theoretical maximum it can reach
        if (n.nal() <= 2)
            n.mustBelieve(time, "<a --> d>", 1f, 1f, 0.25f, 0.99f);
        else
            //originally checked for 0.25% exact confidence
            n.mustBelieve(time, "<a --> d>", 1f, 1f, 0.25f, 0.99f);

        //TextOutput.out(nar);
        n.believe("<a --> b>", 1.0f, 0.9f);
        n.believe("<b --> c>", 1.0f, 0.9f);
        n.believe("<c --> d>", 1.0f, 0.9f);
        n.ask("<a --> d>");




        n.run();
    }


}
