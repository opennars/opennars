package nars.meta;

import nars.NAR;
import nars.io.out.TextOutput;
import nars.meter.NARTrace;
import nars.nar.NewDefault;
import org.junit.Test;

/**
 * Created by me on 8/15/15.
 */
public class BasicRulesTest {

    @Test
    public void testNAL1() {
        //Deriver d = Deriver.defaults;

        NAR n = new NAR(new NewDefault().setInternalExperience(null).level(3));

        n.input("<a --> b>. <b --> c>.");

        NARTrace.out(n);
        TextOutput.out(n);
        n.frame(550);
    }

    @Test public void testSubstitution() {
        // (($1 --> M) ==> C), (S --> M), substitute($1,S) |- C, (Truth:Deduction, Order:ForAllSame)
        NAR n = new NAR(new NewDefault());
        n.input("<<$1 --> M> ==> <C1 --> C2>>. <S --> M>.");
        //OUT: <C1 --> C2>. %1.00;0.81% {70: 1;2}

        TextOutput.out(n);
        n.frame(550);

        //<<$1 --> drunk> ==> <$1--> dead>>. <S --> drunk>.     |-  <S --> dead>.

    }

    @Test public void testSubstitution2() {
        // (($1 --> M) ==> C), (S --> M), substitute($1,S) |- C, (Truth:Deduction, Order:ForAllSame)
        NAR n = new NAR(new NewDefault());
        n.input("<<$1 --> happy> ==> <$1--> dead>>. <S --> happy>.");
        //<<$1 --> drunk> ==> <$1--> dead>>. <S --> drunk>.     |-  <S --> dead>.
        //OUT: <S --> dead>. %1.00;0.81% {58: 1;2}

        TextOutput.out(n);
        n.frame(550);



    }

}
