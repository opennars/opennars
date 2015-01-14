package nars.core.logic.nal1;

import nars.core.Build;
import nars.core.build.Default;
import nars.io.narsese.Narsese;
import org.junit.Test;


public class TestNAL1 extends AbstractNALTest {

    @Override
    public Build build() {
        return new Default().level(1);
    }


    @Test
    public void revision() throws Narsese.InvalidInputException {
        n.believe("<a --> b>");
        n.believe("<a --> b>", 0.10f, 0.60f);
        n.mustOutput(3, "<a --> b>", '.', 0.87f, 0.91f);
    }

    @Test
    public void deduction() throws Narsese.InvalidInputException {
        n.believe("<bird --> animal>"); //Bird is a type of animal.
        n.believe("<robin --> bird>"); //Robin is a type of bird.
        n.mustBelieve(3, "<robin --> animal>", 0.81f); //Robin is a type of animal.
    }

    @Test
    public void abduction() throws Narsese.InvalidInputException {
        n.believe("<sport --> competition>"); //Sport is a type of competition.
        n.believe("<chess --> competition>", 0.90f); //Chess is a type of competition.
        n.mustBelieve(3, "<sport --> chess>", 0.42f); //I guess sport is a type of chess.
        n.mustBelieve(3, "<chess --> sport>", 0.90f, 0.45f); //I guess chess is a type of sport

    }


}
