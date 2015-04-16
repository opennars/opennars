package nars.nal.nal2;


import nars.ProtoNAR;
import nars.io.narsese.InvalidInputException;
import nars.nal.JavaNALTest;
import nars.prototype.Curve;
import nars.prototype.Default;
import nars.prototype.Discretinuous;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static nars.nal.nal7.Tense.Eternal;

public class NAL2Test extends JavaNALTest {

    public NAL2Test(ProtoNAR b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default()}, //NAL8 + NAL9 didnt solve it
                {new Default().level(3)}, //needs 3 for sets
                {new Default().setInternalExperience(null)},
                {new Curve().setInternalExperience(null)},
                {new Default.DefaultMicro() },
                {new Discretinuous()}

                //{new Neuromorphic(4)},
        });
    }



    /** 2.10 */
    @Test
    public void structureTransformation() throws InvalidInputException {
        /*
            /home/me/share/opennars/nars_java/../nal/test/nal2.10.nal '********** structure transformation

                    'Birdie is similar to Tweety
            <Birdie <-> Tweety>. %0.90%

                    'Is Birdie similar to Tweety?
            <{Birdie} <-> {Tweety}>?

                    6

                    'Birdie is similar to Tweety.
                    ''outputMustContain('<{Birdie} <-> {Tweety}>. %0.90;0.73%')

            @1750        */

        long time = 320;
        //TextOutput.out(n);

        nar.believe("<Birdie <-> Tweety>", Eternal, 0.9f, 0.9f)
                .en("Birdie is similar to Tweety.");
        nar.step(1);
        nar.ask("<{Birdie} <-> {Tweety}>")
                .en("Is Birdie similar to Tweety?");

        nar.mustBelieve(time, "<{Birdie} <-> {Tweety}>", 0.8f, 0.95f, 0.70f, 0.76f)
                .en("Birdie is similar to Tweety.");
        nar.run();
    }
}

