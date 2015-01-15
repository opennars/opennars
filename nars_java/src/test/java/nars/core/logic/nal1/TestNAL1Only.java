package nars.core.logic.nal1;

import nars.core.AbstractNALTest;
import nars.core.Build;
import nars.core.Parameters;
import nars.core.build.Default;
import nars.io.narsese.Narsese;
import org.junit.Test;


public class TestNAL1Only extends AbstractNALTest {

    @Override
    public Build build() {
        return new Default().level(1);
    }

    @Test
    public void revision() throws Narsese.InvalidInputException {
        n.mustOutput(3, "<bird --> swimmer>", '.', 0.87f, 0.91f)
                .en("bird is very likely to be a type of swimmer.");
        n.believe("<bird --> swimmer>")
                .en("bird is a type of swimmer.");
        n.believe("<bird --> swimmer>", 0.10f, 0.60f)
                .en("bird is probably not a type of swimmer.");

    }

    @Test
    public void deduction() throws Narsese.InvalidInputException {
        n.believe("<bird --> animal>")
                .en("bird is a type of animal.")
                .es("bird es un tipo de animal.")
                .de("bird ist eine art des animal.");
        n.believe("<robin --> bird>")
                .en("robin is a type of bird.");
        n.mustBelieve(23, "<robin --> animal>", 0.81f)
                .en("robin is a type of animal.");
    }

    @Test
    public void abduction() throws Narsese.InvalidInputException {
        n.believe("<sport --> competition>")
                .en("sport is a type of competition.");
        n.believe("<chess --> competition>", 0.90f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE)
                .en("chess is a type of competition.");

        n.mustBelieve(23, "<sport --> chess>", 1.0f, 0.42f)
                .en("I guess sport is a type of chess.")
                .en("sport is possibly a type of chess.")
                .es("es posible que sport es un tipo de chess.");
        n.mustBelieve(23, "<chess --> sport>", 0.90f, 0.45f)
                .en("I guess chess is a type of sport");
    }

    @Test
    public void induction() throws Narsese.InvalidInputException {
        n.believe("<swan --> swimmer>", 0.90f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE)
                .en("Swan is a type of swimmer.");
        n.believe("<swan --> bird>")
                .en("Swan is a type of bird.");

        n.mustBelieve(23, "<bird --> swimmer>", 0.90f, 0.45f)
                .en("I guess bird is a type of swimmer.");
        n.mustBelieve(23, "<swimmer --> bird>", 1f, 0.42f)
                .en("I guess swimmer is a type of bird.");
    }

    @Test
    public void exemplification() throws Narsese.InvalidInputException {
        n.believe("<robin --> bird>");
        n.believe("<bird --> animal>");
        n.mustOutput(25, "<animal --> robin>. %1.00;0.45%")
                .en("I guess animal is a type of robin.");
    }

    long conversionTime() {
        return 25;
    }


    @Test
    public void conversion() throws Narsese.InvalidInputException {
        n.believe("<bird --> swimmer>");
        n.ask("<swimmer --> bird>")
                .en("Is swimmer a type of bird?");
        n.mustOutput(conversionTime() , "<swimmer --> bird>. %1.00;0.47%");
    }

    @Test
    public void yesnoQuestion() throws Narsese.InvalidInputException {
        n.believe("<bird --> swimmer>");
        n.ask("<bird --> swimmer>");
        n.mustOutput(25, "<bird --> swimmer>. %1.00;0.90%");
    }


    @Test
    public void whQuestion() throws Narsese.InvalidInputException {
        n.believe("<bird --> swimmer>", 1.0f, 0.8f);
        n.ask("<?x --> swimmer>")
                .en("What is a type of swimmer?");
        n.mustOutput(25, "<bird --> swimmer>. %1.00;0.80%");
    }

    long backwardInferenceTime() {
        return 46;
    }

    @Test
    public void backwardInference() throws Narsese.InvalidInputException {
        n.believe("<bird --> swimmer>", 1.0f, 0.8f);
        n.ask("<?1 --> swimmer>");
        n.mustOutput(backwardInferenceTime(), "<?1 --> bird>?").en("What is a type of bird?");
        n.mustOutput(backwardInferenceTime(), "<bird --> ?1>?").en("What is the type of bird?");
    }

    long multistepTime() {
        return 80;
    }

    @Test
    public void multistep() throws Narsese.InvalidInputException {
        n.believe("<a --> b>", 1.0f, 0.9f);
        n.believe("<b --> c>", 1.0f, 0.9f);
        n.believe("<c --> d>", 1.0f, 0.9f);
        n.ask("<a --> d>");

        //originally checked for 0.25% exact confidence
        n.mustBelieve(multistepTime(), "<a --> d>", 1f, 1f, 0.27f, 0.33f);

        //but we know also 73% is highest it can reach
        if (n.nal() == 1)
            n.mustBelieve(multistepTime(), "<a --> d>", 1f, 1f, 0.73f, 0.75f);
    }

}
