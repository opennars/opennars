package nars.core.logic.nal5;

import nars.core.Build;
import nars.core.build.Default;
import nars.core.logic.AbstractNALTest;
import nars.io.narsese.Narsese;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static nars.logic.nal7.Tense.Eternal;


public class NAL5Test extends AbstractNALTest {

    public NAL5Test(Build b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                { new Default() },
                { new Default().setInternalExperience(null) },
                { new Default().level(5) },
                //{ new Neuromorphic(4) },
        });
    }

    /** 5.19 */
    @Test public void compoundDecompositionOnePremise() throws Narsese.InvalidInputException {
        /*

        'Robin can fly and swim.
        $0.90;0.90$ (&&,<robin --> swimmer>,<robin --> [flying]>). %0.9%
        1
        'Robin can swim.
        ''outputMustContain('<robin --> swimmer>. %0.90;0.73%')
        5
        ''//+2 from original
        'Robin can fly.
        ''outputMustContain('<robin --> [flying]>. %0.90;0.73%')

        */

        long time = 1250;


        n.believe("(&&,<robin --> swimmer>,<robin --> [flying]>)", Eternal, 0.9f, 0.9f)
                .en("robin can fly and swim.")
                .en("robin is one of the flying and is a swimmer.");

        n.mustBelieve(time, "<robin --> swimmer>", 0.90f, 0.90f, 0.73f, 0.73f)
                .en("robin can swim.");
        n.mustBelieve(time, "<robin --> [flying]>", 0.90f, 0.90f, 0.73f, 0.73f)
                .en("robin can fly.")
                .en("robin is one of the flying.");

    }
    
}
