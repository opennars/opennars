package nars.nal.nal1;

import nars.Global;
import nars.NARSeed;
import nars.io.out.TextOutput;
import nars.model.impl.*;
import nars.nal.JavaNALTest;
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
                {new Curve().setInternalExperience(null)},
                {new DefaultMicro() },
                {new Classic()},
                {new Solid(1, 64, 0, 4, 0, 3)}
                //{new Neuromorphic(4).setMaxInputsPerCycle(1).level(4)},
        });
    }



    @Test
    public void revision() throws InvalidInputException {
        nar.mustOutput(3, "<bird --> swimmer>", '.', 0.87f, 0.91f)
                .en("bird is very likely to be a type of swimmer.");
        nar.believe("<bird --> swimmer>")
                .en("bird is a type of swimmer.");
        nar.believe("<bird --> swimmer>", 0.10f, 0.60f)
                .en("bird is probably not a type of swimmer.");
        nar.run();
    }

    @Test
    public void deduction() throws InvalidInputException {
        nar.believe("<bird --> animal>")
                .en("bird is a type of animal.")
                .es("bird es un tipo de animal.")
                .de("bird ist eine art des animal.");
        nar.believe("<robin --> bird>")
                .en("robin is a type of bird.");
        nar.mustBelieve(23, "<robin --> animal>", 0.81f)
                .en("robin is a type of animal.");
        nar.run();
    }

    @Test
    public void abduction() throws InvalidInputException {
        nar.believe("<sport --> competition>")
                .en("sport is a type of competition.");
        nar.believe("<chess --> competition>", 0.90f, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                .en("chess is a type of competition.");

        nar.mustBelieve(23, "<sport --> chess>", 1.0f, 0.42f)
                .en("I guess sport is a type of chess.")
                .en("sport is possibly a type of chess.")
                .es("es posible que sport es un tipo de chess.");
        nar.mustBelieve(23, "<chess --> sport>", 0.90f, 0.45f)
                .en("I guess chess is a type of sport");
        nar.run();
    }

    @Test
    public void induction() throws InvalidInputException {
        nar.believe("<swan --> swimmer>", 0.90f, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                .en("Swan is a type of swimmer.");
        nar.believe("<swan --> bird>")
                .en("Swan is a type of bird.");

        nar.mustBelieve(23, "<bird --> swimmer>", 0.90f, 0.45f)
                .en("I guess bird is a type of swimmer.");
        nar.mustBelieve(23, "<swimmer --> bird>", 1f, 0.42f)
                .en("I guess swimmer is a type of bird.");
        nar.run();
    }

    @Test
    public void exemplification() throws InvalidInputException {
        nar.believe("<robin --> bird>");
        nar.believe("<bird --> animal>");
        nar.mustOutput(25, "<animal --> robin>. %1.00;0.45%")
                .en("I guess animal is a type of robin.");
        nar.run();
    }



    @Test
    public void conversion() throws InvalidInputException {
        long time = nar.nal() == 1 ? 35 : 305;
        nar.believe("<bird --> swimmer>");
        nar.ask("<swimmer --> bird>")
                .en("Is swimmer a type of bird?");
        nar.mustOutput(time, "<swimmer --> bird>. %1.00;0.47%");
        nar.run();
    }

    @Test
    public void yesnoQuestion() throws InvalidInputException {
        nar.believe("<bird --> swimmer>");
        nar.ask("<bird --> swimmer>");
        nar.mustOutput(25, "<bird --> swimmer>. %1.00;0.90%");
        nar.run();
    }


    @Test
    public void whQuestion() throws InvalidInputException {
        nar.believe("<bird --> swimmer>", 1.0f, 0.8f);
        nar.ask("<?x --> swimmer>")
                .en("What is a type of swimmer?");
        nar.mustOutput(25, "<bird --> swimmer>. %1.00;0.80%");
        nar.run();
    }


    @Test
    public void backwardInference() throws InvalidInputException {
        long time = build instanceof Solid ? 15 : 350;

        TextOutput.out(nar);

        nar.believe("<bird --> swimmer>", 1.0f, 0.8f);
        nar.ask("<?1 --> swimmer>");
        nar.mustOutput(time, "<?1 --> bird>?").en("What is a type of bird?");
        nar.mustOutput(time, "<bird --> ?1>?").en("What is the type of bird?");
        nar.run();
    }


    @Test
    public void multistep() throws InvalidInputException {
        long time = build instanceof Solid ? 15 : 750;

        //TextOutput.out(nar);
        nar.believe("<a --> b>", 1.0f, 0.9f);
        nar.believe("<b --> c>", 1.0f, 0.9f);
        nar.believe("<c --> d>", 1.0f, 0.9f);
        nar.ask("<a --> d>");

        //originally checked for 0.25% exact confidence
        nar.mustBelieve(time, "<a --> d>", 1f, 1f, 0.25f, 0.99f);

        //but we know also 73% is the theoretical maximum it can reach
        if (nar.nal() == 1)
            nar.mustBelieve(time, "<a --> d>", 1f, 1f, 0.25f, 0.99f);

        nar.run();
    }


}
