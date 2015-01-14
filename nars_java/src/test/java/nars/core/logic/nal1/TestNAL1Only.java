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
        n.believe("<bird --> swimmer>")
                .en("bird is a type of swimmer.");
        n.believe("<bird --> swimmer>", 0.10f, 0.60f)
                .en("bird is probably not a type of swimmer.");
        n.mustOutput(3, "<bird --> swimmer>", '.', 0.87f, 0.91f)
                .en("bird is very likely to be a type of swimmer.");
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


}
