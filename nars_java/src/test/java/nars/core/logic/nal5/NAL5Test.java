package nars.core.logic.nal5;

import nars.core.AbstractNALTest;
import nars.core.Build;
import nars.core.build.Default;
import nars.io.TextOutput;
import nars.io.narsese.Narsese;
import org.junit.Test;


public class NAL5Test extends AbstractNALTest {

    @Override
    public Build build() {
        return new Default();
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

        long time = 25;

        TextOutput.out(n);
        n.believe("(&&,<robin --> swimmer>,<robin --> [flying]>)", 0.9f, 0.9f)
                .en("robin can fly and swim.")
                .en("robin is one of the flying and is a swimmer.");

        n.mustBelieve(time, "<robin --> swimmer>", 0.90f, 0.90f, 0.73f, 0.73f)
                .en("robin can swim.");
        n.mustBelieve(time, "<robin --> [flying]>", 0.90f, 0.90f, 0.73f, 0.73f)
                .en("robin can fly.")
                .en("robin is one of the flying.");

    }
    
}
